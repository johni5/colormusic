package com.del.flc;

import com.del.flc.rxtx.Connection;
import com.del.flc.rxtx.Session;
import com.del.flc.utils.StringUtil;
import com.del.flc.view.Mode;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.del.flc.utils.Utils.T;

public class ColorMusic {

    public static final char CMD_STATE = 0;
    public static final char CMD_SAVE = 1;
    public static final char CMD_LOW_PASS = 2;
    public static final char CMD_MODE = 3;
    public static final char CMD_SMODE = 4;
    public static final char CMD_BR = 5;
    public static final char CMD_CSP = 6;
    public static final char CMD_EMBR = 7;
    public static final char CMD_FSM = 8;
    public static final char CMD_HSTT = 9;
    public static final char CMD_HSTP = 10;
    public static final char CMD_LCOL = 11;
    public static final char CMD_LSAT = 12;
    public static final char CMD_CF_MAX = 13;
    public static final char CMD_RNBP = 14;
    public static final char CMD_RNBST = 15;
    public static final char CMD_RNBST2 = 16;
    public static final char CMD_RSPD = 17;
    public static final char CMD_SMTH = 18;
    public static final char CMD_SMTHF = 19;
    public static final char CMD_STRPER = 20;
    public static final char CMD_STRSMT = 21;

    private CMValue stateON = new CMPowerBtn(CMD_SAVE);
    private CMValue autoTune = new CMSettingsBtn(CMD_LOW_PASS);

    private CMValue mode = new CMComboBox("Режим", CMD_MODE, getModes());
    private CMValue lightMode = new CMComboBox("Режим подсветки", CMD_SMODE, getLightModes());
    private CMValue freqStrobeMode = new CMComboBox("Частотный режим", CMD_FSM, getFreqStrobeModes());

    private CMValue brightness = new CMSlider("Яркость", 0, 255, 20, CMD_BR);
    private CMValue emptyBright = new CMSlider("Яркость подсветки", 0, 255, 5, CMD_EMBR);

    private CMValue smooth = new CMSlider("Плавность", "%.2f", 0.05f, 1.0f, 0.05f, T(0.05f, 0.25f, 0.5f, 0.75f, 1f), CMD_SMTH, v -> (int) (v * 100), v -> (v * 1.0f) / 100.0f);
    private CMValue rainbowStep = new CMSlider("Шаг радуги", "%.1f", 0.5f, 20.f, 0.5f, T(0.5f, 5f, 10f, 15f, 20f), CMD_RNBST, v -> (int) (v * 10), v -> (v * 1.0f) / 10.0f);
    private CMValue smoothFreq = new CMSlider("Плавность", "%.2f", 0.05f, 1.0f, 0.05f, T(0.05f, 0.25f, 0.5f, 0.75f, 1f), CMD_SMTHF, v -> (int) (v * 100), v -> (v * 1.0f) / 100.0f);
    private CMValue maxCoefFreq = new CMSlider("Пиковая частота", "%.1f", 0.0f, 10.0f, 0.1f, T(0f, 2.5f, 5f, 7.5f, 10f), CMD_CF_MAX, v -> (int) (v * 10), v -> (v * 1.0f) / 10.0f);
    private CMValue strobeSmooth = new CMSlider("Плавность", 0, 255, 20, CMD_STRSMT);
    private CMValue strobePeriod = new CMSlider("Период", "%.0f", 1, 1000, 20, T(1f, 250f, 500f, 750f, 1000f), CMD_STRPER, v -> (int) (v / 5), v -> v * 5.0f);
    private CMValue lightColor = new CMSlider("Цвет", 0, 255, 10, CMD_LCOL);
    private CMValue lightSat = new CMSlider("Насыщенность", 0, 255, 20, CMD_LSAT);
    private CMValue colorSpeed = new CMSlider("Скорость", 0, 255, 10, CMD_CSP);
    private CMValue rainbowPeriod = new CMSlider("Скорость", "%.0f", -20, 20, 1, T(-20, -10, 0, 10, 20), CMD_RNBP, f -> (int) (f + 20), f -> f - 20.0f);
    private CMValue rainbowStep2 = new CMSlider("Шаг радуги", "%.1f", 0.5f, 10.f, 0.5f, T(0f, 2.5f, 5, 7.5f, 10), CMD_RNBST2, v -> (int) (v * 10), v -> (v * 1.0f) / 10.0f);
    private CMValue runningSpeed = new CMSlider("Скорость", 1, 255, 10, CMD_RSPD);
    private CMValue hueStep = new CMSlider("Шаг", 1, 255, 5, CMD_HSTP);
    private CMValue hueStart = new CMSlider("Чувствительность", 0, 255, 10, CMD_HSTT);

    private Connection connectionManager;

    public ColorMusic(Connection connectionManager) {
        this.connectionManager = connectionManager;
        Arrays.asList(stateON, autoTune, mode, lightMode, freqStrobeMode, brightness, emptyBright, smooth,
                rainbowStep, smoothFreq, maxCoefFreq, strobeSmooth, strobePeriod, lightColor, lightSat,
                colorSpeed, rainbowPeriod, rainbowStep2, runningSpeed, hueStep, hueStart).
                forEach(v -> v.setConnectionManager(connectionManager));
        mode.addChangeValueListener(e -> {
            update();
            lightMode.readState();
            brightness.readState();
            emptyBright.readState();
            smooth.readState();
            rainbowStep.readState();
            smoothFreq.readState();
            maxCoefFreq.readState();
            freqStrobeMode.readState();
            strobeSmooth.readState();
            strobePeriod.readState();
            lightColor.readState();
            lightSat.readState();
            colorSpeed.readState();
            rainbowPeriod.readState();
            rainbowStep2.readState();
            runningSpeed.readState();
            hueStep.readState();
            hueStart.readState();
        });
        lightMode.addChangeValueListener(e -> {
            update();
            lightColor.readState();
            lightSat.readState();
            colorSpeed.readState();
            rainbowPeriod.readState();
            rainbowStep2.readState();
        });
    }

    private static void addMode(Map<Integer, Mode> m, int i, String s) {
        m.put(i, new Mode(i, s));
    }

    private static Map<Integer, Mode> getModes() {
        Map<Integer, Mode> modes = new LinkedHashMap<>();
        addMode(modes, 0, "VU meter (столбик громкости): от зелёного к красному");
        addMode(modes, 1, "VU meter (столбик громкости): плавно бегущая радуга");
        addMode(modes, 2, "Светомузыка по частотам: 5 полос симметрично");
        addMode(modes, 3, "Светомузыка по частотам: 3 полосы");
        addMode(modes, 4, "Светомузыка по частотам: 1 полоса");
        addMode(modes, 5, "Стробоскоп ");
        addMode(modes, 6, "Подсветка");
        addMode(modes, 7, "Бегущие частоты");
        addMode(modes, 8, "Анализатор спектра");
        return modes;
    }

    private static Map<Integer, Mode> getLightModes() {
        Map<Integer, Mode> lightModes = new LinkedHashMap<>();
        addMode(lightModes, 0, "Постоянный цвет");
        addMode(lightModes, 1, "Плавная смена цвета");
        addMode(lightModes, 2, "Бегущая радуга");
//        addMode(lightModes, 3, "Не назначено");
        return lightModes;
    }

    private static Map<Integer, Mode> getFreqStrobeModes() {
        Map<Integer, Mode> freqStrobeModes = new LinkedHashMap<>();
        addMode(freqStrobeModes, 0, "3 частоты");
        addMode(freqStrobeModes, 1, "Низкие");
        addMode(freqStrobeModes, 2, "Средние");
        addMode(freqStrobeModes, 3, "Высокие");
        return freqStrobeModes;
    }

    public CMValue getMode() {
        return mode;
    }

    public CMValue getLightMode() {
        return lightMode;
    }

    public CMValue getBrightness() {
        return brightness;
    }

    public CMValue getFreqStrobeMode() {
        return freqStrobeMode;
    }

    public CMValue getRainbowStep() {
        return rainbowStep;
    }

    public CMValue getMaxCoefFreq() {
        return maxCoefFreq;
    }

    public CMValue getStrobePeriod() {
        return strobePeriod;
    }

    public CMValue getLightSat() {
        return lightSat;
    }

    public CMValue getRainbowStep2() {
        return rainbowStep2;
    }

    public CMValue getHueStart() {
        return hueStart;
    }

    public CMValue getSmooth() {
        return smooth;
    }

    public CMValue getSmoothFreq() {
        return smoothFreq;
    }

    public CMValue getStrobeSmooth() {
        return strobeSmooth;
    }

    public CMValue getLightColor() {
        return lightColor;
    }

    public CMValue getColorSpeed() {
        return colorSpeed;
    }

    public CMValue getRainbowPeriod() {
        return rainbowPeriod;
    }

    public CMValue getRunningSpeed() {
        return runningSpeed;
    }

    public CMValue getHueStep() {
        return hueStep;
    }

    public CMValue getEmptyBright() {
        return emptyBright;
    }

    public CMValue getStateON() {
        return stateON;
    }

    public CMValue getAutoTune() {
        return autoTune;
    }

    public void readState() {
        List<CMValue> cmValues = Arrays.asList(
                mode, lightMode, freqStrobeMode, rainbowStep, maxCoefFreq, strobePeriod, lightSat,
                rainbowStep2, hueStart, smooth, smoothFreq, strobeSmooth, lightColor, colorSpeed,
                rainbowPeriod, runningSpeed, hueStep, emptyBright, brightness, stateON
        );
        connectionManager.send(new Session(
                3,
                0,
                -1,
                data -> {
                    if (!StringUtil.isTrimmedEmpty(data)) {
                        data = data.trim();
                        if (data.startsWith("{") && data.endsWith("}")) {
                            Queue<CMValue> queue = new ConcurrentLinkedQueue<>(cmValues);
                            data = data.substring(1, data.length() - 1);
                            StringTokenizer st = new StringTokenizer(data, "|");
                            if (st.countTokens() == queue.size()) {
                                while (st.hasMoreTokens()) {
                                    Objects.requireNonNull(queue.poll()).setValue(st.nextToken());
                                }
                                return true;
                            }
                        }
                    }
                    return false;
                },
                a -> {
                    cmValues.forEach(i -> i.setIgnoreUpdate(true));
                    return true;
                },
                a -> {
                    cmValues.forEach(i -> i.setIgnoreUpdate(false));
                    update();
                    return true;
                }
        ));
    }


    public void update() {
        int _mode = getMode().getValue();
        int _lightMode = getLightMode().getValue();
        update(_mode, _lightMode);
    }

    public void update(int _mode, int _lightMode) {
        mode.setEditable(_mode > -1);
        autoTune.setEditable(_mode > -1);
        stateON.setEditable(_mode > -1);
        brightness.setVisible(_mode > -1);
        brightness.setEditable(_mode > -1);
        emptyBright.setVisible(_mode > -1);
        emptyBright.setEditable(_mode > -1);
        smooth.setVisible(_mode == 0 || _mode == 1);
        smooth.setEditable(_mode == 0 || _mode == 1);
        rainbowStep.setVisible(_mode == 1);
        rainbowStep.setEditable(_mode == 1);
        smoothFreq.setVisible(_mode == 2 || _mode == 3 || _mode == 4);
        smoothFreq.setEditable(_mode == 2 || _mode == 3 || _mode == 4);
        maxCoefFreq.setVisible(_mode == 2 || _mode == 3 || _mode == 4 || _mode == 7);
        maxCoefFreq.setEditable(_mode == 2 || _mode == 3 || _mode == 4 || _mode == 7);
        freqStrobeMode.setVisible(_mode == 4 || _mode == 7);
        freqStrobeMode.setEditable(_mode == 4 || _mode == 7);
        strobeSmooth.setVisible(_mode == 5);
        strobeSmooth.setEditable(_mode == 5);
        strobePeriod.setVisible(_mode == 5);
        strobePeriod.setEditable(_mode == 5);
        lightMode.setVisible(_mode == 6);
        lightMode.setEditable(_mode == 6);
        lightColor.setVisible(_mode == 6 && _lightMode == 0);
        lightColor.setEditable(_mode == 6 && _lightMode == 0);
        lightSat.setVisible(_mode == 6 && (_lightMode == 0 || _lightMode == 1));
        lightSat.setEditable(_mode == 6 && (_lightMode == 0 || _lightMode == 1));
        colorSpeed.setVisible(_mode == 6 && _lightMode == 1);
        colorSpeed.setEditable(_mode == 6 && _lightMode == 1);
        rainbowPeriod.setVisible(_mode == 6 && _lightMode == 2);
        rainbowPeriod.setEditable(_mode == 6 && _lightMode == 2);
        rainbowStep2.setVisible(_mode == 6 && _lightMode == 2);
        rainbowStep2.setEditable(_mode == 6 && _lightMode == 2);
        runningSpeed.setVisible(_mode == 7);
        runningSpeed.setEditable(_mode == 7);
        hueStep.setVisible(_mode == 8);
        hueStep.setEditable(_mode == 8);
        hueStart.setVisible(_mode == 8);
        hueStart.setEditable(_mode == 8);

    }

}
