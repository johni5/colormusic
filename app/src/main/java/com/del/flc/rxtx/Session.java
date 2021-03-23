package com.del.flc.rxtx;

import java.util.concurrent.atomic.AtomicInteger;

public class Session {

    private int attempts;
    private int id;
    private int cmd;
    private int data;

    private RxTxReceive onReceive;

    private RxTxOnAction onBegin;
    private RxTxOnAction onComplete;
    private RxTxOnAction onError;

    private AtomicInteger count = new AtomicInteger();

    public Session(int attempts, int cmd, int data, RxTxReceive onReceive, RxTxOnAction onBegin, RxTxOnAction onComplete) {
        this(attempts, onReceive, onBegin, onComplete, null, cmd, data);
    }

    public Session(int attempts, RxTxReceive onReceive, RxTxOnAction onBegin, RxTxOnAction onComplete, RxTxOnAction onError, int cmd, int data) {
        this.attempts = attempts;
        this.onReceive = onReceive;
        this.onBegin = onBegin;
        this.onComplete = onComplete;
        this.onError = onError;
        this.cmd = cmd;
        this.data = data;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAttempts() {
        return attempts;
    }

    public RxTxReceive getOnReceive() {
        return onReceive;
    }

    public RxTxOnAction getOnBegin() {
        return onBegin;
    }

    public RxTxOnAction getOnComplete() {
        return onComplete;
    }

    public RxTxOnAction getOnError() {
        return onError;
    }

    public int getCmd() {
        return cmd;
    }

    public int getData() {
        return data;
    }

    public AtomicInteger getCount() {
        return count;
    }
}
