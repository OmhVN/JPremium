package com.community.jpremium.bungee.listener;

import com.community.jpremium.bungee.service.BungeeMessageService;
import com.community.jpremium.storage.UserProfileRepository;
import com.community.jpremium.common.exception.UserMessageException;
import com.community.jpremium.common.exception.StopProcessingException;
import com.community.jpremium.bungee.listener.BungeePreLoginListener;
import com.community.jpremium.common.util.ProfileDataUtils;
import com.community.jpremium.common.config.BungeeConfigService;
import com.community.jpremium.security.UniqueIdMode;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.JPremium;
import com.community.jpremium.proxy.api.event.bungee.UserEvent;
import com.community.jpremium.proxy.api.resolver.Profile;
import com.community.jpremium.proxy.api.resolver.Resolver;
import com.community.jpremium.proxy.api.resolver.ResolverException;
import java.lang.reflect.Field;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeePreLoginTask
implements Runnable {
    private final JPremium plugin;
    private final ProxyServer proxyServer;
    private final BungeeConfigService config;
    private final UserProfileRepository userRepository;
    private final BungeeMessageService messageService;
    private final Resolver profileResolver;
    private final PreLoginEvent preLoginEvent;
    private final PendingConnection connection;
    private final Field rewriteProfileField;
    private String username;
    private UUID incomingUniqueId;
    private UUID resolvedPremiumUniqueId;

    public BungeePreLoginTask(JPremium plugin, PreLoginEvent preLoginEvent, Field rewriteProfileField) {
        this.plugin = plugin;
        this.proxyServer = plugin.getProxy();
        this.config = plugin.getConfig();
        this.userRepository = plugin.getUserRepository();
        this.messageService = plugin.getMessageService();
        this.profileResolver = plugin.getProfileResolver();
        this.preLoginEvent = preLoginEvent;
        this.connection = preLoginEvent.getConnection();
        this.username = this.connection.getName();
        this.incomingUniqueId = this.connection.getUniqueId();
        this.rewriteProfileField = rewriteProfileField;
    }

    @Override
    public void run() {
        try {
            this.handleFloodgateUserLogin();
            this.validateUsernameAndUniqueness();
            this.loadExistingOfflineProfile();
            this.resolvePremiumProfileByName();
            this.decideOnlineModeAndCreateProfile();
        }
        catch (StopProcessingException stopProcessingException) {
        }
        catch (UserMessageException userMessageException) {
            BaseComponent baseComponent = this.messageService.buildComponentMessage(userMessageException.getMessagePath(), userMessageException.getMessageArgument());
            this.preLoginEvent.setReason(baseComponent);
            this.preLoginEvent.setCancelled(true);
        }
        catch (Throwable throwable) {
            if (throwable.getCause() != null && throwable.getCause().getCause() instanceof SQLIntegrityConstraintViolationException) {
                BaseComponent baseComponent = this.messageService.buildComponentMessage("preLoginErrorUniqueIdCollision", this.username);
                this.preLoginEvent.setReason(baseComponent);
                this.preLoginEvent.setCancelled(true);
                this.plugin.getLogger().warning("Could not create a user for " + this.username + " due to UUID collision with an another player! Details: " + throwable.getMessage());
                return;
            }
            BaseComponent baseComponent = this.messageService.buildComponentMessage("preLoginErrorUserCannotBeLoaded", this.username);
            this.preLoginEvent.setReason(baseComponent);
            this.preLoginEvent.setCancelled(true);
            throwable.printStackTrace();
        }
        finally {
            this.preLoginEvent.completeIntent(this.plugin);
        }
    }

    private void handleFloodgateUserLogin() throws StopProcessingException {
        boolean floodgateSupportEnabled = this.config.getBoolean("floodgateSupport");
        if (!floodgateSupportEnabled) {
            return;
        }
        if (this.incomingUniqueId != null && this.incomingUniqueId.getMostSignificantBits() == 0L) {
            UserProfileData userProfile = this.userRepository.findByUniqueId(this.incomingUniqueId).orElse(null);
            if (userProfile != null) {
                if (!this.username.equals(userProfile.getLastNickname())) {
                    userProfile.setLastNickname(this.username);
                    this.userRepository.update(userProfile);
                }
                this.plugin.getOnlineUserRegistry().add(userProfile);
                throw new StopProcessingException();
            }
            String address = this.connection.getAddress().getAddress().getHostAddress();
            UserProfileData newBedrockProfile = new UserProfileData(this.incomingUniqueId);
            newBedrockProfile.setLastNickname(this.username);
            newBedrockProfile.setFirstAddress(address);
            newBedrockProfile.setFirstSeen(Instant.now());
            this.userRepository.insert(newBedrockProfile);
            this.plugin.getOnlineUserRegistry().add(newBedrockProfile);
            this.plugin.fireEventAsync(new UserEvent.Register(newBedrockProfile, null));
            throw new StopProcessingException();
        }
    }

    private void validateUsernameAndUniqueness() throws UserMessageException {
        if (!ProfileDataUtils.compileUsernamePatternOrDefault(this.config.getString("allowedUsernamePattern")).matcher(this.username).matches()) {
            throw new UserMessageException("preLoginErrorInvalidNickname");
        }
        ProxiedPlayer proxiedPlayer = this.proxyServer.getPlayer(this.username);
        if (proxiedPlayer != null && proxiedPlayer.isConnected()) {
            throw new UserMessageException("preLoginErrorAlreadyOnline");
        }
    }

    private void loadExistingOfflineProfile() throws UserMessageException, StopProcessingException {
        long nowMillis = System.currentTimeMillis();
        int automaticUnregisterDays = this.config.getInt("automaticUnregisterTime");
        UserProfileData userProfile = this.userRepository.findByNickname(this.username).orElse(null);
        if (userProfile != null) {
            if (userProfile.isBedrock()) {
                throw new UserMessageException("preLoginErrorBedrockUser");
            }
            if (!userProfile.isPremium()) {
                String previousNickname = userProfile.getLastNickname();
                Timestamp lastSeenTimestamp = ProfileDataUtils.toTimestamp(userProfile.getLastSeen());
                Timestamp unregisterThresholdTimestamp = new Timestamp(nowMillis - (long)automaticUnregisterDays * 86400000L);
                if (this.username.equals(previousNickname)) {
                    if (automaticUnregisterDays <= 0 || !userProfile.isRegistered() || !userProfile.hasLastSeen() || unregisterThresholdTimestamp.before(lastSeenTimestamp)) {
                        this.connection.setOnlineMode(false);
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
                    this.connection.setOnlineMode(false);
                    this.userRepository.update(userProfile);
                    this.plugin.fireEventAsync(new UserEvent.Unregister(userProfile, null));
                    throw new StopProcessingException();
                }
                throw new UserMessageException("preLoginErrorInvalidNicknameCases", previousNickname);
            }
            this.connection.setOnlineMode(true);
            this.plugin.getOnlineUserRegistry().add(userProfile);
            throw new StopProcessingException();
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
        UserProfileData premiumIdProfile = this.resolvedPremiumUniqueId != null ? this.userRepository.findByPremiumId(this.resolvedPremiumUniqueId).orElse(null) : null;
        if (premiumIdProfile != null) {
            premiumIdProfile.setLastNickname(this.username);
            this.userRepository.update(premiumIdProfile);
            this.connection.setOnlineMode(true);
            this.plugin.getOnlineUserRegistry().add(premiumIdProfile);
            throw new StopProcessingException();
        }
    }

    private void decideOnlineModeAndCreateProfile() throws UserMessageException, StopProcessingException {
        boolean registerOnWebsite = this.config.getBoolean("registerOnWebsite");
        boolean registerPremiumUsers = this.config.getBoolean("registerPremiumUsers");
        boolean useSecondLoginCrackedMode = this.config.getBoolean("secondLoginCracked");
        boolean detectHandshakePremiumUniqueId = this.config.getBoolean("detectPremiumUniqueIdsInHandshake");
        this.resolvedPremiumUniqueId = registerPremiumUsers ? this.resolvedPremiumUniqueId : null;
        UniqueIdMode uniqueIdMode = this.config.getEnum(UniqueIdMode.class, "uniqueIdsType");
        UUID resolvedUniqueId = uniqueIdMode.equals(UniqueIdMode.FIXED) ? UUID.randomUUID() : (uniqueIdMode.equals(UniqueIdMode.OFFLINE) ? ProfileDataUtils.createOfflineUuid(this.username) : this.resolvedPremiumUniqueId);
        UUID offlineUniqueId = uniqueIdMode.equals(UniqueIdMode.FIXED) ? UUID.randomUUID() : ProfileDataUtils.createOfflineUuid(this.username);
        String address = this.connection.getAddress().getAddress().getHostAddress();
        if (detectHandshakePremiumUniqueId && this.hasHandshakePremiumUuid()) {
            this.registerPremiumProfile(resolvedUniqueId, address);
            throw new StopProcessingException();
        }
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
                if (BungeePreLoginListener.getSecondLoginCache().containsKey(secondLoginCacheKey)) {
                    if (BungeePreLoginListener.getSecondLoginCache().get(secondLoginCacheKey).booleanValue()) {
                        this.registerPremiumProfile(resolvedUniqueId, address);
                    } else {
                        this.registerOfflineProfile(offlineUniqueId);
                    }
                    BungeePreLoginListener.getSecondLoginCache().remove(secondLoginCacheKey);
                } else {
                    BungeePreLoginListener.getSecondLoginCache().put(secondLoginCacheKey, Boolean.FALSE);
                    this.connection.setOnlineMode(true);
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
        if (this.rewriteProfileField == null || this.connection.getVersion() < 760) {
            return false;
        }
        try {
            Object rewriteProfile = this.rewriteProfileField.get(this.connection);
            if (rewriteProfile == null) {
                return false;
            }
            Object handshakeUuid = rewriteProfile.getClass().getMethod("getUuid").invoke(rewriteProfile);
            return handshakeUuid instanceof UUID && handshakeUuid.equals(this.resolvedPremiumUniqueId);
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
        this.userRepository.insert(userProfile);
        this.plugin.getOnlineUserRegistry().add(userProfile);
        this.connection.setOnlineMode(true);
        this.plugin.fireEventAsync(new UserEvent.Register(userProfile, null));
    }

    private void registerOfflineProfile(UUID uniqueId) {
        UserProfileData userProfile = new UserProfileData(uniqueId);
        userProfile.setLastNickname(this.username);
        this.userRepository.insert(userProfile);
        this.plugin.getOnlineUserRegistry().add(userProfile);
        this.connection.setOnlineMode(false);
    }
}
