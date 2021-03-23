package com.del.flc.rxtx;

public interface Connection {

    void send(Session cmd);

    void start();

    void reset();

    void exit();

}
