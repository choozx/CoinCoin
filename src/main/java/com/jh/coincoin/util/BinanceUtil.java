package com.jh.coincoin.util;

import com.jh.coincoin.model.type.BinanceType.Side;

public class BinanceUtil {

    /**
     * 바이낸스의 청산가 계산식을 정확하게 파악을 하지 못했기 때문에 근사값으로 대체
     * 자세한 값은 추후에 변경한다.
     */
    public static double calcLiquidationPrice(Side side, double entryPrice, int leverage) {
        double percentage = (100.0/leverage - 0.4)/100.0;

        if (side == Side.BUY) { // 롱 포지션
            return entryPrice - (entryPrice*percentage);
        } else { // 숏 포지션
            return entryPrice + (entryPrice*percentage);
        }
    }


}
