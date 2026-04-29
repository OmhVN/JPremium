package com.community.jpremium.bungee.command;

import com.community.jpremium.bungee.command.AbstractBungeeForceUserCommand;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.JPremium;
import com.community.jpremium.proxy.api.event.bungee.UserEvent;
import net.md_5.bungee.api.CommandSender;

public class BungeeForceUnregisterCommand
extends AbstractBungeeForceUserCommand {
    public BungeeForceUnregisterCommand(JPremium jPremium) {
        super(jPremium, "forceUnregister");
    }

    @Override
    public void executeForTarget(CommandSender commandSender, UserProfileData userProfile, String ... arguments) {
        if (arguments.length != 1) {
            this.messageService.sendMessage(commandSender, "forceUnregisterErrorUsage");
            return;
        }
        if (userProfile == null) {
            this.messageService.sendMessage(commandSender, "forceUnregisterErrorUserNotExist");
            return;
        }
        if (userProfile.isBedrock()) {
            this.messageService.sendMessage(commandSender, "forceUnregisterErrorUserBedrock");
            return;
        }
        if (userProfile.isPremium()) {
            this.messageService.sendMessage(commandSender, "forceUnregisterErrorUserPremium");
            return;
        }
        if (!userProfile.isRegistered()) {
            this.messageService.sendMessage(commandSender, "forceUnregisterErrorUserNotRegistered");
            return;
        }
        userProfile.setLoggedIn(false);
        userProfile.setHashedPassword(null);
        userProfile.setVerificationToken(null);
        userProfile.setEmailAddress(null);
        userProfile.setSessionExpires(null);
        userProfile.setLastAddress(null);
        userProfile.setLastSeen(null);
        userProfile.setFirstAddress(null);
        userProfile.setFirstSeen(null);
        userProfile.setRecoveryCode(null);
        userProfile.setCaptchaCode(null);
        this.messageService.sendMessage(commandSender, "forceUnregisterSuccessUnregistered");
        this.messageService.disconnectUserWithMessage(userProfile, "unregisterSuccessUnregistered");
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.Unregister(userProfile, commandSender));
    }
}

