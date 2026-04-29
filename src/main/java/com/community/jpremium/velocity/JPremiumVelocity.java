package com.community.jpremium.velocity;

import com.community.jpremium.common.config.VelocityConfigService;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.common.service.OnlineUserRegistry;
import com.community.jpremium.common.util.ProfileDataUtils;
import com.community.jpremium.proxy.api.JPremiumApi;
import com.community.jpremium.proxy.api.resolver.CustomResolverProvider;
import com.community.jpremium.proxy.api.resolver.Resolver;
import com.community.jpremium.resolver.MojangProfileResolver;
import com.community.jpremium.security.SecurityRateLimitService;
import com.community.jpremium.storage.SqlUserProfileRepository;
import com.community.jpremium.storage.StorageConfig;
import com.community.jpremium.storage.StorageType;
import com.community.jpremium.storage.UserProfileRepository;
import com.community.jpremium.common.runtime.RuntimeDependency;
import com.community.jpremium.common.runtime.RuntimeDependencyManager;
import com.community.jpremium.storage.hikari.MariaDbHikariConnectionFactory;
import com.community.jpremium.storage.hikari.MySqlHikariConnectionFactory;
import com.community.jpremium.storage.hikari.PostgreSqlHikariConnectionFactory;
import com.community.jpremium.storage.sqlite.SqliteConnectionFactory;
import com.community.jpremium.velocity.bootstrap.VelocityApiBridge;
import com.community.jpremium.velocity.bootstrap.VelocityCommunityModeGuard;
import com.community.jpremium.velocity.command.VelocityActivateSecondFactorCommand;
import com.community.jpremium.velocity.command.VelocityChangeEmailAddressCommand;
import com.community.jpremium.velocity.command.VelocityChangePasswordCommand;
import com.community.jpremium.velocity.command.VelocityConfirmCommand;
import com.community.jpremium.velocity.command.VelocityConfirmPasswordRecoveryCommand;
import com.community.jpremium.velocity.command.VelocityCrackedCommand;
import com.community.jpremium.velocity.command.VelocityCreatePasswordCommand;
import com.community.jpremium.velocity.command.VelocityDeactivateSecondFactorCommand;
import com.community.jpremium.velocity.command.VelocityDestroySessionCommand;
import com.community.jpremium.velocity.command.VelocityForceActivateSecondFactorCommand;
import com.community.jpremium.velocity.command.VelocityForceChangeEmailAddressCommand;
import com.community.jpremium.velocity.command.VelocityForceChangePasswordCommand;
import com.community.jpremium.velocity.command.VelocityForceConfirmPasswordRecoveryCommand;
import com.community.jpremium.velocity.command.VelocityForceCrackedCommand;
import com.community.jpremium.velocity.command.VelocityForceCreatePasswordCommand;
import com.community.jpremium.velocity.command.VelocityForceDeactivateSecondFactorCommand;
import com.community.jpremium.velocity.command.VelocityForceDestroySessionCommand;
import com.community.jpremium.velocity.command.VelocityForceLoginCommand;
import com.community.jpremium.velocity.command.VelocityForceMergePremiumUserProfileCommand;
import com.community.jpremium.velocity.command.VelocityForcePremiumCommand;
import com.community.jpremium.velocity.command.VelocityForcePurgeUserProfileCommand;
import com.community.jpremium.velocity.command.VelocityForceRegisterCommand;
import com.community.jpremium.velocity.command.VelocityForceRequestPasswordRecoveryCommand;
import com.community.jpremium.velocity.command.VelocityForceRequestSecondFactorCommand;
import com.community.jpremium.velocity.command.VelocityForceStartSessionCommand;
import com.community.jpremium.velocity.command.VelocityForceUnregisterCommand;
import com.community.jpremium.velocity.command.VelocityForceViewUserProfileCommand;
import com.community.jpremium.velocity.command.VelocityForceViewUserProfilesCommand;
import com.community.jpremium.velocity.command.VelocityInfoCommand;
import com.community.jpremium.velocity.command.VelocityLoginCommand;
import com.community.jpremium.velocity.command.VelocityPremiumCommand;
import com.community.jpremium.velocity.command.VelocityRegisterCommand;
import com.community.jpremium.velocity.command.VelocityReloadCommand;
import com.community.jpremium.velocity.command.VelocityRequestPasswordRecoveryCommand;
import com.community.jpremium.velocity.command.VelocityRequestSecondFactorCommand;
import com.community.jpremium.velocity.command.VelocityStartSessionCommand;
import com.community.jpremium.velocity.command.VelocityUnregisterCommand;
import com.community.jpremium.velocity.listener.VelocityChatGuardListener;
import com.community.jpremium.velocity.listener.VelocityCommandGuardListener;
import com.community.jpremium.velocity.listener.VelocityDisconnectListener;
import com.community.jpremium.velocity.listener.VelocityGameProfileRequestListener;
import com.community.jpremium.velocity.listener.VelocityInitialServerListener;
import com.community.jpremium.velocity.listener.VelocityLoginListener;
import com.community.jpremium.velocity.listener.VelocityPostLoginListener;
import com.community.jpremium.velocity.listener.VelocityPreLoginListener;
import com.community.jpremium.velocity.listener.VelocityServerKickListener;
import com.community.jpremium.velocity.listener.VelocityServerPostConnectListener;
import com.community.jpremium.velocity.listener.VelocityServerPreConnectListener;
import com.community.jpremium.velocity.service.VelocityMessageService;
import com.community.jpremium.velocity.service.VelocityServerRoutingService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;

@Plugin(id="jpremium", name="JPremium", version="CLEARED-1.26.0", authors={"Jakubson"}, description="A modern authentication plugin for Minecraft proxy servers.", url="https://builtbybit.com/resources/jpremium.17227/")
public class JPremiumVelocity {
    public static final Path PLUGIN_DIRECTORY = Paths.get("plugins/jpremium");
    private static final Path BUNDLED_PROXY_DIRECTORY = Paths.get("resources/proxy");
    public static final ChannelIdentifier STATE_CHANNEL = MinecraftChannelIdentifier.create("jpremium", "state");
    private VelocityConfigService config;
    private VelocityConfigService messages;
    private VelocityMessageService messageService;
    private VelocityServerRoutingService routingService;
    private Resolver profileResolver;
    private GoogleAuthenticator googleAuthenticator;
    private VelocityCommunityModeGuard communityModeGuard;
    private static VelocityApiBridge apiBridge;
    private final java.util.logging.Logger logger;
    private final ProxyServer proxyServer;
    private final Metrics.Factory metricsFactory;
    private ExecutorService executorService;
    private Set<String> weakPasswords;
    private String recoveryTemplateHtml;
    private Logger slf4jLogger;
    private StorageConfig storageConfig;
    private UserProfileRepository userRepository;
    private RuntimeDependencyManager dependencyManager;
    private final OnlineUserRegistry onlineUserRegistry = new OnlineUserRegistry();

    public VelocityConfigService getConfig() {
        return this.config;
    }

    public VelocityConfigService getMessagesConfig() {
        return this.messages;
    }

    public VelocityMessageService getMessageService() {
        return this.messageService;
    }

    public VelocityServerRoutingService getRoutingService() {
        return this.routingService;
    }

    public Resolver getProfileResolver() {
        return this.profileResolver;
    }

    public GoogleAuthenticator getGoogleAuthenticator() {
        return this.googleAuthenticator;
    }

    public ProxyServer getProxyServer() {
        return this.proxyServer;
    }

    public java.util.logging.Logger getLogger() {
        return this.logger;
    }

    public Set<String> getWeakPasswords() {
        return this.weakPasswords;
    }

    public String getRecoveryTemplate() {
        return this.recoveryTemplateHtml;
    }

    public StorageConfig getStorageConfig() {
        return this.storageConfig;
    }

    public UserProfileRepository getUserRepository() {
        return this.userRepository;
    }

    public OnlineUserRegistry getOnlineUserRegistry() {
        return this.onlineUserRegistry;
    }

    public void runAsync(Runnable runnable) {
        this.executorService.execute(runnable);
    }

    public void scheduleRepeatingTask(Runnable runnable, long interval, TimeUnit timeUnit) {
        this.proxyServer.getScheduler().buildTask(this, runnable).delay(interval, timeUnit).repeat(interval, timeUnit).schedule();
    }

    public void scheduleDelayedTask(Runnable runnable, long delay, TimeUnit timeUnit) {
        this.proxyServer.getScheduler().buildTask(this, runnable).delay(delay, timeUnit).schedule();
    }

    public <T> void fireEventAsync(T event) {
        this.runAsync(() -> this.proxyServer.getEventManager().fireAndForget(event));
    }

    public String[] getCommandAliases(String commandName) {
        return this.config.getStringList(commandName.concat("CommandAliases")).toArray(String[]::new);
    }

    @Inject
    public JPremiumVelocity(java.util.logging.Logger logger, Logger logger2, ProxyServer proxyServer, Metrics.Factory factory) {
        this.slf4jLogger = logger2;
        this.logger = logger;
        this.proxyServer = proxyServer;
        this.metricsFactory = factory;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent proxyInitializeEvent) {
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1, new ThreadFactoryBuilder().setNameFormat("JPremium Async Task Executor #%d").build());
        this.logger.info("JPremium is enabling...");
        try {
            this.communityModeGuard = new VelocityCommunityModeGuard(this);
            if (!this.communityModeGuard.checkCommunityMode()) {
                return;
            }
        }
        catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        try {
            if (!this.initializeRuntimeDependencies()) {
                return;
            }
            this.config = new VelocityConfigService(this, "configuration.yml");
            this.messages = new VelocityConfigService(this, "messages.yml");
            this.config.reload();
            this.messages.reload();
            if (!this.initializeAccessTokenIfNeeded()) {
                return;
            }
            this.storageConfig = this.buildStorageConfig(this.config);
            this.userRepository = this.createUserRepository(this.config, this.storageConfig);
            SecurityRateLimitService.initialize(this.config.getInt("recoveringPasswordDelay"), this.config.getInt("addressBanDuration"));
            if (this.config.getBoolean("useLegacyResolver") && this.config.getBoolean("useBackupServer")) {
                if (this.config.getBoolean("useBackupServer-IKnowWhatIamDoing")) {
                    this.logger.warning("YOU ENABLED useBackupServer AND useBackupServer-IKnowWhatIamDoing OPTIONS, BUT THE BACKUP SERVER IS DEPRECATED!");
                    this.logger.warning("YOU ENABLED IT AT YOUR OWN RISK! PREMIUM PLAYERS MAY BE DUPLICATED!");
                } else {
                    this.logger.warning("* * * * * * * * * * * * * * * * * * *");
                    this.logger.warning(" ");
                    this.logger.warning(" YOU ENABLED useBackupServer OPTION, BUT IT'S DEPRECATED!");
                    this.logger.warning(" IT IS HIGHLY RECOMMENDED TO DISABLE IT!");
                    this.logger.warning(" IT WILL NOT WORK UNLESS YOU ADD A NEW OPTION (useBackupServer-IKnowWhatIamDoing: true) TO THE JPREMIUM CONFIGURATION");
                    this.logger.warning(" ");
                    this.logger.warning("* * * * * * * * * * * * * * * * * * *");
                }
            }
            this.loadWeakPasswordSet();
            this.loadRecoveryTemplate();
            this.messageService = new VelocityMessageService(this);
            this.routingService = new VelocityServerRoutingService(this);
            this.profileResolver = CustomResolverProvider.getResolver().orElseGet(() -> {
                this.logger.info("Using the default resolver for fetching premium profiles.");
                return new MojangProfileResolver(ProfileDataUtils.GSON);
            });
            this.googleAuthenticator = new GoogleAuthenticator();
            apiBridge = new VelocityApiBridge(this);
            JPremiumApi.setApp(apiBridge);
        }
        catch (Throwable throwable) {
            this.logger.severe("Could not load JPremium assets!");
            throwable.printStackTrace();
            this.proxyServer.shutdown();
            return;
        }
        EventManager eventManager = this.proxyServer.getEventManager();
        CommandManager commandManager = this.proxyServer.getCommandManager();
        eventManager.register(this, new VelocityPreLoginListener(this));
        eventManager.register(this, new VelocityLoginListener(this));
        eventManager.register(this, new VelocityPostLoginListener(this));
        eventManager.register(this, new VelocityGameProfileRequestListener(this));
        eventManager.register(this, new VelocityChatGuardListener(this));
        eventManager.register(this, new VelocityDisconnectListener(this));
        eventManager.register(this, new VelocityCommandGuardListener(this));
        eventManager.register(this, new VelocityInitialServerListener(this));
        eventManager.register(this, new VelocityServerPreConnectListener(this));
        eventManager.register(this, new VelocityServerPostConnectListener(this));
        eventManager.register(this, new VelocityServerKickListener(this));
        new VelocityLoginCommand(this).register();
        new VelocityRegisterCommand(this).register();
        new VelocityUnregisterCommand(this).register();
        new VelocityPremiumCommand(this).register();
        new VelocityCrackedCommand(this).register();
        new VelocityStartSessionCommand(this).register();
        new VelocityDestroySessionCommand(this).register();
        new VelocityChangePasswordCommand(this).register();
        new VelocityCreatePasswordCommand(this).register();
        new VelocityChangeEmailAddressCommand(this).register();
        new VelocityActivateSecondFactorCommand(this).register();
        new VelocityDeactivateSecondFactorCommand(this).register();
        new VelocityRequestPasswordRecoveryCommand(this).register();
        new VelocityConfirmPasswordRecoveryCommand(this).register();
        new VelocityRequestSecondFactorCommand(this).register();
        if (this.config.getBoolean("riskyCommandsConfirmation")) {
            new VelocityConfirmCommand(this).register();
        }
        new VelocityForceLoginCommand(this).register();
        new VelocityForceRegisterCommand(this).register();
        new VelocityForceUnregisterCommand(this).register();
        new VelocityForcePremiumCommand(this).register();
        new VelocityForceCrackedCommand(this).register();
        new VelocityForceStartSessionCommand(this).register();
        new VelocityForceDestroySessionCommand(this).register();
        new VelocityForceChangePasswordCommand(this).register();
        new VelocityForceCreatePasswordCommand(this).register();
        new VelocityForceChangeEmailAddressCommand(this).register();
        new VelocityForceActivateSecondFactorCommand(this).register();
        new VelocityForceDeactivateSecondFactorCommand(this).register();
        new VelocityForceRequestPasswordRecoveryCommand(this).register();
        new VelocityForceConfirmPasswordRecoveryCommand(this).register();
        new VelocityForceRequestSecondFactorCommand(this).register();
        new VelocityForceViewUserProfileCommand(this).register();
        new VelocityForceViewUserProfilesCommand(this).register();
        new VelocityForcePurgeUserProfileCommand(this).register();
        new VelocityForceMergePremiumUserProfileCommand(this).register();
        commandManager.register("jreload", new VelocityReloadCommand(this));
        commandManager.register("jpremium", new VelocityInfoCommand(this), "jvelocity");
        this.proxyServer.getChannelRegistrar().register(new ChannelIdentifier[]{STATE_CHANNEL});
        Metrics metrics = this.metricsFactory.make(this, 24702);
        this.logger.info("JPremium has been enabled!");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent proxyShutdownEvent) {
        this.logger.info("JPremium is disabling...");
        if (this.userRepository != null) {
            this.userRepository.shutdown();
        }
        if (this.dependencyManager != null) {
            this.dependencyManager.close();
        }
        this.executorService.shutdown();
        try {
            if (!this.executorService.awaitTermination(800L, TimeUnit.MILLISECONDS)) {
                this.executorService.shutdownNow();
            }
        }
        catch (InterruptedException interruptedException) {
            this.executorService.shutdownNow();
        }
        this.logger.info("JPremium has been disabled!");
    }

    private boolean initializeRuntimeDependencies() {
        try {
            this.dependencyManager = new RuntimeDependencyManager(
                    PLUGIN_DIRECTORY,
                    this.logger,
                    jarPath -> this.proxyServer.getPluginManager().addToClasspath(this, jarPath)
            );
            // Match original behavior: ensure required runtime libs are available via Maven Central downloads.
            this.dependencyManager.injectDependencies(Set.of(
                    RuntimeDependency.JAKARTA_MAIL,
                    RuntimeDependency.JAKARTA_ACTIVATION,
                    RuntimeDependency.CAFFEINE_CACHE
            ));
            return true;
        } catch (Throwable t) {
            this.logger.severe("Could not download required dependencies! JPremium will not work!");
            t.printStackTrace();
            this.proxyServer.shutdown();
            return false;
        }
    }

    public boolean initializeAccessTokenIfNeeded() {
        String accessToken = this.config.getString("accessToken");
        if (accessToken.equals("{{ACCESS_TOKEN}}")) {
            try {
                String generatedAccessToken = ProfileDataUtils.generateRandomToken(32);
                Path configPath = PLUGIN_DIRECTORY.resolve("configuration.yml");
                String configContent = Files.readString(configPath);
                configContent = configContent.replace("{{ACCESS_TOKEN}}", generatedAccessToken);
                Files.writeString(configPath, configContent);
                this.config.reload();
                this.logger.severe("* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *");
                this.logger.severe("It seems it is your first time you have used JPremium and you have not configured it yet!");
                this.logger.severe("You need to install JPremium correctly so that your server can run!");
                this.logger.severe("Please follow this wiki page: https://github.com/Jakubson/JPremiumCleared/wiki#first-installation");
                this.logger.severe("If you have troubles with installation, you can contact with the author: https://www.spigotmc.org/conversations/add?to=Jakubson");
                this.logger.severe("* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *");
                this.proxyServer.shutdown();
                return false;
            }
            catch (IOException ioException) {
                this.logger.severe("Could not update configuration.yml with generated access token: " + ioException.getMessage());
                this.proxyServer.shutdown();
                return false;
            }
        }
        return true;
    }

    public void loadWeakPasswordSet() {
        try {
            this.weakPasswords = Set.copyOf(Files.readAllLines(this.ensureResourceFile("passwords.txt")));
        }
        catch (IOException ioException) {
            throw new UncheckedIOException("Could not load passwords.txt", ioException);
        }
    }

    public void loadRecoveryTemplate() {
        try {
            this.recoveryTemplateHtml = Files.readString(this.ensureResourceFile("recovery.html"));
        }
        catch (IOException ioException) {
            throw new UncheckedIOException("Could not load recovery.html", ioException);
        }
    }

    public Path ensureResourceFile(String resourceName) {
        Path bundledResourcePath = BUNDLED_PROXY_DIRECTORY.resolve(resourceName);
        Path targetPath = PLUGIN_DIRECTORY.resolve(resourceName);
        if (Files.notExists(targetPath)) {
            try {
                Files.createDirectories(targetPath.getParent());
                try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(bundledResourcePath.toString().replace("\\", "/"))){
                    if (inputStream != null) {
                        Files.copy(inputStream, targetPath);
                        return targetPath;
                    }
                    throw new IOException(resourceName + " does not exists!");
                }
            }
            catch (IOException ioException) {
                throw new UncheckedIOException("Could not extract bundled resource: " + resourceName, ioException);
            }
        }
        return targetPath;
    }

    public Player findPlayer(UserProfileData userProfile) {
        return this.getProxyServer().getPlayer(userProfile.getUniqueId()).orElse(null);
    }

    private StorageConfig buildStorageConfig(VelocityConfigService velocityConfig) {
        StorageConfig storageConfig = new StorageConfig();
        storageConfig.setHostAndPort("%s:%d".formatted(velocityConfig.getString("storageHost"), velocityConfig.getInt("storagePort")));
        storageConfig.setUsername(velocityConfig.getString("storageUser"));
        storageConfig.setPassword(velocityConfig.getString("storagePassword"));
        storageConfig.setDatabase(velocityConfig.getString("storageDatabase"));
        storageConfig.setMaximumPoolSize(velocityConfig.getInt("connectionPoolSize"));
        storageConfig.setMinimumIdle(velocityConfig.getInt("connectionPoolIdle"));
        storageConfig.setConnectionTimeoutMillis(velocityConfig.getInt("connectionPoolTimeout"));
        storageConfig.setMaxLifetimeMillis(velocityConfig.getInt("connectionPoolLifetime"));
        storageConfig.setKeepaliveMillis(velocityConfig.getInt("connectionKeepAliveTime"));
        storageConfig.setProperties(velocityConfig.getStringList("storageProperties").stream().map(property -> property.split("=")).collect(Collectors.toMap(arguments -> arguments[0], arguments -> arguments[1])));
        return storageConfig;
    }

    private UserProfileRepository createUserRepository(VelocityConfigService velocityConfig, StorageConfig storageConfig) {
        StorageType storageType = velocityConfig.getEnumOrDefault(StorageType.class, "storageType", StorageType.MYSQL);
        SqlUserProfileRepository userRepository = switch (storageType) {
            default -> throw new IncompatibleClassChangeError();
            case SQLITE -> new SqlUserProfileRepository(new SqliteConnectionFactory(PLUGIN_DIRECTORY.resolve("database.db"), this.dependencyManager));
            case MYSQL -> {
                this.dependencyManager.injectDependencies(Set.of(RuntimeDependency.HIKARI_CP, RuntimeDependency.MYSQL_DRIVER));
                yield new SqlUserProfileRepository(new MySqlHikariConnectionFactory(storageConfig));
            }
            case MARIADB -> {
                this.dependencyManager.injectDependencies(Set.of(RuntimeDependency.HIKARI_CP, RuntimeDependency.MARIADB_DRIVER));
                yield new SqlUserProfileRepository(new MariaDbHikariConnectionFactory(storageConfig));
            }
            case POSTGRESQL -> {
                this.dependencyManager.injectDependencies(Set.of(RuntimeDependency.HIKARI_CP, RuntimeDependency.POSTGRESQL_DRIVER));
                yield new SqlUserProfileRepository(new PostgreSqlHikariConnectionFactory(storageConfig));
            }
        };
        userRepository.initialize();
        return userRepository;
    }
}
