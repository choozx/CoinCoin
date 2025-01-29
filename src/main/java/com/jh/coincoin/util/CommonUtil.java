package com.jh.coincoin.util;

import com.jh.coincoin.model.type.BinanceType.Order;
import com.jh.coincoin.model.type.BinanceType.Side;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by dale on 2025-01-29.
 */
public class CommonUtil {

    public static Double formatDecimal(double origin, int scale) {
        BigDecimal bd = new BigDecimal(origin);
        return bd.setScale(scale, RoundingMode.DOWN).doubleValue();
    }
}
