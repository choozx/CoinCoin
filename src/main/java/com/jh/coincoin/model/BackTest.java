package com.jh.coincoin.model;

import com.jh.coincoin.model.type.BinanceType.Side;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

/**
 * Created by dale on 2025-02-13.
 */
public class BackTest {

    @Getter
    @Builder
    public static class BackTestBuyDto {
        private Side side;
        private long entryTime;
        private double avgPrice;
        private double stopPrice;
        private double limitPrice;

        public boolean isPriceHit(double closePrice) {
            if (limitPrice > avgPrice) {
                return closePrice > limitPrice || closePrice < stopPrice;
            } else {
                return closePrice < limitPrice || closePrice > stopPrice;
            }
        }

    }

    @Data
    @Builder
    public static class PnlDto {
        private double avgPrice;
        private double closePrice;
        private double pnl;
        private double pnlPercentage;
    }
}
