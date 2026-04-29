package com.community.jpremium.bungee.service;

import com.community.jpremium.bungee.service.BungeeMessageService;
import com.community.jpremium.storage.UserProfileRepository;
import com.community.jpremium.common.util.ProfileDataUtils;
import com.community.jpremium.common.config.BungeeConfigService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.JPremium;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

public class BungeeServerRoutingService {
    private final JPremium plugin;
    private final ProxyServer proxyServer;
    private final BungeeConfigService config;
    private final UserProfileRepository userRepository;
    private final BungeeMessageService messageService;

    public BungeeServerRoutingService(JPremium plugin) {
        this.plugin = plugin;
        this.proxyServer = plugin.getProxy();
        this.config = plugin.getConfig();
        this.userRepository = plugin.getUserRepository();
        this.messageService = plugin.getMessageService();
    }

    public ServerInfo pickRandomServer(List<String> serverNames) {
        if (serverNames.isEmpty()) {
            return null;
        }
        ServerInfo selectedServer = null;
        while (selectedServer == null && !serverNames.isEmpty()) {
            int selectedIndex = ThreadLocalRandom.current().nextInt(serverNames.size());
            selectedServer = this.proxyServer.getServerInfo(serverNames.get(selectedIndex));
            serverNames.remove(selectedIndex);
        }
        return selectedServer;
    }

    public void redirectToMainOrLastServer(UserProfileData userProfile) {
        ProxiedPlayer proxiedPlayer = this.plugin.findPlayer(userProfile);
        if (proxiedPlayer == null) {
            return;
        }
        if (userProfile.getRequestedServerName() != null) {
            if (proxiedPlayer.getServer() != null && proxiedPlayer.getServer().getInfo().getName().equals(userProfile.getRequestedServerName())) {
                return;
            }
            ServerInfo serverInfo = this.proxyServer.getServerInfo(userProfile.getRequestedServerName());
            if (serverInfo != null) {
                userProfile.setRequestedServerName(null);
                proxiedPlayer.connect(serverInfo);
                return;
            }
        }
        if (userProfile.hasLastServer() && this.config.getBoolean("lastServerRedirection")) {
            ServerInfo serverInfo = this.proxyServer.getServerInfo(userProfile.getLastServer());
            userProfile.setLastServer(null);
            if (serverInfo != null) {
                this.messageService.sendMessageToUser(userProfile, "lastServerRedirection");
                proxiedPlayer.connect(serverInfo);
                return;
            }
        }
        List<String> mainServers = this.config.getStringList("mainServerNames");
        if (mainServers.isEmpty()) {
            return;
        }
        if (proxiedPlayer.getServer() != null && mainServers.contains(proxiedPlayer.getServer().getInfo().getName())) {
            return;
        }
        ServerInfo serverInfo = this.pickRandomServer(mainServers);
        if (serverInfo != null) {
            proxiedPlayer.connect(serverInfo);
        }
    }

    public ServerInfo selectInitialServer(UserProfileData userProfile, ServerInfo requestedServer) {
        List<String> limboServers = this.config.getStringList("limboServerNames");
        List<String> mainServers = this.config.getStringList("mainServerNames");
        if (userProfile.isLogged()) {
            if (userProfile.hasLastServer() && this.config.getBoolean("lastServerRedirection")) {
                ServerInfo lastServer = this.proxyServer.getServerInfo(userProfile.getLastServer());
                userProfile.setLastServer(null);
                this.plugin.runAsync(() -> this.userRepository.update(userProfile));
                if (lastServer != null) {
                    userProfile.setServerRedirected(true);
                    return lastServer;
                }
            }
            if (mainServers.isEmpty()) {
                return null;
            }
            if (this.isForcedHostServer(userProfile, requestedServer)) {
                return requestedServer;
            }
            return this.pickRandomServer(mainServers);
        }
        if (limboServers.isEmpty()) {
            return null;
        }
        if (this.isForcedHostServer(userProfile, requestedServer)) {
            userProfile.setRequestedServerName(requestedServer.getName());
        }
        return this.pickRandomServer(limboServers);
    }

    private boolean isForcedHostServer(UserProfileData userProfile, ServerInfo requestedServer) {
        PendingConnection pendingConnection = this.plugin.findPlayer(userProfile).getPendingConnection();
        InetSocketAddress inetSocketAddress = pendingConnection.getVirtualHost();
        if (inetSocketAddress != null) {
            String forcedHostServerName = pendingConnection.getListener().getForcedHosts().get(inetSocketAddress.getHostString());
            return forcedHostServerName != null && forcedHostServerName.equals(requestedServer.getName());
        }
        return false;
    }

    public void sendStatePayloadToServer(UserProfileData userProfile, Server server) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        ProxiedPlayer proxiedPlayer = this.plugin.findPlayer(userProfile);
        if (proxiedPlayer == null) {
            this.plugin.getLogger().warning("Could not send data to a back-end server for player %s due to missing player!".formatted(userProfile.getLastNickname()));
            return;
        }
        String authState = userProfile.getAuthState().name();
        String captchaCode = userProfile.getCaptchaCode();
        String accessToken = this.config.getString("accessToken");
        String serializedUserProfile = ProfileDataUtils.GSON.toJson(userProfile);
        try {
            dataOutputStream.writeUTF(Objects.requireNonNull(accessToken, "accessToken"));
            dataOutputStream.writeUTF(Objects.requireNonNull(authState, "state"));
            dataOutputStream.writeUTF(Objects.requireNonNull(captchaCode, "captchaCode"));
            dataOutputStream.writeUTF(Objects.requireNonNull(serializedUserProfile, "extraData"));
            server.sendData("jpremium:state", byteArrayOutputStream.toByteArray());
        }
        catch (Throwable throwable) {
            proxiedPlayer.disconnect(TextComponent.fromLegacy("Could not send data to a Spigot server!"));
            throwable.printStackTrace();
        }
    }
}
