package com.community.jpremium.proxy.api.resolver;

import java.util.Optional;

public interface Resolver {
    public Optional<Profile> fetchProfile(String nickname);
}
