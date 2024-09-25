package com.jh.coincoin.model;

import com.jh.coincoin.entity.CandleEntity;
import com.jh.coincoin.model.type.BinanceType;
import lombok.Data;

import java.util.List;

/**
 * Created by dale on 2024-09-07.
 */

@Data
public class Candle {
    private BinanceType.Symbol symbol;
    private long openTime;
    private double openPrice;
    private double highPrice;
    private double lowPrice;
    private double closePrice;
    private double volume;
    private long closeTime;
    private double quoteAssetVolume;
    private int numberOfTrades;
    private double takerBuyBaseAssetVolume;
    private double takerBuyQuoteAssetVolume;

    public Candle(BinanceType.Symbol symbol, List<Object> objects) {
        this.symbol = symbol;
        this.openTime = (long) objects.get(0);
        this.openPrice = Double.parseDouble((String) objects.get(1));
        this.highPrice = Double.parseDouble((String) objects.get(2));
        this.lowPrice = Double.parseDouble((String) objects.get(3));
        this.closePrice = Double.parseDouble((String) objects.get(4));
        this.volume = Double.parseDouble((String) objects.get(5));
        this.closeTime = (long) objects.get(6);
        this.quoteAssetVolume = Double.parseDouble((String) objects.get(7));
        this.numberOfTrades = (int) objects.get(8);
        this.takerBuyBaseAssetVolume = Double.parseDouble((String) objects.get(9));
        this.takerBuyQuoteAssetVolume = Double.parseDouble((String) objects.get(10));
    }

    public Candle(Candle candle) {
        this.symbol = candle.getSymbol();
        this.openTime = candle.getOpenTime();
        this.openPrice = candle.getOpenPrice();
        this.highPrice = candle.getHighPrice();
        this.lowPrice = candle.getLowPrice();
        this.closePrice = candle.getClosePrice();
        this.volume = candle.getVolume();
        this.closeTime = candle.getCloseTime();
        this.quoteAssetVolume = candle.getQuoteAssetVolume();
        this.numberOfTrades = candle.getNumberOfTrades();
        this.takerBuyBaseAssetVolume = candle.getTakerBuyBaseAssetVolume();
        this.takerBuyQuoteAssetVolume = candle.getTakerBuyQuoteAssetVolume();
    }

    public Candle(CandleEntity candle) {
        this.symbol = candle.getSymbol();
        this.openTime = candle.getOpenTime();
        this.openPrice = candle.getOpenPrice();
        this.highPrice = candle.getHighPrice();
        this.lowPrice = candle.getLowPrice();
        this.closePrice = candle.getClosePrice();
        this.volume = candle.getVolume();
        this.closeTime = candle.getCloseTime();
        this.quoteAssetVolume = candle.getQuoteAssetVolume();
        this.numberOfTrades = candle.getNumberOfTrades();
        this.takerBuyBaseAssetVolume = candle.getTakerBuyBaseAssetVolume();
        this.takerBuyQuoteAssetVolume = candle.getTakerBuyQuoteAssetVolume();
    }

    public void updateCandle(Candle candle) {
        setOpenTime(candle.getOpenTime());

        setOpenPrice(candle.getOpenPrice());

        if (candle.getHighPrice() > getHighPrice()) {
            setHighPrice(candle.getHighPrice());
        }

        if (candle.getLowPrice() < getLowPrice()) {
            setLowPrice(candle.getLowPrice());
        }

        setVolume(getVolume() + candle.getVolume());

        setQuoteAssetVolume(getQuoteAssetVolume() + candle.getQuoteAssetVolume());

        setNumberOfTrades(getNumberOfTrades() + candle.getNumberOfTrades());

        setTakerBuyBaseAssetVolume(getTakerBuyBaseAssetVolume() + candle.getTakerBuyBaseAssetVolume());

        setTakerBuyQuoteAssetVolume(getTakerBuyQuoteAssetVolume() + candle.getTakerBuyQuoteAssetVolume());
    }
}
