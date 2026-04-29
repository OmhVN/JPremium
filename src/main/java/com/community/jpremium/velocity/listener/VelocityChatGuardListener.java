package com.community.jpremium.velocity.listener;

import com.community.jpremium.common.service.OnlineUserRegistry;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;

public class VelocityChatGuardListener {
    private final OnlineUserRegistry onlineUserRegistry;

    public VelocityChatGuardListener(JPremiumVelocity jPremiumVelocity) {
        this.onlineUserRegistry = jPremiumVelocity.getOnlineUserRegistry();
    }

    @Subscribe(order=PostOrder.LATE)
    public void onPlayerChat(PlayerChatEvent playerChatEvent) {
        if (playerChatEvent.getResult().isAllowed()) {
            this.enforceChatRestrictions(playerChatEvent);
        }
    }

    private void enforceChatRestrictions(PlayerChatEvent playerChatEvent) {
        Player player = playerChatEvent.getPlayer();
        UserProfileData userProfile = this.onlineUserRegistry.findByUniqueId(player.getUniqueId()).orElse(null);
        if (userProfile == null || !userProfile.isLogged()) {
            playerChatEvent.setResult(PlayerChatEvent.ChatResult.denied());
        }
    }
}
