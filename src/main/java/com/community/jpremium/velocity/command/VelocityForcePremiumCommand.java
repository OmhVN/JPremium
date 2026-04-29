package com.community.jpremium.velocity.command;

import com.community.jpremium.velocity.command.AbstractVelocityForceUserCommand;
import com.community.jpremium.security.UniqueIdMode;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.proxy.api.event.velocity.UserEvent;
import com.community.jpremium.proxy.api.resolver.Profile;
import com.community.jpremium.proxy.api.resolver.ResolverException;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.command.CommandSource;
import java.util.UUID;

public class VelocityForcePremiumCommand
extends AbstractVelocityForceUserCommand {
    public VelocityForcePremiumCommand(JPremiumVelocity jPremiumVelocity) {
        super(jPremiumVelocity, "forcePremium");
    }

    @Override
    public void executeForTarget(CommandSource commandSource, UserProfileData userProfile, String ... arguments) {
        UUID uniqueId;
        UniqueIdMode uniqueIdMode = this.config.getEnum(UniqueIdMode.class, "uniqueIdsType");
        if (!uniqueIdMode.usesOfflineUuid()) {
            this.messageService.sendMessage(commandSource, "forcePremiumErrorFeatureDisabled");
            return;
        }
        if (arguments.length != 1) {
            this.messageService.sendMessage(commandSource, "forcePremiumErrorUsage");
            return;
        }
        if (userProfile == null) {
            this.messageService.sendMessage(commandSource, "forcePremiumErrorUserNotExist");
            return;
        }
        if (userProfile.isBedrock()) {
            this.messageService.sendMessage(commandSource, "forcePremiumErrorUserBedrock");
            return;
        }
        if (userProfile.isPremium()) {
            this.messageService.sendMessage(commandSource, "forcePremiumErrorUserAlreadyPremium");
            return;
        }
        if (!userProfile.isRegistered()) {
            this.messageService.sendMessage(commandSource, "forcePremiumErrorUserNotRegistered");
            return;
        }
        if (userProfile.hasVerificationToken()) {
            this.messageService.sendMessageToUser(userProfile, "forcePremiumErrorSecondFactorActivated");
            return;
        }
        String text = userProfile.getLastNickname();
        try {
            uniqueId = this.profileResolver.fetchProfile(text).map(Profile::getUniqueId).orElse(null);
        }
        catch (ResolverException resolverException) {
            this.messageService.sendMessage(commandSource, "forcePremiumErrorServersDown");
            return;
        }
        if (uniqueId == null) {
            this.messageService.sendMessage(commandSource, "forcePremiumErrorUserNotPremium");
            return;
        }
        UserProfileData existingUserProfile = this.userRepository.findByPremiumId(uniqueId).orElse(null);
        if (existingUserProfile != null) {
            this.messageService.sendMessage(commandSource, "forcePremiumErrorUserAlreadyExists");
            return;
        }
        userProfile.setPremiumId(uniqueId);
        userProfile.setVerificationToken(null);
        userProfile.setSessionExpires(null);
        this.messageService.sendMessage(commandSource, "forcePremiumSuccessPremiumTurnedOn");
        this.messageService.disconnectUserWithMessage(userProfile, "premiumSuccessPremiumTurnedOn");
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.Premium(userProfile, commandSource));
    }
}

