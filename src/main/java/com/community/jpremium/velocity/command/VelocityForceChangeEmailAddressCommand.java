package com.community.jpremium.velocity.command;

import com.community.jpremium.velocity.command.AbstractVelocityForceUserCommand;
import com.community.jpremium.common.util.ProfileDataUtils;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.proxy.api.event.velocity.UserEvent;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.command.CommandSource;

public class VelocityForceChangeEmailAddressCommand
extends AbstractVelocityForceUserCommand {
    public VelocityForceChangeEmailAddressCommand(JPremiumVelocity jPremiumVelocity) {
        super(jPremiumVelocity, "forceChangeEmailAddress");
    }

    @Override
    public void executeForTarget(CommandSource commandSource, UserProfileData userProfile, String ... arguments) {
        if (arguments.length != 2) {
            this.messageService.sendMessage(commandSource, "forceChangeEmailAddressErrorUsage");
            return;
        }
        if (userProfile == null) {
            this.messageService.sendMessage(commandSource, "forceChangeEmailAddressErrorUserNotExist");
            return;
        }
        if (!userProfile.isRegistered()) {
            this.messageService.sendMessage(commandSource, "forceChangeEmailAddressErrorUserNotRegistered");
            return;
        }
        if (!userProfile.hasHashedPassword()) {
            this.messageService.sendMessage(commandSource, "forceChangeEmailAddressErrorUserHasNotPassword");
            return;
        }
        String text = arguments[1].toLowerCase();
        if (!ProfileDataUtils.EMAIL_PATTERN.matcher(text).matches()) {
            this.messageService.sendMessage(commandSource, "forceChangeEmailAddressErrorWrongEmailAddress");
            return;
        }
        userProfile.setEmailAddress(text);
        this.messageService.sendMessage(commandSource, "forceChangeEmailAddressSuccessEmailAddressChanged");
        this.messageService.sendMessageToUser(userProfile, "changeEmailAddressSuccessEmailAddressChanged");
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.ChangeEmailAddress(userProfile, commandSource));
    }
}

