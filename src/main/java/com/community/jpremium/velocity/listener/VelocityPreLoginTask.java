package com.community.jpremium.velocity.listener;

import com.community.jpremium.velocity.service.VelocityMessageService;
import com.community.jpremium.storage.UserProfileRepository;
import com.community.jpremium.common.exception.UserMessageException;
import com.community.jpremium.common.exception.StopProcessingException;
import com.community.jpremium.velocity.listener.VelocityPreLoginListener;
import com.community.jpremium.common.util.ProfileDataUtils;
import com.community.jpremium.security.UniqueIdMode;
import com.community.jpremium.common.config.VelocityConfigService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.proxy.api.event.velocity.UserEvent;
import com.community.jpremium.proxy.api.resolver.Profile;
import com.community.jpremium.proxy.api.resolver.Resolver;
import com.community.jpremium.proxy.api.resolver.ResolverException;
import com.community.jpremium.velocity.JPremiumVelocity;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.InboundConnection;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import net.kyori.adventure.text.Component;

public class VelocityPreLoginTask
implements Runnable {
    private final JPremiumVelocity plugin;
    private final ProxyServer proxyServer;
    private final VelocityConfigService config;
    private final UserProfileRepository userRepository;
    private final VelocityMessageService messageService;
    private final Resolver profileResolver;
    private final PreLoginEvent preLoginEvent;
    private final InboundConnection connection;
    private final String username;
    private UUID resolvedPremiumUniqueId;

    public VelocityPreLoginTask(JPremiumVelocity jPremiumVelocity, PreLoginEvent preLoginEvent) {
        this.plugin = jPremiumVelocity;
        this.proxyServer = jPremiumVelocity.getProxyServer();
        this.config = jPremiumVelocity.getConfig();
        this.userRepository = jPremiumVelocity.getUserRepository();
        this.messageService = jPremiumVelocity.getMessageService();
        this.profileResolver = jPremiumVelocity.getProfileResolver();
        this.preLoginEvent = preLoginEvent;
        this.connection = preLoginEvent.getConnection();
        this.username = preLoginEvent.getUsername();
    }

    @Override
    public void run() {
        try {
            this.validateUsernameAndUniqueness();
            this.loadExistingOfflineProfile();
            this.resolvePremiumProfileByName();
            this.decideOnlineModeAndCreateProfile();
        }
        catch (StopProcessingException stopProcessingException) {
        }
        catch (UserMessageException userMessageException) {
            Component component = this.messageService.buildComponentMessage(userMessageException.getMessagePath(), userMessageException.getMessageArgument());
            this.preLoginEvent.setResult(PreLoginEvent.PreLoginComponentResult.denied(component));
        }
        catch (Throwable throwable) {
            if (throwable.getCause() != null && throwable.getCause().getCause() instanceof SQLIntegrityConstraintViolationException) {
                Component component = this.messageService.buildComponentMessage("preLoginErrorUniqueIdCollision", this.username);
                this.preLoginEvent.setResult(PreLoginEvent.PreLoginComponentResult.denied(component));
                this.plugin.getLogger().warning("Could not create a user for " + this.username + " due to UUID collision with an another player! Details: " + throwable.getMessage());
                return;
            }
            Component component = this.messageService.buildComponentMessage("preLoginErrorUserCannotBeLoaded", this.username);
            this.preLoginEvent.setResult(PreLoginEvent.PreLoginComponentResult.denied(component));
            throwable.printStackTrace();
        }
    }

    private void validateUsernameAndUniqueness() throws UserMessageException {
        if (!ProfileDataUtils.compileUsernamePatternOrDefault(this.config.getString("allowedUsernamePattern")).matcher(this.username).matches()) {
            throw new UserMessageException("preLoginErrorInvalidNickname");
        }
        Player player = this.proxyServer.getPlayer(this.username).orElse(null);
        if (player != null && player.isActive()) {
            throw new UserMessageException("preLoginErrorAlreadyOnline");
        }
    }

    private void loadExistingOfflineProfile() throws UserMessageException, StopProcessingException {
        long nowMillis = System.currentTimeMillis();
        int automaticUnregisterDays = this.config.getInt("automaticUnregisterTime");
        UserProfileData userProfile = this.userRepository.findByNickname(this.username).orElse(null);
        if (userProfile != null && !userProfile.isPremium()) {
            if (userProfile.isBedrock()) {
                throw new UserMessageException("preLoginErrorBedrockUser");
            }
            String previousNickname = userProfile.getLastNickname();
            Timestamp lastSeenTimestamp = ProfileDataUtils.toTimestamp(userProfile.getLastSeen());
            Timestamp unregisterThresholdTimestamp = new Timestamp(nowMillis - (long)automaticUnregisterDays * 86400000L);
            if (this.username.equals(previousNickname)) {
                if (automaticUnregisterDays <= 0 || !userProfile.isRegistered() || !userProfile.hasLastSeen() || unregisterThresholdTimestamp.before(lastSeenTimestamp)) {
                    this.preLoginEvent.setResult(PreLoginEvent.PreLoginComponentResult.forceOfflineMode());
                    this.plugin.getOnlineUserRegistry().add(userProfile);
                    throw new StopProcessingException();
                }
                userProfile.setHashedPassword(null);
                userProfile.setEmailAddress(null);
                userProfile.setSessionExpires(null);
                userProfile.setLastAddress(null);
                userProfile.setLastSeen(null);
                userProfile.setFirstAddress(null);
                userProfile.setFirstSeen(null);
                this.preLoginEvent.setResult(PreLoginEvent.PreLoginComponentResult.forceOfflineMode());
                this.userRepository.update(userProfile);
                this.plugin.fireEventAsync(new UserEvent.Unregister(userProfile, null));
                this.plugin.getOnlineUserRegistry().add(userProfile);
                throw new StopProcessingException();
            }
            throw new UserMessageException("preLoginErrorInvalidNicknameCases", previousNickname);
        }
    }

    private void resolvePremiumProfileByName() throws UserMessageException, StopProcessingException {
        try {
            this.resolvedPremiumUniqueId = this.profileResolver.fetchProfile(this.username).map(Profile::getUniqueId).orElse(null);
        }
        catch (ResolverException resolverException) {
            this.plugin.getLogger().warning("Unexpected error occurred during fetching a profile: " + resolverException.getMessage());
            throw new UserMessageException("preLoginErrorServersDown");
        }
        UserProfileData nicknameProfile = this.userRepository.findByNickname(this.username).orElse(null);
        UserProfileData premiumIdProfile = this.resolvedPremiumUniqueId != null ? this.userRepository.findByPremiumId(this.resolvedPremiumUniqueId).orElse(null) : null;
        if (nicknameProfile != null && nicknameProfile.isPremium() && (premiumIdProfile == null || !nicknameProfile.getUniqueId().equals(premiumIdProfile.getUniqueId()))) {
            nicknameProfile.setLastNickname(null);
            this.userRepository.update(nicknameProfile);
        }
        if (premiumIdProfile != null) {
            String previousNickname = premiumIdProfile.getLastNickname();
            if (this.username.equals(previousNickname)) {
                this.preLoginEvent.setResult(PreLoginEvent.PreLoginComponentResult.forceOnlineMode());
                this.plugin.getOnlineUserRegistry().add(premiumIdProfile);
                throw new StopProcessingException();
            }
            if (previousNickname != null) {
                Player player = this.plugin.getProxyServer().getPlayer(previousNickname).orElse(null);
                if (player != null && player.isActive()) {
                throw new UserMessageException("preLoginErrorAlreadyOnline");
                }
            }
            premiumIdProfile.setLastNickname(this.username);
            this.userRepository.update(premiumIdProfile);
            this.preLoginEvent.setResult(PreLoginEvent.PreLoginComponentResult.forceOnlineMode());
            this.plugin.getOnlineUserRegistry().add(premiumIdProfile);
            throw new StopProcessingException();
        }
    }

    private void decideOnlineModeAndCreateProfile() throws UserMessageException, StopProcessingException {
        boolean registerOnWebsite = this.config.getBoolean("registerOnWebsite");
        boolean registerPremiumUsers = this.config.getBoolean("registerPremiumUsers");
        boolean useSecondLoginCrackedMode = this.config.getBoolean("secondLoginCracked");
        boolean detectHandshakePremiumUniqueId = this.config.getBoolean("detectPremiumUniqueIdsInHandshake");
        UniqueIdMode uniqueIdMode = this.config.getEnum(UniqueIdMode.class, "uniqueIdsType");
        UUID resolvedUniqueId = uniqueIdMode.equals(UniqueIdMode.FIXED) ? UUID.randomUUID() : (uniqueIdMode.equals(UniqueIdMode.OFFLINE) ? ProfileDataUtils.createOfflineUuid(this.username) : this.resolvedPremiumUniqueId);
        UUID offlineUniqueId = uniqueIdMode.equals(UniqueIdMode.FIXED) ? UUID.randomUUID() : ProfileDataUtils.createOfflineUuid(this.username);
        String address = this.connection.getRemoteAddress().getAddress().getHostAddress();
        if (detectHandshakePremiumUniqueId && this.hasHandshakePremiumUuid()) {
            this.registerPremiumProfile(resolvedUniqueId, address);
            throw new StopProcessingException();
        }
        this.resolvedPremiumUniqueId = registerPremiumUsers ? this.resolvedPremiumUniqueId : null;
        if (registerOnWebsite) {
            if (this.resolvedPremiumUniqueId != null) {
                this.registerPremiumProfile(resolvedUniqueId, address);
                throw new StopProcessingException();
            }
            throw new UserMessageException("preLoginErrorRegisterAtWebsite");
        }
        if (this.resolvedPremiumUniqueId != null) {
            if (useSecondLoginCrackedMode) {
                String secondLoginCacheKey = this.username + address;
                if (VelocityPreLoginListener.getSecondLoginCache().getIfPresent(secondLoginCacheKey) != null) {
                    VelocityPreLoginListener.getSecondLoginCache().invalidate(secondLoginCacheKey);
                    this.registerOfflineProfile(offlineUniqueId);
                } else {
                    VelocityPreLoginListener.getSecondLoginCache().put(secondLoginCacheKey, VelocityPreLoginListener.CACHE_MARKER);
                    this.preLoginEvent.setResult(PreLoginEvent.PreLoginComponentResult.forceOnlineMode());
                }
                throw new StopProcessingException();
            }
            this.registerPremiumProfile(resolvedUniqueId, address);
            throw new StopProcessingException();
        }
        this.registerOfflineProfile(offlineUniqueId);
        throw new StopProcessingException();
    }

    private boolean hasHandshakePremiumUuid() {
        if (this.preLoginEvent.getConnection().getProtocolVersion().getProtocol() < ProtocolVersion.MINECRAFT_1_19_1.getProtocol()) {
            return false;
        }
        try {
            Object handshakeUniqueId = this.preLoginEvent.getClass().getMethod("getUniqueId").invoke(this.preLoginEvent);
            return handshakeUniqueId instanceof UUID && handshakeUniqueId.equals(this.resolvedPremiumUniqueId);
        }
        catch (ReflectiveOperationException reflectiveOperationException) {
            return false;
        }
    }

    private void registerPremiumProfile(UUID uniqueId, String address) {
        UserProfileData userProfile = new UserProfileData(uniqueId);
        userProfile.setPremiumId(this.resolvedPremiumUniqueId);
        userProfile.setLastNickname(this.username);
        userProfile.setFirstAddress(address);
        userProfile.setFirstSeen(Instant.now());
        this.preLoginEvent.setResult(PreLoginEvent.PreLoginComponentResult.forceOnlineMode());
        this.userRepository.insert(userProfile);
        this.plugin.getOnlineUserRegistry().add(userProfile);
        this.plugin.fireEventAsync(new UserEvent.Register(userProfile, null));
    }

    private void registerOfflineProfile(UUID uniqueId) {
        UserProfileData userProfile = new UserProfileData(uniqueId);
        userProfile.setLastNickname(this.username);
        this.preLoginEvent.setResult(PreLoginEvent.PreLoginComponentResult.forceOfflineMode());
        this.userRepository.insert(userProfile);
        this.plugin.getOnlineUserRegistry().add(userProfile);
    }
}
