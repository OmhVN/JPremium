package com.community.jpremium.bungee.listener;

import com.community.jpremium.bungee.service.BungeeMessageService;
import com.community.jpremium.storage.UserProfileRepository;
import com.community.jpremium.common.service.OnlineUserRegistry;
import com.community.jpremium.common.config.BungeeConfigService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.JPremium;
import java.util.List;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeeDisconnectListener
implements Listener {
    private final JPremium plugin;
    private final OnlineUserRegistry onlineUserRegistry;
    private final BungeeConfigService config;
    private final UserProfileRepository userRepository;
    private final BungeeMessageService messageService;

    public BungeeDisconnectListener(JPremium plugin) {
        this.plugin = plugin;
        this.onlineUserRegistry = plugin.getOnlineUserRegistry();
        this.config = plugin.getConfig();
        this.userRepository = plugin.getUserRepository();
        this.messageService = plugin.getMessageService();
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent playerDisconnectEvent) {
        ProxiedPlayer proxiedPlayer = playerDisconnectEvent.getPlayer();
        UserProfileData userProfile = this.onlineUserRegistry.findByUniqueId(proxiedPlayer.getUniqueId()).orElse(null);
        if (userProfile == null) {
            return;
        }
        this.onlineUserRegistry.remove(userProfile);
        if (!userProfile.isLogged()) {
            userProfile.setLoginDeadlineMillis(0L);
            userProfile.setRequestedServerName(null);
        }
        userProfile.setLoggedIn(false);
        userProfile.setRecoveryCode(null);
        userProfile.clearPendingConfirmationAction();
        boolean lastServerRedirectionEnabled = this.config.getBoolean("lastServerRedirection");
        if (!lastServerRedirectionEnabled) {
            return;
        }
        List<String> limboServers = this.config.getStringList("limboServerNames");
        List<String> mainServers = this.config.getStringList("mainServerNames");
        Server server = proxiedPlayer.getServer();
        if (server == null) {
            return;
        }
        ServerInfo serverInfo = server.getInfo();
        String serverName = serverInfo.getName();
        if (!mainServers.contains(serverName) && !limboServers.contains(serverName)) {
            userProfile.setLastServer(serverName);
            this.plugin.runAsync(() -> this.userRepository.update(userProfile));
        }
    }
}

