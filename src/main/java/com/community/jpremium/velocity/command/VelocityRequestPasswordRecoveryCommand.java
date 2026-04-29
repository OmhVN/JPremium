package com.community.jpremium.velocity.command;

import com.community.jpremium.common.util.ProfileDataUtils;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.velocity.command.AbstractVelocityPlayerCommand;
import com.community.jpremium.proxy.api.event.velocity.UserEvent;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import java.util.Map;

public class VelocityRequestPasswordRecoveryCommand
extends AbstractVelocityPlayerCommand {
    public VelocityRequestPasswordRecoveryCommand(JPremiumVelocity jPremiumVelocity) {
        super(jPremiumVelocity, "requestPasswordRecovery");
    }

    @Override
    public void executeForPlayer(Player player, UserProfileData userProfile, String[] arguments) {
        if (!userProfile.isRegistered()) {
            this.messageService.sendMessageToUser(userProfile, "requestPasswordRecoveryErrorUserNotRegistered");
            return;
        }
        if (!userProfile.hasHashedPassword()) {
            this.messageService.sendMessageToUser(userProfile, "requestPasswordRecoveryErrorUserHasNotPassword");
            return;
        }
        if (!userProfile.hasEmailAddress()) {
            this.messageService.sendMessageToUser(userProfile, "requestPasswordRecoveryErrorUserHasNotEmailAddress");
            return;
        }
        if (arguments.length != 1) {
            this.messageService.sendMessageToUser(userProfile, "requestPasswordRecoveryErrorUsage");
            return;
        }
        if (!userProfile.getEmailAddress().equalsIgnoreCase(arguments[0])) {
            this.messageService.sendMessageToUser(userProfile, "requestPasswordRecoveryErrorWrongEmailAddress");
            return;
        }
        if (userProfile.hasCachedAddress()) {
            this.messageService.sendMessageToUser(userProfile, "requestPasswordRecoveryErrorRecoveringDelay");
            return;
        }
        String recoveryCode = ProfileDataUtils.generateSecondFactorToken();
        userProfile.setCachedAddress(recoveryCode);
        this.messageService.sendMessageToUser(userProfile, "requestPasswordRecoverySuccessRequestedPasswordRecovery");
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.RecoveryPassword(userProfile, (CommandSource)player));
        String subject = this.config.getString("passwordRecoverySubject");
        String template = this.plugin.getRecoveryTemplate();
        this.plugin.runAsync(() -> this.messageService.sendRecoveryEmail(userProfile, subject, template, Map.of("%code%", recoveryCode, "%nickname%", userProfile.getLastNickname(), "%address%", userProfile.getLastAddress())));
    }
}

