package com.community.jpremium.bungee.command;

import com.community.jpremium.bungee.command.AbstractBungeeForceUserCommand;
import com.community.jpremium.security.PasswordHashService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.JPremium;
import com.community.jpremium.proxy.api.event.bungee.UserEvent;
import net.md_5.bungee.api.CommandSender;

public class BungeeForceChangePasswordCommand
extends AbstractBungeeForceUserCommand {
    public BungeeForceChangePasswordCommand(JPremium jPremium) {
        super(jPremium, "forceChangePassword");
    }

    @Override
    public void executeForTarget(CommandSender commandSender, UserProfileData userProfile, String ... arguments) {
        if (arguments.length != 2) {
            this.messageService.sendMessage(commandSender, "forceChangePasswordErrorUsage");
            return;
        }
        if (userProfile == null) {
            this.messageService.sendMessage(commandSender, "forceChangePasswordErrorUserNotExist");
            return;
        }
        if (!userProfile.isRegistered()) {
            this.messageService.sendMessage(commandSender, "forceChangePasswordErrorUserNotRegistered");
            return;
        }
        if (!userProfile.hasHashedPassword()) {
            this.messageService.sendMessage(commandSender, "forceChangePasswordErrorUserHasNotPassword");
            return;
        }
        PasswordHashService.HashAlgorithm hashAlgorithm = this.config.getEnum(PasswordHashService.HashAlgorithm.class, "passwordHashingAlgorithm");
        String hashedPassword = PasswordHashService.hashPassword(hashAlgorithm, arguments[1]);
        userProfile.setHashedPassword(hashedPassword);
        this.messageService.sendMessage(commandSender, "forceChangePasswordSuccessPasswordChanged");
        this.messageService.sendMessageToUser(userProfile, "changePasswordSuccessPasswordChanged");
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.ChangePassword(userProfile, commandSender));
    }
}

