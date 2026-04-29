package com.community.jpremium.velocity.command;

import com.community.jpremium.velocity.command.AbstractVelocityForceUserCommand;
import com.community.jpremium.security.PasswordHashService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.proxy.api.event.velocity.UserEvent;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.command.CommandSource;

public class VelocityForceConfirmPasswordRecoveryCommand
extends AbstractVelocityForceUserCommand {
    public VelocityForceConfirmPasswordRecoveryCommand(JPremiumVelocity jPremiumVelocity) {
        super(jPremiumVelocity, "forceConfirmPasswordRecovery");
    }

    @Override
    public void executeForTarget(CommandSource commandSource, UserProfileData userProfile, String ... arguments) {
        if (arguments.length != 2) {
            this.messageService.sendMessage(commandSource, "forceConfirmPasswordRecoveryErrorUsage");
            return;
        }
        if (userProfile == null) {
            this.messageService.sendMessage(commandSource, "forceConfirmPasswordRecoveryErrorUserNotExist");
            return;
        }
        if (!userProfile.hasCachedAddress()) {
            this.messageService.sendMessage(commandSource, "forceConfirmPasswordRecoveryErrorUserHasNotRecoveryCode");
            return;
        }
        PasswordHashService.HashAlgorithm hashAlgorithm = this.config.getEnum(PasswordHashService.HashAlgorithm.class, "passwordHashingAlgorithm");
        String hashedPassword = PasswordHashService.hashPassword(hashAlgorithm, arguments[1]);
        userProfile.setHashedPassword(hashedPassword);
        userProfile.setCachedAddress(null);
        this.messageService.sendMessage(commandSource, "forceConfirmPasswordRecoverySuccessConfirmedPasswordRecovery");
        this.messageService.sendMessageToUser(userProfile, "confirmPasswordRecoverySuccessConfirmedPasswordRecovery");
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.RecoveryPassword(userProfile, commandSource));
    }
}

