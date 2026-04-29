package com.community.jpremium.bungee.command;

import com.community.jpremium.bungee.command.AbstractBungeeForceUserCommand;
import com.community.jpremium.security.PasswordHashService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.JPremium;
import com.community.jpremium.proxy.api.event.bungee.UserEvent;
import java.time.Instant;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

public class BungeeForceRegisterCommand
extends AbstractBungeeForceUserCommand {
    public BungeeForceRegisterCommand(JPremium jPremium) {
        super(jPremium, "forceRegister");
    }

    @Override
    public void executeForTarget(CommandSender commandSender, UserProfileData userProfile, String ... arguments) {
        if (arguments.length != 2) {
            this.messageService.sendMessage(commandSender, "forceRegisterErrorUsage");
            return;
        }
        if (userProfile == null) {
            this.messageService.sendMessage(commandSender, "forceRegisterErrorUserNotExist");
            return;
        }
        if (userProfile.isBedrock()) {
            this.messageService.sendMessage(commandSender, "forceRegisterErrorUserBedrock");
            return;
        }
        if (userProfile.isPremium()) {
            this.messageService.sendMessage(commandSender, "forceRegisterErrorUserPremium");
            return;
        }
        if (userProfile.isRegistered()) {
            this.messageService.sendMessage(commandSender, "forceRegisterErrorUserAlreadyRegistered");
            return;
        }
        int automaticSessionMinutes = this.config.getInt("automaticSessionTime");
        PasswordHashService.HashAlgorithm hashAlgorithm = this.config.getEnum(PasswordHashService.HashAlgorithm.class, "passwordHashingAlgorithm");
        String hashedPassword = PasswordHashService.hashPassword(hashAlgorithm, arguments[1]);
        ProxiedPlayer proxiedPlayer = this.plugin.findPlayer(userProfile);
        userProfile.setHashedPassword(hashedPassword);
        if (proxiedPlayer != null) {
            Instant now = Instant.now();
            Instant sessionExpiry = now.plusSeconds(automaticSessionMinutes * 60);
            Server server = proxiedPlayer.getServer();
            String address = proxiedPlayer.getAddress().getAddress().getHostAddress();
            userProfile.setLoggedIn(true);
            userProfile.setLoginDeadlineMillis(0L);
            userProfile.setLastAddress(address);
            userProfile.setLastSeen(now);
            userProfile.setFirstAddress(address);
            userProfile.setFirstSeen(now);
            if (automaticSessionMinutes > 0) {
                userProfile.setSessionExpires(sessionExpiry);
            }
            this.messageService.clearActionBar(userProfile);
            this.messageService.clearBossBar(userProfile);
            this.messageService.sendTitleBundleToUser(userProfile, "registerSuccessRegistered");
            this.routingService.sendStatePayloadToServer(userProfile, server);
            this.routingService.redirectToMainOrLastServer(userProfile);
            if (userProfile.hasSession()) {
                this.plugin.fireEventAsync(new UserEvent.StartSession(userProfile, proxiedPlayer));
            }
        }
        this.messageService.sendMessage(commandSender, "forceRegisterSuccessRegistered");
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.Register(userProfile, commandSender));
    }
}

