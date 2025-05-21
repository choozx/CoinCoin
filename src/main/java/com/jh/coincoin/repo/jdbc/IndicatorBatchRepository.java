package com.jh.coincoin.repo.jdbc;

import com.jh.coincoin.entity.RsiIndicatorEntity;
import com.jh.coincoin.model.type.IndicatorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by dale on 2025-02-11.
 * JPA + Mysql 조합에서는 bulk insert가 힘듬.
 * 따라서 JDBC를 이용한다.
 * 출처 : https://do5do.tistory.com/13
 */

@Slf4j
@Repository
@RequiredArgsConstructor
public class IndicatorBatchRepository {

    private final JdbcTemplate jdbcTemplate;

    public void bulkInsert(List<RsiIndicatorEntity> rsiIndicatorEntityList) {
        String sql = "INSERT INTO indicator (open_time, symbol, `interval`, value) VALUES (?,?,?,?)";
        List<Object[]> batchArgs = rsiIndicatorEntityList.stream()
                .map(indicator -> new Object[]{indicator.getOpenTime(), indicator.getSymbol().getCode(), indicator.getInterval(), indicator.getValue()}).toList();

        var firstIndicatorEntity = rsiIndicatorEntityList.get(0);
        jdbcTemplate.batchUpdate(sql, batchArgs);

        consoleLog(IndicatorType.RSI, firstIndicatorEntity.getInterval(), batchArgs.size());
    }

    private void consoleLog(IndicatorType type, int interval, int updateIndicatorCount) {
        log.info("지표 업데이트 {} : {} : {}", type, interval, updateIndicatorCount);
    }
}
