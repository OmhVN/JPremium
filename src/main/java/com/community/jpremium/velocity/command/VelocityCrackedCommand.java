package com.community.jpremium.velocity.command;

import com.community.jpremium.security.PasswordHashService;
import com.community.jpremium.security.UniqueIdMode;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.velocity.command.AbstractVelocityPlayerCommand;
import com.community.jpremium.proxy.api.event.velocity.UserEvent;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

public class VelocityCrackedCommand
extends AbstractVelocityPlayerCommand {
    public VelocityCrackedCommand(JPremiumVelocity jPremiumVelocity) {
        super(jPremiumVelocity, "cracked");
    }

    @Override
    public void executeForPlayer(Player player, UserProfileData userProfile, String[] arguments) {
        UniqueIdMode uniqueIdMode = this.config.getEnum(UniqueIdMode.class, "uniqueIdsType");
        if (!uniqueIdMode.usesOfflineUuid()) {
            this.messageService.sendMessageToUser(userProfile, "crackedErrorFeatureDisabled");
            return;
        }
        if (userProfile.isBedrock()) {
            this.messageService.sendMessageToUser(userProfile, "crackedErrorUserBedrock");
            return;
        }
        if (!userProfile.isPremium()) {
            this.messageService.sendMessageToUser(userProfile, "crackedErrorUserNotPremium");
            return;
        }
        if (!userProfile.hasHashedPassword()) {
            this.messageService.sendMessageToUser(userProfile, "crackedErrorUserHasNotPassword");
            return;
        }
        if (arguments.length != 1) {
            this.messageService.sendMessageToUser(userProfile, "crackedErrorUsage");
            return;
        }
        if (!PasswordHashService.verifyPassword(arguments[0], userProfile.getHashedPassword())) {
            this.messageService.sendMessageToUser(userProfile, "crackedErrorWrongPassword");
            return;
        }
        this.runOrQueueConfirmation(userProfile, "crackedConfirmation", () -> {
            userProfile.setPremiumId(null);
            this.messageService.disconnectUserWithMessage(userProfile, "crackedSuccessCrackedTurnedOn");
            this.userRepository.update(userProfile);
            this.plugin.fireEventAsync(new UserEvent.Cracked(userProfile, (CommandSource)player));
        });
    }
}

