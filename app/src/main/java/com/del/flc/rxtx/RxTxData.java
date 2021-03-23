package com.del.flc.rxtx;

import com.fazecast.jSerialComm.SerialPort;

public class RxTxData {

    private SerialPort serialPort;
    private int attempt;
    private Exception e;

    public RxTxData(int attempt, SerialPort serialPort, Exception e) {
        this.attempt = attempt;
        this.serialPort = serialPort;
        this.e = e;
    }

    public RxTxData(int attempt, Exception e) {
        this.attempt = attempt;
        this.e = e;
    }

    public RxTxData(int attempt, SerialPort serialPort) {
        this.attempt = attempt;
        this.serialPort = serialPort;
    }

    public SerialPort getSerialPort() {
        return serialPort;
    }

    public Exception getE() {
        return e;
    }

    public int getAttempt() {
        return attempt;
    }
}
