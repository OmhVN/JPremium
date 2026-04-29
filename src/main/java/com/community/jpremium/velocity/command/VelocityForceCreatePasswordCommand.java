package com.community.jpremium.velocity.command;

import com.community.jpremium.velocity.command.AbstractVelocityForceUserCommand;
import com.community.jpremium.security.PasswordHashService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.proxy.api.event.velocity.UserEvent;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.command.CommandSource;

public class VelocityForceCreatePasswordCommand
extends AbstractVelocityForceUserCommand {
    public VelocityForceCreatePasswordCommand(JPremiumVelocity jPremiumVelocity) {
        super(jPremiumVelocity, "forceCreatePassword");
    }

    @Override
    public void executeForTarget(CommandSource commandSource, UserProfileData userProfile, String ... arguments) {
        if (arguments.length != 2) {
            this.messageService.sendMessage(commandSource, "forceCreatePasswordErrorUsage");
            return;
        }
        if (userProfile == null) {
            this.messageService.sendMessage(commandSource, "forceCreatePasswordErrorUserNotExist");
            return;
        }
        if (!userProfile.isRegistered()) {
            this.messageService.sendMessage(commandSource, "forceCreatePasswordErrorUserNotRegistered");
            return;
        }
        if (userProfile.hasHashedPassword()) {
            this.messageService.sendMessage(commandSource, "forceCreatePasswordErrorUserAlreadyHasPassword");
            return;
        }
        PasswordHashService.HashAlgorithm hashAlgorithm = this.config.getEnum(PasswordHashService.HashAlgorithm.class, "passwordHashingAlgorithm");
        String hashedPassword = PasswordHashService.hashPassword(hashAlgorithm, arguments[1]);
        userProfile.setHashedPassword(hashedPassword);
        this.messageService.sendMessage(commandSource, "forceCreatePasswordSuccessPasswordCreated");
        this.messageService.sendMessageToUser(userProfile, "createPasswordSuccessPasswordCreated");
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.CreatePassword(userProfile, commandSource));
    }
}

