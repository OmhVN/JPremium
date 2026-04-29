package com.community.jpremium.velocity.command;

import com.community.jpremium.velocity.command.AbstractVelocityForceUserCommand;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.proxy.api.event.velocity.UserEvent;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import java.time.Instant;

public class VelocityForceLoginCommand
extends AbstractVelocityForceUserCommand {
    public VelocityForceLoginCommand(JPremiumVelocity jPremiumVelocity) {
        super(jPremiumVelocity, "forceLogin");
    }

    @Override
    public void executeForTarget(CommandSource commandSource, UserProfileData userProfile, String ... arguments) {
        if (arguments.length != 1) {
            this.messageService.sendMessage(commandSource, "forceLoginErrorUsage");
            return;
        }
        if (userProfile == null) {
            this.messageService.sendMessage(commandSource, "forceLoginErrorUserNotExist");
            return;
        }
        if (userProfile.isBedrock()) {
            this.messageService.sendMessage(commandSource, "forceLoginErrorUserBedrock");
            return;
        }
        if (userProfile.isPremium()) {
            this.messageService.sendMessage(commandSource, "forceLoginErrorUserPremium");
            return;
        }
        Player player = this.plugin.findPlayer(userProfile);
        if (player == null) {
            this.messageService.sendMessage(commandSource, "forceLoginErrorUserNotOnline");
            return;
        }
        if (!userProfile.isRegistered()) {
            this.messageService.sendMessage(commandSource, "forceLoginErrorUserNotRegistered");
            return;
        }
        if (userProfile.isLogged()) {
            this.messageService.sendMessage(commandSource, "forceLoginErrorUserAlreadyLogged");
            return;
        }
        int n = this.config.getInt("automaticSessionTime");
        Instant instant = Instant.now();
        Instant instant2 = instant.plusSeconds((long)n * 60L);
        String text = player.getRemoteAddress().getAddress().getHostAddress();
        userProfile.setLoggedIn(true);
        userProfile.setLoginDeadlineMillis(0L);
        userProfile.setLastAddress(text);
        userProfile.setLastSeen(instant);
        if (!userProfile.hasFirstAddress()) {
            userProfile.setFirstAddress(text);
        }
        if (!userProfile.hasFirstSeen()) {
            userProfile.setFirstSeen(instant);
        }
        if (n > 0) {
            userProfile.setSessionExpires(instant2);
        }
        this.messageService.clearActionBar(userProfile);
        this.messageService.clearBossBar(userProfile);
        this.messageService.sendTitleBundleToUser(userProfile, "loginSuccessLogged");
        this.messageService.sendMessage(commandSource, "forceLoginSuccessLogged");
        if (!userProfile.hasEmailAddress()) {
            this.messageService.sendMessageToUser(userProfile, "loginReminderEmail");
        }
        if (!userProfile.hasVerificationToken()) {
            this.messageService.sendMessageToUser(userProfile, "loginReminderSecondFactor");
        }
        this.routingService.sendStatePayloadToServer(userProfile, player);
        this.routingService.redirectToMainOrLastServer(userProfile);
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.Login(userProfile, commandSource));
        if (userProfile.hasSession()) {
            this.plugin.fireEventAsync(new UserEvent.StartSession(userProfile, (CommandSource)player));
        }
    }
}

