package com.community.jpremium.bungee.command;

import com.community.jpremium.bungee.command.AbstractBungeeForceUserCommand;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.JPremium;
import com.community.jpremium.proxy.api.event.bungee.UserEvent;
import net.md_5.bungee.api.CommandSender;

public class BungeeForceDeactivateSecondFactorCommand
extends AbstractBungeeForceUserCommand {
    public BungeeForceDeactivateSecondFactorCommand(JPremium jPremium) {
        super(jPremium, "forceDeactivateSecondFactor");
    }

    @Override
    public void executeForTarget(CommandSender commandSender, UserProfileData userProfile, String ... arguments) {
        if (arguments.length != 1) {
            this.messageService.sendMessage(commandSender, "forceDeactivateSecondFactorErrorUsage");
            return;
        }
        if (userProfile == null) {
            this.messageService.sendMessage(commandSender, "forceDeactivateSecondFactorErrorUserNotExist");
            return;
        }
        if (userProfile.isBedrock()) {
            this.messageService.sendMessage(commandSender, "forceDeactivateSecondFactorErrorUserBedrock");
            return;
        }
        if (userProfile.isPremium()) {
            this.messageService.sendMessage(commandSender, "forceDeactivateSecondFactorErrorUserPremium");
            return;
        }
        if (!userProfile.isRegistered()) {
            this.messageService.sendMessage(commandSender, "forceDeactivateSecondFactorErrorUserNotRegistered");
            return;
        }
        if (!userProfile.hasVerificationToken()) {
            this.messageService.sendMessage(commandSender, "forceDeactivateSecondFactorErrorUserHasNotSecondFactor");
            return;
        }
        userProfile.setVerificationToken(null);
        this.messageService.sendMessage(commandSender, "forceDeactivateSecondFactorSuccessSecondFactorDeactivated");
        this.messageService.sendMessageToUser(userProfile, "deactivateSecondFactorSuccessSecondFactorDeactivated");
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.DeactivateSecondFactor(userProfile, commandSender));
    }
}

