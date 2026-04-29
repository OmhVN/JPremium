package com.community.jpremium.bungee.command;

import com.community.jpremium.bungee.command.AbstractBungeeCommand;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.JPremium;
import java.util.Collection;
import net.md_5.bungee.api.CommandSender;

public class BungeeForceViewUserProfilesCommand
extends AbstractBungeeCommand {
    public BungeeForceViewUserProfilesCommand(JPremium jPremium) {
        super(jPremium, "forceViewUserProfiles", "jpremium.command.forceViewUserProfiles");
    }

    @Override
    public void executeCommand(CommandSender commandSender, String[] arguments) {
        if (arguments.length < 1) {
            this.messageService.sendMessage(commandSender, "forceViewUserProfilesErrorUsage");
            return;
        }
        String text = arguments[0];
        this.messageService.sendMessage(commandSender, "forceViewUserProfilesLoadingProfiles");
        Collection<UserProfileData> collection = this.userRepository.findByAddress(text);
        if (collection.isEmpty()) {
            this.messageService.sendMessage(commandSender, "forceViewUserProfilesErrorUsersNotExist");
            return;
        }
        this.messageService.sendMessage(commandSender, "forceViewUserProfilesSuccessHeader");
        int n = 1;
        for (UserProfileData userProfile : collection) {
            this.messageService.sendMessageWithNickname(commandSender, userProfile.getLastNickname(), "forceViewUserProfilesSuccessProfile", "%ordinal%", String.valueOf(n++), "%unique_id%", userProfile.getUniqueId().toString(), "%premium_id%", userProfile.getPremiumId() != null ? userProfile.getPremiumId().toString() : "-", "%email_address%", userProfile.getEmailAddress() != null ? userProfile.getEmailAddress() : "-", "%session_expires%", userProfile.getSessionExpires() != null ? userProfile.getSessionExpires().toString() : "-", "%last_server%", userProfile.getLastServer() != null ? userProfile.getLastServer() : "-", "%last_address%", userProfile.getLastAddress() != null ? userProfile.getLastAddress() : "-", "%last_seen%", userProfile.getLastSeen() != null ? userProfile.getLastSeen().toString() : "-", "%first_address%", userProfile.getFirstAddress() != null ? userProfile.getFirstAddress() : "-", "%first_seen%", userProfile.getFirstSeen() != null ? userProfile.getFirstSeen().toString() : "-");
        }
    }
}

