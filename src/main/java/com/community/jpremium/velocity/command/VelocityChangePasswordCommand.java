package com.community.jpremium.velocity.command;

import com.community.jpremium.security.PasswordHashService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.velocity.command.AbstractVelocityPlayerCommand;
import com.community.jpremium.proxy.api.event.velocity.UserEvent;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

public class VelocityChangePasswordCommand
extends AbstractVelocityPlayerCommand {
    public VelocityChangePasswordCommand(JPremiumVelocity jPremiumVelocity) {
        super(jPremiumVelocity, "changePassword");
    }

    @Override
    public void executeForPlayer(Player player, UserProfileData userProfile, String[] arguments) {
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
        if (arguments[1].toLowerCase().contains(player.getUsername().toLowerCase())) {
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
        this.plugin.fireEventAsync(new UserEvent.ChangePassword(userProfile, (CommandSource)player));
    }
}

