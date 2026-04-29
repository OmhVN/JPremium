package com.community.jpremium.velocity.listener;

import com.community.jpremium.common.config.VelocityConfigService;
import com.community.jpremium.velocity.listener.VelocityPreLoginTask;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import java.time.Duration;

public class VelocityPreLoginListener {
    private final JPremiumVelocity plugin;
    private final VelocityConfigService config;
    public static final Object CACHE_MARKER = new Object();
    private static final Cache<String, Object> SECOND_LOGIN_CACHE = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofMinutes(5L)).build();

    public static Cache<String, Object> getSecondLoginCache() {
        return SECOND_LOGIN_CACHE;
    }

    public VelocityPreLoginListener(JPremiumVelocity plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    @Subscribe(order=PostOrder.LAST)
    public EventTask onPreLogin(PreLoginEvent preLoginEvent) {
        if (preLoginEvent.getResult().isAllowed() && preLoginEvent.getConnection().isActive()) {
            boolean floodgateSupportEnabled = this.config.getBoolean("floodgateSupport");
            if (floodgateSupportEnabled && preLoginEvent.getResult().isForceOfflineMode()) {
                return null;
            }
            return EventTask.async((Runnable)new VelocityPreLoginTask(this.plugin, preLoginEvent));
        }
        return null;
    }
}
