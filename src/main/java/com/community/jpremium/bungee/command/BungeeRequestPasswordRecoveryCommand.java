package com.community.jpremium.bungee.command;

import com.community.jpremium.common.util.ProfileDataUtils;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.command.AbstractBungeePlayerCommand;
import com.community.jpremium.bungee.JPremium;
import com.community.jpremium.proxy.api.event.bungee.UserEvent;
import java.util.Map;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeRequestPasswordRecoveryCommand
extends AbstractBungeePlayerCommand {
    public BungeeRequestPasswordRecoveryCommand(JPremium jPremium) {
        super(jPremium, "requestPasswordRecovery");
    }

    @Override
    public void executeForPlayer(ProxiedPlayer proxiedPlayer, UserProfileData userProfile, String[] arguments) {
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
        this.plugin.fireEventAsync(new UserEvent.RecoveryPassword(userProfile, proxiedPlayer));
        String subject = this.config.getString("passwordRecoverySubject");
        String template = this.plugin.getRecoveryTemplate();
        this.plugin.runAsync(() -> this.messageService.sendRecoveryEmail(userProfile, subject, template, Map.of("%code%", recoveryCode, "%nickname%", userProfile.getLastNickname(), "%address%", userProfile.getLastAddress())));
    }
}

