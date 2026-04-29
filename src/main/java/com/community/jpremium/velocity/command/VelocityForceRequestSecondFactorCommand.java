package com.community.jpremium.velocity.command;

import com.community.jpremium.velocity.command.AbstractVelocityForceUserCommand;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.proxy.api.event.velocity.UserEvent;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

public class VelocityForceRequestSecondFactorCommand
extends AbstractVelocityForceUserCommand {
    public VelocityForceRequestSecondFactorCommand(JPremiumVelocity jPremiumVelocity) {
        super(jPremiumVelocity, "forceRequestSecondFactor");
    }

    @Override
    public void executeForTarget(CommandSource commandSource, UserProfileData userProfile, String ... arguments) {
        if (arguments.length != 1) {
            this.messageService.sendMessage(commandSource, "forceRequestSecondFactorErrorUsage");
            return;
        }
        if (userProfile == null) {
            this.messageService.sendMessage(commandSource, "forceRequestSecondFactorErrorUserNotExist");
            return;
        }
        if (userProfile.isBedrock()) {
            this.messageService.sendMessage(commandSource, "forceRequestSecondFactorErrorUserBedrock");
            return;
        }
        if (userProfile.isPremium()) {
            this.messageService.sendMessage(commandSource, "forceRequestSecondFactorErrorUserPremium");
            return;
        }
        if (!userProfile.isRegistered()) {
            this.messageService.sendMessage(commandSource, "forceRequestSecondFactorErrorUserNotRegistered");
            return;
        }
        if (userProfile.hasVerificationToken()) {
            this.messageService.sendMessage(commandSource, "forceRequestSecondFactorErrorUserHasAlreadySecondFactor");
            return;
        }
        GoogleAuthenticatorKey googleAuthenticatorKey = this.plugin.getGoogleAuthenticator().createCredentials();
        userProfile.setRecoveryCode(googleAuthenticatorKey.getKey());
        String serverName = this.config.getString("serverName");
        String otpAuthUri = String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", serverName, userProfile.getLastNickname(), googleAuthenticatorKey.getKey(), serverName);
        try {
            String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=256x256&data=" + URLEncoder.encode(otpAuthUri, "UTF-8");
            Component component = this.messageService.buildComponentMessage("requestSecondFactorSuccessSecondFactorRequested", "").clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, qrUrl));
            Player player = this.plugin.findPlayer(userProfile);
            if (player != null) {
                player.sendMessage(component);
            }
            this.messageService.sendMessage(commandSource, "forceRequestSecondFactorSuccessSecondFactorRequested");
            this.plugin.fireEventAsync(new UserEvent.RequestSecondFactor(userProfile, commandSource));
        }
        catch (UnsupportedEncodingException unsupportedEncodingException) {
            unsupportedEncodingException.printStackTrace();
        }
    }
}

