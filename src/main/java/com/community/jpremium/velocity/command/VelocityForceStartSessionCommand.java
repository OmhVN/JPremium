package com.community.jpremium.velocity.command;

import com.community.jpremium.velocity.command.AbstractVelocityForceUserCommand;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.proxy.api.event.velocity.UserEvent;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.command.CommandSource;
import java.time.Instant;

public class VelocityForceStartSessionCommand
extends AbstractVelocityForceUserCommand {
    public VelocityForceStartSessionCommand(JPremiumVelocity jPremiumVelocity) {
        super(jPremiumVelocity, "forceStartSession");
    }

    @Override
    public void executeForTarget(CommandSource commandSource, UserProfileData userProfile, String ... arguments) {
        if (arguments.length != 1) {
            this.messageService.sendMessage(commandSource, "forceStartSessionErrorUsage");
            return;
        }
        if (userProfile == null) {
            this.messageService.sendMessage(commandSource, "forceStartSessionErrorUserNotExist");
            return;
        }
        if (userProfile.isBedrock()) {
            this.messageService.sendMessage(commandSource, "forceStartSessionErrorUserBedrock");
            return;
        }
        if (userProfile.isPremium()) {
            this.messageService.sendMessage(commandSource, "forceStartSessionErrorUserPremium");
            return;
        }
        if (!userProfile.isRegistered()) {
            this.messageService.sendMessage(commandSource, "forceStartSessionErrorUserNotRegistered");
            return;
        }
        if (!userProfile.isLogged()) {
            this.messageService.sendMessage(commandSource, "forceStartSessionErrorUserNotLogged");
            return;
        }
        if (userProfile.hasSession()) {
            this.messageService.sendMessage(commandSource, "forceStartSessionErrorUserAlreadyHasSession");
            return;
        }
        userProfile.setSessionExpires(Instant.now().plusSeconds((long)this.config.getInt("manuallySessionTime") * 60L));
        this.messageService.sendMessage(commandSource, "forceStartSessionSuccessSessionStarted");
        this.messageService.sendMessageToUser(userProfile, "startSessionSuccessSessionStarted");
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.StartSession(userProfile, commandSource));
    }
}

