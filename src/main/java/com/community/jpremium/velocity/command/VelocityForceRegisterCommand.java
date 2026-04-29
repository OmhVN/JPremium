package com.community.jpremium.velocity.command;

import com.community.jpremium.velocity.command.AbstractVelocityForceUserCommand;
import com.community.jpremium.security.PasswordHashService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.proxy.api.event.velocity.UserEvent;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import java.time.Instant;

public class VelocityForceRegisterCommand
extends AbstractVelocityForceUserCommand {
    public VelocityForceRegisterCommand(JPremiumVelocity plugin) {
        super(plugin, "forceRegister");
    }

    @Override
    public void executeForTarget(CommandSource commandSource, UserProfileData userProfile, String ... arguments) {
        if (arguments.length != 2) {
            this.messageService.sendMessage(commandSource, "forceRegisterErrorUsage");
            return;
        }
        if (userProfile == null) {
            this.messageService.sendMessage(commandSource, "forceRegisterErrorUserNotExist");
            return;
        }
        if (userProfile.isBedrock()) {
            this.messageService.sendMessage(commandSource, "forceRegisterErrorUserBedrock");
            return;
        }
        if (userProfile.isPremium()) {
            this.messageService.sendMessage(commandSource, "forceRegisterErrorUserPremium");
            return;
        }
        if (userProfile.isRegistered()) {
            this.messageService.sendMessage(commandSource, "forceRegisterErrorUserAlreadyRegistered");
            return;
        }
        int automaticSessionMinutes = this.config.getInt("automaticSessionTime");
        PasswordHashService.HashAlgorithm hashAlgorithm = this.config.getEnum(PasswordHashService.HashAlgorithm.class, "passwordHashingAlgorithm");
        String hashedPassword = PasswordHashService.hashPassword(hashAlgorithm, arguments[1]);
        userProfile.setHashedPassword(hashedPassword);
        Player player = this.plugin.findPlayer(userProfile);
        if (player != null) {
            Instant now = Instant.now();
            Instant sessionExpiry = now.plusSeconds((long)automaticSessionMinutes * 60L);
            String address = player.getRemoteAddress().getAddress().getHostAddress();
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
            this.routingService.sendStatePayloadToServer(userProfile, player);
            this.routingService.redirectToMainOrLastServer(userProfile);
            if (userProfile.hasSession()) {
                this.plugin.fireEventAsync(new UserEvent.StartSession(userProfile, (CommandSource)player));
            }
        }
        this.messageService.sendMessage(commandSource, "forceRegisterSuccessRegistered");
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.Register(userProfile, commandSource));
    }
}

