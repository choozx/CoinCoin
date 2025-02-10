package com.jh.coincoin.model.consts;

/**
 * Created by dale on 2024-09-19.
 */
public class GlobalConst {

    public static final int MAX_STORAGE_CANDLE_COUNT = 12000;
    public static final int MAX_TRACKING_SYMBOL_COUNT = 5;
    public static final float MIN_ORDER_AMOUNT = 10.0f;

    public static final int MIN_LEVERAGE = 1;
    public static final int MAX_LEVERAGE = 125;

    public static final double MIN_MARGIN_RATIO = 0.01;
    public static final double MAX_MARGIN_RATIO = 1.00;

    public static final double MIN_ORDER_PRICE_GAP_PER = 0.001;

    public static final int MAX_STORAGE_INDICATOR_COUNT = 12000;
    public static final long CHUNK_SIZE = 1000;
}
