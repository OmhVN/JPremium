package com.community.jpremium.bungee.command;

import com.community.jpremium.bungee.command.AbstractBungeeForceUserCommand;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.JPremium;
import net.md_5.bungee.api.CommandSender;

public class BungeeForcePurgeUserProfileCommand
extends AbstractBungeeForceUserCommand {
    public BungeeForcePurgeUserProfileCommand(JPremium jPremium) {
        super(jPremium, "forcePurgeUserProfile");
    }

    @Override
    public void executeForTarget(CommandSender commandSender, UserProfileData userProfile, String ... arguments) {
        if (arguments.length != 1) {
            this.messageService.sendMessage(commandSender, "forcePurgeUserProfileErrorUsage");
            return;
        }
        if (userProfile == null) {
            this.messageService.sendMessage(commandSender, "forcePurgeUserProfileErrorUserNotExist");
            return;
        }
        this.messageService.disconnectUserWithMessage(userProfile, "forcePurgeUserProfileSuccessKickedMessage");
        this.messageService.sendMessage(commandSender, "forcePurgeUserProfileSuccessUserProfilePurged");
        this.userRepository.delete(userProfile);
    }
}

