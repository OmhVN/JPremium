package com.community.jpremium.velocity.command;

import com.community.jpremium.security.PasswordHashService;
import com.community.jpremium.security.UniqueIdMode;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.velocity.command.AbstractVelocityPlayerCommand;
import com.community.jpremium.proxy.api.event.velocity.UserEvent;
import com.community.jpremium.proxy.api.resolver.Profile;
import com.community.jpremium.proxy.api.resolver.ResolverException;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import java.util.UUID;

public class VelocityPremiumCommand
extends AbstractVelocityPlayerCommand {
    public VelocityPremiumCommand(JPremiumVelocity jPremiumVelocity) {
        super(jPremiumVelocity, "premium");
    }

    @Override
    public void executeForPlayer(Player player, UserProfileData userProfile, String[] arguments) {
        UUID uniqueId;
        UniqueIdMode uniqueIdMode = this.config.getEnum(UniqueIdMode.class, "uniqueIdsType");
        if (!uniqueIdMode.usesOfflineUuid()) {
            this.messageService.sendMessageToUser(userProfile, "premiumErrorFeatureDisabled");
            return;
        }
        if (userProfile.isBedrock()) {
            this.messageService.sendMessageToUser(userProfile, "premiumErrorUserBedrock");
            return;
        }
        if (userProfile.isPremium()) {
            this.messageService.sendMessageToUser(userProfile, "premiumErrorUserAlreadyPremium");
            return;
        }
        if (!userProfile.isRegistered()) {
            this.messageService.sendMessageToUser(userProfile, "premiumErrorUserNotRegistered");
            return;
        }
        if (!userProfile.isLogged()) {
            this.messageService.sendMessageToUser(userProfile, "premiumErrorUserNotLogged");
            return;
        }
        if (arguments.length != 1) {
            this.messageService.sendMessageToUser(userProfile, "premiumErrorUsage");
            return;
        }
        if (!PasswordHashService.verifyPassword(arguments[0], userProfile.getHashedPassword())) {
            this.messageService.sendMessageToUser(userProfile, "premiumErrorWrongPassword");
            return;
        }
        if (userProfile.hasVerificationToken()) {
            this.messageService.sendMessageToUser(userProfile, "premiumErrorSecondFactorActivated");
            return;
        }
        try {
            uniqueId = this.profileResolver.fetchProfile(player.getUsername()).map(Profile::getUniqueId).orElse(null);
        }
        catch (ResolverException resolverException) {
            this.messageService.sendMessageToUser(userProfile, "premiumErrorServersDown");
            return;
        }
        if (uniqueId == null) {
            this.messageService.sendMessageToUser(userProfile, "premiumErrorUserNotPremium");
            return;
        }
        UserProfileData existingUserProfile = this.userRepository.findByPremiumId(uniqueId).orElse(null);
        if (existingUserProfile != null) {
            this.messageService.sendMessageToUser(userProfile, "premiumErrorUserAlreadyExists");
            return;
        }
        this.runOrQueueConfirmation(userProfile, "premiumConfirmation", () -> {
            userProfile.setPremiumId(uniqueId);
            userProfile.setVerificationToken(null);
            userProfile.setSessionExpires(null);
            this.messageService.disconnectUserWithMessage(userProfile, "premiumSuccessPremiumTurnedOn");
            this.userRepository.update(userProfile);
            this.plugin.fireEventAsync(new UserEvent.Premium(userProfile, (CommandSource)player));
        });
    }
}

