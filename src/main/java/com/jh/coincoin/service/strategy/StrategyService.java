package com.jh.coincoin.service.strategy;

import com.jh.coincoin.entity.TradeStrategyEntity;
import com.jh.coincoin.model.Strategy.TradeStrategyDto;
import com.jh.coincoin.repo.TradeStrategyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StrategyService {

    private final TradeStrategyRepository tradeStrategyRepository;

    public List<TradeStrategyDto> getTradeStrategyListByInterval(List<Integer> matchingIntervalList) {
        List<TradeStrategyEntity> tradeStrategyEntityList = tradeStrategyRepository.findAllByIntervalIn(matchingIntervalList);

        List<TradeStrategyDto> tradeStrategyDtoList = new ArrayList<>();
        for (TradeStrategyEntity tradeStrategyEntity : tradeStrategyEntityList) {
            tradeStrategyDtoList.add(TradeStrategyDto.create(tradeStrategyEntity));
        }
        return tradeStrategyDtoList;
    }


}
