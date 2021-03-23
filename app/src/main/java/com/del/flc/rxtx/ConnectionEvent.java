package com.del.flc.rxtx;

public class ConnectionEvent {

    private Events event;
    private String portName;

    public ConnectionEvent(Events event) {
        this.event = event;
    }

    public ConnectionEvent(Events event, String portName) {
        this(event);
        this.portName = portName;
    }

    public Events getEvent() {
        return event;
    }

    public String getPortName() {
        return portName;
    }

    public enum Events {

        READY,
        CONNECTED,
        WAIT,
        TRANSMIT,
        RECEIVE,
        BREAK,

    }

}
