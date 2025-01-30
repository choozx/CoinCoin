package com.jh.coincoin.service.slack.actions;

import com.jh.coincoin.model.type.SlackType.Command;
import com.jh.coincoin.service.AdminService;
import com.jh.coincoin.service.slack.ActionHandler;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dale on 2024-10-21.
 */

@Service
public class HelpAction extends ActionHandler {

    private final AdminService adminService;

    public HelpAction(String webHookURL, AdminService adminService) {
        super(webHookURL);
        this.adminService = adminService;
    }

    @Override
    public Command getCommand() {
        return Command.HELP;
    }

    @Override
    public void doAction(List<String> commandContextList) {
        String helpContext = adminService.getHelpContext();

        Map<String, String> context = new HashMap<>();
        context.put("HELP", helpContext);

        sendMessage(context);
    }
}
