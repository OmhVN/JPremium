package com.community.jpremium.bungee.command;

import com.community.jpremium.security.PasswordHashService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.command.AbstractBungeePlayerCommand;
import com.community.jpremium.bungee.JPremium;
import com.community.jpremium.proxy.api.event.bungee.UserEvent;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeUnregisterCommand
extends AbstractBungeePlayerCommand {
    public BungeeUnregisterCommand(JPremium jPremium) {
        super(jPremium, "unregister");
    }

    @Override
    public void executeForPlayer(ProxiedPlayer proxiedPlayer, UserProfileData userProfile, String[] arguments) {
        if (userProfile.isBedrock()) {
            this.messageService.sendMessageToUser(userProfile, "unregisterErrorUserBedrock");
            return;
        }
        if (userProfile.isPremium()) {
            this.messageService.sendMessageToUser(userProfile, "unregisterErrorUserPremium");
            return;
        }
        if (!userProfile.isRegistered()) {
            this.messageService.sendMessageToUser(userProfile, "unregisterErrorUserNotRegistered");
            return;
        }
        if (!userProfile.isLogged()) {
            this.messageService.sendMessageToUser(userProfile, "unregisterErrorUserNotLogged");
            return;
        }
        if (arguments.length != 1) {
            this.messageService.sendMessageToUser(userProfile, "unregisterErrorUsage");
            return;
        }
        if (!PasswordHashService.verifyPassword(arguments[0], userProfile.getHashedPassword())) {
            this.messageService.sendMessageToUser(userProfile, "unregisterErrorWrongPassword");
            return;
        }
        if (userProfile.hasVerificationToken()) {
            this.messageService.sendMessageToUser(userProfile, "unregisterErrorSecondFactorActivated");
            return;
        }
        this.runOrQueueConfirmation(userProfile, "unregisterConfirmation", new UnregisterConfirmationAction(this.plugin, userProfile));
    }

    private static class UnregisterConfirmationAction
    implements Runnable {
        private final JPremium plugin;
        private final UserProfileData targetUser;

        public UnregisterConfirmationAction(JPremium jPremium, UserProfileData userProfile) {
            this.plugin = jPremium;
            this.targetUser = userProfile;
        }

        @Override
        public void run() {
            this.targetUser.setLoggedIn(false);
            this.targetUser.setHashedPassword(null);
            this.targetUser.setVerificationToken(null);
            this.targetUser.setEmailAddress(null);
            this.targetUser.setSessionExpires(null);
            this.targetUser.setLastAddress(null);
            this.targetUser.setLastSeen(null);
            this.targetUser.setFirstAddress(null);
            this.targetUser.setFirstSeen(null);
            this.targetUser.setRecoveryCode(null);
            this.targetUser.setCaptchaCode(null);
            this.plugin.getMessageService().disconnectUserWithMessage(this.targetUser, "unregisterSuccessUnregistered");
            this.plugin.getUserRepository().update(this.targetUser);
            this.plugin.fireEventAsync(new UserEvent.Unregister(this.targetUser, this.plugin.findPlayer(this.targetUser)));
        }
    }
}

