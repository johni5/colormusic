package com.del.flc.rxtx;

import com.del.flc.utils.Configure;
import com.del.flc.utils.Logger;
import com.del.flc.utils.MyExecutors;
import com.del.flc.utils.StringUtil;
import com.fazecast.jSerialComm.SerialPort;
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

public class ConnectionManager2 implements Runnable, Connection {

    private ConnectionListener listener;
    private Logger logger;
    private Future future;
    private SerialPort serialPort;
    private AtomicBoolean work = new AtomicBoolean();
    private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private Queue<Session> cmdQueue = new ConcurrentLinkedQueue<>();

    public ConnectionManager2(ConnectionListener listener, Logger logger) {
        this.listener = listener;
        this.logger = logger;
    }

    public void start() {
        if (future != null && !future.isCancelled()) {
            future.cancel(true);
        }
        future = MyExecutors.getInstance().submit(this);
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

    @Override
    public void send(Session cmd) {
        rwl.writeLock().lock();
        try {
            cmdQueue.offer(cmd);
        } finally {
            rwl.writeLock().unlock();
        }
    }

    @Override
    public void reset() {
        if (isInWork()) {
            denyPorts.clear();
        } else {
            start();
        }
    }

    private Set<String> denyPorts = Sets.newConcurrentHashSet();

    @Override
    public void run() {
        work.set(true);
        listener.connectionChangeState(new ConnectionEvent(ConnectionEvent.Events.READY));
        while (work.get()) {
            if (serialPort == null) {
                SerialPort[] commPorts = SerialPort.getCommPorts();
                String oldPortName = Configure.getInstance().getPortName();
                if (!StringUtil.isTrimmedEmpty(oldPortName)) {
                    Arrays.sort(commPorts, (o1, o2) -> {
                        String n1 = o1.getSystemPortName();
                        String n2 = o2.getSystemPortName();
                        if (n1.equals(oldPortName)) return -1;
                        if (n2.equals(oldPortName)) return 1;
                        return n1.compareTo(n2);
                    });
                }
                for (SerialPort sp : commPorts) {
                    String portName = sp.getSystemPortName();
                    if (denyPorts.contains(portName)) continue;
                    info("Попытка соединения с " + portName);
                    serialPort = MyExecutors.getInstance().safeGet(10, TimeUnit.SECONDS, () -> {
                        try {
                            Thread.sleep(50);
                            if (sp.openPort()) {
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
                                        serialWrite(sp, (byte) 3);
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
                            } else {
                                error("Не могу открыть порт");
                            }
                            Thread.sleep(100);
                        } catch (Exception e) {
                            error("Ошибка", e);
                        }
                        return null;
                    });
                    if (serialPort == null) denyPorts.add(portName);
                    else {
                        Configure.getInstance().setPortName(portName);
                        break;
                    }
                }
            }
            if (serialPort != null && serialPort.isOpen()) {
                try {
                    List<Session> _copy = Lists.newArrayList();
                    rwl.writeLock().lock();
                    try {
                        Session m = cmdQueue.poll();
                        while (m != null) {
                            _copy.add(m);
                            m = cmdQueue.poll();
                        }
                        cmdQueue.clear();
                    } finally {
                        rwl.writeLock().unlock();
                    }
                    _copy.forEach(m -> {
                        int attempt = m.getAttempts();
                        try {
                            boolean ok = false;

                            // purge port
                            while (serialPort.bytesAvailable() > 0) {
                                int available = serialPort.bytesAvailable();
                                info("В топку [байтов]: " + serialPort.readBytes(new byte[available], available));
                                Thread.sleep(50);
                            }

                            if (m.getOnBegin() == null || m.getOnBegin().process(new RxTxData(attempt, serialPort))) {
                                try {
                                    while (!ok && attempt-- > 0) {
                                        int cmd = m.getCmd();
                                        int data = m.getData();
                                        byte[] bytes = new byte[data > -1 ? 2 : 1];
                                        bytes[0] = (byte) cmd;
                                        if (data > -1) bytes[1] = (byte) data;
                                        logger.info("Отправил: " + Arrays.toString(bytes));
                                        listener.connectionChangeState(new ConnectionEvent(ConnectionEvent.Events.TRANSMIT, serialPort.getSystemPortName()));
                                        try {
                                            serialWrite(serialPort, bytes);
                                            if (m.getOnReceive() == null) ok = true;
                                        } catch (Exception e) {
                                            error("Ошибка записи");
                                        }
                                        if (m.getOnReceive() != null) {
                                            listener.connectionChangeState(new ConnectionEvent(ConnectionEvent.Events.RECEIVE, serialPort.getSystemPortName()));
                                            Thread.sleep(200);
                                            String value = serialRead(serialPort);
                                            if (!StringUtil.isTrimmedEmpty(value)) {
                                                logger.info("Принял: " + value);
                                                ok = m.getOnReceive().process(value);
                                                if (!ok) Thread.sleep(50);
                                            }
                                        }
                                    }
                                    if (!ok && m.getOnError() != null) {
                                        m.getOnError().process(new RxTxData(attempt, serialPort));
                                    }
                                } finally {
                                    if (m.getOnComplete() != null) {
                                        m.getOnComplete().process(new RxTxData(attempt, serialPort));
                                    }
                                }
                            }
                        } catch (Exception e) {
                            if (m.getOnError() != null) {
                                m.getOnError().process(new RxTxData(attempt, serialPort));
                            }
                        }
                    });
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
        int c = 100; // 100*10ms = 1sec

        while (c-- > 0 && !StringUtil.endWithBreak(sb.toString())) {
            while (comPort.bytesAvailable() > 0) {
                byte[] b = new byte[1];
                comPort.readBytes(b, 1);
                sb.append(new String(b));
                if (StringUtil.endWithBreak(sb.toString())) break;
            }
            if (!StringUtil.endWithBreak(sb.toString()))
                Thread.sleep(10);
        }
        if (StringUtil.endWithBreak(sb.toString())) return sb.toString();
        return null;
    }

    public void serialWrite(SerialPort comPort, byte... b) throws InterruptedException, IOException {
        comPort.setComPortTimeouts(65536, 0, 0);
        Thread.sleep(5L);
        comPort.getOutputStream().write(b);
    }


}
