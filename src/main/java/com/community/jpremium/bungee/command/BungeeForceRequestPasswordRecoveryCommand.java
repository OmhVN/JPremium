package com.community.jpremium.bungee.command;

import com.community.jpremium.bungee.command.AbstractBungeeForceUserCommand;
import com.community.jpremium.common.util.ProfileDataUtils;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.JPremium;
import com.community.jpremium.proxy.api.event.bungee.UserEvent;
import java.util.Map;
import net.md_5.bungee.api.CommandSender;

public class BungeeForceRequestPasswordRecoveryCommand
extends AbstractBungeeForceUserCommand {
    public BungeeForceRequestPasswordRecoveryCommand(JPremium jPremium) {
        super(jPremium, "forceRequestPasswordRecovery");
    }

    @Override
    public void executeForTarget(CommandSender commandSender, UserProfileData userProfile, String ... arguments) {
        if (arguments.length != 1) {
            this.messageService.sendMessage(commandSender, "forceRequestPasswordRecoveryErrorUsage");
            return;
        }
        if (userProfile == null) {
            this.messageService.sendMessage(commandSender, "forceRequestPasswordRecoveryErrorUserNotExist");
            return;
        }
        if (!userProfile.isRegistered()) {
            this.messageService.sendMessage(commandSender, "forceRequestPasswordRecoveryErrorUserNotRegistered");
            return;
        }
        if (!userProfile.hasHashedPassword()) {
            this.messageService.sendMessage(commandSender, "forceRequestPasswordRecoveryErrorUserHasNotPassword");
            return;
        }
        if (!userProfile.hasEmailAddress()) {
            this.messageService.sendMessage(commandSender, "forceRequestPasswordRecoveryErrorUserHasNotEmailAddress");
            return;
        }
        String text = ProfileDataUtils.generateSecondFactorToken();
        userProfile.setCachedAddress(text);
        this.messageService.sendMessage(commandSender, "forceRequestPasswordRecoverySuccessRequestedPasswordRecovery");
        this.messageService.sendMessageToUser(userProfile, "requestPasswordRecoverySuccessRequestedPasswordRecovery");
        this.userRepository.update(userProfile);
        this.plugin.fireEventAsync(new UserEvent.RecoveryPassword(userProfile, commandSender));
        this.plugin.runAsync(() -> this.messageService.sendRecoveryEmail(userProfile, this.config.getString("passwordRecoverySubject"), this.plugin.getRecoveryTemplate(), Map.of("%code%", text, "%nickname%", userProfile.getLastNickname(), "%address%", userProfile.getLastAddress())));
    }
}

