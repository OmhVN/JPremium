package com.community.jpremium.velocity.command;

import com.community.jpremium.velocity.command.AbstractVelocityForceUserCommand;
import com.community.jpremium.common.util.ProfileDataUtils;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.proxy.api.event.velocity.UserEvent;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.command.CommandSource;
import java.util.Map;

public class VelocityForceRequestPasswordRecoveryCommand
extends AbstractVelocityForceUserCommand {
    public VelocityForceRequestPasswordRecoveryCommand(JPremiumVelocity jPremiumVelocity) {
        super(jPremiumVelocity, "forceRequestPasswordRecovery");
    }

    @Override
    public void executeForTarget(CommandSource commandSource, UserProfileData userProfile, String ... arguments) {
        if (arguments.length != 1) {
            this.messageService.sendMessage(commandSource, "forceRequestPasswordRecoveryErrorUsage");
            return;
        }
        if (userProfile == null) {
            this.messageService.sendMessage(commandSource, "forceRequestPasswordRecoveryErrorUserNotExist");
            return;
        }
        if (!userProfile.isRegistered()) {
            this.messageService.sendMessage(commandSource, "forceRequestPasswordRecoveryErrorUserNotRegistered");
            return;
        }
        if (!userProfile.hasHashedPassword()) {
            this.messageService.sendMessage(commandSource, "forceRequestPasswordRecoveryErrorUserHasNotPassword");
            return;
        }
        if (!userProfile.hasEmailAddress()) {
            this.messageService.sendMessage(commandSource, "forceRequestPasswordRecoveryErrorUserHasNotEmailAddress");
            return;
        }
        String recoveryCode = ProfileDataUtils.generateSecondFactorToken();
        userProfile.setCachedAddress(recoveryCode);
        this.messageService.sendMessage(commandSource, "forceRequestPasswordRecoverySuccessRequestedPasswordRecovery");
        this.messageService.sendMessageToUser(userProfile, "requestPasswordRecoverySuccessRequestedPasswordRecovery");
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.RecoveryPassword(userProfile, commandSource));
        String subject = this.config.getString("passwordRecoverySubject");
        String template = this.plugin.getRecoveryTemplate();
        this.plugin.runAsync(() -> this.messageService.sendRecoveryEmail(userProfile, subject, template, Map.of("%code%", recoveryCode, "%nickname%", userProfile.getLastNickname(), "%address%", userProfile.getLastAddress())));
    }
}

