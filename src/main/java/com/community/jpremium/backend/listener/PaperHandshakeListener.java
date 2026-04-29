package com.community.jpremium.backend.listener;

import com.community.jpremium.backend.config.BackendConfigService;
import com.community.jpremium.backend.service.HandshakePayloadParser;
import com.destroystokyo.paper.event.player.PlayerHandshakeEvent;
import java.util.logging.Logger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class PaperHandshakeListener
implements Listener {
    private final BackendConfigService backendConfig;
    private final HandshakePayloadParser handshakePayloadParser;

    private PaperHandshakeListener(Logger logger, BackendConfigService backendConfig) {
        this.backendConfig = backendConfig;
        this.handshakePayloadParser = new HandshakePayloadParser(logger, backendConfig);
    }

    public static void registerListener(Plugin plugin, Logger logger, BackendConfigService backendConfig) {
        plugin.getServer().getPluginManager().registerEvents(new PaperHandshakeListener(logger, backendConfig), plugin);
    }

    @EventHandler(priority=EventPriority.LOW)
    public void onPlayerHandshake(PlayerHandshakeEvent playerHandshakeEvent) {
        String originalHandshake = playerHandshakeEvent.getOriginalHandshake();
        HandshakePayloadParser.AuthState handshakeState = this.handshakePayloadParser.parse(originalHandshake);
        if (handshakeState.isValid()) {
            playerHandshakeEvent.setServerHostname("");
            playerHandshakeEvent.setUniqueId(handshakeState.getUniqueId());
            playerHandshakeEvent.setSocketAddressHostname(handshakeState.getState());
            playerHandshakeEvent.setPropertiesJson(handshakeState.getSerializedProfile());
        } else {
            playerHandshakeEvent.setFailed(true);
            playerHandshakeEvent.setFailMessage(this.backendConfig.getDisconnectionMessage());
        }
    }
}

