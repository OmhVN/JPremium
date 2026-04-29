package com.community.jpremium.bungee.listener;

import com.community.jpremium.common.service.OnlineUserRegistry;
import com.community.jpremium.common.config.BungeeConfigService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.JPremium;
import java.util.List;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeeTabCompleteGuardListener
implements Listener {
    private final BungeeConfigService config;
    private final OnlineUserRegistry onlineUserRegistry;

    public BungeeTabCompleteGuardListener(JPremium jPremium) {
        this.config = jPremium.getConfig();
        this.onlineUserRegistry = jPremium.getOnlineUserRegistry();
    }

    @EventHandler(priority=-64)
    public void onTabCompleteLowest(TabCompleteEvent tabCompleteEvent) {
        if (!tabCompleteEvent.isCancelled()) {
            this.enforceTabCompleteRestrictions(tabCompleteEvent);
        }
    }

    @EventHandler(priority=64)
    public void onTabCompleteHighest(TabCompleteEvent tabCompleteEvent) {
        if (!tabCompleteEvent.isCancelled()) {
            this.enforceTabCompleteRestrictions(tabCompleteEvent);
        }
    }

    private void enforceTabCompleteRestrictions(TabCompleteEvent tabCompleteEvent) {
        ProxiedPlayer proxiedPlayer = (ProxiedPlayer)tabCompleteEvent.getSender();
        UserProfileData userProfile = this.onlineUserRegistry.findByUniqueId(proxiedPlayer.getUniqueId()).orElseThrow();
        if (userProfile.isLogged()) {
            return;
        }
        List<String> list = this.config.getStringList("logoutUserCommands");
        if (!list.contains(this.extractCommandName(tabCompleteEvent))) {
            tabCompleteEvent.setCancelled(true);
        }
    }

    private String extractCommandName(TabCompleteEvent tabCompleteEvent) {
        String text = tabCompleteEvent.getCursor().split(" ")[0];
        return text.length() > 1 ? text.substring(1) : text;
    }
}

