package com.jh.coincoin.service;

import com.jh.coincoin.entity.AdminEntity;
import com.jh.coincoin.model.consts.GlobalConst;
import com.jh.coincoin.model.type.BinanceType.Interval;
import com.jh.coincoin.model.type.BinanceType.Symbol;
import com.jh.coincoin.model.type.ErrorType;
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
    private List<String> trackingIndicatorNameList;
    private Interval interval;
    private Pair<Double, Double> rsiSetting;
    private int rsiPeriod;
    private String helpContext;

    @PostConstruct
    public void init() {
        List<AdminEntity> adminEntityList = adminRepository.findAll();
        rawAdminMap = new HashMap<>();

        adminEntityList.forEach(admin -> rawAdminMap.put(admin.getName(), admin.getValue()));

        trackingSymbolList = parseSymbol();
        trackingIndicatorNameList = parseList("TRACKING_INDICATOR_NAME");

        interval = Interval.of(rawAdminMap.get("INTERVAL"));
        rsiSetting = parsePairDouble(rawAdminMap.get("RSI_SETTING"));
        rsiPeriod = Integer.parseInt(rawAdminMap.get("RSI_PERIOD"));

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

    public void setIndicator() {

    }

    public boolean isDetect(String name, String result) {
        if (name.equals("RSI")){
            double rsi = Double.parseDouble(result);
            return rsi <= rsiSetting.getLeft() || rsi >= rsiSetting.getRight();
        }

        return false;
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

    private List<String> parseList(String rawString) {
        String[] rawStringIndex = rawString.split(Pattern.quote("|"));
        return Arrays.stream(rawStringIndex).toList();
    }

    private Pair<Double, Double> parsePairDouble(String rawString) {
        String[] rawStringIndex = rawString.split(Pattern.quote("|"));
        return Pair.of(Double.parseDouble(rawStringIndex[0]), Double.parseDouble(rawStringIndex[1]));
    }
}
