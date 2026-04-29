package com.community.jpremium.resolver;

import com.community.jpremium.proxy.api.resolver.Profile;
import com.community.jpremium.proxy.api.resolver.Resolver;
import com.community.jpremium.proxy.api.resolver.ResolverException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class MojangProfileResolver
implements Resolver {
    private static final String MOJANG_PROFILE_URL = "https://api.mojang.com/users/profiles/minecraft/%s";
    private static final String MICROSOFT_PROFILE_URL = "https://api.minecraftservices.com/minecraft/profile/lookup/name/%s";
    private static final String UNEXPECTED_STATUS_TEMPLATE = "API returned an unexpected status code: %s";
    private static final String FALLBACK_FAILURE_TEMPLATE = "Could not fetch profile! Mojang error: %s; Minecraft error: %s";

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_BASE_DELAY_MS = 1000L;

    private final Gson gson;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5L))
            .build();
    private final Cache<String, Optional<Profile>> profileCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(45L))
            .build();

    public MojangProfileResolver(Gson gson) {
        this.gson = Objects.requireNonNull(gson);
    }

    @Override
    public Optional<Profile> fetchProfile(String nickname) {
        return this.profileCache.get(nickname.toLowerCase(Locale.ROOT), normalizedNickname -> {
            try {
                return this.fetchFromEndpoint(MOJANG_PROFILE_URL, normalizedNickname);
            } catch (ResolverException mojangException) {
                try {
                    return this.fetchFromEndpoint(MICROSOFT_PROFILE_URL, normalizedNickname);
                } catch (ResolverException microsoftException) {
                    throw new ResolverException(String.format(FALLBACK_FAILURE_TEMPLATE, mojangException.getMessage(), microsoftException.getMessage()));
                }
            }
        });
    }

    private Optional<Profile> fetchFromEndpoint(String endpointFormat, String nickname) {
        String endpointUrl = String.format(endpointFormat, nickname);
        ResolverException lastException = null;

        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            try {
                if (attempt > 0) {
                    long delay = RETRY_BASE_DELAY_MS * (1L << (attempt - 1));
                    Thread.sleep(delay);
                }

                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .timeout(Duration.ofSeconds(5L))
                        .uri(URI.create(endpointUrl))
                        .build();
                HttpResponse<String> httpResponse = this.httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

                switch (httpResponse.statusCode()) {
                    case 200: {
                        JsonObject jsonObject = (JsonObject) this.gson.fromJson(httpResponse.body(), JsonObject.class);
                        String profileId = jsonObject.get("id").getAsString();
                        String profileName = jsonObject.get("name").getAsString();
                        return Optional.of(Profile.fromRawProfile(profileId, profileName));
                    }
                    case 204:
                    case 404: {
                        return Optional.empty();
                    }
                    case 429: {
                        lastException = new ResolverException(String.format(UNEXPECTED_STATUS_TEMPLATE, httpResponse.statusCode()));
                        continue;
                    }
                    default: {
                        throw new ResolverException(String.format(UNEXPECTED_STATUS_TEMPLATE, httpResponse.statusCode()));
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ResolverException(e);
            } catch (ResolverException e) {
                throw e;
            } catch (Exception e) {
                throw new ResolverException(e);
            }
        }

        throw lastException != null ? lastException
                : new ResolverException(String.format(UNEXPECTED_STATUS_TEMPLATE, 429));
    }
}
