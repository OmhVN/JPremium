package com.community.jpremium.storage;

import com.community.jpremium.common.model.UserProfileData;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository {
    public Optional<UserProfileData> findByUniqueId(UUID uniqueId);

    public Optional<UserProfileData> findByPremiumId(UUID premiumId);

    public Optional<UserProfileData> findByNickname(String nickname);

    public Collection<UserProfileData> findByAddress(String address);

    public void insert(UserProfileData userProfile);

    public void update(UserProfileData userProfile);

    public void delete(UserProfileData userProfile);

    public void initialize();

    public void shutdown();
}
