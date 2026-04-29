package com.community.jpremium.velocity.command;

import com.community.jpremium.velocity.command.AbstractVelocityForceUserCommand;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.proxy.api.event.velocity.UserEvent;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.command.CommandSource;

public class VelocityForceDeactivateSecondFactorCommand
extends AbstractVelocityForceUserCommand {
    public VelocityForceDeactivateSecondFactorCommand(JPremiumVelocity jPremiumVelocity) {
        super(jPremiumVelocity, "forceDeactivateSecondFactor");
    }

    @Override
    public void executeForTarget(CommandSource commandSource, UserProfileData userProfile, String ... arguments) {
        if (arguments.length != 1) {
            this.messageService.sendMessage(commandSource, "forceDeactivateSecondFactorErrorUsage");
            return;
        }
        if (userProfile == null) {
            this.messageService.sendMessage(commandSource, "forceDeactivateSecondFactorErrorUserNotExist");
            return;
        }
        if (userProfile.isBedrock()) {
            this.messageService.sendMessage(commandSource, "forceDeactivateSecondFactorErrorUserBedrock");
            return;
        }
        if (userProfile.isPremium()) {
            this.messageService.sendMessage(commandSource, "forceDeactivateSecondFactorErrorUserPremium");
            return;
        }
        if (!userProfile.isRegistered()) {
            this.messageService.sendMessage(commandSource, "forceDeactivateSecondFactorErrorUserNotRegistered");
            return;
        }
        if (!userProfile.hasVerificationToken()) {
            this.messageService.sendMessage(commandSource, "forceDeactivateSecondFactorErrorUserHasNotSecondFactor");
            return;
        }
        userProfile.setVerificationToken(null);
        this.messageService.sendMessage(commandSource, "forceDeactivateSecondFactorSuccessSecondFactorDeactivated");
        this.messageService.sendMessageToUser(userProfile, "deactivateSecondFactorSuccessSecondFactorDeactivated");
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.DeactivateSecondFactor(userProfile, commandSource));
    }
}

