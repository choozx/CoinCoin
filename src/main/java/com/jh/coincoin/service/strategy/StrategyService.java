package com.jh.coincoin.service.strategy;

import com.jh.coincoin.entity.BuyStrategyEntity;
import com.jh.coincoin.entity.TradeStrategyEntity;
import com.jh.coincoin.model.Strategy.OrderStrategyDto;
import com.jh.coincoin.model.Strategy.BuyStrategyDto;
import com.jh.coincoin.model.Strategy.TradeStrategyDto;
import com.jh.coincoin.model.type.ErrorType;
import com.jh.coincoin.repo.BuyStrategyRepository;
import com.jh.coincoin.repo.OrderStrategyRepository;
import com.jh.coincoin.repo.TradeStrategyRepository;
import com.jh.coincoin.support.ServerException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StrategyService {

    private final TradeStrategyRepository tradeStrategyRepository;
    private final OrderStrategyRepository orderStrategyRepository;
    private final BuyStrategyRepository buyStrategyRepository;

    public TradeStrategyDto getTradeStrategy(long tradeStrategyIdx) {
        Optional<TradeStrategyEntity> optionalTradeStrategyEntity = tradeStrategyRepository.findById(tradeStrategyIdx);

        if (optionalTradeStrategyEntity.isEmpty())
            throw new ServerException(ErrorType.NOT_FOUND_STRATEGY, "매매 전략을 찾을 수 없습니다.");

        return TradeStrategyDto.create(optionalTradeStrategyEntity.get());
    }

    public List<TradeStrategyDto> getTradeStrategyListByInterval(List<Integer> matchingIntervalList) {
        List<TradeStrategyEntity> tradeStrategyEntityList = tradeStrategyRepository.findAllByIntervalIn(matchingIntervalList);

        List<TradeStrategyDto> tradeStrategyDtoList = new ArrayList<>();
        for (TradeStrategyEntity tradeStrategyEntity : tradeStrategyEntityList) {
            tradeStrategyDtoList.add(TradeStrategyDto.create(tradeStrategyEntity));
        }
        return tradeStrategyDtoList;
    }

    public List<TradeStrategyDto> getAllTradeStrategy() {
        List<TradeStrategyEntity> tradeStrategyEntityList = tradeStrategyRepository.findAll();

        List<TradeStrategyDto> tradeStrategyDtoList = new ArrayList<>();
        for (TradeStrategyEntity tradeStrategyEntity : tradeStrategyEntityList) {
            tradeStrategyDtoList.add(TradeStrategyDto.create(tradeStrategyEntity));
        }
        return tradeStrategyDtoList;
    }

    public List<OrderStrategyDto> getOrderStrategyList() {
        List<OrderStrategyDto> orderStrategyDtoList = new ArrayList<>();
        orderStrategyRepository.findAll().forEach(orderStrategyEntity -> orderStrategyDtoList.add(OrderStrategyDto.create(orderStrategyEntity)));
        return orderStrategyDtoList;
    }

    public List<BuyStrategyDto> getBuyStrategyList() {
        List<BuyStrategyEntity> buyStrategyEntityList = buyStrategyRepository.findAll();
        return buyStrategyEntityList.stream().map(BuyStrategyDto::create).collect(Collectors.toList());
    }
}
