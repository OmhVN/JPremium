package com.community.jpremium.bungee.command;

import com.community.jpremium.bungee.command.AbstractBungeeForceUserCommand;
import com.community.jpremium.security.PasswordHashService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.JPremium;
import com.community.jpremium.proxy.api.event.bungee.UserEvent;
import net.md_5.bungee.api.CommandSender;

public class BungeeForceCreatePasswordCommand
extends AbstractBungeeForceUserCommand {
    public BungeeForceCreatePasswordCommand(JPremium jPremium) {
        super(jPremium, "forceCreatePassword");
    }

    @Override
    public void executeForTarget(CommandSender commandSender, UserProfileData userProfile, String ... arguments) {
        if (arguments.length != 2) {
            this.messageService.sendMessage(commandSender, "forceCreatePasswordErrorUsage");
            return;
        }
        if (userProfile == null) {
            this.messageService.sendMessage(commandSender, "forceCreatePasswordErrorUserNotExist");
            return;
        }
        if (!userProfile.isRegistered()) {
            this.messageService.sendMessage(commandSender, "forceCreatePasswordErrorUserNotRegistered");
            return;
        }
        if (userProfile.hasHashedPassword()) {
            this.messageService.sendMessage(commandSender, "forceCreatePasswordErrorUserAlreadyHasPassword");
            return;
        }
        PasswordHashService.HashAlgorithm hashAlgorithm = this.config.getEnum(PasswordHashService.HashAlgorithm.class, "passwordHashingAlgorithm");
        String hashedPassword = PasswordHashService.hashPassword(hashAlgorithm, arguments[1]);
        userProfile.setHashedPassword(hashedPassword);
        this.messageService.sendMessage(commandSender, "forceCreatePasswordSuccessPasswordCreated");
        this.messageService.sendMessageToUser(userProfile, "createPasswordSuccessPasswordCreated");
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.CreatePassword(userProfile, commandSender));
    }
}

