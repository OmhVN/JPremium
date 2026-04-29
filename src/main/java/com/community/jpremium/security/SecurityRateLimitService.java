package com.community.jpremium.security;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SecurityRateLimitService {
    private static Cache<UUID, String> cachedAddressesByUserId;
    private static Cache<String, Integer> addressAttempts;

    public static void initialize(long l, long secondaryMillis) {
        cachedAddressesByUserId = CacheBuilder.newBuilder().expireAfterWrite(Math.max(l, 1L), TimeUnit.MINUTES).build();
        addressAttempts = CacheBuilder.newBuilder().expireAfterWrite(Math.max(secondaryMillis, 1L), TimeUnit.MINUTES).build();
    }

    public static String getCachedAddress(UUID uniqueId) {
        return cachedAddressesByUserId.getIfPresent(uniqueId);
    }

    public static void cacheAddress(UUID uniqueId, String text) {
        cachedAddressesByUserId.put(uniqueId, text);
    }

    public static void clearCachedAddress(UUID uniqueId) {
        cachedAddressesByUserId.invalidate(uniqueId);
    }

    public static int getAddressAttemptCount(String text) {
        Integer n = addressAttempts.getIfPresent(text);
        return n != null ? n : 0;
    }

    public static void incrementAddressAttemptCount(String text) {
        Integer n = addressAttempts.getIfPresent(text);
        if (n == null) {
            n = 0;
        }
        n = n + 1;
        addressAttempts.put(text, n);
    }
}
