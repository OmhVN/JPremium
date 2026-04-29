package com.community.jpremium.velocity.command;

import com.community.jpremium.security.PasswordHashService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.velocity.command.AbstractVelocityPlayerCommand;
import com.community.jpremium.proxy.api.event.velocity.UserEvent;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

public class VelocityActivateSecondFactorCommand
extends AbstractVelocityPlayerCommand {
    public VelocityActivateSecondFactorCommand(JPremiumVelocity jPremiumVelocity) {
        super(jPremiumVelocity, "activateSecondFactor");
    }

    @Override
    public void executeForPlayer(Player player, UserProfileData userProfile, String[] arguments) {
        int n;
        if (userProfile.isBedrock()) {
            this.messageService.sendMessageToUser(userProfile, "activateSecondFactorErrorUserBedrock");
            return;
        }
        if (userProfile.isPremium()) {
            this.messageService.sendMessageToUser(userProfile, "activateSecondFactorErrorUserPremium");
            return;
        }
        if (!userProfile.isRegistered()) {
            this.messageService.sendMessageToUser(userProfile, "activateSecondFactorErrorUserNotRegistered");
            return;
        }
        if (!userProfile.isLogged()) {
            this.messageService.sendMessageToUser(userProfile, "activateSecondFactorErrorUserNotLogged");
            return;
        }
        if (userProfile.hasVerificationToken()) {
            this.messageService.sendMessageToUser(userProfile, "activateSecondFactorErrorUserHasAlreadySecondFactor");
            return;
        }
        if (!userProfile.hasRecoveryCode()) {
            this.messageService.sendMessageToUser(userProfile, "activateSecondFactorErrorUserNotRequestedSecondFactor");
            return;
        }
        if (arguments.length != 2) {
            this.messageService.sendMessageToUser(userProfile, "activateSecondFactorErrorUsage");
            return;
        }
        if (!PasswordHashService.verifyPassword(arguments[0], userProfile.getHashedPassword())) {
            this.messageService.sendMessageToUser(userProfile, "activateSecondFactorErrorWrongPassword");
            return;
        }
        try {
            n = Integer.parseInt(arguments[1]);
        }
        catch (NumberFormatException numberFormatException) {
            this.messageService.sendMessageToUser(userProfile, "activateSecondFactorErrorWrongCode");
            return;
        }
        if (!this.plugin.getGoogleAuthenticator().authorize(userProfile.getRecoveryCode(), n)) {
            this.messageService.sendMessageToUser(userProfile, "activateSecondFactorErrorWrongCode");
            return;
        }
        userProfile.setVerificationToken(userProfile.getRecoveryCode());
        userProfile.setRecoveryCode(null);
        this.messageService.sendMessageToUser(userProfile, "activateSecondFactorSuccessSecondFactorActivated");
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.ActivateSecondFactor(userProfile, (CommandSource)player));
    }
}

