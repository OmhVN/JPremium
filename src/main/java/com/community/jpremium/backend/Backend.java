package com.community.jpremium.backend;

import com.community.jpremium.backend.config.BackendConfigService;
import com.community.jpremium.backend.listener.BackendMoveRestrictionListener;
import com.community.jpremium.backend.listener.BackendPlayerLoginListener;
import com.community.jpremium.backend.listener.BackendPluginMessageStateListener;
import com.community.jpremium.backend.listener.BackendRestrictionListeners;
import com.community.jpremium.backend.listener.PaperHandshakeListener;
import com.community.jpremium.backend.listener.ProtocolLibHandshakeListener;
import com.community.jpremium.backend.service.BackendStateRegistry;
import com.community.jpremium.backend.service.CaptchaMapRenderer;
import com.community.jpremium.bungee.JPremium;
import com.community.jpremium.common.util.ReflectionUtils;
import com.community.jpremium.integration.placeholder.PlaceholderApiBootstrap;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class Backend
extends JavaPlugin {
    private Logger logger;
    private Server server;
    private Messenger messenger;
    private PluginManager pluginManager;
    private BackendStateRegistry stateRegistry;
    private BackendConfigService backendConfig;

    public void onLoad() {
        this.logger = this.getLogger();
        this.server = this.getServer();
        this.messenger = this.getServer().getMessenger();
        this.pluginManager = this.getServer().getPluginManager();
    }

    public void onEnable() {
        try {
            this.stateRegistry = new BackendStateRegistry();
            this.backendConfig = new BackendConfigService();
            this.registerHandshakeListeners();
            this.registerRestrictionListeners();
            this.registerBackendListeners();
            PlaceholderApiBootstrap.registerPlaceholderExpansion(this, this.stateRegistry);
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void registerHandshakeListeners() {
        if (this.isVelocityModernMode()) {
            this.logger.info("The Velocity modern mode detected! JPremium won't register any handshake listeners!");
        } else if (this.pluginManager.isPluginEnabled("BungeeGuard")) {
            this.logger.info("BungeeGuard detected! JPremium won't register any handshake listeners!");
        } else if (this.backendConfig.isAccessTokenDisabled()) {
            this.logger.info("The 'accessTokenDisabled' option is enabled! JPremium won't register any handshake listeners!");
        } else if (this.isPaperHandshakeAvailable()) {
            PaperHandshakeListener.registerListener(this, this.logger, this.backendConfig);
            this.logger.info("JPremium will use Paper's handshake listener!");
        } else if (this.isProtocolLibAvailable()) {
            ProtocolLibHandshakeListener.registerListener(this, this.logger, this.backendConfig);
            this.logger.info("JPremium will use use ProtocolLib's handshake listener!");
        } else {
            this.logger.severe("Your server does not support any player's handshake listeners! The server will shut down now!");
            this.logger.severe("If your server is using version 1.9.4 or newer, please upgrade to PaperSpigot - https://papermc.io");
            this.logger.severe("If your server is using version 1.9.2 or older, please install ProtocolLib - https://www.spigotmc.org/resources/1997/");
            this.server.shutdown();
        }
    }

    private void registerRestrictionListeners() {
        if (this.backendConfig.isMovementRestricted()) {
            this.pluginManager.registerEvents(new BackendMoveRestrictionListener(this.stateRegistry), this);
        }
        if (this.backendConfig.isInteractionRestricted()) {
            this.pluginManager.registerEvents(new BackendRestrictionListeners.LegacyInteractionRestrictionListener(this.stateRegistry), this);
            if (this.isSwapHandSupported()) {
                this.pluginManager.registerEvents(new BackendRestrictionListeners.ModernInteractionRestrictionListener(this.stateRegistry), this);
            }
        }
    }

    private void registerBackendListeners() {
        CaptchaMapRenderer captchaMapRenderer = new CaptchaMapRenderer(this.stateRegistry, this.backendConfig);
        BackendPluginMessageStateListener pluginMessageListener = new BackendPluginMessageStateListener(this.logger, this.stateRegistry, this.backendConfig, captchaMapRenderer.getCaptchaItem());
        BackendPlayerLoginListener playerLoginListener = new BackendPlayerLoginListener(this.stateRegistry, this.backendConfig, captchaMapRenderer.getCaptchaItem());
        this.pluginManager.registerEvents(playerLoginListener, this);
        this.messenger.registerIncomingPluginChannel(this, "jpremium:state", pluginMessageListener);
        if (this.backendConfig.getCaptchaMapSlot() >= 0) {
            captchaMapRenderer.registerMapRenderer();
        }
    }

    private boolean isVelocityModernMode() {
        try {
            Object spigotServer = this.getServer().spigot();
            Object paperConfig = spigotServer.getClass().getMethod("getPaperConfig").invoke(spigotServer);
            Object velocityEnabled = paperConfig.getClass().getMethod("getBoolean", String.class).invoke(paperConfig, "proxies.velocity.enabled");
            return velocityEnabled instanceof Boolean enabled && enabled;
        }
        catch (Throwable throwable) {
            return false;
        }
    }

    private boolean isPaperHandshakeAvailable() {
        return ReflectionUtils.isClassPresent("com.destroystokyo.paper.event.player.PlayerHandshakeEvent");
    }

    private boolean isProtocolLibAvailable() {
        return ReflectionUtils.isClassPresent("com.comphenix.protocol.ProtocolLibrary");
    }

    public boolean isSwapHandSupported() {
        return ReflectionUtils.isClassPresent("org.bukkit.event.player.PlayerSwapHandItemsEvent");
    }
}
