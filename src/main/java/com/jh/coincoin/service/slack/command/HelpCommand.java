package com.jh.coincoin.service.slack.command;

import com.jh.coincoin.model.type.SlackType.SlashCommand;
import com.jh.coincoin.service.AdminService;
import com.jh.coincoin.service.SlackMessageService;
import com.jh.coincoin.service.slack.SlashCommandHandler;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dale on 2024-10-21.
 */

@Service
public class HelpCommand extends SlashCommandHandler {

    private final AdminService adminService;

    public HelpCommand(SlackMessageService slackMessageService, AdminService adminService) {
        super(slackMessageService);
        this.adminService = adminService;
    }


    @Override
    public SlashCommand getCommand() {
        return SlashCommand.HELP;
    }

    @Override
    public void doCommand(String triggerId, String parameter) {
        String helpContext = adminService.getHelpContext();

        Map<String, String> context = new HashMap<>();
        context.put("HELP", helpContext);

        slackMessageService.sendMessage(context);
    }
}
