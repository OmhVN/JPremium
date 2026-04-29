package com.community.jpremium.velocity.command;

import com.community.jpremium.security.PasswordHashService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.velocity.command.AbstractVelocityPlayerCommand;
import com.community.jpremium.proxy.api.event.velocity.UserEvent;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import java.time.Instant;

public class VelocityRegisterCommand
extends AbstractVelocityPlayerCommand {
    public VelocityRegisterCommand(JPremiumVelocity jPremiumVelocity) {
        super(jPremiumVelocity, "register");
    }

    @Override
    public void executeForPlayer(Player player, UserProfileData userProfile, String[] arguments) {
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
        if (arguments[0].toLowerCase().contains(player.getUsername().toLowerCase())) {
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
        Instant sessionExpiry = now.plusSeconds((long)automaticSessionMinutes * 60L);
        String address = player.getRemoteAddress().getAddress().getHostAddress();
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
        this.routingService.sendStatePayloadToServer(userProfile, player);
        this.routingService.redirectToMainOrLastServer(userProfile);
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.Register(userProfile, (CommandSource)player));
        if (userProfile.hasSession()) {
            this.plugin.fireEventAsync(new UserEvent.StartSession(userProfile, (CommandSource)player));
        }
    }
}

