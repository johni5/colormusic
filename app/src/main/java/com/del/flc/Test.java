package com.del.flc;


import com.del.flc.utils.Configure;
import com.del.flc.utils.StringUtil;
import com.fazecast.jSerialComm.SerialPort;

import java.util.Arrays;

public class Test {

    public static void main(String[] args) {

//        byte[] c = new byte[]{58, 50, 32, 49, 59, 51, 32, 49, 59, 50, 32, 56, 59, 51, 32, 49, 13, 10, 58, 50, 32, 56, 59, 51, 32, 49, 13, 10};
        SerialPort[] commPorts = SerialPort.getCommPorts();
//        String oldPortName = "COM8";
//        if (!StringUtil.isTrimmedEmpty(oldPortName)) {
//            Arrays.sort(commPorts, (o1, o2) -> {
//                String n1 = o1.getSystemPortName();
//                String n2 = o2.getSystemPortName();
//                if (n1.equals(oldPortName)) return -1;
//                if (n2.equals(oldPortName)) return 1;
//                return n2.compareTo(n1);
//            });
//        }
        for (SerialPort commPort : commPorts) {
            System.out.println(commPort.getSystemPortName());
        }



/*
        ConnectionManager connectionManager = new ConnectionManager(e -> {
            switch (e.getEvent()) {
                case READY:
                    System.out.println("Ready");
                    break;
                case CONNECTED:
                    System.out.println("Connected: " + e.getPortName());
                    break;
                case RECEIVE:
                    System.out.println("Получение");
                    break;
                case TRANSMIT:
                    System.out.println("Отправка");
                    break;
                case BREAK:
                    System.out.println("Break: " + e.getPortName());
                    break;
            }
        }, new Logger() {
            @Override
            public void info(String m) {
                System.out.println(m);
            }

            @Override
            public void error(String m) {
                System.out.println(m);
            }

            @Override
            public void error(String m, Throwable t) {
                t.printStackTrace();
            }
        });
        connectionManager.start();

        Scanner sc = new Scanner(System.in);
        String number = sc.next();
        while (!number.equals("0")) {
            System.out.println("В очередь: " + number);
            byte[] bytes = number.getBytes();
            connectionManager.send(
                    new Session(
                            3,
                            d -> {
                                byte b = bytes[0];
                                System.out.println(b);
                                d.getSerialPort().writeInt(b);
                                Thread.sleep(500);
                                return true;
                            },
                            d -> {
                                String text = d.getSerialPort().readString();
                                if (StringUtil.isTrimmedEmpty(text)) {
                                    Thread.sleep(500);
                                    return false;
                                }
                                System.out.println(text);
                                return true;
                            },
                            d -> {
                                if (d.getE() != null) {
                                    d.getE().printStackTrace();
                                } else {
                                    System.out.println("Превышено число попыток");
                                }
                                return true;
                            }
                    )
            );
            number = sc.next();
        }
        System.out.println("Выход");
        connectionManager.exit();
*/


/*
        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss[SSS]");

        try {
            System.out.println("Get for 5 sec...");
            MyExecutors.getInstance().safeSubmit(5, TimeUnit.SECONDS, () -> {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            System.out.println("success: ");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Shutdown");
        MyExecutors.getInstance().shutdown();
*/

    }


}
