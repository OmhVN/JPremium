package com.community.jpremium.velocity.command;

import com.community.jpremium.velocity.command.AbstractVelocityForceUserCommand;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.proxy.api.event.velocity.UserEvent;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.command.CommandSource;

public class VelocityForceUnregisterCommand
extends AbstractVelocityForceUserCommand {
    public VelocityForceUnregisterCommand(JPremiumVelocity jPremiumVelocity) {
        super(jPremiumVelocity, "forceUnregister");
    }

    @Override
    public void executeForTarget(CommandSource commandSource, UserProfileData userProfile, String ... arguments) {
        if (arguments.length != 1) {
            this.messageService.sendMessage(commandSource, "forceUnregisterErrorUsage");
            return;
        }
        if (userProfile == null) {
            this.messageService.sendMessage(commandSource, "forceUnregisterErrorUserNotExist");
            return;
        }
        if (userProfile.isBedrock()) {
            this.messageService.sendMessage(commandSource, "forceUnregisterErrorUserBedrock");
            return;
        }
        if (userProfile.isPremium()) {
            this.messageService.sendMessage(commandSource, "forceUnregisterErrorUserPremium");
            return;
        }
        if (!userProfile.isRegistered()) {
            this.messageService.sendMessage(commandSource, "forceUnregisterErrorUserNotRegistered");
            return;
        }
        userProfile.setLoggedIn(false);
        userProfile.setHashedPassword(null);
        userProfile.setVerificationToken(null);
        userProfile.setEmailAddress(null);
        userProfile.setSessionExpires(null);
        userProfile.setLastAddress(null);
        userProfile.setLastSeen(null);
        userProfile.setFirstAddress(null);
        userProfile.setFirstSeen(null);
        userProfile.setRecoveryCode(null);
        userProfile.setCaptchaCode(null);
        this.messageService.sendMessage(commandSource, "forceUnregisterSuccessUnregistered");
        this.messageService.disconnectUserWithMessage(userProfile, "unregisterSuccessUnregistered");
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.Unregister(userProfile, commandSource));
    }
}

