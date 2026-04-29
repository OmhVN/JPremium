package com.community.jpremium.bungee.command;

import com.community.jpremium.bungee.command.AbstractBungeeForceUserCommand;
import com.community.jpremium.common.util.ProfileDataUtils;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.JPremium;
import com.community.jpremium.proxy.api.event.bungee.UserEvent;
import net.md_5.bungee.api.CommandSender;

public class BungeeForceChangeEmailAddressCommand
extends AbstractBungeeForceUserCommand {
    public BungeeForceChangeEmailAddressCommand(JPremium jPremium) {
        super(jPremium, "forceChangeEmailAddress");
    }

    @Override
    public void executeForTarget(CommandSender commandSender, UserProfileData userProfile, String ... arguments) {
        if (arguments.length != 2) {
            this.messageService.sendMessage(commandSender, "forceChangeEmailAddressErrorUsage");
            return;
        }
        if (userProfile == null) {
            this.messageService.sendMessage(commandSender, "forceChangeEmailAddressErrorUserNotExist");
            return;
        }
        if (!userProfile.isRegistered()) {
            this.messageService.sendMessage(commandSender, "forceChangeEmailAddressErrorUserNotRegistered");
            return;
        }
        if (!userProfile.hasHashedPassword()) {
            this.messageService.sendMessage(commandSender, "forceChangeEmailAddressErrorUserHasNotPassword");
            return;
        }
        String text = arguments[1].toLowerCase();
        if (!ProfileDataUtils.EMAIL_PATTERN.matcher(text).matches()) {
            this.messageService.sendMessage(commandSender, "forceChangeEmailAddressErrorWrongEmailAddress");
            return;
        }
        userProfile.setEmailAddress(text);
        this.messageService.sendMessage(commandSender, "forceChangeEmailAddressSuccessEmailAddressChanged");
        this.messageService.sendMessageToUser(userProfile, "changeEmailAddressSuccessEmailAddressChanged");
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.ChangeEmailAddress(userProfile, commandSender));
    }
}

