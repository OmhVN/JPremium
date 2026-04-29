package com.community.jpremium.velocity.command;

import com.community.jpremium.velocity.command.AbstractVelocityForceUserCommand;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.proxy.api.event.velocity.UserEvent;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.command.CommandSource;

public class VelocityForceDestroySessionCommand
extends AbstractVelocityForceUserCommand {
    public VelocityForceDestroySessionCommand(JPremiumVelocity jPremiumVelocity) {
        super(jPremiumVelocity, "forceDestroySession");
    }

    @Override
    public void executeForTarget(CommandSource commandSource, UserProfileData userProfile, String ... arguments) {
        if (arguments.length != 1) {
            this.messageService.sendMessage(commandSource, "forceDestroySessionErrorUsage");
            return;
        }
        if (userProfile == null) {
            this.messageService.sendMessage(commandSource, "forceDestroySessionErrorUserNotExist");
            return;
        }
        if (userProfile.isBedrock()) {
            this.messageService.sendMessage(commandSource, "forceDestroySessionErrorUserBedrock");
            return;
        }
        if (userProfile.isPremium()) {
            this.messageService.sendMessage(commandSource, "forceDestroySessionErrorUserPremium");
            return;
        }
        if (!userProfile.isRegistered()) {
            this.messageService.sendMessage(commandSource, "forceDestroySessionErrorUserNotRegistered");
            return;
        }
        if (!userProfile.hasSession()) {
            this.messageService.sendMessage(commandSource, "forceDestroySessionErrorUserHasNotSession");
            return;
        }
        userProfile.setSessionExpires(null);
        this.messageService.sendMessage(commandSource, "forceDestroySessionSuccessSessionDestroyed");
        this.messageService.sendMessageToUser(userProfile, "destroySessionSuccessSessionDestroyed");
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.DestroySession(userProfile, commandSource));
    }
}

