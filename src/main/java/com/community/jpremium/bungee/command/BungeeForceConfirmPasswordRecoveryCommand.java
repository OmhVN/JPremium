package com.community.jpremium.bungee.command;

import com.community.jpremium.bungee.command.AbstractBungeeForceUserCommand;
import com.community.jpremium.security.PasswordHashService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.JPremium;
import com.community.jpremium.proxy.api.event.bungee.UserEvent;
import net.md_5.bungee.api.CommandSender;

public class BungeeForceConfirmPasswordRecoveryCommand
extends AbstractBungeeForceUserCommand {
    public BungeeForceConfirmPasswordRecoveryCommand(JPremium jPremium) {
        super(jPremium, "forceConfirmPasswordRecovery");
    }

    @Override
    public void executeForTarget(CommandSender commandSender, UserProfileData userProfile, String ... arguments) {
        if (arguments.length != 2) {
            this.messageService.sendMessage(commandSender, "forceConfirmPasswordRecoveryErrorUsage");
            return;
        }
        if (userProfile == null) {
            this.messageService.sendMessage(commandSender, "forceConfirmPasswordRecoveryErrorUserNotExist");
            return;
        }
        if (!userProfile.hasCachedAddress()) {
            this.messageService.sendMessage(commandSender, "forceConfirmPasswordRecoveryErrorUserHasNotRecoveryCode");
            return;
        }
        PasswordHashService.HashAlgorithm hashAlgorithm = this.config.getEnum(PasswordHashService.HashAlgorithm.class, "passwordHashingAlgorithm");
        String hashedPassword = PasswordHashService.hashPassword(hashAlgorithm, arguments[1]);
        userProfile.setHashedPassword(hashedPassword);
        userProfile.setCachedAddress(null);
        this.messageService.sendMessage(commandSender, "forceConfirmPasswordRecoverySuccessConfirmedPasswordRecovery");
        this.messageService.sendMessageToUser(userProfile, "confirmPasswordRecoverySuccessConfirmedPasswordRecovery");
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.RecoveryPassword(userProfile, commandSender));
    }
}

