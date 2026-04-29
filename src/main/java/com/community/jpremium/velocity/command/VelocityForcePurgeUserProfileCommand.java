package com.community.jpremium.velocity.command;

import com.community.jpremium.velocity.command.AbstractVelocityForceUserCommand;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.command.CommandSource;

public class VelocityForcePurgeUserProfileCommand
extends AbstractVelocityForceUserCommand {
    public VelocityForcePurgeUserProfileCommand(JPremiumVelocity jPremiumVelocity) {
        super(jPremiumVelocity, "forcePurgeUserProfile");
    }

    @Override
    public void executeForTarget(CommandSource commandSource, UserProfileData userProfile, String ... arguments) {
        if (arguments.length != 1) {
            this.messageService.sendMessage(commandSource, "forcePurgeUserProfileErrorUsage");
            return;
        }
        if (userProfile == null) {
            this.messageService.sendMessage(commandSource, "forcePurgeUserProfileErrorUserNotExist");
            return;
        }
        this.messageService.disconnectUserWithMessage(userProfile, "forcePurgeUserProfileSuccessKickedMessage");
        this.messageService.sendMessage(commandSource, "forcePurgeUserProfileSuccessUserProfilePurged");
        this.userRepository.delete(userProfile);
    }
}

