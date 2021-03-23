package com.del.flc;

import com.del.flc.rxtx.Session;

public class CMPowerBtn extends CMButton {

    public CMPowerBtn(int cmd) {
        super("...", cmd);
        this.state = -1;
    }

    @Override
    public boolean setValue(String value) {
        boolean ok = super.setValue(value);
        if (ok) {
            changeState();
        }
        return ok;
    }

    @Override
    public void writeState(boolean force) {
        connectionManager.send(new Session(
                3,
                cmd,
                getValue(),
                this::setValue,
                rxtx -> {
                    setEditable(false);
                    return true;
                },
                rxtx -> {
                    setEditable(true);
                    notifyListeners();
                    return true;
                }
        ));
    }

    private void changeState() {
        if (state > 0) {
            btn.setText("Выкл");
            state = 0;
        } else {
            btn.setText("Вкл");
            state = 1;
        }
    }
}
