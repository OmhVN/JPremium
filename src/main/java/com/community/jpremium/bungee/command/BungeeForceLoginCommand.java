package com.community.jpremium.bungee.command;

import com.community.jpremium.bungee.command.AbstractBungeeForceUserCommand;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.JPremium;
import com.community.jpremium.proxy.api.event.bungee.UserEvent;
import java.time.Instant;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

public class BungeeForceLoginCommand
extends AbstractBungeeForceUserCommand {
    public BungeeForceLoginCommand(JPremium jPremium) {
        super(jPremium, "forceLogin");
    }

    @Override
    public void executeForTarget(CommandSender commandSender, UserProfileData userProfile, String ... arguments) {
        if (arguments.length != 1) {
            this.messageService.sendMessage(commandSender, "forceLoginErrorUsage");
            return;
        }
        if (userProfile == null) {
            this.messageService.sendMessage(commandSender, "forceLoginErrorUserNotExist");
            return;
        }
        if (userProfile.isBedrock()) {
            this.messageService.sendMessage(commandSender, "forceLoginErrorUserBedrock");
            return;
        }
        if (userProfile.isPremium()) {
            this.messageService.sendMessage(commandSender, "forceLoginErrorUserPremium");
            return;
        }
        ProxiedPlayer proxiedPlayer = this.plugin.findPlayer(userProfile);
        if (proxiedPlayer == null) {
            this.messageService.sendMessage(commandSender, "forceLoginErrorUserNotOnline");
            return;
        }
        if (!userProfile.isRegistered()) {
            this.messageService.sendMessage(commandSender, "forceLoginErrorUserNotRegistered");
            return;
        }
        if (userProfile.isLogged()) {
            this.messageService.sendMessage(commandSender, "forceLoginErrorUserAlreadyLogged");
            return;
        }
        int automaticSessionMinutes = this.config.getInt("automaticSessionTime");
        Instant now = Instant.now();
        Instant sessionExpiresAt = now.plusSeconds((long)automaticSessionMinutes * 60);
        Server server = proxiedPlayer.getServer();
        String currentAddress = proxiedPlayer.getAddress().getAddress().getHostAddress();
        userProfile.setLoggedIn(true);
        userProfile.setLoginDeadlineMillis(0L);
        userProfile.setLastAddress(currentAddress);
        userProfile.setLastSeen(now);
        if (!userProfile.hasFirstAddress()) {
            userProfile.setFirstAddress(currentAddress);
        }
        if (!userProfile.hasFirstSeen()) {
            userProfile.setFirstSeen(now);
        }
        if (automaticSessionMinutes > 0) {
            userProfile.setSessionExpires(sessionExpiresAt);
        }
        this.messageService.clearActionBar(userProfile);
        this.messageService.clearBossBar(userProfile);
        this.messageService.sendTitleBundleToUser(userProfile, "loginSuccessLogged");
        this.messageService.sendMessage(commandSender, "forceLoginSuccessLogged");
        if (!userProfile.hasEmailAddress()) {
            this.messageService.sendMessageToUser(userProfile, "loginReminderEmail");
        }
        if (!userProfile.hasVerificationToken()) {
            this.messageService.sendMessageToUser(userProfile, "loginReminderSecondFactor");
        }
        this.routingService.sendStatePayloadToServer(userProfile, server);
        this.routingService.redirectToMainOrLastServer(userProfile);
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.Login(userProfile, commandSender));
        if (userProfile.hasSession()) {
            this.plugin.fireEventAsync(new UserEvent.StartSession(userProfile, proxiedPlayer));
        }
    }
}

