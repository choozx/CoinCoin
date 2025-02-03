package com.jh.coincoin.service.strategy;

import com.jh.coincoin.entity.StrategyEntity;
import com.jh.coincoin.model.Strategy.StrategyDto;
import com.jh.coincoin.repo.StrategyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StrategyService {

    private final StrategyRepository strategyRepository;

    public List<StrategyDto> getStrategyListByInterval(List<Integer> matchingIntervalList) {
        List<StrategyEntity> strategyEntityList = strategyRepository.findAllByIntervalIn(matchingIntervalList);

        List<StrategyDto> strategyDtoList = new ArrayList<>();
        for (StrategyEntity strategyEntity : strategyEntityList) {
            strategyDtoList.add(StrategyDto.create(strategyEntity));
        }
        return strategyDtoList;
    }
}
