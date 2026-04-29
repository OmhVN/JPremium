package com.community.jpremium.bungee.command;

import com.community.jpremium.common.util.ProfileDataUtils;
import com.community.jpremium.security.PasswordHashService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.command.AbstractBungeePlayerCommand;
import com.community.jpremium.bungee.JPremium;
import com.community.jpremium.proxy.api.event.bungee.UserEvent;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeChangeEmailAddressCommand
extends AbstractBungeePlayerCommand {
    public BungeeChangeEmailAddressCommand(JPremium jPremium) {
        super(jPremium, "changeEmailAddress");
    }

    @Override
    public void executeForPlayer(ProxiedPlayer proxiedPlayer, UserProfileData userProfile, String[] arguments) {
        if (!userProfile.isRegistered()) {
            this.messageService.sendMessageToUser(userProfile, "changeEmailAddressErrorUserNotRegistered");
            return;
        }
        if (!userProfile.isLogged()) {
            this.messageService.sendMessageToUser(userProfile, "changeEmailAddressErrorUserNotLogged");
            return;
        }
        if (!userProfile.hasHashedPassword()) {
            this.messageService.sendMessageToUser(userProfile, "changeEmailAddressErrorUserHasNotPassword");
            return;
        }
        if (arguments.length != 2) {
            this.messageService.sendMessageToUser(userProfile, "changeEmailAddressErrorUsage");
            return;
        }
        if (!PasswordHashService.verifyPassword(arguments[0], userProfile.getHashedPassword())) {
            this.messageService.sendMessageToUser(userProfile, "changeEmailAddressErrorWrongPassword");
            return;
        }
        if (!ProfileDataUtils.EMAIL_PATTERN.matcher(arguments[1]).matches()) {
            this.messageService.sendMessageToUser(userProfile, "changeEmailAddressErrorWrongEmailAddress");
            return;
        }
        userProfile.setEmailAddress(arguments[1]);
        this.messageService.sendMessageToUser(userProfile, "changeEmailAddressSuccessEmailAddressChanged");
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.ChangeEmailAddress(userProfile, proxiedPlayer));
    }
}

