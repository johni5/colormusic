package com.del.flc.rxtx;

@FunctionalInterface
public interface RxTxBegin {

    boolean process(RxTxData data) throws Exception;

}
