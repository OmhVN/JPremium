package com.community.jpremium.bungee.command;

import com.community.jpremium.security.PasswordHashService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.command.AbstractBungeePlayerCommand;
import com.community.jpremium.bungee.JPremium;
import com.community.jpremium.proxy.api.event.bungee.UserEvent;
import java.time.Instant;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

public class BungeeRegisterCommand
extends AbstractBungeePlayerCommand {
    public BungeeRegisterCommand(JPremium jPremium) {
        super(jPremium, "register");
    }

    @Override
    public void executeForPlayer(ProxiedPlayer proxiedPlayer, UserProfileData userProfile, String[] arguments) {
        if (userProfile.isBedrock()) {
            this.messageService.sendMessageToUser(userProfile, "registerErrorUserBedrock");
            return;
        }
        if (userProfile.isPremium()) {
            this.messageService.sendMessageToUser(userProfile, "registerErrorUserPremium");
            return;
        }
        if (userProfile.isRegistered()) {
            this.messageService.sendMessageToUser(userProfile, "registerErrorUserAlreadyRegistered");
            return;
        }
        boolean confirmPasswordEnabled = this.config.getBoolean("confirmPassword");
        boolean captchaVerificationEnabled = this.config.getBoolean("verifyCaptchaCode");
        int automaticSessionMinutes = this.config.getInt("automaticSessionTime");
        int maximumProfilesPerAddress = this.config.getInt("maximumUserProfilesPerAddress");
        int requiredArgumentCount = captchaVerificationEnabled && confirmPasswordEnabled ? 3 : (captchaVerificationEnabled || confirmPasswordEnabled ? 2 : 1);
        String safePasswordPattern = this.config.getString("safePasswordPattern");
        PasswordHashService.HashAlgorithm hashAlgorithm = this.config.getEnum(PasswordHashService.HashAlgorithm.class, "passwordHashingAlgorithm");
        if (arguments.length != requiredArgumentCount) {
            this.messageService.sendMessageToUser(userProfile, "registerErrorUsage");
            return;
        }
        if (!arguments[0].equals(arguments[confirmPasswordEnabled ? 1 : 0])) {
            this.messageService.sendMessageToUser(userProfile, "registerErrorDifferentPasswords");
            return;
        }
        if (!arguments[0].matches(safePasswordPattern)) {
            this.messageService.sendMessageToUser(userProfile, "registerErrorUnsafePassword");
            return;
        }
        if (arguments[0].toLowerCase().contains(proxiedPlayer.getName().toLowerCase())) {
            this.messageService.sendMessageToUser(userProfile, "registerErrorPasswordContainsNickname");
            return;
        }
        if (this.plugin.getWeakPasswords().contains(arguments[0].toLowerCase())) {
            this.messageService.sendMessageToUser(userProfile, "registerErrorPasswordTooWeak");
            return;
        }
        if (captchaVerificationEnabled && !userProfile.getCaptchaCode().equals(arguments[confirmPasswordEnabled ? 2 : 1])) {
            this.messageService.sendMessageToUser(userProfile, "registerErrorWrongCaptchaCode");
            return;
        }
        Instant now = Instant.now();
        Instant sessionExpiry = now.plusSeconds(automaticSessionMinutes * 60);
        Server server = proxiedPlayer.getServer();
        String address = proxiedPlayer.getAddress().getAddress().getHostAddress();
        String hashedPassword = PasswordHashService.hashPassword(hashAlgorithm, arguments[0]);
        if (maximumProfilesPerAddress > 0 && maximumProfilesPerAddress <= this.userRepository.findByAddress(address).size()) {
            this.messageService.sendMessageToUser(userProfile, "registerErrorUserHasTooManyAccounts");
            return;
        }
        userProfile.setLoggedIn(true);
        userProfile.setLoginDeadlineMillis(0L);
        userProfile.setHashedPassword(hashedPassword);
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
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.Register(userProfile, proxiedPlayer));
        if (userProfile.hasSession()) {
            this.plugin.fireEventAsync(new UserEvent.StartSession(userProfile, proxiedPlayer));
        }
    }
}

