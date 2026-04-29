package com.community.jpremium.bungee.command;

import com.community.jpremium.bungee.command.AbstractBungeeForceUserCommand;
import com.community.jpremium.security.UniqueIdMode;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.JPremium;
import com.community.jpremium.proxy.api.event.bungee.UserEvent;
import com.community.jpremium.proxy.api.resolver.Profile;
import com.community.jpremium.proxy.api.resolver.ResolverException;
import java.util.UUID;
import net.md_5.bungee.api.CommandSender;

public class BungeeForcePremiumCommand
extends AbstractBungeeForceUserCommand {
    public BungeeForcePremiumCommand(JPremium jPremium) {
        super(jPremium, "forcePremium");
    }

    @Override
    public void executeForTarget(CommandSender commandSender, UserProfileData userProfile, String ... arguments) {
        UUID uniqueId;
        UniqueIdMode uniqueIdMode = this.config.getEnum(UniqueIdMode.class, "uniqueIdsType");
        if (!uniqueIdMode.usesOfflineUuid()) {
            this.messageService.sendMessage(commandSender, "forcePremiumErrorFeatureDisabled");
            return;
        }
        if (arguments.length != 1) {
            this.messageService.sendMessage(commandSender, "forcePremiumErrorUsage");
            return;
        }
        if (userProfile == null) {
            this.messageService.sendMessage(commandSender, "forcePremiumErrorUserNotExist");
            return;
        }
        if (userProfile.isBedrock()) {
            this.messageService.sendMessage(commandSender, "forcePremiumErrorUserBedrock");
            return;
        }
        if (userProfile.isPremium()) {
            this.messageService.sendMessage(commandSender, "forcePremiumErrorUserAlreadyPremium");
            return;
        }
        if (!userProfile.isRegistered()) {
            this.messageService.sendMessage(commandSender, "forcePremiumErrorUserNotRegistered");
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
            this.messageService.sendMessage(commandSender, "forcePremiumErrorServersDown");
            return;
        }
        if (uniqueId == null) {
            this.messageService.sendMessage(commandSender, "forcePremiumErrorUserNotPremium");
            return;
        }
        UserProfileData existingUserProfile = this.userRepository.findByPremiumId(uniqueId).orElse(null);
        if (existingUserProfile != null) {
            this.messageService.sendMessage(commandSender, "forcePremiumErrorUserAlreadyExists");
            return;
        }
        userProfile.setPremiumId(uniqueId);
        userProfile.setVerificationToken(null);
        userProfile.setSessionExpires(null);
        this.messageService.sendMessage(commandSender, "forcePremiumSuccessPremiumTurnedOn");
        this.messageService.disconnectUserWithMessage(userProfile, "premiumSuccessPremiumTurnedOn");
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.Premium(userProfile, commandSender));
    }
}

