package com.community.jpremium.proxy.api.resolver;

import com.community.jpremium.proxy.api.resolver.Resolver;
import java.util.Optional;

public class CustomResolverProvider {
    private static Resolver resolver;

    public static Optional<Resolver> getResolver() {
        return Optional.ofNullable(resolver);
    }

    public static void setResolver(Resolver resolver) {
        CustomResolverProvider.resolver = resolver;
    }
}

