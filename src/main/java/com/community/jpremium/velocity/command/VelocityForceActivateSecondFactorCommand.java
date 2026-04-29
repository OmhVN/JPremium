package com.community.jpremium.velocity.command;

import com.community.jpremium.velocity.command.AbstractVelocityForceUserCommand;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.proxy.api.event.velocity.UserEvent;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.command.CommandSource;

public class VelocityForceActivateSecondFactorCommand
extends AbstractVelocityForceUserCommand {
    public VelocityForceActivateSecondFactorCommand(JPremiumVelocity jPremiumVelocity) {
        super(jPremiumVelocity, "forceActivateSecondFactor");
    }

    @Override
    public void executeForTarget(CommandSource commandSource, UserProfileData userProfile, String ... arguments) {
        if (arguments.length != 1) {
            this.messageService.sendMessage(commandSource, "forceActivateSecondFactorErrorUsage");
            return;
        }
        if (userProfile == null) {
            this.messageService.sendMessage(commandSource, "forceActivateSecondFactorErrorUserNotExist");
            return;
        }
        if (userProfile.isBedrock()) {
            this.messageService.sendMessage(commandSource, "forceActivateSecondFactorErrorUserBedrock");
            return;
        }
        if (userProfile.isPremium()) {
            this.messageService.sendMessage(commandSource, "forceActivateSecondFactorErrorUserPremium");
            return;
        }
        if (!userProfile.isRegistered()) {
            this.messageService.sendMessage(commandSource, "forceActivateSecondFactorErrorUserNotRegistered");
            return;
        }
        if (userProfile.hasVerificationToken()) {
            this.messageService.sendMessage(commandSource, "forceActivateSecondFactorErrorUserHasAlreadySecondFactor");
            return;
        }
        if (!userProfile.hasRecoveryCode()) {
            this.messageService.sendMessage(commandSource, "forceActivateSecondFactorErrorUserNotRequestedSecondFactor");
            return;
        }
        userProfile.setVerificationToken(userProfile.getRecoveryCode());
        userProfile.setRecoveryCode(null);
        this.messageService.sendMessage(commandSource, "forceActivateSecondFactorSuccessSecondFactorActivated");
        this.messageService.sendMessageToUser(userProfile, "activateSecondFactorSuccessSecondFactorActivated");
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.ActivateSecondFactor(userProfile, commandSource));
    }
}

