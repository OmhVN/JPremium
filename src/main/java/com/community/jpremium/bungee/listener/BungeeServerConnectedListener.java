package com.community.jpremium.bungee.listener;

import com.community.jpremium.bungee.service.BungeeServerRoutingService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.JPremium;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeeServerConnectedListener
implements Listener {
    private final JPremium plugin;
    private final BungeeServerRoutingService routingService;

    public BungeeServerConnectedListener(JPremium jPremium) {
        this.plugin = jPremium;
        this.routingService = jPremium.getRoutingService();
    }

    @EventHandler
    public void onServerConnected(ServerConnectedEvent serverConnectedEvent) {
        ProxiedPlayer proxiedPlayer = serverConnectedEvent.getPlayer();
        Server server = serverConnectedEvent.getServer();
        UserProfileData userProfile = this.plugin.getOnlineUserRegistry().findByUniqueId(proxiedPlayer.getUniqueId()).orElse(null);
        if (userProfile == null) {
            return;
        }
        this.routingService.sendStatePayloadToServer(userProfile, server);
    }
}
