package com.community.jpremium.common.model;

import com.community.jpremium.security.SecurityRateLimitService;
import com.community.jpremium.proxy.api.user.User;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class UserProfileData
implements User {
    @Expose
    @SerializedName(value="uniqueId")
    private UUID uniqueId;
    @Expose
    @SerializedName(value="premiumId")
    private UUID premiumId;
    @Expose
    @SerializedName(value="lastNickname")
    private String lastNickname;
    @Expose
    @SerializedName(value="emailAddress")
    private String emailAddress;
    @Expose
    @SerializedName(value="hashedPassword")
    private String hashedPassword;
    @Expose
    @SerializedName(value="verificationToken")
    private String verificationToken;
    @Expose
    @SerializedName(value="sessionExpires")
    private Instant sessionExpires;
    @Expose
    @SerializedName(value="lastServer")
    private String lastServer;
    @Expose
    @SerializedName(value="lastSeen")
    private Instant lastSeen;
    @Expose
    @SerializedName(value="lastAddress")
    private String lastAddress;
    @Expose
    @SerializedName(value="firstSeen")
    private Instant firstSeen;
    @Expose
    @SerializedName(value="firstAddress")
    private String firstAddress;
    private long loginDeadlineMillis;
    private boolean loggedIn;
    private boolean serverRedirected;
    private String captchaCode;
    private String recoveryCode;
    private Runnable pendingConfirmationAction;
    private long pendingConfirmationDeadlineMillis;
    private String requestedServerName;

    @Override
    public UUID getUniqueId() {
        return this.uniqueId;
    }

    public void setUniqueId(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Override
    public UUID getPremiumId() {
        return this.premiumId;
    }

    public void setPremiumId(UUID premiumId) {
        this.premiumId = premiumId;
    }

    @Override
    public String getLastNickname() {
        return this.lastNickname;
    }

    public void setLastNickname(String lastNickname) {
        this.lastNickname = lastNickname;
    }

    @Override
    public String getEmailAddress() {
        return this.emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    @Override
    public String getHashedPassword() {
        return this.hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    @Override
    public String getVerificationToken() {
        return this.verificationToken;
    }

    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }

    @Override
    public Instant getSessionExpires() {
        return this.sessionExpires;
    }

    public void setSessionExpires(Instant instant) {
        this.sessionExpires = instant;
    }

    @Override
    public String getLastServer() {
        return this.lastServer;
    }

    public void setLastServer(String lastServer) {
        this.lastServer = lastServer;
    }

    @Override
    public Instant getLastSeen() {
        return this.lastSeen;
    }

    public void setLastSeen(Instant instant) {
        this.lastSeen = instant;
    }

    @Override
    public String getLastAddress() {
        return this.lastAddress;
    }

    public void setLastAddress(String lastAddress) {
        this.lastAddress = lastAddress;
    }

    @Override
    public Instant getFirstSeen() {
        return this.firstSeen;
    }

    public void setFirstSeen(Instant instant) {
        this.firstSeen = instant;
    }

    @Override
    public String getFirstAddress() {
        return this.firstAddress;
    }

    public void setFirstAddress(String firstAddress) {
        this.firstAddress = firstAddress;
    }

    public UserProfileData() {
        this.captchaCode = "";
    }

    public UserProfileData(UUID uniqueId) {
        this.uniqueId = uniqueId;
        this.captchaCode = "";
    }

    public String getRequestedServerName() {
        return this.requestedServerName;
    }

    public void setRequestedServerName(String requestedServerName) {
        this.requestedServerName = requestedServerName;
    }

    public boolean hasPendingConfirmationAction() {
        return this.pendingConfirmationAction != null && this.pendingConfirmationDeadlineMillis > System.currentTimeMillis();
    }

    public void setPendingConfirmationAction(Runnable runnable) {
        Objects.requireNonNull(runnable, "confirmableCommand");
        this.pendingConfirmationAction = runnable;
        this.pendingConfirmationDeadlineMillis = System.currentTimeMillis() + Duration.ofMinutes(2L).toMillis();
    }

    public void runPendingConfirmationAction() {
        Objects.requireNonNull(this.pendingConfirmationAction, "confirmableCommand");
        this.pendingConfirmationAction.run();
        this.pendingConfirmationAction = null;
        this.pendingConfirmationDeadlineMillis = 0L;
    }

    public void clearPendingConfirmationAction() {
        this.pendingConfirmationAction = null;
        this.pendingConfirmationDeadlineMillis = 0L;
    }

    public String toString() {
        return String.format("User[uniqueId=%s, username=%s, premiumId=%s]", this.uniqueId, this.lastNickname, this.premiumId);
    }

    @Override
    public boolean hasLastNickname() {
        return this.lastNickname != null;
    }

    @Override
    public boolean hasHashedPassword() {
        return this.hashedPassword != null;
    }

    @Override
    public boolean hasVerificationToken() {
        return this.verificationToken != null && !this.verificationToken.isEmpty();
    }

    @Override
    public boolean hasEmailAddress() {
        return this.emailAddress != null;
    }

    @Override
    public boolean hasSession() {
        return this.sessionExpires != null;
    }

    @Override
    public boolean hasLastServer() {
        return this.lastServer != null;
    }

    @Override
    public boolean hasLastAddress() {
        return this.lastAddress != null;
    }

    @Override
    public boolean hasLastSeen() {
        return this.lastSeen != null;
    }

    @Override
    public boolean hasFirstAddress() {
        return this.firstAddress != null;
    }

    @Override
    public boolean hasFirstSeen() {
        return this.firstSeen != null;
    }

    @Override
    public boolean isBedrock() {
        return this.uniqueId.getMostSignificantBits() == 0L;
    }

    @Override
    public boolean isPremium() {
        return this.premiumId != null;
    }

    @Override
    public boolean isRegistered() {
        return this.isBedrock() || this.isPremium() || this.hasHashedPassword();
    }

    @Override
    public boolean isLogged() {
        return this.loggedIn;
    }

    public long getLoginDeadlineMillis() {
        return this.loginDeadlineMillis;
    }

    public boolean wasServerRedirected() {
        return this.serverRedirected;
    }

    public boolean hasRecoveryCode() {
        return this.recoveryCode != null;
    }

    public boolean hasCachedAddress() {
        return this.getCachedAddress() != null;
    }

    public AuthState getAuthState() {
        if (this.isPremium()) {
            return AuthState.PREMIUM;
        }
        if (this.isLogged()) {
            return AuthState.LOGGED;
        }
        if (this.isRegistered()) {
            return AuthState.REGISTERED;
        }
        return AuthState.UNREGISTERED;
    }

    public String getCaptchaCode() {
        return this.captchaCode;
    }

    public String getCachedAddress() {
        return SecurityRateLimitService.getCachedAddress(this.uniqueId);
    }

    public String getRecoveryCode() {
        return this.recoveryCode;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public void setServerRedirected(boolean serverRedirected) {
        this.serverRedirected = serverRedirected;
    }

    public void setLoginDeadlineMillis(long loginDeadlineMillis) {
        this.loginDeadlineMillis = loginDeadlineMillis;
    }

    public void setCaptchaCode(String captchaCode) {
        this.captchaCode = captchaCode;
    }

    public void setCachedAddress(String cachedAddress) {
        if (cachedAddress != null) {
            SecurityRateLimitService.cacheAddress(this.uniqueId, cachedAddress);
        } else {
            SecurityRateLimitService.clearCachedAddress(this.uniqueId);
        }
    }

    public void setRecoveryCode(String recoveryCode) {
        this.recoveryCode = recoveryCode;
    }

    public static enum AuthState {
        PREMIUM,
        LOGGED,
        REGISTERED,
        UNREGISTERED,
        UNKNOWN;

        public boolean allowsBackendAccess() {
            return this == PREMIUM || this == LOGGED;
        }
    }
}
