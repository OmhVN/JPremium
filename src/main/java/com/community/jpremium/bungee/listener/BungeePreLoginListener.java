package com.community.jpremium.bungee.listener;

import com.community.jpremium.bungee.listener.BungeePreLoginTask;
import com.community.jpremium.bungee.JPremium;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;

public class BungeePreLoginListener
implements Listener {
    private final JPremium plugin;
    private final Field loginRequestField;
    private static final Map<String, Boolean> SECOND_LOGIN_CACHE = new ConcurrentHashMap<String, Boolean>();

    public static Map<String, Boolean> getSecondLoginCache() {
        return SECOND_LOGIN_CACHE;
    }

    public BungeePreLoginListener(JPremium jPremium) {
        this.plugin = jPremium;
        this.loginRequestField = BungeePreLoginListener.resolveLoginRequestField(jPremium.getLogger());
    }

    private static Field resolveLoginRequestField(Logger logger) {
        try {
            Field field = Class.forName("net.md_5.bungee.connection.InitialHandler").getDeclaredField("loginRequest");
            field.setAccessible(true);
            return field;
        }
        catch (ClassNotFoundException | NoSuchFieldException noSuchFieldException) {
            logger.warning("Could not find loginRequest filed in InitialHandler! You are using an old proxy or your fork does not have that field?");
            return null;
        }
    }

    @EventHandler(priority=127)
    public void onPreLogin(PreLoginEvent preLoginEvent) {
        if (preLoginEvent.isCancelled()) {
            return;
        }
        if (!preLoginEvent.getConnection().isConnected()) {
            return;
        }
        preLoginEvent.registerIntent(this.plugin);
        this.plugin.runAsync(new BungeePreLoginTask(this.plugin, preLoginEvent, this.loginRequestField));
    }
}
