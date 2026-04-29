package com.community.jpremium.integration.placeholder;

import com.community.jpremium.backend.service.BackendStateRegistry;
import com.google.gson.JsonElement;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.function.Function;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

public class JPremiumPlaceholderExpansion
extends PlaceholderExpansion {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
    private final Plugin plugin;
    private final BackendStateRegistry stateRegistry;

    public JPremiumPlaceholderExpansion(Plugin plugin, BackendStateRegistry stateRegistry) {
        this.plugin = plugin;
        this.stateRegistry = stateRegistry;
    }

    public String getIdentifier() {
        return "JPremium";
    }

    public String getAuthor() {
        return "Jakubson";
    }

    public String getVersion() {
        return this.plugin.getDescription().getVersion();
    }

    public String onRequest(OfflinePlayer offlinePlayer, String placeholderKey) {
        if (offlinePlayer == null) {
            return null;
        }
        UUID uniqueId = offlinePlayer.getUniqueId();
        BackendStateRegistry.AuthState authState = this.stateRegistry.find(uniqueId).orElse(null);
        if (authState == null) {
            return null;
        }
        if (placeholderKey.startsWith("state")) {
            return authState.getUserState().name();
        }
        if (placeholderKey.startsWith("captcha_code")) {
            return authState.getCaptchaCode();
        }
        if (placeholderKey.startsWith("unique_id")) {
            return this.readStringField(authState, "uniqueId");
        }
        if (placeholderKey.startsWith("premium_id")) {
            return this.readStringField(authState, "premiumId");
        }
        if (placeholderKey.startsWith("last_nickname")) {
            return this.readStringField(authState, "lastNickname");
        }
        if (placeholderKey.startsWith("hashed_password")) {
            return this.readStringField(authState, "hashedPassword");
        }
        if (placeholderKey.startsWith("verification_token")) {
            return this.readStringField(authState, "verificationToken");
        }
        if (placeholderKey.startsWith("email_address")) {
            return this.readStringField(authState, "emailAddress");
        }
        if (placeholderKey.startsWith("session_expires")) {
            return this.readInstantField(authState, "sessionExpires");
        }
        if (placeholderKey.startsWith("last_server")) {
            return this.readStringField(authState, "lastServer");
        }
        if (placeholderKey.startsWith("last_address")) {
            return this.readStringField(authState, "lastAddress");
        }
        if (placeholderKey.startsWith("last_seen")) {
            return this.readInstantField(authState, "lastSeen");
        }
        if (placeholderKey.startsWith("first_address")) {
            return this.readStringField(authState, "firstAddress");
        }
        if (placeholderKey.startsWith("first_seen")) {
            return this.readInstantField(authState, "firstSeen");
        }
        return null;
    }

    private String readStringField(BackendStateRegistry.AuthState authState, String fieldName) {
        return this.readField(authState, fieldName, JsonElement::getAsString);
    }

    private String readInstantField(BackendStateRegistry.AuthState authState, String fieldName) {
        return this.readField(authState, fieldName, jsonElement -> DATE_FORMATTER.format(Instant.ofEpochSecond(jsonElement.getAsLong())));
    }

    private String readField(BackendStateRegistry.AuthState authState, String fieldName, Function<JsonElement, String> mapper) {
        if (authState.hasProfileJson() && authState.getProfileJson().has(fieldName)) {
            return mapper.apply(authState.getProfileJson().get(fieldName));
        }
        return "<null>";
    }
}
