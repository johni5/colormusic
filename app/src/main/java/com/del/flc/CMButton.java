package com.del.flc;

import com.del.flc.utils.StringUtil;

import javax.swing.*;

public class CMButton extends CMValue {

    protected int state;
    protected JButton btn;

    public CMButton(String title, int cmd) {
        super(title, cmd);
        btn = new JButton(title);
        btn.addActionListener(e -> writeState(false));
    }

    @Override
    void setEditable(boolean editable) {
        btn.setEnabled(editable);
    }

    @Override
    public int getValue() {
        return state;
    }

    @Override
    public boolean setValue(String value) {
        Float digit = StringUtil.parseDigit(value);
        if (digit != null) {
            this.state = digit.intValue();
            return true;
        }
        return false;
    }

    @Override
    public boolean equalsValue(String value) {
        Float digit = StringUtil.parseDigit(value);
        if (digit != null) {
            return this.state == digit.intValue();
        }
        return false;
    }

    @Override
    public JComponent getComponent() {
        return btn;
    }


}
