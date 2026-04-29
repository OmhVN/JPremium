package com.community.jpremium.bungee.command;

import com.community.jpremium.bungee.command.AbstractBungeeForceUserCommand;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.JPremium;
import com.community.jpremium.proxy.api.event.bungee.UserEvent;
import net.md_5.bungee.api.CommandSender;

public class BungeeForceDestroySessionCommand
extends AbstractBungeeForceUserCommand {
    public BungeeForceDestroySessionCommand(JPremium jPremium) {
        super(jPremium, "forceDestroySession");
    }

    @Override
    public void executeForTarget(CommandSender commandSender, UserProfileData userProfile, String ... arguments) {
        if (arguments.length != 1) {
            this.messageService.sendMessage(commandSender, "forceDestroySessionErrorUsage");
            return;
        }
        if (userProfile == null) {
            this.messageService.sendMessage(commandSender, "forceDestroySessionErrorUserNotExist");
            return;
        }
        if (userProfile.isBedrock()) {
            this.messageService.sendMessage(commandSender, "forceDestroySessionErrorUserBedrock");
            return;
        }
        if (userProfile.isPremium()) {
            this.messageService.sendMessage(commandSender, "forceDestroySessionErrorUserPremium");
            return;
        }
        if (!userProfile.isRegistered()) {
            this.messageService.sendMessage(commandSender, "forceDestroySessionErrorUserNotRegistered");
            return;
        }
        if (!userProfile.hasSession()) {
            this.messageService.sendMessage(commandSender, "forceDestroySessionErrorUserHasNotSession");
            return;
        }
        userProfile.setSessionExpires(null);
        this.messageService.sendMessage(commandSender, "forceDestroySessionSuccessSessionDestroyed");
        this.messageService.sendMessageToUser(userProfile, "destroySessionSuccessSessionDestroyed");
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.DestroySession(userProfile, commandSender));
    }
}

