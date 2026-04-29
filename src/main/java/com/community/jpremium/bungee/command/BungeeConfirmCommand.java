package com.community.jpremium.bungee.command;

import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.command.AbstractBungeePlayerCommand;
import com.community.jpremium.bungee.JPremium;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeConfirmCommand
extends AbstractBungeePlayerCommand {
    public BungeeConfirmCommand(JPremium jPremium) {
        super(jPremium, "confirm");
    }

    @Override
    public void executeForPlayer(ProxiedPlayer proxiedPlayer, UserProfileData userProfile, String[] arguments) {
        if (!userProfile.hasPendingConfirmationAction()) {
            this.messageService.sendMessageToUser(userProfile, "confirmNotWaiting");
            return;
        }
        userProfile.runPendingConfirmationAction();
    }
}

