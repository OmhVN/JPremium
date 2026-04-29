package com.community.jpremium.velocity.command;

import com.community.jpremium.velocity.command.AbstractVelocityForceUserCommand;
import com.community.jpremium.security.UniqueIdMode;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.proxy.api.event.velocity.UserEvent;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.command.CommandSource;

public class VelocityForceCrackedCommand
extends AbstractVelocityForceUserCommand {
    public VelocityForceCrackedCommand(JPremiumVelocity jPremiumVelocity) {
        super(jPremiumVelocity, "forceCracked");
    }

    @Override
    public void executeForTarget(CommandSource commandSource, UserProfileData userProfile, String ... arguments) {
        UniqueIdMode uniqueIdMode = this.config.getEnum(UniqueIdMode.class, "uniqueIdsType");
        if (!uniqueIdMode.usesOfflineUuid()) {
            this.messageService.sendMessage(commandSource, "forceCrackedErrorFeatureDisabled");
            return;
        }
        if (arguments.length != 1) {
            this.messageService.sendMessage(commandSource, "forceCrackedErrorUsage");
            return;
        }
        if (userProfile == null) {
            this.messageService.sendMessage(commandSource, "forceCrackedErrorUserNotExist");
            return;
        }
        if (userProfile.isBedrock()) {
            this.messageService.sendMessage(commandSource, "forceCrackedErrorUserBedrock");
            return;
        }
        if (!userProfile.isPremium()) {
            this.messageService.sendMessage(commandSource, "forceCrackedUserAlreadyCracked");
            return;
        }
        if (!userProfile.hasHashedPassword()) {
            this.messageService.sendMessage(commandSource, "forceCrackedUserHasNotPassword");
            return;
        }
        userProfile.setPremiumId(null);
        this.messageService.sendMessage(commandSource, "forceCrackedSuccessCrackedTurnedOn");
        this.messageService.disconnectUserWithMessage(userProfile, "crackedSuccessCrackedTurnedOn");
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.Cracked(userProfile, commandSource));
    }
}

