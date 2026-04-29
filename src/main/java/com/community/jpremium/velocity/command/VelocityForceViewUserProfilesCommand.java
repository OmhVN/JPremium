package com.community.jpremium.velocity.command;

import com.community.jpremium.velocity.command.AbstractVelocityCommand;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.command.CommandSource;
import java.util.Collection;

public class VelocityForceViewUserProfilesCommand
extends AbstractVelocityCommand {
    public VelocityForceViewUserProfilesCommand(JPremiumVelocity jPremiumVelocity) {
        super(jPremiumVelocity, "forceViewUserProfiles", "jpremium.command.forceViewUserProfiles");
    }

    @Override
    public void executeCommand(CommandSource commandSource, String[] arguments) {
        if (arguments.length < 1) {
            this.messageService.sendMessage(commandSource, "forceViewUserProfilesErrorUsage");
            return;
        }
        String text = arguments[0];
        this.messageService.sendMessage(commandSource, "forceViewUserProfilesLoadingProfiles");
        Collection<UserProfileData> collection = this.userRepository.findByAddress(text);
        if (collection.isEmpty()) {
            this.messageService.sendMessage(commandSource, "forceViewUserProfilesErrorUsersNotExist");
            return;
        }
        this.messageService.sendMessage(commandSource, "forceViewUserProfilesSuccessHeader");
        int n = 1;
        for (UserProfileData userProfile : collection) {
            this.messageService.sendMessageWithNickname(commandSource, userProfile.getLastNickname(), "forceViewUserProfilesSuccessProfile", "%ordinal%", String.valueOf(n++), "%unique_id%", userProfile.getUniqueId().toString(), "%premium_id%", userProfile.getPremiumId() != null ? userProfile.getPremiumId().toString() : "-", "%email_address%", userProfile.getEmailAddress() != null ? userProfile.getEmailAddress() : "-", "%session_expires%", userProfile.getSessionExpires() != null ? userProfile.getSessionExpires().toString() : "-", "%last_server%", userProfile.getLastServer() != null ? userProfile.getLastServer() : "-", "%last_address%", userProfile.getLastAddress() != null ? userProfile.getLastAddress() : "-", "%last_seen%", userProfile.getLastSeen() != null ? userProfile.getLastSeen().toString() : "-", "%first_address%", userProfile.getFirstAddress() != null ? userProfile.getFirstAddress() : "-", "%first_seen%", userProfile.getFirstSeen() != null ? userProfile.getFirstSeen().toString() : "-");
        }
    }
}

