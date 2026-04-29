package com.community.jpremium.velocity.command;

import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.velocity.command.AbstractVelocityPlayerCommand;
import com.community.jpremium.proxy.api.event.velocity.UserEvent;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;

public class VelocityRequestSecondFactorCommand
extends AbstractVelocityPlayerCommand {
    public VelocityRequestSecondFactorCommand(JPremiumVelocity jPremiumVelocity) {
        super(jPremiumVelocity, "requestSecondFactor");
    }

    @Override
    public void executeForPlayer(Player player, UserProfileData userProfile, String[] arguments) {
        if (userProfile.isBedrock()) {
            this.messageService.sendMessageToUser(userProfile, "requestSecondFactorErrorUserBedrock");
            return;
        }
        if (userProfile.isPremium()) {
            this.messageService.sendMessageToUser(userProfile, "requestSecondFactorErrorUserPremium");
            return;
        }
        if (!userProfile.isRegistered()) {
            this.messageService.sendMessageToUser(userProfile, "requestSecondFactorErrorUserNotRegistered");
            return;
        }
        if (!userProfile.isLogged()) {
            this.messageService.sendMessageToUser(userProfile, "requestSecondFactorErrorUserNotLogged");
            return;
        }
        if (userProfile.hasVerificationToken()) {
            this.messageService.sendMessageToUser(userProfile, "requestSecondFactorErrorUserHasAlreadySecondFactor");
            return;
        }
        if (arguments.length != 0) {
            this.messageService.sendMessageToUser(userProfile, "requestSecondFactorErrorUsage");
            return;
        }
        GoogleAuthenticatorKey googleAuthenticatorKey = this.plugin.getGoogleAuthenticator().createCredentials();
        userProfile.setRecoveryCode(googleAuthenticatorKey.getKey());
        String serverName = this.config.getString("serverName");
        String otpAuthUri = String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", serverName, player.getUsername(), googleAuthenticatorKey.getKey(), serverName);
        try {
            String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=256x256&data=" + URLEncoder.encode(otpAuthUri, "UTF-8");
            Component component = this.messageService.buildComponentMessage("requestSecondFactorSuccessSecondFactorRequested", "").clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, qrUrl));
            player.sendMessage(component);
            this.plugin.fireEventAsync(new UserEvent.RequestSecondFactor(userProfile, player));
        }
        catch (UnsupportedEncodingException unsupportedEncodingException) {
            unsupportedEncodingException.printStackTrace();
        }
    }
}

