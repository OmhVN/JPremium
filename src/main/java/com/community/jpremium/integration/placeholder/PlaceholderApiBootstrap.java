package com.community.jpremium.integration.placeholder;

import com.community.jpremium.backend.service.BackendStateRegistry;
import com.community.jpremium.integration.placeholder.JPremiumPlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class PlaceholderApiBootstrap {
    public static void registerPlaceholderExpansion(Plugin plugin, BackendStateRegistry stateRegistry) {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new JPremiumPlaceholderExpansion(plugin, stateRegistry).register();
        }
    }
}

