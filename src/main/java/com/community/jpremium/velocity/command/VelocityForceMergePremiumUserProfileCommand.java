package com.community.jpremium.velocity.command;

import com.community.jpremium.velocity.command.AbstractVelocityForceUserCommand;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.command.CommandSource;

public class VelocityForceMergePremiumUserProfileCommand
extends AbstractVelocityForceUserCommand {
    public VelocityForceMergePremiumUserProfileCommand(JPremiumVelocity jPremiumVelocity) {
        super(jPremiumVelocity, "forceMergePremiumUserProfile");
    }

    @Override
    public void executeForTarget(CommandSource commandSource, UserProfileData userProfile, String ... arguments) {
        if (arguments.length != 2) {
            this.messageService.sendMessage(commandSource, "forceMergePremiumUserProfileErrorUsage");
            return;
        }
        if (userProfile == null) {
            this.messageService.sendMessage(commandSource, "forceMergePremiumUserProfileErrorUserNotExist");
            return;
        }
        if (!userProfile.isPremium()) {
            this.messageService.sendMessage(commandSource, "forceMergePremiumUserProfileErrorUserNotPremium");
            return;
        }
        UserProfileData existingUserProfile = this.userRepository.findByNickname(arguments[1]).orElse(null);
        if (existingUserProfile == null) {
            this.messageService.sendMessage(commandSource, "forceMergePremiumUserProfileErrorMergedWithUserNotExist");
            return;
        }
        if (existingUserProfile.isBedrock()) {
            this.messageService.sendMessage(commandSource, "forceMergePremiumUserProfileErrorMergedWithUserBedrock");
            return;
        }
        if (existingUserProfile.isPremium()) {
            this.messageService.sendMessage(commandSource, "forceMergePremiumUserProfileErrorMergedWithUserPremium");
            return;
        }
        existingUserProfile.setPremiumId(userProfile.getPremiumId());
        existingUserProfile.setLastNickname(userProfile.getLastNickname());
        this.messageService.disconnectUserWithMessage(userProfile, "forceMergePremiumUserProfileSuccessKickedMessage");
        this.messageService.disconnectUserWithMessage(existingUserProfile, "forceMergePremiumUserProfileSuccessKickedMessage");
        this.userRepository.delete(userProfile);
        this.userRepository.update(existingUserProfile);
        this.messageService.sendMessage(commandSource, "forceMergePremiumUserProfileSuccessMerged");
    }
}

