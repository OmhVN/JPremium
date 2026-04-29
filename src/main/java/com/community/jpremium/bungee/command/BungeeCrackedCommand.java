package com.community.jpremium.bungee.command;

import com.community.jpremium.security.PasswordHashService;
import com.community.jpremium.security.UniqueIdMode;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.command.AbstractBungeePlayerCommand;
import com.community.jpremium.bungee.JPremium;
import com.community.jpremium.proxy.api.event.bungee.UserEvent;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeCrackedCommand
extends AbstractBungeePlayerCommand {
    public BungeeCrackedCommand(JPremium jPremium) {
        super(jPremium, "cracked");
    }

    @Override
    public void executeForPlayer(ProxiedPlayer proxiedPlayer, UserProfileData userProfile, String[] arguments) {
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
        this.runOrQueueConfirmation(userProfile, "crackedConfirmation", new CrackedConfirmationAction(this.plugin, userProfile));
    }

    private static class CrackedConfirmationAction
    implements Runnable {
        private final JPremium plugin;
        private final UserProfileData targetUser;

        public CrackedConfirmationAction(JPremium jPremium, UserProfileData userProfile) {
            this.plugin = jPremium;
            this.targetUser = userProfile;
        }

        @Override
        public void run() {
            this.targetUser.setPremiumId(null);
            this.plugin.getMessageService().disconnectUserWithMessage(this.targetUser, "crackedSuccessCrackedTurnedOn");
            this.plugin.getUserRepository().update(this.targetUser);
            this.plugin.fireEventAsync(new UserEvent.Cracked(this.targetUser, this.plugin.findPlayer(this.targetUser)));
        }
    }
}

