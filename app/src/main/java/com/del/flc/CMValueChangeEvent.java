package com.del.flc;

public class CMValueChangeEvent {

    private CMValue value;
    private Object event;

    public CMValueChangeEvent(CMValue value, Object event) {
        this.value = value;
        this.event = event;
    }

    public CMValue getValue() {
        return value;
    }

    public Object getEvent() {
        return event;
    }
}
