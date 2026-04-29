package com.community.jpremium.bungee.listener;

import com.community.jpremium.bungee.service.BungeeMessageService;
import com.community.jpremium.common.service.OnlineUserRegistry;
import com.community.jpremium.bungee.service.BungeeServerRoutingService;
import com.community.jpremium.common.config.BungeeConfigService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.JPremium;
import java.util.List;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeeServerKickListener
implements Listener {
    private final BungeeConfigService config;
    private final OnlineUserRegistry onlineUserRegistry;
    private final BungeeMessageService messageService;
    private final BungeeServerRoutingService routingService;

    public BungeeServerKickListener(JPremium plugin) {
        this.config = plugin.getConfig();
        this.onlineUserRegistry = plugin.getOnlineUserRegistry();
        this.messageService = plugin.getMessageService();
        this.routingService = plugin.getRoutingService();
    }

    @EventHandler
    public void onServerKick(ServerKickEvent serverKickEvent) {
        boolean disconnectRedirectionEnabled = this.config.getBoolean("disconnectRedirection");
        if (!disconnectRedirectionEnabled) {
            return;
        }
        ProxiedPlayer proxiedPlayer = serverKickEvent.getPlayer();
        UserProfileData userProfile = this.onlineUserRegistry.findByUniqueId(proxiedPlayer.getUniqueId()).orElse(null);
        if (userProfile == null) {
            return;
        }
        ServerKickEvent.State state = serverKickEvent.getState();
        BaseComponent[] baseComponentArray = serverKickEvent.getKickReasonComponent();
        ServerInfo serverInfo = serverKickEvent.getKickedFrom();
        String kickedServerName = serverInfo.getName();
        if (!proxiedPlayer.isConnected() || !state.equals(ServerKickEvent.State.CONNECTED)) {
            return;
        }
        List<String> limboServers = this.config.getStringList("limboServerNames");
        List<String> mainServers = this.config.getStringList("mainServerNames");
        limboServers.remove(kickedServerName);
        mainServers.remove(kickedServerName);
        ServerInfo fallbackServer = this.routingService.pickRandomServer(userProfile.isLogged() ? mainServers : limboServers);
        if (fallbackServer != null) {
            serverKickEvent.setCancelServer(fallbackServer);
            serverKickEvent.setCancelled(true);
            this.messageService.sendTitleBundleToUser(userProfile, "disconnectRedirection", "%reason%", TextComponent.toLegacyText(baseComponentArray));
        }
    }
}

