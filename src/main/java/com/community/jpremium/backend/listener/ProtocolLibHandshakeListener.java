package com.community.jpremium.backend.listener;

import com.community.jpremium.backend.config.BackendConfigService;
import com.community.jpremium.backend.service.HandshakePayloadParser;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.temporary.TemporaryPlayerFactory;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ProtocolLibHandshakeListener
extends PacketAdapter {
    private final Logger logger;
    private final BackendConfigService backendConfig;
    private final HandshakePayloadParser handshakePayloadParser;

    private ProtocolLibHandshakeListener(Plugin plugin, Logger logger, BackendConfigService backendConfig) {
        super(plugin, ListenerPriority.LOW, new PacketType[]{PacketType.Handshake.Client.SET_PROTOCOL});
        this.logger = logger;
        this.backendConfig = backendConfig;
        this.handshakePayloadParser = new HandshakePayloadParser(logger, backendConfig);
    }

    public static void registerListener(Plugin plugin, Logger logger, BackendConfigService backendConfig) {
        ProtocolLibrary.getProtocolManager().addPacketListener((PacketListener)new ProtocolLibHandshakeListener(plugin, logger, backendConfig));
    }

    public void onPacketReceiving(PacketEvent packetEvent) {
        PacketContainer packetContainer = packetEvent.getPacket();
        PacketType.Protocol protocol = (PacketType.Protocol)packetContainer.getProtocols().read(0);
        String originalHandshake = packetContainer.getStrings().read(0);
        if (protocol != PacketType.Protocol.LOGIN) {
            return;
        }
        HandshakePayloadParser.AuthState handshakeState = this.handshakePayloadParser.parse(originalHandshake);
        if (handshakeState.isValid()) {
            packetContainer.getStrings().write(0, handshakeState.toHandshakeSuffix());
        } else {
            packetContainer.getStrings().write(0, "null");
            this.disconnectPlayer(packetEvent.getPlayer());
        }
    }

    private void disconnectPlayer(Player player) {
        PacketContainer packetContainer = new PacketContainer(PacketType.Login.Server.DISCONNECT);
        WrappedChatComponent wrappedChatComponent = WrappedChatComponent.fromText(this.backendConfig.getDisconnectionMessage());
        packetContainer.getModifier().writeDefaults();
        packetContainer.getChatComponents().write(0, wrappedChatComponent);
        ProtocolLibrary.getProtocolManager().sendServerPacket(player, packetContainer);
        try {
            TemporaryPlayerFactory.getInjectorFromPlayer((Player)player).disconnect(this.backendConfig.getDisconnectionMessage());
        }
        catch (NoClassDefFoundError noClassDefFoundError) {
            this.logger.severe("Your server probably uses an old version of ProtocolLib. Please update your ProtocolLib to version 5.0.0 or higher!");
            throw noClassDefFoundError;
        }
    }
}
