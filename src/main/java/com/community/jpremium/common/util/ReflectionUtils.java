package com.community.jpremium.common.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

public class ReflectionUtils {
    private ReflectionUtils() {
        throw new AssertionError();
    }

    public static String encodeBase64(String text) {
        return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    public static UUID parseUuidWithoutDashes(String text) {
        return UUID.fromString(text.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
    }

    public static boolean isClassPresent(String text) {
        try {
            Class.forName(text);
            return true;
        }
        catch (ClassNotFoundException classNotFoundException) {
            return false;
        }
    }
}

