package com.community.jpremium.bungee.listener;

import com.community.jpremium.common.service.OnlineUserRegistry;
import com.community.jpremium.bungee.service.BungeeServerRoutingService;
import com.community.jpremium.common.config.BungeeConfigService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.JPremium;
import java.util.List;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeeServerConnectListener
implements Listener {
    private final BungeeConfigService config;
    private final BungeeServerRoutingService routingService;
    private final OnlineUserRegistry onlineUserRegistry;

    public BungeeServerConnectListener(JPremium jPremium) {
        this.config = jPremium.getConfig();
        this.routingService = jPremium.getRoutingService();
        this.onlineUserRegistry = jPremium.getOnlineUserRegistry();
    }

    @EventHandler(priority=64)
    public void onServerConnect(ServerConnectEvent serverConnectEvent) {
        ProxiedPlayer proxiedPlayer = serverConnectEvent.getPlayer();
        UserProfileData userProfile = this.onlineUserRegistry.findByUniqueId(proxiedPlayer.getUniqueId()).orElse(null);
        if (userProfile == null) {
            return;
        }
        ServerConnectEvent.Reason reason = serverConnectEvent.getReason();
        ServerInfo targetServer = serverConnectEvent.getTarget();
        String targetServerName = targetServer.getName();
        List<String> limboServers = this.config.getStringList("limboServerNames");
        if (reason.equals(ServerConnectEvent.Reason.JOIN_PROXY) || reason.equals(ServerConnectEvent.Reason.LOBBY_FALLBACK) || reason.equals(ServerConnectEvent.Reason.UNKNOWN)) {
            ServerInfo initialServer = this.routingService.selectInitialServer(userProfile, targetServer);
            if (initialServer != null) {
                serverConnectEvent.setTarget(initialServer);
            }
            return;
        }
        if (!userProfile.isLogged() && !limboServers.isEmpty() && !limboServers.contains(targetServerName)) {
            serverConnectEvent.setCancelled(true);
        }
    }
}

