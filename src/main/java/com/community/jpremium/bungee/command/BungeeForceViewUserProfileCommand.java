package com.community.jpremium.bungee.command;

import com.community.jpremium.bungee.command.AbstractBungeeForceUserCommand;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.JPremium;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class BungeeForceViewUserProfileCommand
extends AbstractBungeeForceUserCommand {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    public BungeeForceViewUserProfileCommand(JPremium jPremium) {
        super(jPremium, "forceViewUserProfile");
    }

    @Override
    public void executeForTarget(CommandSender commandSender, UserProfileData userProfile, String ... arguments) {
        if (arguments.length != 1) {
            this.messageService.sendMessage(commandSender, "forceViewUserProfileErrorUsage");
            return;
        }
        if (userProfile == null) {
            this.messageService.sendMessage(commandSender, "forceViewUserProfileErrorUserNotExist");
            return;
        }
        this.messageService.sendMessageForUser(commandSender, userProfile, "forceViewUserProfileSuccessProfile", "%unique_id%", this.formatValueOrNoData(userProfile.getUniqueId()), "%premium_id%", this.formatValueOrNoData(userProfile.getPremiumId()), "%nickname%", this.formatValueOrNoData(userProfile.getLastNickname()), "%hashed_password%", this.formatValueOrNoData(userProfile.getHashedPassword()), "%email_address%", this.formatValueOrNoData(userProfile.getEmailAddress()), "%session_expires%", this.formatInstantOrNoData(userProfile.getSessionExpires()), "%last_server%", this.formatValueOrNoData(userProfile.getLastServer()), "%last_address%", this.formatValueOrNoData(userProfile.getLastAddress()), "%last_seen%", this.formatInstantOrNoData(userProfile.getLastSeen()), "%first_address%", this.formatValueOrNoData(userProfile.getFirstAddress()), "%first_seen%", this.formatInstantOrNoData(userProfile.getFirstSeen()));
    }

    private String formatInstantOrNoData(Instant instant) {
        return instant != null ? DATE_FORMATTER.format(instant) : TextComponent.toLegacyText((BaseComponent[])new BaseComponent[]{this.messageService.buildComponentMessage("forceViewUserProfileSuccessNoData", "")});
    }

    private String formatValueOrNoData(Object object) {
        return object != null ? object.toString() : TextComponent.toLegacyText((BaseComponent[])new BaseComponent[]{this.messageService.buildComponentMessage("forceViewUserProfileSuccessNoData", "")});
    }
}

