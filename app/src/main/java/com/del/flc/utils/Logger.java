package com.del.flc.utils;

public interface Logger {


    void info(String m);

    void error(String m);

    void error(String m, Throwable t);

}
