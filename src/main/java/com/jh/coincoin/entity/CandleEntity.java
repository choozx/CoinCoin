package com.jh.coincoin.entity;

import com.jh.coincoin.model.Candle;
import com.jh.coincoin.model.type.BinanceType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

/**
 * Created by dale on 2024-09-17.
 */

@Entity
@Getter
@NoArgsConstructor
@Table(name = "candle")
public class CandleEntity implements Persistable<Long> {

    @Id
    @Column(name = "candle_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long candleId;
    @Column(name = "open_time")
    private long openTime;
    @Column(name = "symbol") @Convert(converter = BinanceType.Symbol.SymbolConverter.class)
    private BinanceType.Symbol symbol;
    @Column(name = "open_price")
    private double openPrice;
    @Column(name = "high_price")
    private double highPrice;
    @Column(name = "low_price")
    private double lowPrice;
    @Column(name = "close_price")
    private double closePrice;
    @Column(name = "volume")
    private double volume;
    @Column(name = "close_time")
    private long closeTime;
    @Column(name = "quote_asset_volume")
    private double quoteAssetVolume;
    @Column(name = "number_of_trades")
    private int numberOfTrades;
    @Column(name = "taker_buy_base_asset_volume")
    private double takerBuyBaseAssetVolume;
    @Column(name = "taker_buy_quote_asset_volume")
    private double takerBuyQuoteAssetVolume;

    public static CandleEntity create(Candle candle) {
        CandleEntity entity = new CandleEntity();
        entity.openTime = candle.getOpenTime();
        entity.symbol = candle.getSymbol();
        entity.openPrice = candle.getOpenPrice();
        entity.highPrice = candle.getHighPrice();
        entity.lowPrice = candle.getLowPrice();
        entity.closePrice = candle.getClosePrice();
        entity.closeTime = candle.getCloseTime();
        entity.quoteAssetVolume = candle.getQuoteAssetVolume();
        entity.numberOfTrades = candle.getNumberOfTrades();
        entity.takerBuyBaseAssetVolume = candle.getTakerBuyBaseAssetVolume();
        entity.takerBuyQuoteAssetVolume = candle.getTakerBuyQuoteAssetVolume();
        return entity;
    }

    @Override
    public Long getId() {
        return candleId;
    }

    @Override
    public boolean isNew() {
        return false;
    }
}
