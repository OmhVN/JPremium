package com.community.jpremium.velocity.listener;

import com.community.jpremium.common.service.OnlineUserRegistry;
import com.community.jpremium.velocity.service.VelocityServerRoutingService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

public class VelocityInitialServerListener {
    private final OnlineUserRegistry onlineUserRegistry;
    private final VelocityServerRoutingService routingService;

    public VelocityInitialServerListener(JPremiumVelocity jPremiumVelocity) {
        this.onlineUserRegistry = jPremiumVelocity.getOnlineUserRegistry();
        this.routingService = jPremiumVelocity.getRoutingService();
    }

    @Subscribe
    public void onPlayerChooseInitialServer(PlayerChooseInitialServerEvent playerChooseInitialServerEvent) {
        Player player = playerChooseInitialServerEvent.getPlayer();
        UserProfileData userProfile = this.onlineUserRegistry.findByUniqueId(player.getUniqueId()).orElse(null);
        if (userProfile == null) {
            return;
        }
        RegisteredServer registeredServer = this.routingService.selectInitialServer(userProfile);
        if (registeredServer != null) {
            playerChooseInitialServerEvent.setInitialServer(registeredServer);
        }
    }
}
