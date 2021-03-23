package com.del.flc.rxtx;

import com.del.flc.utils.Logger;
import com.del.flc.utils.MyExecutors;
import com.del.flc.utils.StringUtil;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConnectionManager implements Runnable, SerialPortDataListener, Connection {

    private ConnectionListener listener;
    private Logger logger;
    private Future future;
    private SerialPort serialPort;
    private AtomicBoolean work = new AtomicBoolean();
    private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private Queue<Session> cmdQueue = new ConcurrentLinkedQueue<>();
    private static AtomicInteger ID_GEN = new AtomicInteger();

    public ConnectionManager(ConnectionListener listener, Logger logger) {
        this.listener = listener;
        this.logger = logger;
    }

    public void start() {
        if (future != null) {
            future.cancel(true);
        }
        future = MyExecutors.getInstance().submit(this);
    }

    @Override
    public void reset() {
        //
    }

    public void stop() {
        work.set(false);
    }

    public void exit() {
        work.set(false);
        try {
            future.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            //
        }
        MyExecutors.getInstance().shutdown();
    }

    public boolean isInWork() {
        return work.get();
    }

    public void send(Session cmd) {
        rwl.writeLock().lock();
        try {
            int id = ID_GEN.getAndIncrement();
            cmd.setId(id);
            cmdQueue.offer(cmd);
        } finally {
            rwl.writeLock().unlock();
        }
    }

    @Override
    public void run() {
        work.set(true);
        Set<String> denyPorts = Sets.newHashSet();
        listener.connectionChangeState(new ConnectionEvent(ConnectionEvent.Events.READY));
        while (work.get()) {
            if (serialPort == null) {
                for (SerialPort sp : SerialPort.getCommPorts()) {
                    String portName = sp.getSystemPortName();
                    if (denyPorts.contains(portName)) continue;
                    info("Попытка соединения с " + portName);
                    serialPort = MyExecutors.getInstance().safeGet(10, TimeUnit.SECONDS, () -> {
                        try {
                            if (!sp.isOpen() && sp.openPort()) {
                                sp.setBaudRate(9600);
                                sp.setNumDataBits(8);
                                sp.setNumStopBits(SerialPort.ONE_STOP_BIT);
                                sp.setParity(SerialPort.NO_PARITY);
                                if (sp.isOpen()) {
                                    info("Открыл. Устанавливаю соединение");
                                    AtomicBoolean checked = new AtomicBoolean();
                                    sp.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
                                    AtomicInteger attempt = new AtomicInteger(3); // 3 попытки проверить порт
                                    while (!checked.get() && attempt.getAndDecrement() > 0) {
                                        info("Попытка " + (3 - attempt.get()));
                                        serialWrite(sp, (byte) 2, (byte) ID_GEN.getAndIncrement(), (byte) 0);
                                        try {
                                            String res = serialRead(sp);
                                            if (!StringUtil.isTrimmedEmpty(res)) {
                                                info("Ответ пришел");
                                                checked.set(true);
                                            } else {
                                                error("Не дождался ответа");
                                            }
                                        } catch (Exception e) {
                                            error("Не дождался ответа");
                                        }
                                    }
                                    if (!checked.get()) {
                                        sp.closePort();
                                        info("Закрыл " + portName);
                                    } else {
                                        info("Подключено к " + portName);
                                        listener.connectionChangeState(new ConnectionEvent(ConnectionEvent.Events.CONNECTED, portName));
                                        return sp;
                                    }
                                }
                            }
                            Thread.sleep(100);
                        } catch (Exception e) {
                            error("Ошибка", e);
                        }
                        return null;
                    });
                    if (serialPort == null) denyPorts.add(portName);
                    else {
                        serialPort.addDataListener(this);
                        break;
                    }
                }
            }
            if (serialPort != null && serialPort.isOpen()) {
                try {
                    List<Session> _copy = Lists.newArrayList();
                    rwl.readLock().lock();
                    try {
                        cmdQueue.forEach(s -> {
                            if (s.getAttempts() > s.getCount().intValue()) {
                                _copy.add(s);
                            }
                        });
                    } finally {
                        rwl.readLock().unlock();
                    }
                    List<Byte> buffer = Lists.newArrayList();
                    _copy.forEach(m -> {
                        int attempt = m.getCount().incrementAndGet();
                        if (m.getOnBegin() == null || m.getOnBegin().process(new RxTxData(attempt, serialPort))) {
                            buffer.add(m.getData() > -1 ? (byte) 3 : (byte) 2);
                            buffer.add((byte) m.getId());
                            buffer.add((byte) m.getCmd());
                            if (m.getData() > -1) buffer.add((byte) m.getData());
                        }
                    });
                    if (buffer.size() > 0) {
                        byte[] bytes = new byte[buffer.size()];
                        int i = 0;
                        for (Byte b : buffer) {
                            bytes[i++] = b;
                        }
                        try {
                            logger.info("Отправил: " + Arrays.toString(bytes));
                            listener.connectionChangeState(new ConnectionEvent(ConnectionEvent.Events.TRANSMIT, serialPort.getSystemPortName()));
                            serialWrite(serialPort, bytes);
                        } catch (Exception e) {
                            error("Ошибка записи");
                        }
                    }
//                    rwl.writeLock().lock();
//                    try {
//                        cmdQueue.removeIf(s -> s.getAttempts() <= s.getCount().incrementAndGet());
//                    } finally {
//                        rwl.writeLock().unlock();
//                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    listener.connectionChangeState(new ConnectionEvent(ConnectionEvent.Events.BREAK, serialPort.getSystemPortName()));
                }
                listener.connectionChangeState(new ConnectionEvent(ConnectionEvent.Events.WAIT, serialPort.getSystemPortName()));
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
        info("Закрываю соединение");
        if (serialPort != null && serialPort.isOpen()) {
            try {
                serialPort.closePort();
            } catch (Exception e) {
                error("Ошибка", e);
            }
        }
        info("Завершаю работу");
        info("Выхожу");
    }

    public void info(String m) {
        if (logger != null) logger.info(m);
    }

    public void error(String m) {
        if (logger != null) logger.error(m);
    }

    public void error(String m, Throwable t) {
        if (logger != null) logger.error(m, t);
    }

    private static String serialRead(SerialPort comPort) throws InterruptedException {
        comPort.setComPortTimeouts(1, 0, 0);

        StringBuilder sb = new StringBuilder();
        int c = 5;

        while (c-- > 0 && !StringUtil.endWithBreak(sb.toString())) {
            while (comPort.bytesAvailable() > 0) {
                byte[] b = new byte[1];
                comPort.readBytes(b, 1);
                System.out.println("<< " + Arrays.toString(b));
                sb.append(new String(b));
                if (StringUtil.endWithBreak(sb.toString())) break;
            }
            if (!StringUtil.endWithBreak(sb.toString()))
                Thread.sleep(100);
        }
        if (StringUtil.endWithBreak(sb.toString())) return sb.toString();
        return null;
    }

    public void serialWrite(SerialPort comPort, byte... b) throws InterruptedException, IOException {
        comPort.setComPortTimeouts(65536, 0, 0);
        Thread.sleep(5L);
        byte[] data = new byte[b.length + 12];
        for (int i = 0; i < 10; i++) data[i] = (byte) 240;
        data[10] = (byte) 58;
        data[11] = (byte) b.length;
        int i = 12;
        for (byte b1 : b) {
            data[i++] = b1;
        }
        System.out.println(">> " + Arrays.toString(data));
        comPort.getOutputStream().write(data);
    }


    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
    }

    private StringBuilder readBuffer = new StringBuilder();

    @Override
    public void serialEvent(SerialPortEvent event) {
        if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
            return;
        try {

            serialPort.setComPortTimeouts(1, 0, 0);
            byte[] buf = new byte[serialPort.bytesAvailable()];
            serialPort.readBytes(buf, buf.length);

            System.out.println("<< " + Arrays.toString(buf) + " -> " + new String(buf));

            readBuffer.append(new String(buf));

            String sb = readBuffer.toString();
            if (sb.contains(":") && sb.contains(StringUtil._break)) {
                int beginIndex = sb.lastIndexOf(":");
                int endIndex = sb.lastIndexOf(StringUtil._break);
                if (beginIndex + 1 < endIndex) {
                    String data = sb.substring(beginIndex + 1, endIndex);
                    String[] split = data.split(";");
                    for (String part : split) {
                        String[] ps = part.split(" ");
                        if (ps.length > 1) {
                            int id = Integer.parseInt(ps[0]);
                            System.out.println("ID: " + id);
                            rwl.writeLock().lock();
                            try {
                                cmdQueue.removeIf(s -> {
                                    if (s.getId() == id) {
                                        try {
                                            return s.getOnReceive().process(ps[1].trim());
                                        } catch (Exception e) {
                                            s.getOnError().process(new RxTxData(s.getCount().intValue(), serialPort));
                                        } finally {
                                            if (s.getOnComplete() != null) {
                                                s.getOnComplete().process(new RxTxData(s.getCount().intValue(), serialPort));
                                            }
                                        }
                                    }
                                    return false;
                                });
                            } finally {
                                rwl.writeLock().unlock();
                            }
                        }
                    }
                    readBuffer.setLength(0);
                }
            }


//            String value = serialRead(serialPort);
//
//            if (!StringUtil.isTrimmedEmpty(value)) {
//                logger.info("Принял: " + value);
//                String[] split = value.split(" ");
//                if (split.length > 1) {
//                    int id = Integer.parseInt(split[0]);
//                    rwl.writeLock().lock();
//                    try {
//                        cmdQueue.removeIf(s -> {
//                            if (s.getId() == id) {
//                                try {
//                                    return s.getOnReceive().process(split[1]);
//                                } catch (Exception e) {
//                                    s.getOnError().process(new RxTxData(s.getCount().intValue(), serialPort));
//                                }
//                            }
//                            return false;
//                        });
//                    } finally {
//                        rwl.writeLock().unlock();
//                    }
//                }
//            }
        } catch (Exception e) {
            error(e.getMessage());
        }

    }
}
