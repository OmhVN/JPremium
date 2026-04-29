package com.community.jpremium.bungee.listener;

import com.community.jpremium.bungee.service.BungeeMessageService;
import com.community.jpremium.storage.UserProfileRepository;
import com.community.jpremium.common.service.OnlineUserRegistry;
import com.community.jpremium.common.config.BungeeConfigService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.JPremium;
import java.lang.reflect.Field;
import java.util.UUID;
import java.util.logging.Logger;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeeLoginListener
implements Listener {
    private final JPremium plugin;
    private final BungeeConfigService config;
    private final UserProfileRepository userRepository;
    private final BungeeMessageService messageService;
    private final boolean handshakeRewriteDisabled;
    private final Logger logger;
    private final OnlineUserRegistry onlineUserRegistry;
    private final Field uniqueIdField;
    private final Field rewriteIdField;

    public BungeeLoginListener(JPremium plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.userRepository = plugin.getUserRepository();
        this.messageService = plugin.getMessageService();
        this.handshakeRewriteDisabled = plugin.getProxy().getPluginManager().getPlugin("BungeeGuard") != null || this.config.getBoolean("accessTokenDisabled");
        this.logger = plugin.getLogger();
        this.onlineUserRegistry = plugin.getOnlineUserRegistry();
        this.uniqueIdField = this.resolveUniqueIdField(this.logger);
        this.rewriteIdField = this.resolveRewriteIdField(this.logger);
    }

    private Field resolveUniqueIdField(Logger logger) {
        try {
            return Class.forName("net.md_5.bungee.connection.InitialHandler").getDeclaredField("uniqueId");
        }
        catch (ClassNotFoundException | NoSuchFieldException noSuchFieldException) {
            logger.severe("Could not find uniqueId field in InitialHandler! JPremium will not work correctly!");
            return null;
        }
    }

    private Field resolveRewriteIdField(Logger logger) {
        try {
            return Class.forName("net.md_5.bungee.connection.InitialHandler").getDeclaredField("rewriteId");
        }
        catch (ClassNotFoundException | NoSuchFieldException noSuchFieldException) {
            logger.warning("Could not find rewriteId field in InitialHandler! You are using an old proxy or your fork does not have that field?");
            return null;
        }
    }

    @EventHandler(priority=-128)
    public void onLogin(LoginEvent loginEvent) {
        PendingConnection pendingConnection = loginEvent.getConnection();
        String address = pendingConnection.getAddress().getAddress().getHostAddress();
        String username = pendingConnection.getName();
        String secondLoginCacheKey = username + address;
        BaseComponent premiumVerificationMessage = this.messageService.buildComponentMessage("preLoginPremiumVerification", username);
        BaseComponent userNotLoadedMessage = this.messageService.buildComponentMessage("preLoginErrorUserNotLoaded", username);
        if (BungeePreLoginListener.getSecondLoginCache().containsKey(secondLoginCacheKey)) {
            BungeePreLoginListener.getSecondLoginCache().put(secondLoginCacheKey, Boolean.TRUE);
            loginEvent.setReason(premiumVerificationMessage);
            loginEvent.setCancelled(true);
            return;
        }
        UserProfileData userProfile = this.onlineUserRegistry.findByNickname(username).orElse(null);
        String accessToken = this.config.getString("accessToken");
        Object initialHandler = pendingConnection;
        if (userProfile != null) {
            UUID pendingConnectionUniqueId = pendingConnection.getUniqueId();
            UUID userPremiumId = userProfile.getPremiumId();
            if (userProfile.isPremium() && !userPremiumId.equals(pendingConnectionUniqueId)) {
                userProfile.setLastNickname(null);
                this.plugin.runAsync(() -> this.userRepository.update(userProfile));
                loginEvent.setReason(TextComponent.fromLegacy("[Authorization] Please re-join to the server", ChatColor.GREEN));
                loginEvent.setCancelled(true);
                return;
            }
            UUID userUniqueId = userProfile.getUniqueId();
            try {
                if (this.uniqueIdField != null) {
                    this.uniqueIdField.setAccessible(true);
                    this.uniqueIdField.set(initialHandler, userUniqueId);
                }
                if (this.rewriteIdField != null) {
                    this.rewriteIdField.setAccessible(true);
                    this.rewriteIdField.set(initialHandler, userUniqueId);
                }
                if (!this.handshakeRewriteDisabled) {
                    Object handshake = initialHandler.getClass().getMethod("getHandshake").invoke(initialHandler);
                    handshake.getClass().getMethod("setHost", String.class).invoke(handshake, accessToken);
                    handshake.getClass().getMethod("setPort", Integer.TYPE).invoke(handshake, 0);
                }
            }
            catch (ReflectiveOperationException reflectiveOperationException) {
                this.logger.warning("Could not rewrite login handler metadata: " + reflectiveOperationException.getMessage());
            }
        } else {
            loginEvent.setReason(userNotLoadedMessage);
            loginEvent.setCancelled(true);
        }
    }
}
