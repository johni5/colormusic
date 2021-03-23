package com.del.flc.rxtx;

@FunctionalInterface
public interface RxTxSend {

    byte[] process(RxTxData data) throws Exception;

}
