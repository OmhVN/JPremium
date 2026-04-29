package com.community.jpremium.backend.service;

import com.community.jpremium.common.util.ProfileDataUtils;
import com.community.jpremium.common.model.UserProfileData;
import com.google.gson.JsonObject;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BackendStateRegistry {
    private final Map<UUID, AuthState> statesByPlayerId = new ConcurrentHashMap<UUID, AuthState>();

    public Optional<AuthState> find(UUID uniqueId) {
        return Optional.ofNullable(this.statesByPlayerId.get(uniqueId));
    }

    public void add(AuthState state) {
        this.statesByPlayerId.put(state.getPlayerUniqueId(), state);
    }

    public void remove(AuthState state) {
        this.statesByPlayerId.remove(state.getPlayerUniqueId());
    }

    public static class AuthState {
        private final UUID playerUniqueId;
        private UserProfileData.AuthState userState;
        private String captchaCode;
        private JsonObject profileJson;
        private boolean captchaRendered;

        public UUID getPlayerUniqueId() {
            return this.playerUniqueId;
        }

        public UserProfileData.AuthState getUserState() {
            return this.userState;
        }

        public void setUserState(UserProfileData.AuthState authState) {
            this.userState = authState;
        }

        public String getCaptchaCode() {
            return this.captchaCode;
        }

        public void setCaptchaCode(String captchaCode) {
            this.captchaCode = captchaCode;
        }

        public JsonObject getProfileJson() {
            return this.profileJson;
        }

        public void setProfileJson(String serializedProfileJson) {
            this.profileJson = ProfileDataUtils.GSON.fromJson(serializedProfileJson, JsonObject.class);
        }

        public boolean isCaptchaRendered() {
            return this.captchaRendered;
        }

        public void setCaptchaRendered(boolean captchaRendered) {
            this.captchaRendered = captchaRendered;
        }

        public boolean hasCaptchaCode() {
            return this.captchaCode != null;
        }

        public boolean hasProfileJson() {
            return this.profileJson != null;
        }

        private AuthState(UUID uniqueId) {
            this.playerUniqueId = uniqueId;
            this.userState = UserProfileData.AuthState.UNKNOWN;
        }

        public static AuthState fromPlayer(Player player) {
            return new AuthState(player.getUniqueId());
        }

        public Player getPlayer() {
            return Bukkit.getPlayer(this.playerUniqueId);
        }
    }
}

