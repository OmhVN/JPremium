package com.community.jpremium.bungee.command;

import com.community.jpremium.bungee.command.AbstractBungeeForceUserCommand;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.JPremium;
import net.md_5.bungee.api.CommandSender;

public class BungeeForceMergePremiumUserProfileCommand
extends AbstractBungeeForceUserCommand {
    public BungeeForceMergePremiumUserProfileCommand(JPremium jPremium) {
        super(jPremium, "forceMergePremiumUserProfile");
    }

    @Override
    public void executeForTarget(CommandSender commandSender, UserProfileData userProfile, String ... arguments) {
        if (arguments.length != 2) {
            this.messageService.sendMessage(commandSender, "forceMergePremiumUserProfileErrorUsage");
            return;
        }
        if (userProfile == null) {
            this.messageService.sendMessage(commandSender, "forceMergePremiumUserProfileErrorUserNotExist");
            return;
        }
        if (!userProfile.isPremium()) {
            this.messageService.sendMessage(commandSender, "forceMergePremiumUserProfileErrorUserNotPremium");
            return;
        }
        UserProfileData existingUserProfile = this.userRepository.findByNickname(arguments[1]).orElse(null);
        if (existingUserProfile == null) {
            this.messageService.sendMessage(commandSender, "forceMergePremiumUserProfileErrorMergedWithUserNotExist");
            return;
        }
        if (existingUserProfile.isBedrock()) {
            this.messageService.sendMessage(commandSender, "forceMergePremiumUserProfileErrorMergedWithUserBedrock");
            return;
        }
        if (existingUserProfile.isPremium()) {
            this.messageService.sendMessage(commandSender, "forceMergePremiumUserProfileErrorMergedWithUserPremium");
            return;
        }
        existingUserProfile.setPremiumId(userProfile.getPremiumId());
        existingUserProfile.setLastNickname(userProfile.getLastNickname());
        this.messageService.disconnectUserWithMessage(userProfile, "forceMergePremiumUserProfileSuccessKickedMessage");
        this.messageService.disconnectUserWithMessage(existingUserProfile, "forceMergePremiumUserProfileSuccessKickedMessage");
        this.userRepository.delete(userProfile);
        this.userRepository.update(existingUserProfile);
        this.messageService.sendMessage(commandSender, "forceMergePremiumUserProfileSuccessMerged");
    }
}

