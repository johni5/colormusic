package com.del.flc.rxtx;

@FunctionalInterface
public interface RxTxComplete {

    boolean process(RxTxData data) throws Exception;

}
