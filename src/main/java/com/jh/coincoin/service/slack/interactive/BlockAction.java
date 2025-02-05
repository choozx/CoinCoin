package com.jh.coincoin.service.slack.interactive;

import com.fasterxml.jackson.databind.JsonNode;
import com.jh.coincoin.model.type.SlackType.SheetType;
import com.jh.coincoin.model.type.SlackType.InteractiveType;

import com.jh.coincoin.service.slack.InteractiveTypeHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockAction implements InteractiveTypeHandler {

    private Map<SheetType, SheetHandler> strategySheetMap;

    @Autowired
    public void setStrategySheetMap(Set<SheetHandler> sheetHandlerSet) {
        this.strategySheetMap = sheetHandlerSet.stream().collect(Collectors.toMap(SheetHandler::getType, Function.identity()));
    }

    @Override
    public InteractiveType getType() {
        return InteractiveType.BLOCK_ACTIONS;
    }

    @Override
    public void handleInteractiveType(JsonNode jsonNode) {
        SheetType sheetType = SheetType.ORDER;
        SheetHandler handler = strategySheetMap.get(sheetType);

        String viewId = "";
        JsonNode selectedOption = jsonNode.get("view");
        handler.updateSheet(viewId, selectedOption);
    }
}
