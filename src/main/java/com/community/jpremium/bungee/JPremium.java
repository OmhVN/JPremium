package com.community.jpremium.bungee;

import com.community.jpremium.bungee.command.BungeeForceActivateSecondFactorCommand;
import com.community.jpremium.bungee.command.BungeeForceChangeEmailAddressCommand;
import com.community.jpremium.bungee.command.BungeeForceChangePasswordCommand;
import com.community.jpremium.bungee.command.BungeeForceConfirmPasswordRecoveryCommand;
import com.community.jpremium.bungee.command.BungeeForceCrackedCommand;
import com.community.jpremium.bungee.command.BungeeForceCreatePasswordCommand;
import com.community.jpremium.bungee.command.BungeeForceDeactivateSecondFactorCommand;
import com.community.jpremium.bungee.command.BungeeForceDestroySessionCommand;
import com.community.jpremium.bungee.command.BungeeForceLoginCommand;
import com.community.jpremium.bungee.command.BungeeForceMergePremiumUserProfileCommand;
import com.community.jpremium.bungee.command.BungeeForcePremiumCommand;
import com.community.jpremium.bungee.command.BungeeForcePurgeUserProfileCommand;
import com.community.jpremium.bungee.command.BungeeForceRegisterCommand;
import com.community.jpremium.bungee.command.BungeeForceRequestPasswordRecoveryCommand;
import com.community.jpremium.bungee.command.BungeeForceRequestSecondFactorCommand;
import com.community.jpremium.bungee.command.BungeeForceStartSessionCommand;
import com.community.jpremium.bungee.command.BungeeForceUnregisterCommand;
import com.community.jpremium.bungee.command.BungeeForceViewUserProfileCommand;
import com.community.jpremium.bungee.command.BungeeForceViewUserProfilesCommand;
import com.community.jpremium.bungee.service.BungeeMessageService;
import com.community.jpremium.bungee.command.BungeeReloadCommand;
import com.community.jpremium.bungee.command.BungeeInfoCommand;
import com.community.jpremium.storage.UserProfileRepository;
import com.community.jpremium.bungee.listener.BungeeChatGuardListener;
import com.community.jpremium.bungee.listener.BungeeDisconnectListener;
import com.community.jpremium.bungee.listener.BungeeLoginListener;
import com.community.jpremium.bungee.listener.BungeePostLoginListener;
import com.community.jpremium.bungee.listener.BungeePreLoginListener;
import com.community.jpremium.bungee.listener.BungeeServerConnectListener;
import com.community.jpremium.bungee.listener.BungeeServerConnectedListener;
import com.community.jpremium.bungee.listener.BungeeServerKickListener;
import com.community.jpremium.bungee.listener.BungeeTabCompleteGuardListener;
import com.community.jpremium.storage.SqlUserProfileRepository;
import com.community.jpremium.storage.sqlite.SqliteConnectionFactory;
import com.community.jpremium.common.util.ProfileDataUtils;
import com.community.jpremium.resolver.MojangProfileResolver;
import com.community.jpremium.bungee.command.BungeeLoginCommand;
import com.community.jpremium.bungee.command.BungeePremiumCommand;
import com.community.jpremium.bungee.command.BungeeRegisterCommand;
import com.community.jpremium.bungee.command.BungeeRequestPasswordRecoveryCommand;
import com.community.jpremium.bungee.command.BungeeRequestSecondFactorCommand;
import com.community.jpremium.bungee.command.BungeeStartSessionCommand;
import com.community.jpremium.bungee.command.BungeeUnregisterCommand;
import com.community.jpremium.storage.StorageConfig;
import com.community.jpremium.storage.hikari.MariaDbHikariConnectionFactory;
import com.community.jpremium.storage.hikari.MySqlHikariConnectionFactory;
import com.community.jpremium.storage.hikari.PostgreSqlHikariConnectionFactory;
import com.community.jpremium.common.service.OnlineUserRegistry;
import com.community.jpremium.bungee.service.BungeeServerRoutingService;
import com.community.jpremium.bungee.bootstrap.BungeeCommunityModeGuard;
import com.community.jpremium.common.config.BungeeConfigService;
import com.community.jpremium.security.SecurityRateLimitService;
import com.community.jpremium.storage.StorageType;
import com.community.jpremium.bungee.command.BungeeActivateSecondFactorCommand;
import com.community.jpremium.bungee.command.BungeeChangeEmailAddressCommand;
import com.community.jpremium.bungee.command.BungeeChangePasswordCommand;
import com.community.jpremium.bungee.command.BungeeConfirmCommand;
import com.community.jpremium.bungee.command.BungeeConfirmPasswordRecoveryCommand;
import com.community.jpremium.bungee.command.BungeeCrackedCommand;
import com.community.jpremium.bungee.command.BungeeCreatePasswordCommand;
import com.community.jpremium.bungee.command.BungeeDeactivateSecondFactorCommand;
import com.community.jpremium.bungee.command.BungeeDestroySessionCommand;
import com.community.jpremium.common.model.UserProfileData;
import com.community.jpremium.bungee.bootstrap.BungeeApiBridge;
import com.community.jpremium.proxy.api.JPremiumApi;
import com.community.jpremium.proxy.api.resolver.CustomResolverProvider;
import com.community.jpremium.proxy.api.resolver.Resolver;
import com.community.jpremium.common.runtime.RuntimeDependency;
import com.community.jpremium.common.runtime.RuntimeDependencyManager;
import com.community.jpremium.common.runtime.UrlClassLoaderClasspathInjector;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.io.IOException;
import java.io.UncheckedIOException;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.net.URLClassLoader;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Event;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.api.scheduler.TaskScheduler;
import org.bstats.bungeecord.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JPremium
extends Plugin
{
    private static final Logger SLF4J_LOGGER = LoggerFactory.getLogger(JPremium.class);
    public static final Path PLUGIN_DIRECTORY = Paths.get("plugins/JPremium");
    private static final Path BUNDLED_PROXY_DIRECTORY = Paths.get("resources/proxy");
    private BungeeConfigService config;
    private BungeeConfigService messages;
    private BungeeMessageService messageService;
    private BungeeServerRoutingService routingService;
    private Resolver profileResolver;
    private GoogleAuthenticator googleAuthenticator;
    private BungeeCommunityModeGuard communityModeGuard;
    private static BungeeApiBridge apiBridge;
    private java.util.logging.Logger logger;
    private ProxyServer proxyServer;
    private PluginManager pluginManager;
    private TaskScheduler taskScheduler;
    private ExecutorService executorService;
    private Set<String> weakPasswords;
    private String recoveryTemplateHtml;
    private StorageConfig storageConfig;
    private UserProfileRepository userRepository;
    private RuntimeDependencyManager dependencyManager;
    private final OnlineUserRegistry onlineUserRegistry = new OnlineUserRegistry();

    public BungeeConfigService getConfig() {
        return this.config;
    }

    public BungeeConfigService getMessagesConfig() {
        return this.messages;
    }

    public BungeeMessageService getMessageService() {
        return this.messageService;
    }

    public BungeeServerRoutingService getRoutingService() {
        return this.routingService;
    }

    public Resolver getProfileResolver() {
        return this.profileResolver;
    }

    public GoogleAuthenticator getGoogleAuthenticator() {
        return this.googleAuthenticator;
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
        this.taskScheduler.schedule(this, runnable, interval, interval, timeUnit);
    }

    public void scheduleDelayedTask(Runnable runnable, long delay, TimeUnit timeUnit) {
        this.taskScheduler.schedule(this, runnable, delay, timeUnit);
    }

    public <T extends Event> void fireEventAsync(T event) {
        this.runAsync(() -> this.pluginManager.callEvent(event));
    }

    public String[] getCommandAliases(String commandName) {
        return this.config.getStringList(commandName.concat("CommandAliases")).toArray(String[]::new);
    }

    public void onEnable() {
        this.logger = this.getLogger();
        this.proxyServer = this.getProxy();
        this.pluginManager = this.proxyServer.getPluginManager();
        this.taskScheduler = this.proxyServer.getScheduler();
        this.executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1, new ThreadFactoryBuilder().setNameFormat("JPremium Async Task Executor #%d").build());
        this.logger.info("JPremium is enabling...");
        try {
            this.communityModeGuard = new BungeeCommunityModeGuard(this);
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
            this.config = new BungeeConfigService(this, "configuration.yml");
            this.messages = new BungeeConfigService(this, "messages.yml");
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
            this.messageService = new BungeeMessageService(this);
            this.routingService = new BungeeServerRoutingService(this);
            this.profileResolver = CustomResolverProvider.getResolver().orElseGet(() -> {
                this.logger.info("Using the default resolver for fetching premium profiles.");
                return new MojangProfileResolver(ProfileDataUtils.GSON);
            });
            this.googleAuthenticator = new GoogleAuthenticator();
            apiBridge = new BungeeApiBridge(this);
            JPremiumApi.setApp(apiBridge);
        }
        catch (Throwable throwable) {
            this.logger.severe("Could not load JPremium assets!");
            throwable.printStackTrace();
            this.proxyServer.stop();
            return;
        }
        if (this.pluginManager.getPlugin("BungeeGuard") != null) {
            this.logger.info("BungeeGuard detected! JPremium won't add the access token to the player's handshake!");
        } else if (this.config.getBoolean("accessTokenDisabled")) {
            this.logger.info("The 'accessTokenDisabled' option is enabled! JPremium won't add the access token to the player's handshake!");
        }
        this.pluginManager.registerListener(this, new BungeePreLoginListener(this));
        this.pluginManager.registerListener(this, new BungeeLoginListener(this));
        this.pluginManager.registerListener(this, new BungeePostLoginListener(this));
        this.pluginManager.registerListener(this, new BungeeDisconnectListener(this));
        this.pluginManager.registerListener(this, new BungeeServerConnectListener(this));
        this.pluginManager.registerListener(this, new BungeeServerConnectedListener(this));
        this.pluginManager.registerListener(this, new BungeeServerKickListener(this));
        this.pluginManager.registerListener(this, new BungeeChatGuardListener(this));
        this.pluginManager.registerListener(this, new BungeeTabCompleteGuardListener(this));
        this.pluginManager.registerCommand(this, new BungeeLoginCommand(this));
        this.pluginManager.registerCommand(this, new BungeeRegisterCommand(this));
        this.pluginManager.registerCommand(this, new BungeeUnregisterCommand(this));
        this.pluginManager.registerCommand(this, new BungeeChangePasswordCommand(this));
        this.pluginManager.registerCommand(this, new BungeeCreatePasswordCommand(this));
        this.pluginManager.registerCommand(this, new BungeePremiumCommand(this));
        this.pluginManager.registerCommand(this, new BungeeCrackedCommand(this));
        this.pluginManager.registerCommand(this, new BungeeStartSessionCommand(this));
        this.pluginManager.registerCommand(this, new BungeeDestroySessionCommand(this));
        this.pluginManager.registerCommand(this, new BungeeChangeEmailAddressCommand(this));
        this.pluginManager.registerCommand(this, new BungeeRequestPasswordRecoveryCommand(this));
        this.pluginManager.registerCommand(this, new BungeeConfirmPasswordRecoveryCommand(this));
        this.pluginManager.registerCommand(this, new BungeeActivateSecondFactorCommand(this));
        this.pluginManager.registerCommand(this, new BungeeDeactivateSecondFactorCommand(this));
        this.pluginManager.registerCommand(this, new BungeeRequestSecondFactorCommand(this));
        if (this.config.getBoolean("riskyCommandsConfirmation")) {
            this.pluginManager.registerCommand(this, new BungeeConfirmCommand(this));
        }
        this.pluginManager.registerCommand(this, new BungeeForceLoginCommand(this));
        this.pluginManager.registerCommand(this, new BungeeForceRegisterCommand(this));
        this.pluginManager.registerCommand(this, new BungeeForceUnregisterCommand(this));
        this.pluginManager.registerCommand(this, new BungeeForceChangePasswordCommand(this));
        this.pluginManager.registerCommand(this, new BungeeForceCreatePasswordCommand(this));
        this.pluginManager.registerCommand(this, new BungeeForcePremiumCommand(this));
        this.pluginManager.registerCommand(this, new BungeeForceCrackedCommand(this));
        this.pluginManager.registerCommand(this, new BungeeForceStartSessionCommand(this));
        this.pluginManager.registerCommand(this, new BungeeForceDestroySessionCommand(this));
        this.pluginManager.registerCommand(this, new BungeeForceChangeEmailAddressCommand(this));
        this.pluginManager.registerCommand(this, new BungeeForceRequestPasswordRecoveryCommand(this));
        this.pluginManager.registerCommand(this, new BungeeForceConfirmPasswordRecoveryCommand(this));
        this.pluginManager.registerCommand(this, new BungeeForceRequestSecondFactorCommand(this));
        this.pluginManager.registerCommand(this, new BungeeForceActivateSecondFactorCommand(this));
        this.pluginManager.registerCommand(this, new BungeeForceDeactivateSecondFactorCommand(this));
        this.pluginManager.registerCommand(this, new BungeeForceViewUserProfileCommand(this));
        this.pluginManager.registerCommand(this, new BungeeForceViewUserProfilesCommand(this));
        this.pluginManager.registerCommand(this, new BungeeForcePurgeUserProfileCommand(this));
        this.pluginManager.registerCommand(this, new BungeeForceMergePremiumUserProfileCommand(this));
        this.pluginManager.registerCommand(this, new BungeeReloadCommand(this));
        this.pluginManager.registerCommand(this, new BungeeInfoCommand(this));
        this.proxyServer.registerChannel("jpremium:state");
        new Metrics(this, 7905);
        this.logger.info("JPremium has been enabled!");
    }

    public void onDisable() {
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
            URLClassLoader pluginClassLoader = (URLClassLoader)this.getClass().getClassLoader();
            this.dependencyManager = new RuntimeDependencyManager(
                    PLUGIN_DIRECTORY,
                    this.logger,
                    new UrlClassLoaderClasspathInjector(pluginClassLoader)
            );
            // Download and inject only runtime-only dependencies (Caffeine and HikariCP are now shaded in)
            this.dependencyManager.injectDependencies(Set.of(
                    RuntimeDependency.JAKARTA_MAIL,
                    RuntimeDependency.JAKARTA_ACTIVATION
            ));
            return true;
        } catch (Throwable t) {
            this.logger.severe("Could not download required dependencies! JPremium will not work!");
            t.printStackTrace();
            this.proxyServer.stop();
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
                this.proxyServer.stop();
                return false;
            }
            catch (IOException ioException) {
                this.logger.severe("Could not update configuration.yml with generated access token: " + ioException.getMessage());
                this.proxyServer.stop();
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
                Files.copy(this.getResourceAsStream(bundledResourcePath.toString().replace("\\", "/")), targetPath);
            }
            catch (IOException ioException) {
                throw new UncheckedIOException("Could not extract bundled resource: " + resourceName, ioException);
            }
        }
        return targetPath;
    }

    public ProxiedPlayer findPlayer(UserProfileData userProfile) {
        return this.proxyServer.getPlayer(userProfile.getUniqueId());
    }

    private StorageConfig buildStorageConfig(BungeeConfigService bungeeConfig) {
        StorageConfig storageConfig = new StorageConfig();
        storageConfig.setHostAndPort("%s:%d".formatted(bungeeConfig.getString("storageHost"), bungeeConfig.getInt("storagePort")));
        storageConfig.setUsername(bungeeConfig.getString("storageUser"));
        storageConfig.setPassword(bungeeConfig.getString("storagePassword"));
        storageConfig.setDatabase(bungeeConfig.getString("storageDatabase"));
        storageConfig.setMaximumPoolSize(bungeeConfig.getInt("connectionPoolSize"));
        storageConfig.setMinimumIdle(bungeeConfig.getInt("connectionPoolIdle"));
        storageConfig.setConnectionTimeoutMillis(bungeeConfig.getInt("connectionPoolTimeout"));
        storageConfig.setMaxLifetimeMillis(bungeeConfig.getInt("connectionPoolLifetime"));
        storageConfig.setKeepaliveMillis(bungeeConfig.getInt("connectionKeepAliveTime"));
        storageConfig.setProperties(bungeeConfig.getStringList("storageProperties").stream().map(property -> property.split("=")).collect(Collectors.toMap(arguments -> arguments[0], arguments -> arguments[1])));
        return storageConfig;
    }

    private UserProfileRepository createUserRepository(BungeeConfigService bungeeConfig, StorageConfig storageConfig) {
        StorageType storageType = bungeeConfig.getEnumOrDefault(StorageType.class, "storageType", StorageType.MYSQL);
        SqlUserProfileRepository userRepository = switch (storageType) {
            default -> throw new IncompatibleClassChangeError();
            case SQLITE -> new SqlUserProfileRepository(new SqliteConnectionFactory(PLUGIN_DIRECTORY.resolve("database.db"), this.dependencyManager));
            case MYSQL -> {
                this.dependencyManager.injectDependencies(Set.of(RuntimeDependency.MYSQL_DRIVER));
                yield new SqlUserProfileRepository(new MySqlHikariConnectionFactory(storageConfig));
            }
            case MARIADB -> {
                this.dependencyManager.injectDependencies(Set.of(RuntimeDependency.MARIADB_DRIVER));
                yield new SqlUserProfileRepository(new MariaDbHikariConnectionFactory(storageConfig));
            }
            case POSTGRESQL -> {
                this.dependencyManager.injectDependencies(Set.of(RuntimeDependency.POSTGRESQL_DRIVER));
                yield new SqlUserProfileRepository(new PostgreSqlHikariConnectionFactory(storageConfig));
            }
        };
        userRepository.initialize();
        return userRepository;
    }
}
