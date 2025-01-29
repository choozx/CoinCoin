package com.jh.coincoin.service;

import com.jh.coincoin.entity.AdminEntity;
import com.jh.coincoin.model.consts.GlobalConst;
import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.ErrorType;
import com.jh.coincoin.model.type.IndicatorType;
import com.jh.coincoin.model.type.StrategyType.BuyStrategyType;
import com.jh.coincoin.model.type.StrategyType.OrderStrategyType;
import com.jh.coincoin.repo.AdminRepository;
import com.jh.coincoin.support.ServerException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by dale on 2024-09-11.
 */

@Service
@Getter
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private Map<String, String> rawAdminMap;

    private List<Symbol> trackingSymbolList;  // 공유자원이라 동시성 이슈가 있긴하지만, 일단 나만 쓰는거라 나중에 생각...
    private List<IndicatorType> trackingIndicatorList;
    private Interval interval;
    private Pair<Double, Double> alertRsiValuePair; // 슬랙 알람을 위한 rsi값
    private Pair<Double, Double> orderRsiValuePair; // 포지션 진입을 위한 rsi값
    private int rsiPeriod;
    private String helpContext;
    private float orderBalanceRatio;
    private int leverage;
    private Pair<Double, Double> riskRewardRatio;

    @PostConstruct
    public void init() {
        List<AdminEntity> adminEntityList = adminRepository.findAll();
        rawAdminMap = new HashMap<>();

        adminEntityList.forEach(admin -> rawAdminMap.put(admin.getName(), admin.getValue()));

        trackingSymbolList = parseSymbol();
        trackingIndicatorList = parseList(rawAdminMap.get("TRACKING_INDICATOR_NAME"));

        interval = Interval.of(rawAdminMap.get("INTERVAL"));
        alertRsiValuePair = parsePairDouble(rawAdminMap.get("RSI_SETTING"));
        orderRsiValuePair = Pair.of(20.0, 80.0);
        rsiPeriod = Integer.parseInt(rawAdminMap.get("RSI_PERIOD"));
        orderBalanceRatio = Float.parseFloat(rawAdminMap.get("ORDER_BALANCE_PERCENT"));
        leverage = Integer.parseInt(rawAdminMap.get("LEVERAGE"));
        riskRewardRatio = parsePairDouble(rawAdminMap.get("RISK_REWARD_RATIO"));

        helpContext = rawAdminMap.get("HELP_CONTEXT");
    }

    public void setSymbol(Symbol symbol) {
        if (trackingSymbolList.size() >= GlobalConst.MAX_TRACKING_SYMBOL_COUNT)
            throw new ServerException(ErrorType.OVERFLOW_CANDLE_COUNT, "추가 가능한 코인갯수 초과");

        trackingSymbolList.add(symbol);

        AdminEntity adminEntity = adminRepository.findByName("TRACKING_SYMBOL");
        List<Integer> symbolCodeList = trackingSymbolList.stream().map(Symbol::getCode).toList();
        adminEntity.changeValue(symbolCodeList.stream().map(String::valueOf).collect(Collectors.joining("|")));

        adminRepository.saveAndFlush(adminEntity);
    }

    public void deleteSymbol(Symbol symbol) {
        trackingSymbolList.remove(symbol);

        AdminEntity adminEntity = adminRepository.findByName("TRACKING_SYMBOL");
        List<Integer> symbolCodeList = trackingSymbolList.stream().map(Symbol::getCode).toList();
        adminEntity.changeValue(symbolCodeList.stream().map(String::valueOf).collect(Collectors.joining("|")));

        adminRepository.saveAndFlush(adminEntity);
    }

    private List<Symbol> parseSymbol() {
        String rawSymbol = rawAdminMap.get("TRACKING_SYMBOL");
        String[] symbolCodeIndex = rawSymbol.split(Pattern.quote("|"));
        List<Symbol> symbolList = new ArrayList<>();
        for (String code : symbolCodeIndex) {
            Symbol symbol = Symbol.of(Integer.parseInt(code));
            symbolList.add(symbol);
        }
        return symbolList;
    }

    private List<IndicatorType> parseList(String rawString) {
        String[] rawStringIndex = rawString.split(Pattern.quote("|"));
        return Arrays.stream(rawStringIndex).map(s -> IndicatorType.of(Integer.parseInt(s))).collect(Collectors.toList());
    }

    private Pair<Double, Double> parsePairDouble(String rawString) {
        String[] rawStringIndex = rawString.split(Pattern.quote("|"));
        return Pair.of(Double.parseDouble(rawStringIndex[0]), Double.parseDouble(rawStringIndex[1]));
    }

    public List<OrderStrategyType> getFollowOrderStrategyList() {
        return new ArrayList<>();
    }

    public BuyStrategyType getFollowBuyStrategy() {
        return BuyStrategyType.STOP_AND_LIMIT;
    }
}
