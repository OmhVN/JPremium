package com.community.jpremium.bungee.command;

import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.command.AbstractBungeePlayerCommand;
import com.community.jpremium.bungee.JPremium;
import com.community.jpremium.proxy.api.event.bungee.UserEvent;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeRequestSecondFactorCommand
extends AbstractBungeePlayerCommand {
    public BungeeRequestSecondFactorCommand(JPremium jPremium) {
        super(jPremium, "requestSecondFactor");
    }

    @Override
    public void executeForPlayer(ProxiedPlayer proxiedPlayer, UserProfileData userProfile, String[] arguments) {
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
        String otpAuthUri = String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", serverName, proxiedPlayer.getName(), googleAuthenticatorKey.getKey(), serverName);
        try {
            String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=256x256&data=" + URLEncoder.encode(otpAuthUri, "UTF-8");
            BaseComponent baseComponent = this.messageService.buildComponentMessage("requestSecondFactorSuccessSecondFactorRequested", "");
            baseComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, qrUrl));
            for (BaseComponent baseComponent2 : baseComponent.getExtra()) {
                baseComponent2.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, qrUrl));
            }
            proxiedPlayer.sendMessage(baseComponent);
            this.plugin.fireEventAsync(new UserEvent.RequestSecondFactor(userProfile, proxiedPlayer));
        }
        catch (UnsupportedEncodingException unsupportedEncodingException) {
            unsupportedEncodingException.printStackTrace();
        }
    }
}

