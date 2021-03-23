package com.del.flc;

import com.del.flc.utils.StringUtil;
import com.del.flc.view.Mode;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.Vector;

public class CMComboBox extends CMValue {

    private JComboBox<Mode> cb;
    private JPanel container;
    private Map<Integer, Mode> items;

    public CMComboBox(String title, int cmd, Map<Integer, Mode> items) {
        super(title, cmd);
        this.items = items;
        container = new JPanel(new BorderLayout());
        cb = new JComboBox<>(new Vector<>(items.values()));
        JLabel jl2_h = new JLabel(getTitle());
        container.add(jl2_h, BorderLayout.WEST);
        container.add(cb, BorderLayout.CENTER);
        cb.addActionListener(e -> writeState(false));
    }

    @Override
    void setEditable(boolean editable) {
        cb.setEnabled(editable);
    }

    @Override
    public int getValue() {
        Object si = cb.getSelectedItem();
        if (si != null) {
            return ((Mode) si).getId();
        }
        return -1;
    }

    @Override
    public boolean setValue(String value) {
        Float f = StringUtil.parseDigit(value);
        if (f != null && f >= 0 && items.containsKey(f.intValue())) {
            cb.setSelectedItem(items.get(f.intValue()));
            return true;
        }
        return false;
    }

    @Override
    public boolean equalsValue(String value) {
        Float f = StringUtil.parseDigit(value);
        if (f != null && f >= 0 && items.containsKey(f.intValue())) {
            Mode m = (Mode) cb.getSelectedItem();
            if (m != null) {
                return items.get(f.intValue()).getId().equals(m.getId());
            }
        }
        return false;
    }

    @Override
    public JComponent getComponent() {
        return container;
    }

}
