package com.jh.coincoin.repo.jdbc;

import com.jh.coincoin.entity.IndicatorEntity;
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

    public void bulkInsert(List<IndicatorEntity> indicatorEntityList) {
        String sql = "INSERT INTO indicator (type, open_time, symbol, `interval`, value) VALUES (?,?,?,?,?)";
        List<Object[]> batchArgs = indicatorEntityList.stream()
                .map(indicator -> new Object[]{indicator.getType().getCode(), indicator.getOpenTime(), indicator.getSymbol().getCode(), indicator.getInterval(), indicator.getValue()}).toList();

        var firstIndicatorEntity = indicatorEntityList.get(0);
        jdbcTemplate.batchUpdate(sql, batchArgs);
        log.info("지표 업데이트 {} : {} : {}", firstIndicatorEntity.getType(), firstIndicatorEntity.getInterval(), indicatorEntityList.size());
    }
}
