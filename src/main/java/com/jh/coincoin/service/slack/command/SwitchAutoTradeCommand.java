package com.jh.coincoin.service.slack.command;

import com.jh.coincoin.model.type.SlackType.SlashCommand;
import com.jh.coincoin.service.AdminService;
import com.jh.coincoin.service.SlackMessageService;
import com.jh.coincoin.service.slack.SlashCommandHandler;
import org.springframework.stereotype.Service;

@Service
public class SwitchAutoTradeCommand extends SlashCommandHandler {

    private final AdminService adminService;

    public SwitchAutoTradeCommand(SlackMessageService slackMessageService, AdminService adminService) {
        super(slackMessageService);
        this.adminService = adminService;
    }

    @Override
    public SlashCommand getCommand() {
        return SlashCommand.SWITCH_AUTO_TRADE;
    }

    @Override
    public void doCommand(String triggerId, String parameter) {
        boolean onOff = parameter.equals("on");
        adminService.switchAutoTrade(onOff);

        slackMessageService.sendMessage("자동 매매 " + (onOff ? "시작" : "종료"));
    }
}
