package com.community.jpremium.common.service;

import com.community.jpremium.common.model.UserProfileData;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class OnlineUserRegistry {
    private final Map<UUID, UserProfileData> profilesByUniqueId = new ConcurrentHashMap<UUID, UserProfileData>();

    public Set<UserProfileData> getOnlineProfiles() {
        return Set.copyOf(this.profilesByUniqueId.values());
    }

    public Optional<UserProfileData> findByUniqueId(UUID uniqueId) {
        return Optional.ofNullable(this.profilesByUniqueId.get(uniqueId));
    }

    public void add(UserProfileData userProfile) {
        this.profilesByUniqueId.put(userProfile.getUniqueId(), userProfile);
    }

    public void remove(UserProfileData userProfile) {
        this.profilesByUniqueId.remove(userProfile.getUniqueId());
    }

    public Optional<UserProfileData> findByNickname(String text) {
        return this.profilesByUniqueId.values().stream().filter(userProfile -> text.equalsIgnoreCase(userProfile.getLastNickname())).findFirst();
    }

    public Optional<UserProfileData> findByPremiumId(UUID uniqueId) {
        return this.profilesByUniqueId.values().stream().filter(userProfile -> uniqueId.equals(userProfile.getPremiumId())).findFirst();
    }
}

