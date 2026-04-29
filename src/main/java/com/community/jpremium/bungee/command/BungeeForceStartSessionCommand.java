package com.community.jpremium.bungee.command;

import com.community.jpremium.bungee.command.AbstractBungeeForceUserCommand;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.JPremium;
import com.community.jpremium.proxy.api.event.bungee.UserEvent;
import java.time.Instant;
import net.md_5.bungee.api.CommandSender;

public class BungeeForceStartSessionCommand
extends AbstractBungeeForceUserCommand {
    public BungeeForceStartSessionCommand(JPremium jPremium) {
        super(jPremium, "forceStartSession");
    }

    @Override
    public void executeForTarget(CommandSender commandSender, UserProfileData userProfile, String ... arguments) {
        if (arguments.length != 1) {
            this.messageService.sendMessage(commandSender, "forceStartSessionErrorUsage");
            return;
        }
        if (userProfile == null) {
            this.messageService.sendMessage(commandSender, "forceStartSessionErrorUserNotExist");
            return;
        }
        if (userProfile.isBedrock()) {
            this.messageService.sendMessage(commandSender, "forceStartSessionErrorUserBedrock");
            return;
        }
        if (userProfile.isPremium()) {
            this.messageService.sendMessage(commandSender, "forceStartSessionErrorUserPremium");
            return;
        }
        if (!userProfile.isRegistered()) {
            this.messageService.sendMessage(commandSender, "forceStartSessionErrorUserNotRegistered");
            return;
        }
        if (!userProfile.isLogged()) {
            this.messageService.sendMessage(commandSender, "forceStartSessionErrorUserNotLogged");
            return;
        }
        if (userProfile.hasSession()) {
            this.messageService.sendMessage(commandSender, "forceStartSessionErrorUserAlreadyHasSession");
            return;
        }
        userProfile.setSessionExpires(Instant.now().plusSeconds(this.config.getInt("manuallySessionTime") * 60));
        this.messageService.sendMessage(commandSender, "forceStartSessionSuccessSessionStarted");
        this.messageService.sendMessageToUser(userProfile, "startSessionSuccessSessionStarted");
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.StartSession(userProfile, commandSender));
    }
}

