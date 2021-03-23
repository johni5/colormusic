package com.del.flc.utils;

import org.apache.log4j.Logger;

import java.util.ResourceBundle;

public class Utils {

    final static private Logger logger = Logger.getLogger("FLC");

    private static ResourceBundle info;

    public static Logger getLogger() {
        return logger;
    }

    public static boolean isTrimmedEmpty(Object val) {
        return val == null || val.toString().trim().length() == 0;
    }

    public static <T> T nvl(T t1, T t2) {
        return t1 == null ? t2 : t1;
    }

    public static ResourceBundle getInfo() {
        if (info == null) {
            info = ResourceBundle.getBundle("info");
        }
        return info;
    }

    final static int cm = Integer.parseInt("1111100000000000", 2);
    final static int dm = Integer.parseInt("0000011111111111", 2);

/*
    public static int encode(CMValue cmValue) {
        int c = cmValue.getCmd();
        int d = cmValue.getNormalizedValue() & dm;
        int p = (c << 11) & cm;
        p |= d;
        return p;
    }
*/

    public static float[] T(float... v) {
        return v;
    }

}
