package com.community.jpremium.velocity.service;

import com.community.jpremium.velocity.service.VelocityMessageService;
import com.community.jpremium.storage.UserProfileRepository;
import com.community.jpremium.common.util.ProfileDataUtils;
import com.community.jpremium.common.config.VelocityConfigService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import net.kyori.adventure.text.Component;

public class VelocityServerRoutingService {
    private final JPremiumVelocity plugin;
    private final ProxyServer proxyServer;
    private final VelocityConfigService config;
    private final UserProfileRepository userRepository;
    private final VelocityMessageService messageService;

    public VelocityServerRoutingService(JPremiumVelocity plugin) {
        this.proxyServer = plugin.getProxyServer();
        this.config = plugin.getConfig();
        this.userRepository = plugin.getUserRepository();
        this.messageService = plugin.getMessageService();
        this.plugin = plugin;
    }

    public RegisteredServer pickRandomServer(List<String> serverNames) {
        if (serverNames.isEmpty()) {
            return null;
        }
        RegisteredServer selectedServer = null;
        while (selectedServer == null && !serverNames.isEmpty()) {
            int selectedIndex = ThreadLocalRandom.current().nextInt(serverNames.size());
            selectedServer = this.proxyServer.getServer(serverNames.get(selectedIndex)).orElse(null);
            serverNames.remove(selectedIndex);
        }
        return selectedServer;
    }

    public void redirectToMainOrLastServer(UserProfileData userProfile) {
        Player player = this.plugin.findPlayer(userProfile);
        if (player == null) {
            return;
        }
        if (userProfile.hasLastServer() && this.config.getBoolean("lastServerRedirection")) {
            RegisteredServer registeredServer = this.proxyServer.getServer(userProfile.getLastServer()).orElse(null);
            userProfile.setLastServer(null);
            if (registeredServer != null) {
                userProfile.setServerRedirected(true);
                player.createConnectionRequest(registeredServer).fireAndForget();
                return;
            }
        }
        List<String> mainServers = this.config.getStringList("mainServerNames");
        if (mainServers.isEmpty()) {
            return;
        }
        ServerConnection serverConnection = player.getCurrentServer().orElse(null);
        if (serverConnection != null && mainServers.contains(serverConnection.getServerInfo().getName())) {
            return;
        }
        RegisteredServer redirectServer = this.pickRandomServer(mainServers);
        if (redirectServer != null) {
            player.createConnectionRequest(redirectServer).fireAndForget();
        }
    }

    public RegisteredServer selectInitialServer(UserProfileData userProfile) {
        List<String> limboServers = this.config.getStringList("limboServerNames");
        List<String> mainServers = this.config.getStringList("mainServerNames");
        if (userProfile.isLogged()) {
            if (userProfile.hasLastServer() && this.config.getBoolean("lastServerRedirection")) {
                RegisteredServer registeredServer = this.proxyServer.getServer(userProfile.getLastServer()).orElse(null);
                userProfile.setLastServer(null);
                this.plugin.runAsync(() -> this.userRepository.update(userProfile));
                if (registeredServer != null) {
                    userProfile.setServerRedirected(true);
                    return registeredServer;
                }
            }
            if (mainServers.isEmpty()) {
                return null;
            }
            return this.pickRandomServer(mainServers);
        }
        if (limboServers.isEmpty()) {
            return null;
        }
        return this.pickRandomServer(limboServers);
    }

    public void sendStatePayloadToServer(UserProfileData userProfile, Player player) {
        ServerConnection serverConnection = player.getCurrentServer().orElse(null);
        if (serverConnection == null) {
            this.plugin.getLogger().warning("Could not send data to a back-end server for player %s because the player isn't connected to any server!".formatted(userProfile.getLastNickname()));
            return;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        String authState = userProfile.getAuthState().name();
        String captchaCode = userProfile.getCaptchaCode();
        String accessToken = this.config.getString("accessToken");
        String serializedUserProfile = ProfileDataUtils.GSON.toJson(userProfile);
        try {
            dataOutputStream.writeUTF(Objects.requireNonNull(accessToken, "accessToken"));
            dataOutputStream.writeUTF(Objects.requireNonNull(authState, "state"));
            dataOutputStream.writeUTF(Objects.requireNonNull(captchaCode, "captchaCode"));
            dataOutputStream.writeUTF(Objects.requireNonNull(serializedUserProfile, "extraData"));
            serverConnection.sendPluginMessage(JPremiumVelocity.STATE_CHANNEL, byteArrayOutputStream.toByteArray());
        }
        catch (Throwable throwable) {
            player.disconnect(Component.text("Could not send data to a Spigot server!"));
            throwable.printStackTrace();
        }
    }
}
