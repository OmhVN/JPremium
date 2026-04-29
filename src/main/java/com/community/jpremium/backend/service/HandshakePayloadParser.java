package com.community.jpremium.backend.service;

import com.community.jpremium.common.util.ProfileDataUtils;
import com.community.jpremium.common.util.ReflectionUtils;
import com.community.jpremium.backend.config.BackendConfigService;
import java.util.UUID;
import java.util.logging.Logger;

public class HandshakePayloadParser {
    private static final String DECODE_ERROR_TEMPLATE = "An unexpected exception occurred during decoding the handshake packet (%s)!";
    private static final String PACKET_TOO_LARGE_TEMPLATE = "The handshake packet is too large (%s) - maximum is 8000!";
    private static final String INVALID_ARGUMENT_COUNT_TEMPLATE = "The handshake packet contains incorrect arguments count (%s)!";
    private static final String INVALID_ACCESS_TOKEN_TEMPLATE = "The handshake packet contains an incorrect access token (%s)!";
    private final Logger logger;
    private final BackendConfigService backendConfig;

    public HandshakePayloadParser(Logger logger, BackendConfigService backendConfig) {
        this.logger = logger;
        this.backendConfig = backendConfig;
    }

    public AuthState parse(String handshakePacket) {
        try {
            return this.parseUnchecked(handshakePacket);
        }
        catch (Throwable throwable) {
            this.logWarning(DECODE_ERROR_TEMPLATE, ReflectionUtils.encodeBase64(handshakePacket));
            throwable.printStackTrace();
            return AuthState.invalid();
        }
    }

    private AuthState parseUnchecked(String handshakePacket) {
        if (handshakePacket.length() > 8000) {
            this.logWarning(PACKET_TOO_LARGE_TEMPLATE, handshakePacket.length());
            return AuthState.invalid();
        }
        String[] arguments = handshakePacket.split("\u0000");
        if (arguments.length != 3 && arguments.length != 4) {
            this.logWarning(INVALID_ARGUMENT_COUNT_TEMPLATE, ReflectionUtils.encodeBase64(handshakePacket));
            return AuthState.invalid();
        }
        String accessToken = arguments[0];
        String authState = arguments[1];
        String uniqueIdRaw = arguments[2];
        String serializedProfile = arguments.length > 3 ? arguments[3] : "[]";
        String expectedAccessToken = this.backendConfig.getAccessToken();
        if (!expectedAccessToken.equals(accessToken)) {
            this.logWarning(INVALID_ACCESS_TOKEN_TEMPLATE, ReflectionUtils.encodeBase64(handshakePacket));
            return AuthState.invalid();
        }
        return AuthState.of(ProfileDataUtils.parseUuidWithoutDashes(uniqueIdRaw), authState, serializedProfile);
    }

    private void logWarning(String template, Object ... arguments) {
        this.logger.warning(String.format(template, arguments));
    }

    public static class AuthState {
        private static final AuthState INVALID_STATE = new AuthState(null, null, null);
        private final UUID uniqueId;
        private final String state;
        private final String serializedProfile;

        public UUID getUniqueId() {
            return this.uniqueId;
        }

        public String getState() {
            return this.state;
        }

        public String getSerializedProfile() {
            return this.serializedProfile;
        }

        public boolean isValid() {
            return this.uniqueId != null && this.state != null && this.serializedProfile != null;
        }

        private AuthState(UUID uniqueId, String state, String serializedProfile) {
            this.uniqueId = uniqueId;
            this.state = state;
            this.serializedProfile = serializedProfile;
        }

        public String toHandshakeSuffix() {
            return "\u0000" + this.state + "\u0000" + this.uniqueId + "\u0000" + this.serializedProfile;
        }

        public static AuthState invalid() {
            return INVALID_STATE;
        }

        public static AuthState of(UUID uniqueId, String state, String serializedProfile) {
            return new AuthState(uniqueId, state, serializedProfile);
        }
    }
}
