package com.del.flc;

import com.del.flc.utils.StringUtil;
import com.del.flc.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.function.Function;

public class CMSlider extends CMValue {

    private static float[] ticks_255 = Utils.T(0f, 50f, 100f, 150f, 200f, 255f);

    private String format;
    private float min;
    private float max;
    private float step;
    private float[] ticks;
    private Function<Float, Integer> toInt;
    private Function<Integer, Float> toFloat;

    private JSlider js;
    private JPanel container;

    public CMSlider(String title, int min, int max, int step, int cmd) {
        this(title, (float) min, (float) max, (float) step, ticks_255, cmd);
    }

    public CMSlider(String title, float min, float max, float step, float[] ticks, int cmd) {
        this(title, "%.0f", min, max, step, ticks, cmd, Float::intValue, Integer::floatValue);
    }

    public CMSlider(String title, String format, float min, float max, float step, float[] ticks, int cmd, Function<Float, Integer> toInt, Function<Integer, Float> toFloat) {
        super(title, cmd);
        this.min = min;
        this.max = max;
        this.step = step;
        this.format = format;
        this.ticks = ticks;
        this.toInt = toInt;
        this.toFloat = toFloat;
        init();
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    public float getStep() {
        return step;
    }

    public String getFormat() {
        return format;
    }

    public int getIntMin() {
        return toInt.apply(min);
    }

    public int getIntMax() {
        return toInt.apply(max);
    }

    @Override
    void setEditable(boolean editable) {
        js.setEnabled(editable);
    }

    @Override
    public int getValue() {
        return js.getValue();
    }

    @Override
    public boolean setValue(String value) {
        Float f = StringUtil.parseDigit(value);
        if (f != null) {
            js.setValue(Math.min(getIntMax(), Math.max(getIntMin(), getInt(f))));
            return true;
        }
        return false;
    }

    @Override
    public boolean equalsValue(String value) {
        Float f = StringUtil.parseDigit(value);
        if (f != null) {
            return js.getValue() == getInt(f);
        }
        return false;
    }

    private Timer timer;

    @Override
    public JComponent getComponent() {
        return container;
    }

    public int getInt(float val) {
        return toInt.apply(val);
    }

    public float getFloat(int val) {
        return toFloat.apply(val);
    }

    private void init() {
        container = new JPanel(new BorderLayout());
        js = new JSlider(SwingConstants.HORIZONTAL, getIntMin(), getIntMax(), getIntMin());
        JLabel jl2_h = new JLabel(getTitle());
        JLabel jl2 = new JLabel(String.format("%s(%s)", String.format(getFormat(), Math.min(max, Math.max(min, getFloat(js.getValue())))), js.getValue()));
        container.add(jl2_h, BorderLayout.NORTH);
        container.add(js, BorderLayout.CENTER);
        container.add(jl2, BorderLayout.SOUTH);

        Dictionary<Integer, JLabel> labels = new Hashtable<>();
        for (float tick : ticks) {
            labels.put(getInt(tick), new JLabel("<html><font color=blue size=2>" + String.format(getFormat(), tick)));
        }
        js.setLabelTable(labels);
//        js.setMajorTickSpacing(in.get(tickMaxStep));
        js.setMinorTickSpacing(getInt(step));
        js.setPaintLabels(true);
        js.setPaintTicks(true);
        js.setPaintTrack(true);
//        js.setSnapToTicks(true);
//        js.setAutoscrolls(true);
        timer = new Timer(1500, e -> writeState(false));
        timer.setRepeats(false);
        js.addChangeListener(l -> jl2.setText(String.format("%s(%s)", String.format(getFormat(), Math.min(max, Math.max(min, getFloat(js.getValue())))), js.getValue())));
        js.addChangeListener(e -> {
            if (isIgnoreUpdate()) return;
            if (timer.isRunning()) timer.restart();
            else timer.start();
        });
        container.setVisible(false);
    }

}
