package com.del.flc.rxtx;

@FunctionalInterface
public interface RxTxReceive {

    boolean process(String data) throws Exception;

}
