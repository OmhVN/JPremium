package com.community.jpremium.bungee.command;

import com.community.jpremium.security.PasswordHashService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.command.AbstractBungeePlayerCommand;
import com.community.jpremium.bungee.JPremium;
import com.community.jpremium.proxy.api.event.bungee.UserEvent;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeConfirmPasswordRecoveryCommand
extends AbstractBungeePlayerCommand {
    public BungeeConfirmPasswordRecoveryCommand(JPremium jPremium) {
        super(jPremium, "confirmPasswordRecovery");
    }

    @Override
    public void executeForPlayer(ProxiedPlayer proxiedPlayer, UserProfileData userProfile, String[] arguments) {
        if (!userProfile.isRegistered()) {
            this.messageService.sendMessageToUser(userProfile, "confirmPasswordRecoveryErrorUserNotRegistered");
            return;
        }
        if (!userProfile.hasCachedAddress()) {
            this.messageService.sendMessageToUser(userProfile, "confirmPasswordRecoveryErrorUserHasNotRecoveryCode");
            return;
        }
        boolean confirmPasswordEnabled = this.config.getBoolean("confirmPassword");
        if (arguments.length != (confirmPasswordEnabled ? 3 : 2)) {
            this.messageService.sendMessageToUser(userProfile, "confirmPasswordRecoveryErrorUsage");
            return;
        }
        if (!arguments[1].equals(arguments[confirmPasswordEnabled ? 2 : 1])) {
            this.messageService.sendMessageToUser(userProfile, "confirmPasswordRecoveryErrorDifferentNewPasswords");
            return;
        }
        String safePasswordPattern = this.config.getString("safePasswordPattern");
        PasswordHashService.HashAlgorithm hashAlgorithm = this.config.getEnum(PasswordHashService.HashAlgorithm.class, "passwordHashingAlgorithm");
        String hashedPassword = PasswordHashService.hashPassword(hashAlgorithm, arguments[1]);
        if (!arguments[1].matches(safePasswordPattern)) {
            this.messageService.sendMessageToUser(userProfile, "confirmPasswordRecoveryErrorUnsafeNewPassword");
            return;
        }
        if (arguments[1].toLowerCase().contains(proxiedPlayer.getName().toLowerCase())) {
            this.messageService.sendMessageToUser(userProfile, "confirmPasswordRecoveryErrorNewPasswordContainsNickname");
            return;
        }
        if (this.plugin.getWeakPasswords().contains(arguments[1].toLowerCase())) {
            this.messageService.sendMessageToUser(userProfile, "confirmPasswordRecoveryErrorPasswordTooWeak");
            return;
        }
        if (!userProfile.getCachedAddress().equals(arguments[0])) {
            this.messageService.sendMessageToUser(userProfile, "confirmPasswordRecoveryErrorWrongRecoveryCode");
            return;
        }
        userProfile.setCachedAddress(null);
        userProfile.setHashedPassword(hashedPassword);
        this.messageService.sendMessageToUser(userProfile, "confirmPasswordRecoverySuccessConfirmedPasswordRecovery");
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.RecoveryPassword(userProfile, proxiedPlayer));
    }
}

