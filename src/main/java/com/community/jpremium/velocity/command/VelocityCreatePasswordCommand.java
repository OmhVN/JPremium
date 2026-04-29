package com.community.jpremium.velocity.command;

import com.community.jpremium.security.PasswordHashService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.velocity.command.AbstractVelocityPlayerCommand;
import com.community.jpremium.proxy.api.event.velocity.UserEvent;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

public class VelocityCreatePasswordCommand
extends AbstractVelocityPlayerCommand {
    public VelocityCreatePasswordCommand(JPremiumVelocity jPremiumVelocity) {
        super(jPremiumVelocity, "createPassword");
    }

    @Override
    public void executeForPlayer(Player player, UserProfileData userProfile, String[] arguments) {
        if (!userProfile.isRegistered()) {
            this.messageService.sendMessageToUser(userProfile, "createPasswordErrorUserNotRegistered");
            return;
        }
        if (!userProfile.isLogged()) {
            this.messageService.sendMessageToUser(userProfile, "createPasswordErrorUserNotLogged");
            return;
        }
        if (userProfile.hasHashedPassword()) {
            this.messageService.sendMessageToUser(userProfile, "createPasswordErrorUserAlreadyHasPassword");
            return;
        }
        boolean confirmPasswordEnabled = this.config.getBoolean("confirmPassword");
        if (arguments.length != (confirmPasswordEnabled ? 2 : 1)) {
            this.messageService.sendMessageToUser(userProfile, "createPasswordErrorUsage");
            return;
        }
        String safePasswordPattern = this.config.getString("safePasswordPattern");
        PasswordHashService.HashAlgorithm hashAlgorithm = this.config.getEnum(PasswordHashService.HashAlgorithm.class, "passwordHashingAlgorithm");
        String hashedPassword = PasswordHashService.hashPassword(hashAlgorithm, arguments[0]);
        if (!arguments[0].equals(arguments[confirmPasswordEnabled ? 1 : 0])) {
            this.messageService.sendMessageToUser(userProfile, "createPasswordErrorDifferentPasswords");
            return;
        }
        if (!arguments[0].matches(safePasswordPattern)) {
            this.messageService.sendMessageToUser(userProfile, "createPasswordErrorUnsafePassword");
            return;
        }
        if (arguments[0].toLowerCase().contains(player.getUsername().toLowerCase())) {
            this.messageService.sendMessageToUser(userProfile, "createPasswordErrorPasswordContainsNickname");
            return;
        }
        if (this.plugin.getWeakPasswords().contains(arguments[0].toLowerCase())) {
            this.messageService.sendMessageToUser(userProfile, "createPasswordErrorPasswordTooWeak");
            return;
        }
        userProfile.setHashedPassword(hashedPassword);
        this.messageService.sendMessageToUser(userProfile, "createPasswordSuccessPasswordCreated");
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.CreatePassword(userProfile, (CommandSource)player));
    }
}

