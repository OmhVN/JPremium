package com.community.jpremium.velocity.listener;

import com.community.jpremium.common.service.OnlineUserRegistry;
import com.community.jpremium.common.config.VelocityConfigService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.proxy.Player;
import java.util.List;

public class VelocityCommandGuardListener {
    private final VelocityConfigService config;
    private final OnlineUserRegistry onlineUserRegistry;

    public VelocityCommandGuardListener(JPremiumVelocity jPremiumVelocity) {
        this.config = jPremiumVelocity.getConfig();
        this.onlineUserRegistry = jPremiumVelocity.getOnlineUserRegistry();
    }

    @Subscribe
    public void onCommandExecute(CommandExecuteEvent commandExecuteEvent) {
        if (!(commandExecuteEvent.getCommandSource() instanceof Player player)) {
            return;
        }
        UserProfileData userProfile = this.onlineUserRegistry.findByUniqueId(player.getUniqueId()).orElse(null);
        if (userProfile == null) {
            commandExecuteEvent.setResult(CommandExecuteEvent.CommandResult.denied());
            return;
        }
        if (userProfile.isLogged()) {
            return;
        }
        List<String> logoutUserCommands = this.config.getStringList("logoutUserCommands");
        if (!logoutUserCommands.contains(this.extractCommandName(commandExecuteEvent))) {
            commandExecuteEvent.setResult(CommandExecuteEvent.CommandResult.denied());
        }
    }

    private String extractCommandName(CommandExecuteEvent commandExecuteEvent) {
        return commandExecuteEvent.getCommand().split(" ")[0];
    }
}
