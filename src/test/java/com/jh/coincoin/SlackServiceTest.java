package com.jh.coincoin;

import com.jh.coincoin.service.slack.actions.NewStrategySlashCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;

/**
 * Created by dale on 2024-09-07.
 */

@Slf4j
@ActiveProfiles("dale")
@SpringBootTest
@RequiredArgsConstructor
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class SlackServiceTest {

    private final NewStrategySlashCommand newStrategyAction;

    @Test
    public void 슬랙에_블락_킷_보내기() {
        newStrategyAction.doCommand("", "");
    }
}
