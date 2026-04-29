package com.community.jpremium.velocity.listener;

import com.community.jpremium.common.service.OnlineUserRegistry;
import com.community.jpremium.common.config.VelocityConfigService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import java.util.List;

public class VelocityServerPreConnectListener {
    private final VelocityConfigService config;
    private final OnlineUserRegistry onlineUserRegistry;

    public VelocityServerPreConnectListener(JPremiumVelocity jPremiumVelocity) {
        this.config = jPremiumVelocity.getConfig();
        this.onlineUserRegistry = jPremiumVelocity.getOnlineUserRegistry();
    }

    @Subscribe
    public void onServerPreConnect(ServerPreConnectEvent serverPreConnectEvent) {
        Player player = serverPreConnectEvent.getPlayer();
        UserProfileData userProfile = this.onlineUserRegistry.findByUniqueId(player.getUniqueId()).orElse(null);
        if (userProfile == null) {
            return;
        }
        if (userProfile.isLogged()) {
            return;
        }
        RegisteredServer requestedServer = serverPreConnectEvent.getResult().getServer().orElse(null);
        if (requestedServer == null) {
            return;
        }
        List<String> limboServerNames = this.config.getStringList("limboServerNames");
        if (limboServerNames.isEmpty()) {
            return;
        }
        if (!limboServerNames.contains(requestedServer.getServerInfo().getName())) {
            serverPreConnectEvent.setResult(ServerPreConnectEvent.ServerResult.denied());
        }
    }
}
