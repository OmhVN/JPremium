package com.community.jpremium.velocity.command;

import com.community.jpremium.security.PasswordHashService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.velocity.command.AbstractVelocityPlayerCommand;
import com.community.jpremium.proxy.api.event.velocity.UserEvent;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

public class VelocityDeactivateSecondFactorCommand
extends AbstractVelocityPlayerCommand {
    public VelocityDeactivateSecondFactorCommand(JPremiumVelocity jPremiumVelocity) {
        super(jPremiumVelocity, "deactivateSecondFactor");
    }

    @Override
    public void executeForPlayer(Player player, UserProfileData userProfile, String[] arguments) {
        int n;
        if (userProfile.isBedrock()) {
            this.messageService.sendMessageToUser(userProfile, "deactivateSecondFactorErrorUserBedrock");
            return;
        }
        if (userProfile.isPremium()) {
            this.messageService.sendMessageToUser(userProfile, "deactivateSecondFactorErrorUserPremium");
            return;
        }
        if (!userProfile.isRegistered()) {
            this.messageService.sendMessageToUser(userProfile, "deactivateSecondFactorErrorUserNotRegistered");
            return;
        }
        if (!userProfile.isLogged()) {
            this.messageService.sendMessageToUser(userProfile, "deactivateSecondFactorErrorUserNotLogged");
            return;
        }
        if (!userProfile.hasVerificationToken()) {
            this.messageService.sendMessageToUser(userProfile, "deactivateSecondFactorErrorUserHasNotSecondFactor");
            return;
        }
        if (arguments.length != 2) {
            this.messageService.sendMessageToUser(userProfile, "deactivateSecondFactorErrorUsage");
            return;
        }
        if (!PasswordHashService.verifyPassword(arguments[0], userProfile.getHashedPassword())) {
            this.messageService.sendMessageToUser(userProfile, "deactivateSecondFactorErrorWrongPassword");
            return;
        }
        try {
            n = Integer.parseInt(arguments[1]);
        }
        catch (NumberFormatException numberFormatException) {
            this.messageService.sendMessageToUser(userProfile, "deactivateSecondFactorErrorWrongCode");
            return;
        }
        if (!this.plugin.getGoogleAuthenticator().authorize(userProfile.getVerificationToken(), n)) {
            this.messageService.sendMessageToUser(userProfile, "deactivateSecondFactorErrorWrongCode");
            return;
        }
        userProfile.setVerificationToken(null);
        this.messageService.sendMessageToUser(userProfile, "deactivateSecondFactorSuccessSecondFactorDeactivated");
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.DeactivateSecondFactor(userProfile, (CommandSource)player));
    }
}

