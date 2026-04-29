package com.community.jpremium.velocity.command;

import com.community.jpremium.velocity.command.AbstractVelocityForceUserCommand;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.command.CommandSource;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class VelocityForceViewUserProfileCommand
extends AbstractVelocityForceUserCommand {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    public VelocityForceViewUserProfileCommand(JPremiumVelocity jPremiumVelocity) {
        super(jPremiumVelocity, "forceViewUserProfile");
    }

    @Override
    public void executeForTarget(CommandSource commandSource, UserProfileData userProfile, String ... arguments) {
        if (arguments.length != 1) {
            this.messageService.sendMessage(commandSource, "forceViewUserProfileErrorUsage");
            return;
        }
        if (userProfile == null) {
            this.messageService.sendMessage(commandSource, "forceViewUserProfileErrorUserNotExist");
            return;
        }
        this.messageService.sendMessageForUser(commandSource, userProfile, "forceViewUserProfileSuccessProfile", "%unique_id%", this.formatValueOrNoData(userProfile.getUniqueId()), "%premium_id%", this.formatValueOrNoData(userProfile.getPremiumId()), "%nickname%", this.formatValueOrNoData(userProfile.getLastNickname()), "%hashed_password%", this.formatValueOrNoData(userProfile.getHashedPassword()), "%email_address%", this.formatValueOrNoData(userProfile.getEmailAddress()), "%session_expires%", this.formatInstantOrNoData(userProfile.getSessionExpires()), "%last_server%", this.formatValueOrNoData(userProfile.getLastServer()), "%last_address%", this.formatValueOrNoData(userProfile.getLastAddress()), "%last_seen%", this.formatInstantOrNoData(userProfile.getLastSeen()), "%first_address%", this.formatValueOrNoData(userProfile.getFirstAddress()), "%first_seen%", this.formatInstantOrNoData(userProfile.getFirstSeen()));
    }

    private String formatInstantOrNoData(Instant instant) {
        return instant != null ? DATE_FORMATTER.format(instant) : LegacyComponentSerializer.legacyAmpersand().serialize(this.messageService.buildComponentMessage("forceViewUserProfileSuccessNoData", ""));
    }

    private String formatValueOrNoData(Object object) {
        return object != null ? object.toString() : LegacyComponentSerializer.legacyAmpersand().serialize(this.messageService.buildComponentMessage("forceViewUserProfileSuccessNoData", ""));
    }
}

