package com.community.jpremium.proxy.api.resolver;

import com.community.jpremium.common.util.ProfileDataUtils;
import java.util.Objects;
import java.util.UUID;

public final class Profile {
    private final UUID uniqueId;
    private final String username;

    public Profile(UUID uniqueId, String username) {
        Objects.requireNonNull(uniqueId);
        Objects.requireNonNull(username);
        this.uniqueId = uniqueId;
        this.username = username;
    }

    public static Profile fromRawProfile(String uniqueIdWithoutDashes, String username) {
        return new Profile(ProfileDataUtils.parseUuidWithoutDashes(uniqueIdWithoutDashes), username);
    }

    public UUID getUniqueId() {
        return this.uniqueId;
    }

    public String getName() {
        return this.username;
    }

    @Override
    public String toString() {
        return "Profile[id=" + this.uniqueId + ", name=" + this.username + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.uniqueId, this.username);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Profile profile)) {
            return false;
        }
        return Objects.equals(this.uniqueId, profile.uniqueId) && Objects.equals(this.username, profile.username);
    }
}
