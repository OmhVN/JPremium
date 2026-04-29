package com.community.jpremium.bungee.listener;

import com.community.jpremium.common.service.OnlineUserRegistry;
import com.community.jpremium.common.config.BungeeConfigService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.JPremium;
import java.util.List;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeeChatGuardListener
implements Listener {
    private final BungeeConfigService config;
    private final OnlineUserRegistry onlineUserRegistry;

    public BungeeChatGuardListener(JPremium jPremium) {
        this.config = jPremium.getConfig();
        this.onlineUserRegistry = jPremium.getOnlineUserRegistry();
    }

    @EventHandler(priority=-64)
    public void onChatLowest(ChatEvent chatEvent) {
        if (!chatEvent.isCancelled()) {
            this.enforceChatRestrictions(chatEvent);
        }
    }

    @EventHandler(priority=64)
    public void onChatHighest(ChatEvent chatEvent) {
        if (!chatEvent.isCancelled()) {
            this.enforceChatRestrictions(chatEvent);
        }
    }

    private void enforceChatRestrictions(ChatEvent chatEvent) {
        ProxiedPlayer proxiedPlayer = (ProxiedPlayer)chatEvent.getSender();
        UserProfileData userProfile = this.onlineUserRegistry.findByUniqueId(proxiedPlayer.getUniqueId()).orElseThrow();
        if (userProfile.isLogged()) {
            return;
        }
        if (!chatEvent.isCommand()) {
            chatEvent.setCancelled(true);
            return;
        }
        List<String> list = this.config.getStringList("logoutUserCommands");
        if (!list.contains(this.extractCommandName(chatEvent))) {
            chatEvent.setCancelled(true);
        }
    }

    private String extractCommandName(ChatEvent chatEvent) {
        String text = chatEvent.getMessage().split(" ")[0];
        return text.length() > 1 ? text.substring(1) : text;
    }
}

