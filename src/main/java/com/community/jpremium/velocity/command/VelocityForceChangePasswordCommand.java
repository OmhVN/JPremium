package com.community.jpremium.velocity.command;

import com.community.jpremium.velocity.command.AbstractVelocityForceUserCommand;
import com.community.jpremium.security.PasswordHashService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.proxy.api.event.velocity.UserEvent;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.command.CommandSource;

public class VelocityForceChangePasswordCommand
extends AbstractVelocityForceUserCommand {
    public VelocityForceChangePasswordCommand(JPremiumVelocity jPremiumVelocity) {
        super(jPremiumVelocity, "forceChangePassword");
    }

    @Override
    public void executeForTarget(CommandSource commandSource, UserProfileData userProfile, String ... arguments) {
        if (arguments.length != 2) {
            this.messageService.sendMessage(commandSource, "forceChangePasswordErrorUsage");
            return;
        }
        if (userProfile == null) {
            this.messageService.sendMessage(commandSource, "forceChangePasswordErrorUserNotExist");
            return;
        }
        if (!userProfile.isRegistered()) {
            this.messageService.sendMessage(commandSource, "forceChangePasswordErrorUserNotRegistered");
            return;
        }
        if (!userProfile.hasHashedPassword()) {
            this.messageService.sendMessage(commandSource, "forceChangePasswordErrorUserHasNotPassword");
            return;
        }
        PasswordHashService.HashAlgorithm hashAlgorithm = this.config.getEnum(PasswordHashService.HashAlgorithm.class, "passwordHashingAlgorithm");
        String hashedPassword = PasswordHashService.hashPassword(hashAlgorithm, arguments[1]);
        userProfile.setHashedPassword(hashedPassword);
        this.messageService.sendMessage(commandSource, "forceChangePasswordSuccessPasswordChanged");
        this.messageService.sendMessageToUser(userProfile, "changePasswordSuccessPasswordChanged");
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.ChangePassword(userProfile, commandSource));
    }
}

