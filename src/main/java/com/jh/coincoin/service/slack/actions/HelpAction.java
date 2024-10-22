package com.jh.coincoin.service.slack.actions;

import com.jh.coincoin.model.type.SlackType.Command;
import com.jh.coincoin.service.AdminService;
import com.jh.coincoin.service.slack.ActionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dale on 2024-10-21.
 */

@Service
@RequiredArgsConstructor
public class HelpAction implements ActionHandler {

    private final AdminService adminService;

    @Override
    public Command getCommand() {
        return Command.HELP;
    }

    @Override
    public Map<String, String> doAction(List<String> commandContextList) {
        String helpContext = adminService.getHelpContext();

        Map<String, String> context = new HashMap<>();
        context.put("HELP", helpContext);
        return context;
    }
}
