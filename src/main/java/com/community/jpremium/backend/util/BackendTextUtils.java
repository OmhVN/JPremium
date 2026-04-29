package com.community.jpremium.backend.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;

public class BackendTextUtils {
    private BackendTextUtils() {
        throw new AssertionError();
    }

    public static int normalizeMapSlot(int configuredSlot) {
        int clampedSlot = configuredSlot >= 0 && configuredSlot <= 9 ? configuredSlot : 0;
        return clampedSlot - 1;
    }

    public static String translateColorCodes(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String sanitizeCaptchaMessage(String text) {
        return text.replaceAll("[^a-zA-Z0-9 %_]", "?");
    }

    public static Location parseLocation(String text) {
        String[] parts = text.split("/");
        if (parts.length == 6) {
            String worldName = parts[0];
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = Float.parseFloat(parts[4]);
            float pitch = Float.parseFloat(parts[5]);
            return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
        }
        return null;
    }
}

