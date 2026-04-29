package com.community.jpremium.velocity.listener;

import com.community.jpremium.storage.UserProfileRepository;
import com.community.jpremium.common.util.ProfileDataUtils;
import com.community.jpremium.security.UniqueIdMode;
import com.community.jpremium.common.config.VelocityConfigService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.proxy.api.event.velocity.UserEvent;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.GameProfileRequestEvent;
import java.time.Instant;
import java.util.UUID;

public class VelocityGameProfileRequestListener {
    private final JPremiumVelocity plugin;
    private final VelocityConfigService config;
    private final UserProfileRepository userRepository;

    public VelocityGameProfileRequestListener(JPremiumVelocity plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.userRepository = plugin.getUserRepository();
    }

    @Subscribe
    public void onGameProfileRequest(GameProfileRequestEvent gameProfileRequestEvent) {
        boolean floodgateSupportEnabled = this.config.getBoolean("floodgateSupport");
        if (floodgateSupportEnabled && gameProfileRequestEvent.getGameProfile().getId().getMostSignificantBits() == 0L) {
            return;
        }
        String username = gameProfileRequestEvent.getUsername();
        String address = gameProfileRequestEvent.getConnection().getRemoteAddress().getAddress().getHostAddress();
        String secondLoginCacheKey = username + address;
        if (VelocityPreLoginListener.getSecondLoginCache().getIfPresent(secondLoginCacheKey) != null) {
            VelocityPreLoginListener.getSecondLoginCache().invalidate(secondLoginCacheKey);
            UniqueIdMode uniqueIdMode = this.config.getEnum(UniqueIdMode.class, "uniqueIdsType");
            UUID resolvedUniqueId = uniqueIdMode == UniqueIdMode.FIXED ? UUID.randomUUID() : (uniqueIdMode == UniqueIdMode.OFFLINE ? ProfileDataUtils.createOfflineUuid(username) : gameProfileRequestEvent.getOriginalProfile().getId());
            UUID premiumId = gameProfileRequestEvent.getOriginalProfile().getId();
            UserProfileData userProfile = new UserProfileData(resolvedUniqueId);
            userProfile.setPremiumId(premiumId);
            userProfile.setLastNickname(username);
            userProfile.setFirstAddress(address);
            userProfile.setFirstSeen(Instant.now());
            gameProfileRequestEvent.setGameProfile(gameProfileRequestEvent.getGameProfile().withId(userProfile.getUniqueId()));
            this.plugin.runAsync(() -> this.userRepository.insert(userProfile));
            this.plugin.getOnlineUserRegistry().add(userProfile);
            this.plugin.fireEventAsync(new UserEvent.Register(userProfile, null));
        }
        UserProfileData cachedUserProfile = this.plugin.getOnlineUserRegistry().findByNickname(gameProfileRequestEvent.getUsername()).orElse(null);
        if (cachedUserProfile != null) {
            gameProfileRequestEvent.setGameProfile(gameProfileRequestEvent.getGameProfile().withId(cachedUserProfile.getUniqueId()));
        }
    }
}

