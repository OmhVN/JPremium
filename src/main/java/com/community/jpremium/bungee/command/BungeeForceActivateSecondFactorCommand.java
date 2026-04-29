package com.community.jpremium.bungee.command;

import com.community.jpremium.bungee.command.AbstractBungeeForceUserCommand;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.JPremium;
import com.community.jpremium.proxy.api.event.bungee.UserEvent;
import net.md_5.bungee.api.CommandSender;

public class BungeeForceActivateSecondFactorCommand
extends AbstractBungeeForceUserCommand {
    public BungeeForceActivateSecondFactorCommand(JPremium jPremium) {
        super(jPremium, "forceActivateSecondFactor");
    }

    @Override
    public void executeForTarget(CommandSender commandSender, UserProfileData userProfile, String ... arguments) {
        if (arguments.length != 1) {
            this.messageService.sendMessage(commandSender, "forceActivateSecondFactorErrorUsage");
            return;
        }
        if (userProfile == null) {
            this.messageService.sendMessage(commandSender, "forceActivateSecondFactorErrorUserNotExist");
            return;
        }
        if (userProfile.isBedrock()) {
            this.messageService.sendMessage(commandSender, "forceActivateSecondFactorErrorUserBedrock");
            return;
        }
        if (userProfile.isPremium()) {
            this.messageService.sendMessage(commandSender, "forceActivateSecondFactorErrorUserPremium");
            return;
        }
        if (!userProfile.isRegistered()) {
            this.messageService.sendMessage(commandSender, "forceActivateSecondFactorErrorUserNotRegistered");
            return;
        }
        if (userProfile.hasVerificationToken()) {
            this.messageService.sendMessage(commandSender, "forceActivateSecondFactorErrorUserHasAlreadySecondFactor");
            return;
        }
        if (!userProfile.hasRecoveryCode()) {
            this.messageService.sendMessage(commandSender, "forceActivateSecondFactorErrorUserNotRequestedSecondFactor");
            return;
        }
        userProfile.setVerificationToken(userProfile.getRecoveryCode());
        userProfile.setRecoveryCode(null);
        this.messageService.sendMessage(commandSender, "forceActivateSecondFactorSuccessSecondFactorActivated");
        this.messageService.sendMessageToUser(userProfile, "activateSecondFactorSuccessSecondFactorActivated");
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.ActivateSecondFactor(userProfile, commandSender));
    }
}

