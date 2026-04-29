package com.community.jpremium.proxy.api.user;

import java.time.Instant;
import java.util.UUID;

public interface User {
    public UUID getUniqueId();

    public UUID getPremiumId();

    public String getLastNickname();

    public String getHashedPassword();

    public String getVerificationToken();

    public String getEmailAddress();

    public Instant getSessionExpires();

    public String getLastServer();

    public String getLastAddress();

    public Instant getLastSeen();

    public String getFirstAddress();

    public Instant getFirstSeen();

    public boolean hasLastNickname();

    public boolean hasHashedPassword();

    public boolean hasVerificationToken();

    public boolean hasEmailAddress();

    public boolean hasSession();

    public boolean hasLastServer();

    public boolean hasLastAddress();

    public boolean hasLastSeen();

    public boolean hasFirstAddress();

    public boolean hasFirstSeen();

    public boolean isBedrock();

    public boolean isPremium();

    public boolean isRegistered();

    public boolean isLogged();
}

