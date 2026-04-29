package com.community.jpremium.bungee.command;

import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.command.AbstractBungeePlayerCommand;
import com.community.jpremium.bungee.JPremium;
import com.community.jpremium.proxy.api.event.bungee.UserEvent;
import java.time.Instant;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeStartSessionCommand
extends AbstractBungeePlayerCommand {
    public BungeeStartSessionCommand(JPremium jPremium) {
        super(jPremium, "startSession");
    }

    @Override
    public void executeForPlayer(ProxiedPlayer proxiedPlayer, UserProfileData userProfile, String[] arguments) {
        if (userProfile.isBedrock()) {
            this.messageService.sendMessageToUser(userProfile, "startSessionErrorUserBedrock");
            return;
        }
        if (userProfile.isPremium()) {
            this.messageService.sendMessageToUser(userProfile, "startSessionErrorUserPremium");
            return;
        }
        if (!userProfile.isRegistered()) {
            this.messageService.sendMessageToUser(userProfile, "startSessionErrorUserNotRegistered");
            return;
        }
        if (!userProfile.isLogged()) {
            this.messageService.sendMessageToUser(userProfile, "startSessionErrorUserNotLogged");
            return;
        }
        if (userProfile.hasSession()) {
            this.messageService.sendMessageToUser(userProfile, "startSessionErrorUserHasAlreadySession");
            return;
        }
        if (arguments.length != 0) {
            this.messageService.sendMessageToUser(userProfile, "startSessionErrorUsage");
            return;
        }
        userProfile.setSessionExpires(Instant.now().plusSeconds(this.config.getInt("manuallySessionTime") * 60));
        this.messageService.sendMessageToUser(userProfile, "startSessionSuccessSessionStarted");
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.StartSession(userProfile, proxiedPlayer));
    }
}

