package com.del.flc;

import com.del.flc.rxtx.Connection;
import com.del.flc.rxtx.Session;

import javax.swing.*;

abstract public class CMValue {

    protected String title;
    protected int cmd;
    protected Connection connectionManager;
    private CMValueChangeListener listener;
    private volatile boolean ignoreUpdate;

    public CMValue(String title, int cmd) {
        this.title = title;
        this.cmd = cmd;
    }

    public Connection getConnectionManager() {
        return connectionManager;
    }

    public void setConnectionManager(Connection connectionManager) {
        this.connectionManager = connectionManager;
    }

    abstract void setEditable(boolean editable);

    abstract public int getValue();

    abstract public boolean setValue(String value);

    abstract public boolean equalsValue(String value);

    public void addChangeValueListener(CMValueChangeListener listener) {
        this.listener = listener;
    }

    public void setVisible(boolean v) {
        getComponent().setVisible(v);
    }

    public boolean isVisible() {
        return getComponent().isVisible();
    }

    public int getCmd() {
        return cmd;
    }

    public String getTitle() {
        return title;
    }

    abstract public JComponent getComponent();

    protected void notifyListeners() {
        if (listener != null) listener.actionPerformed(new CMValueChangeEvent(this, null));
    }

    public void readState() {
        if (isVisible())
            this.connectionManager.send(new Session(
                    3,
                    cmd,
                    -1,
                    data -> {
                        if (!equalsValue(data)) {
                            return setValue(data);
                        }
                        return true;
                    },
                    rxtx -> {
                        setEditable(false);
                        return true;
                    },
                    rxtx -> {
                        setEditable(true);
                        return true;
                    }
            ));
    }

    public boolean isIgnoreUpdate() {
        return ignoreUpdate;
    }

    public void setIgnoreUpdate(boolean ignoreUpdate) {
        this.ignoreUpdate = ignoreUpdate;
    }

    public void writeState(boolean force) {
        if (!ignoreUpdate)
            this.connectionManager.send(new Session(
                    3,
                    cmd,
                    getValue(),
                    data -> force || equalsValue(data),
                    rxtx -> {
                        setEditable(false);
                        return true;
                    },
                    rxtx -> {
                        setEditable(true);
                        notifyListeners();
                        return true;
                    }
            )); // если  getValue() == -1 то не отсылал
    }

}
