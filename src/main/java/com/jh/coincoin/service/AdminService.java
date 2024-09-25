package com.jh.coincoin.service;

import com.jh.coincoin.model.type.BinanceType;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dale on 2024-09-11.
 */

@Service
@Getter
public class AdminService {

    private List<BinanceType.Symbol> trackingSymbolList = new ArrayList<>();    // 공유자원이라 동시성 이슈가 있긴하지만, 일단 나만 쓰는거라 나중에 생각...
    private List<String> trackingIndicatorNameList = new ArrayList<>();
    private BinanceType.Interval interval;
    private Pair<Double, Double> rsiSetting;
    private int rsiPeriod;

    public AdminService() {
        // TODO DB에서 어드민값 가져와 셋팅
        trackingSymbolList.add(BinanceType.Symbol.BTC_USDT);
        trackingSymbolList.add(BinanceType.Symbol.ETH_USDT);

        trackingIndicatorNameList.add("RSI");

        interval = BinanceType.Interval.FIFTEEN_MINUTE;

        rsiSetting = Pair.of(30.0, 70.0);
        rsiPeriod = 14;
    }

    public void setSymbol() {

    }

    public void setIndicator() {

    }

    public boolean isDetect(String name, String result) {
        if (name.equals("RSI")){
            double rsi = Double.parseDouble(result);
            return rsi <= rsiSetting.getLeft() || rsi >= rsiSetting.getRight();
        }

        return false;
    }
}
