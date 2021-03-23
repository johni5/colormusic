package com.del.flc.rxtx;

@FunctionalInterface
public interface RxTxOnAction {

    boolean process(RxTxData data);

}
