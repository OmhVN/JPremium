package com.community.jpremium.velocity.command;

import com.community.jpremium.security.PasswordHashService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.velocity.command.AbstractVelocityPlayerCommand;
import com.community.jpremium.proxy.api.event.velocity.UserEvent;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

public class VelocityUnregisterCommand
extends AbstractVelocityPlayerCommand {
    public VelocityUnregisterCommand(JPremiumVelocity jPremiumVelocity) {
        super(jPremiumVelocity, "unregister");
    }

    @Override
    public void executeForPlayer(Player player, UserProfileData userProfile, String[] arguments) {
        if (userProfile.isBedrock()) {
            this.messageService.sendMessageToUser(userProfile, "unregisterErrorUserBedrock");
            return;
        }
        if (userProfile.isPremium()) {
            this.messageService.sendMessageToUser(userProfile, "unregisterErrorUserPremium");
            return;
        }
        if (!userProfile.isRegistered()) {
            this.messageService.sendMessageToUser(userProfile, "unregisterErrorUserNotRegistered");
            return;
        }
        if (!userProfile.isLogged()) {
            this.messageService.sendMessageToUser(userProfile, "unregisterErrorUserNotLogged");
            return;
        }
        if (arguments.length != 1) {
            this.messageService.sendMessageToUser(userProfile, "unregisterErrorUsage");
            return;
        }
        if (!PasswordHashService.verifyPassword(arguments[0], userProfile.getHashedPassword())) {
            this.messageService.sendMessageToUser(userProfile, "unregisterErrorWrongPassword");
            return;
        }
        if (userProfile.hasVerificationToken()) {
            this.messageService.sendMessageToUser(userProfile, "unregisterErrorSecondFactorActivated");
            return;
        }
        this.runOrQueueConfirmation(userProfile, "unregisterConfirmation", () -> {
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
            this.messageService.disconnectUserWithMessage(userProfile, "unregisterSuccessUnregistered");
            this.userRepository.update(userProfile);
            this.plugin.fireEventAsync(new UserEvent.Unregister(userProfile, (CommandSource)player));
        });
    }
}

