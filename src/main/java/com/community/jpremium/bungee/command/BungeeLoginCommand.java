package com.community.jpremium.bungee.command;

import com.community.jpremium.security.SecurityRateLimitService;
import com.community.jpremium.security.PasswordHashService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.command.AbstractBungeePlayerCommand;
import com.community.jpremium.bungee.JPremium;
import com.community.jpremium.proxy.api.event.bungee.UserEvent;
import java.time.Instant;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

public class BungeeLoginCommand
extends AbstractBungeePlayerCommand {
    public BungeeLoginCommand(JPremium jPremium) {
        super(jPremium, "login");
    }

    @Override
    public void executeForPlayer(ProxiedPlayer proxiedPlayer, UserProfileData userProfile, String[] arguments) {
        int verificationCode;
        if (userProfile.isBedrock()) {
            this.messageService.sendMessageToUser(userProfile, "loginErrorUserBedrock");
            return;
        }
        if (userProfile.isPremium()) {
            this.messageService.sendMessageToUser(userProfile, "loginErrorUserPremium");
            return;
        }
        if (!userProfile.isRegistered()) {
            this.messageService.sendMessageToUser(userProfile, "loginErrorUserNotRegistered");
            return;
        }
        if (userProfile.isLogged()) {
            this.messageService.sendMessageToUser(userProfile, "loginErrorUserAlreadyLogged");
            return;
        }
        if (arguments.length != (userProfile.hasVerificationToken() ? 2 : 1)) {
            this.messageService.sendMessageToUser(userProfile, userProfile.hasVerificationToken() ? "loginErrorSecurityUsage" : "loginErrorUsage");
            return;
        }
        String passwordInput = arguments[0];
        String currentHashedPassword = userProfile.getHashedPassword();
        int automaticSessionMinutes = this.config.getInt("automaticSessionTime");
        int maxLoginAttemptsBeforeDisconnect = this.config.getInt("maximumLoginTriesBeforeDisconnection");
        boolean rehashPasswordOnAlgorithmChange = this.config.getBoolean("rehashPasswordWhenUsingDifferentAlgorithm");
        PasswordHashService.HashAlgorithm hashAlgorithm = this.config.getEnum(PasswordHashService.HashAlgorithm.class, "passwordHashingAlgorithm");
        String address = proxiedPlayer.getAddress().getAddress().getHostAddress();
        if (maxLoginAttemptsBeforeDisconnect > 0 && SecurityRateLimitService.getAddressAttemptCount(address) >= maxLoginAttemptsBeforeDisconnect) {
            this.messageService.disconnectUserWithMessage(userProfile, "loginErrorAddressBanned");
            return;
        }
        if (!PasswordHashService.verifyPassword(passwordInput, currentHashedPassword)) {
            this.plugin.fireEventAsync(new UserEvent.FailedLogin(userProfile, proxiedPlayer, UserEvent.FailedLogin.Reason.WRONG_PASSWORD));
            this.messageService.sendMessageToUser(userProfile, "loginErrorWrongPassword");
            if (maxLoginAttemptsBeforeDisconnect > 0) {
                SecurityRateLimitService.incrementAddressAttemptCount(address);
            }
            return;
        }
        if (userProfile.hasVerificationToken()) {
            try {
                verificationCode = Integer.parseInt(arguments[1]);
            }
            catch (NumberFormatException numberFormatException) {
                this.plugin.fireEventAsync(new UserEvent.FailedLogin(userProfile, proxiedPlayer, UserEvent.FailedLogin.Reason.WRONG_TOTP));
                this.messageService.sendMessageToUser(userProfile, "loginErrorWrongCode");
                if (maxLoginAttemptsBeforeDisconnect > 0) {
                    SecurityRateLimitService.incrementAddressAttemptCount(address);
                }
                return;
            }
            if (!this.plugin.getGoogleAuthenticator().authorize(userProfile.getVerificationToken(), verificationCode)) {
                this.plugin.fireEventAsync(new UserEvent.FailedLogin(userProfile, proxiedPlayer, UserEvent.FailedLogin.Reason.WRONG_TOTP));
                this.messageService.sendMessageToUser(userProfile, "loginErrorWrongCode");
                if (maxLoginAttemptsBeforeDisconnect > 0) {
                    SecurityRateLimitService.incrementAddressAttemptCount(address);
                }
                return;
            }
        }
        Instant now = Instant.now();
        Instant sessionExpiry = now.plusSeconds(automaticSessionMinutes * 60L);
        Server server = proxiedPlayer.getServer();
        userProfile.setLoggedIn(true);
        userProfile.setLoginDeadlineMillis(0L);
        userProfile.setLastAddress(address);
        userProfile.setLastSeen(now);
        if (rehashPasswordOnAlgorithmChange && PasswordHashService.needsRehash(hashAlgorithm, currentHashedPassword)) {
            userProfile.setHashedPassword(PasswordHashService.hashPassword(hashAlgorithm, passwordInput));
        }
        if (!userProfile.hasFirstAddress()) {
            userProfile.setFirstAddress(address);
        }
        if (!userProfile.hasFirstSeen()) {
            userProfile.setFirstSeen(now);
        }
        if (automaticSessionMinutes > 0) {
            userProfile.setSessionExpires(sessionExpiry);
        }
        this.messageService.clearActionBar(userProfile);
        this.messageService.clearBossBar(userProfile);
        this.messageService.sendTitleBundleToUser(userProfile, "loginSuccessLogged");
        if (!userProfile.hasEmailAddress()) {
            this.messageService.sendMessageToUser(userProfile, "loginReminderEmail");
        }
        if (!userProfile.hasVerificationToken()) {
            this.messageService.sendMessageToUser(userProfile, "loginReminderSecondFactor");
        }
        this.routingService.sendStatePayloadToServer(userProfile, server);
        this.routingService.redirectToMainOrLastServer(userProfile);
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.Login(userProfile, proxiedPlayer));
        if (userProfile.hasSession()) {
            this.plugin.fireEventAsync(new UserEvent.StartSession(userProfile, proxiedPlayer));
        }
    }
}
