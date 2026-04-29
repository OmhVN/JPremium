package com.community.jpremium.bungee.command;

import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.command.AbstractBungeePlayerCommand;
import com.community.jpremium.bungee.JPremium;
import com.community.jpremium.proxy.api.event.bungee.UserEvent;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeDestroySessionCommand
extends AbstractBungeePlayerCommand {
    public BungeeDestroySessionCommand(JPremium jPremium) {
        super(jPremium, "destroySession");
    }

    @Override
    public void executeForPlayer(ProxiedPlayer proxiedPlayer, UserProfileData userProfile, String[] arguments) {
        if (userProfile.isBedrock()) {
            this.messageService.sendMessageToUser(userProfile, "destroySessionErrorUserBedrock");
            return;
        }
        if (userProfile.isPremium()) {
            this.messageService.sendMessageToUser(userProfile, "destroySessionErrorUserPremium");
            return;
        }
        if (!userProfile.isRegistered()) {
            this.messageService.sendMessageToUser(userProfile, "destroySessionErrorUserNotRegistered");
            return;
        }
        if (!userProfile.isLogged()) {
            this.messageService.sendMessageToUser(userProfile, "destroySessionErrorUserNotLogged");
            return;
        }
        if (!userProfile.hasSession()) {
            this.messageService.sendMessageToUser(userProfile, "destroySessionErrorUserHasNotSession");
            return;
        }
        if (arguments.length != 0) {
            this.messageService.sendMessageToUser(userProfile, "destroySessionErrorUsage");
            return;
        }
        userProfile.setSessionExpires(null);
        this.messageService.sendMessageToUser(userProfile, "destroySessionSuccessSessionDestroyed");
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.DestroySession(userProfile, proxiedPlayer));
    }
}

