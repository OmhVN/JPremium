package com.community.jpremium.velocity.command;

import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.velocity.command.AbstractVelocityPlayerCommand;
import com.community.jpremium.proxy.api.event.velocity.UserEvent;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import java.time.Instant;

public class VelocityStartSessionCommand
extends AbstractVelocityPlayerCommand {
    public VelocityStartSessionCommand(JPremiumVelocity jPremiumVelocity) {
        super(jPremiumVelocity, "startSession");
    }

    @Override
    public void executeForPlayer(Player player, UserProfileData userProfile, String[] arguments) {
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
        userProfile.setSessionExpires(Instant.now().plusSeconds((long)this.config.getInt("manuallySessionTime") * 60L));
        this.messageService.sendMessageToUser(userProfile, "startSessionSuccessSessionStarted");
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.StartSession(userProfile, (CommandSource)player));
    }
}

