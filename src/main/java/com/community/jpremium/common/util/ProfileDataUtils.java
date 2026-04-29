package com.community.jpremium.common.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

public class ProfileDataUtils {
    public static final Pattern DEFAULT_USERNAME_PATTERN = Pattern.compile("[a-zA-Z0-9_]{2,16}");
    public static final Pattern EMAIL_PATTERN = Pattern.compile("[\\S]{2,32}@[\\S]{2,32}\\.[a-zA-Z]{2,16}");
    private static final Random RANDOM = new SecureRandom();
    private static final String ALPHANUMERIC_CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final String ALPHANUMERIC_UPPERCASE_CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final String NUMERIC_CHARSET = "0123456789";
    public static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().registerTypeAdapter(Instant.class, new InstantEpochTypeAdapter().nullSafe()).create();

    private ProfileDataUtils() {
        throw new AssertionError();
    }

    public static String generateRandomToken(int length) {
        return ProfileDataUtils.generateRandomString(ALPHANUMERIC_CHARSET, length);
    }

    public static String generateSecondFactorToken() {
        return ProfileDataUtils.generateRandomString(ALPHANUMERIC_UPPERCASE_CHARSET, 6);
    }

    public static String generateNumericCode() {
        return ProfileDataUtils.generateRandomString(NUMERIC_CHARSET, 6);
    }

    private static String generateRandomString(String alphabet, int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; ++i) {
            int alphabetLength = alphabet.length();
            int randomIndex = RANDOM.nextInt(alphabetLength);
            char randomChar = alphabet.charAt(randomIndex);
            builder.append(randomChar);
        }
        return builder.toString();
    }

    public static UUID createOfflineUuid(String nickname) {
        String offlineName = "OfflinePlayer:" + nickname;
        return UUID.nameUUIDFromBytes(offlineName.getBytes());
    }

    public static <T> T[] prepend(T[] elements, T element) {
        T[] expandedArray = Arrays.copyOf(elements, elements.length + 1);
        expandedArray[0] = element;
        System.arraycopy(elements, 0, expandedArray, 1, elements.length);
        return expandedArray;
    }

    public static <T> T[] trimLast(T[] elements) {
        T[] trimmedArray = Arrays.copyOf(elements, elements.length - 1);
        System.arraycopy(elements, 0, trimmedArray, 0, elements.length - 1);
        return trimmedArray;
    }

    public static UUID parseUuidWithoutDashes(String uuidWithoutDashes) {
        return uuidWithoutDashes == null || uuidWithoutDashes.isEmpty() ? null : UUID.fromString(uuidWithoutDashes.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
    }

    public static String toUuidWithoutDashes(UUID uniqueId) {
        return uniqueId == null ? null : uniqueId.toString().replace("-", "");
    }

    public static Instant toInstant(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toInstant();
    }

    public static Timestamp toTimestamp(Instant instant) {
        if (instant == null) {
            return null;
        }
        return Timestamp.from(instant);
    }

    public static Pattern compileUsernamePatternOrDefault(String configuredPattern) {
        if (configuredPattern != null && !configuredPattern.isBlank()) {
            return Pattern.compile(configuredPattern);
        }
        return DEFAULT_USERNAME_PATTERN;
    }

    public static Optional<UUID> parseOptionalUuid(String value) {
        try {
            if (value.length() == 32) {
                return Optional.of(UUID.fromString(value.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5")));
            }
            if (value.length() == 36) {
                return Optional.of(UUID.fromString(value));
            }
        }
        catch (IllegalArgumentException illegalArgumentException) {
            // empty catch block
        }
        return Optional.empty();
    }
}
