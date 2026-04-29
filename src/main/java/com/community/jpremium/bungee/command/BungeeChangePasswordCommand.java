package com.community.jpremium.bungee.command;

import com.community.jpremium.security.PasswordHashService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.command.AbstractBungeePlayerCommand;
import com.community.jpremium.bungee.JPremium;
import com.community.jpremium.proxy.api.event.bungee.UserEvent;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeChangePasswordCommand
extends AbstractBungeePlayerCommand {
    public BungeeChangePasswordCommand(JPremium jPremium) {
        super(jPremium, "changePassword");
    }

    @Override
    public void executeForPlayer(ProxiedPlayer proxiedPlayer, UserProfileData userProfile, String[] arguments) {
        if (!userProfile.isRegistered()) {
            this.messageService.sendMessageToUser(userProfile, "changePasswordErrorUserNotRegistered");
            return;
        }
        if (!userProfile.isLogged()) {
            this.messageService.sendMessageToUser(userProfile, "changePasswordErrorUserNotLogged");
            return;
        }
        if (!userProfile.hasHashedPassword()) {
            this.messageService.sendMessageToUser(userProfile, "changePasswordErrorUserHasNotPassword");
            return;
        }
        boolean confirmPasswordEnabled = this.config.getBoolean("confirmPassword");
        if (arguments.length != (confirmPasswordEnabled ? 3 : 2)) {
            this.messageService.sendMessageToUser(userProfile, "changePasswordErrorUsage");
            return;
        }
        if (!arguments[1].equals(arguments[confirmPasswordEnabled ? 2 : 1])) {
            this.messageService.sendMessageToUser(userProfile, "changePasswordErrorDifferentNewPasswords");
            return;
        }
        String safePasswordPattern = this.config.getString("safePasswordPattern");
        PasswordHashService.HashAlgorithm hashAlgorithm = this.config.getEnum(PasswordHashService.HashAlgorithm.class, "passwordHashingAlgorithm");
        String hashedPassword = PasswordHashService.hashPassword(hashAlgorithm, arguments[1]);
        if (!arguments[1].matches(safePasswordPattern)) {
            this.messageService.sendMessageToUser(userProfile, "changePasswordErrorUnsafeNewPassword");
            return;
        }
        if (arguments[1].toLowerCase().contains(proxiedPlayer.getName().toLowerCase())) {
            this.messageService.sendMessageToUser(userProfile, "changePasswordErrorNewPasswordContainsNickname");
            return;
        }
        if (this.plugin.getWeakPasswords().contains(arguments[1].toLowerCase())) {
            this.messageService.sendMessageToUser(userProfile, "changePasswordErrorPasswordTooWeak");
            return;
        }
        if (!PasswordHashService.verifyPassword(arguments[0], userProfile.getHashedPassword())) {
            this.messageService.sendMessageToUser(userProfile, "changePasswordErrorWrongCurrentPassword");
            return;
        }
        userProfile.setHashedPassword(hashedPassword);
        this.messageService.sendMessageToUser(userProfile, "changePasswordSuccessPasswordChanged");
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.ChangePassword(userProfile, proxiedPlayer));
    }
}

