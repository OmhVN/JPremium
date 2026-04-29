package com.community.jpremium.bungee.command;

import com.community.jpremium.bungee.command.AbstractBungeeForceUserCommand;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.JPremium;
import com.community.jpremium.proxy.api.event.bungee.UserEvent;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeForceRequestSecondFactorCommand
extends AbstractBungeeForceUserCommand {
    public BungeeForceRequestSecondFactorCommand(JPremium jPremium) {
        super(jPremium, "forceRequestSecondFactor");
    }

    @Override
    public void executeForTarget(CommandSender commandSender, UserProfileData userProfile, String ... arguments) {
        if (arguments.length != 1) {
            this.messageService.sendMessage(commandSender, "forceRequestSecondFactorErrorUsage");
            return;
        }
        if (userProfile == null) {
            this.messageService.sendMessage(commandSender, "forceRequestSecondFactorErrorUserNotExist");
            return;
        }
        if (userProfile.isBedrock()) {
            this.messageService.sendMessage(commandSender, "forceRequestSecondFactorErrorUserBedrock");
            return;
        }
        if (userProfile.isPremium()) {
            this.messageService.sendMessage(commandSender, "forceRequestSecondFactorErrorUserPremium");
            return;
        }
        if (!userProfile.isRegistered()) {
            this.messageService.sendMessage(commandSender, "forceRequestSecondFactorErrorUserNotRegistered");
            return;
        }
        if (userProfile.hasVerificationToken()) {
            this.messageService.sendMessage(commandSender, "forceRequestSecondFactorErrorUserHasAlreadySecondFactor");
            return;
        }
        GoogleAuthenticatorKey googleAuthenticatorKey = this.plugin.getGoogleAuthenticator().createCredentials();
        userProfile.setRecoveryCode(googleAuthenticatorKey.getKey());
        String serverName = this.config.getString("serverName");
        String otpAuthUri = String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", serverName, userProfile.getLastNickname(), googleAuthenticatorKey.getKey(), serverName);
        try {
            String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=256x256&data=" + URLEncoder.encode(otpAuthUri, "UTF-8");
            BaseComponent baseComponent = this.messageService.buildComponentMessage("requestSecondFactorSuccessSecondFactorRequested", "");
            baseComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, qrUrl));
            for (BaseComponent baseComponent2 : baseComponent.getExtra()) {
                baseComponent2.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, qrUrl));
            }
            ProxiedPlayer proxiedPlayer = this.plugin.findPlayer(userProfile);
            if (proxiedPlayer != null) {
                proxiedPlayer.sendMessage(baseComponent);
            }
            this.messageService.sendMessage(commandSender, "forceRequestSecondFactorSuccessSecondFactorRequested");
            this.plugin.fireEventAsync(new UserEvent.RequestSecondFactor(userProfile, commandSender));
        }
        catch (UnsupportedEncodingException unsupportedEncodingException) {
            unsupportedEncodingException.printStackTrace();
        }
    }
}

