package com.community.jpremium.velocity.command;

import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.velocity.command.AbstractVelocityPlayerCommand;
import com.community.jpremium.proxy.api.event.velocity.UserEvent;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

public class VelocityDestroySessionCommand
extends AbstractVelocityPlayerCommand {
    public VelocityDestroySessionCommand(JPremiumVelocity jPremiumVelocity) {
        super(jPremiumVelocity, "destroySession");
    }

    @Override
    public void executeForPlayer(Player player, UserProfileData userProfile, String[] arguments) {
        if (userProfile.isBedrock()) {
            this.messageService.sendMessageToUser(userProfile, "destroySessionErrorUserBedrock");
            return;
        }
        if (userProfile.isPremium()) {
            this.messageService.sendMessageToUser(userProfile, "destroySessionErrorUserPremium");
            return;
        }
        if (!userProfile.isRegistered()) {
            this.messageService.sendMessageToUser(userProfile, "destroySessionErrorUserNotRegistered");
            return;
        }
        if (!userProfile.isLogged()) {
            this.messageService.sendMessageToUser(userProfile, "destroySessionErrorUserNotLogged");
            return;
        }
        if (!userProfile.hasSession()) {
            this.messageService.sendMessageToUser(userProfile, "destroySessionErrorUserHasNotSession");
            return;
        }
        if (arguments.length != 0) {
            this.messageService.sendMessageToUser(userProfile, "destroySessionErrorUsage");
            return;
        }
        userProfile.setSessionExpires(null);
        this.messageService.sendMessageToUser(userProfile, "destroySessionSuccessSessionDestroyed");
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.DestroySession(userProfile, (CommandSource)player));
    }
}

