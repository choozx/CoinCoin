package com.jh.coincoin.util;

import com.jh.coincoin.model.type.BinanceType.Side;

public class BinanceUtil {

    public static double calcLiquidationPrice(Side side, double entryPrice, int leverage, double maintenanceMarginRate) {
        double diff = entryPrice * maintenanceMarginRate / leverage;
        return side.equals(Side.BUY) ? entryPrice - diff : entryPrice + diff;
    }
}
