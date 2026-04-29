package com.community.jpremium.bungee.command;

import com.community.jpremium.bungee.command.AbstractBungeeForceUserCommand;
import com.community.jpremium.security.UniqueIdMode;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.JPremium;
import com.community.jpremium.proxy.api.event.bungee.UserEvent;
import net.md_5.bungee.api.CommandSender;

public class BungeeForceCrackedCommand
extends AbstractBungeeForceUserCommand {
    public BungeeForceCrackedCommand(JPremium jPremium) {
        super(jPremium, "forceCracked");
    }

    @Override
    public void executeForTarget(CommandSender commandSender, UserProfileData userProfile, String ... arguments) {
        UniqueIdMode uniqueIdMode = this.config.getEnum(UniqueIdMode.class, "uniqueIdsType");
        if (!uniqueIdMode.usesOfflineUuid()) {
            this.messageService.sendMessage(commandSender, "forceCrackedErrorFeatureDisabled");
            return;
        }
        if (arguments.length != 1) {
            this.messageService.sendMessage(commandSender, "forceCrackedErrorUsage");
            return;
        }
        if (userProfile == null) {
            this.messageService.sendMessage(commandSender, "forceCrackedErrorUserNotExist");
            return;
        }
        if (userProfile.isBedrock()) {
            this.messageService.sendMessage(commandSender, "forceCrackedErrorUserBedrock");
            return;
        }
        if (!userProfile.isPremium()) {
            this.messageService.sendMessage(commandSender, "forceCrackedUserAlreadyCracked");
            return;
        }
        if (!userProfile.hasHashedPassword()) {
            this.messageService.sendMessage(commandSender, "forceCrackedUserHasNotPassword");
            return;
        }
        userProfile.setPremiumId(null);
        this.messageService.sendMessage(commandSender, "forceCrackedSuccessCrackedTurnedOn");
        this.messageService.disconnectUserWithMessage(userProfile, "crackedSuccessCrackedTurnedOn");
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.Cracked(userProfile, commandSender));
    }
}

