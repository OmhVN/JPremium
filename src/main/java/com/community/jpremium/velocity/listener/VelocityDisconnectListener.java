package com.community.jpremium.velocity.listener;

import com.community.jpremium.velocity.service.VelocityMessageService;
import com.community.jpremium.storage.UserProfileRepository;
import com.community.jpremium.common.service.OnlineUserRegistry;
import com.community.jpremium.common.config.VelocityConfigService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.ServerInfo;
import java.util.List;

public class VelocityDisconnectListener {
    private final JPremiumVelocity plugin;
    private final OnlineUserRegistry onlineUserRegistry;
    private final VelocityConfigService config;
    private final UserProfileRepository userRepository;
    private final VelocityMessageService messageService;

    public VelocityDisconnectListener(JPremiumVelocity plugin) {
        this.plugin = plugin;
        this.onlineUserRegistry = plugin.getOnlineUserRegistry();
        this.config = plugin.getConfig();
        this.userRepository = plugin.getUserRepository();
        this.messageService = plugin.getMessageService();
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent disconnectEvent) {
        Player player = disconnectEvent.getPlayer();
        UserProfileData userProfile = this.onlineUserRegistry.findByUniqueId(player.getUniqueId()).orElse(null);
        if (userProfile == null) {
            return;
        }
        this.onlineUserRegistry.remove(userProfile);
        if (!userProfile.isLogged()) {
            userProfile.setLoginDeadlineMillis(0L);
            userProfile.setRequestedServerName(null);
            this.messageService.clearBossBar(userProfile);
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
        ServerConnection serverConnection = player.getCurrentServer().orElse(null);
        if (serverConnection == null) {
            return;
        }
        ServerInfo serverInfo = serverConnection.getServerInfo();
        String serverName = serverInfo.getName();
        if (!mainServers.contains(serverName) && !limboServers.contains(serverName)) {
            userProfile.setLastServer(serverName);
            this.plugin.runAsync(() -> this.userRepository.update(userProfile));
        }
    }
}

