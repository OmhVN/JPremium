package com.community.jpremium.velocity.listener;

import com.community.jpremium.velocity.service.VelocityMessageService;
import com.community.jpremium.common.service.OnlineUserRegistry;
import com.community.jpremium.velocity.service.VelocityServerRoutingService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;

public class VelocityServerPostConnectListener {
    private final OnlineUserRegistry onlineUserRegistry;
    private final VelocityServerRoutingService routingService;
    private final VelocityMessageService messageService;

    public VelocityServerPostConnectListener(JPremiumVelocity jPremiumVelocity) {
        this.onlineUserRegistry = jPremiumVelocity.getOnlineUserRegistry();
        this.routingService = jPremiumVelocity.getRoutingService();
        this.messageService = jPremiumVelocity.getMessageService();
    }

    @Subscribe
    public void onServerPostConnect(ServerPostConnectEvent serverPostConnectEvent) {
        Player player = serverPostConnectEvent.getPlayer();
        UserProfileData userProfile = this.onlineUserRegistry.findByUniqueId(player.getUniqueId()).orElse(null);
        if (userProfile == null) {
            return;
        }
        if (userProfile.wasServerRedirected()) {
            userProfile.setServerRedirected(false);
            this.messageService.sendMessageToUser(userProfile, "lastServerRedirection");
        }
        this.routingService.sendStatePayloadToServer(userProfile, player);
    }
}
