package com.community.jpremium.velocity.listener;

import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import java.util.List;
import net.kyori.adventure.text.Component;

public class VelocityServerKickListener {
    private final JPremiumVelocity plugin;

    public VelocityServerKickListener(JPremiumVelocity jPremiumVelocity) {
        this.plugin = jPremiumVelocity;
    }

    @Subscribe
    public void onKickedFromServer(KickedFromServerEvent kickedFromServerEvent) {
        Player player = kickedFromServerEvent.getPlayer();
        UserProfileData userProfile = this.plugin.getOnlineUserRegistry().findByUniqueId(player.getUniqueId()).orElse(null);
        if (userProfile == null || !userProfile.wasServerRedirected()) {
            return;
        }
        userProfile.setServerRedirected(false);
        List<String> mainServerNames = this.plugin.getConfig().getStringList("mainServerNames");
        Component message = this.plugin.getMessageService().buildComponentMessage("lastServerRedirectionError", player.getUsername());
        ServerConnection currentServer = player.getCurrentServer().orElse(null);
        if (currentServer != null && mainServerNames.contains(currentServer.getServerInfo().getName())) {
            kickedFromServerEvent.setResult(KickedFromServerEvent.Notify.create(message));
            return;
        }
        RegisteredServer fallbackServer = this.plugin.getRoutingService().pickRandomServer(mainServerNames);
        kickedFromServerEvent.setResult(KickedFromServerEvent.RedirectPlayer.create(fallbackServer, message));
    }
}
