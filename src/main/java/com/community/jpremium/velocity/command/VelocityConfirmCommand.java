package com.community.jpremium.velocity.command;

import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.velocity.command.AbstractVelocityPlayerCommand;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.proxy.Player;

public class VelocityConfirmCommand
extends AbstractVelocityPlayerCommand {
    public VelocityConfirmCommand(JPremiumVelocity jPremiumVelocity) {
        super(jPremiumVelocity, "confirm");
    }

    @Override
    public void executeForPlayer(Player player, UserProfileData userProfile, String[] arguments) {
        if (!userProfile.hasPendingConfirmationAction()) {
            this.messageService.sendMessageToUser(userProfile, "confirmNotWaiting");
            return;
        }
        userProfile.runPendingConfirmationAction();
    }
}

