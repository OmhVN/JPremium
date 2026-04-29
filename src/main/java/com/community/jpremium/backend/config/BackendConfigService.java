package com.community.jpremium.backend.config;

import com.community.jpremium.backend.util.BackendTextUtils;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class BackendConfigService {
    private static final Path BUNDLED_RESOURCE_DIRECTORY = Paths.get("resources/backend");
    private static final Path PLUGIN_DATA_DIRECTORY = Paths.get("plugins/JPremium");
    private static final Path CONFIG_FILE_NAME = Paths.get("configuration.yml");
    private static final Path CAPTCHA_IMAGE_FILE_NAME = Paths.get("image.png");
    private BufferedImage captchaImage;
    private String accessToken;
    private String spawnLocation;
    private String captchaMessage;
    private String disconnectionMessage;
    private boolean accessTokenDisabled;
    private boolean restrictedMovement;
    private boolean restrictedInteractions;
    private boolean blindnessEffect;
    private int captchaMapSlot;

    public BufferedImage getCaptchaImage() {
        return this.captchaImage;
    }

    public String getAccessToken() {
        return this.accessToken;
    }

    public String getSpawnLocation() {
        return this.spawnLocation;
    }

    public String getCaptchaMessage() {
        return this.captchaMessage;
    }

    public String getDisconnectionMessage() {
        return this.disconnectionMessage;
    }

    public boolean isAccessTokenDisabled() {
        return this.accessTokenDisabled;
    }

    public boolean isMovementRestricted() {
        return this.restrictedMovement;
    }

    public boolean isInteractionRestricted() {
        return this.restrictedInteractions;
    }

    public boolean isBlindnessEnabled() {
        return this.blindnessEffect;
    }

    public int getCaptchaMapSlot() {
        return this.captchaMapSlot;
    }

    public BackendConfigService() {
        this.ensureBackendResources();
        this.loadBackendConfig();
    }

    private void ensureBackendResources() {
        try {
            Path configPath = PLUGIN_DATA_DIRECTORY.resolve(CONFIG_FILE_NAME);
            Path captchaImagePath = PLUGIN_DATA_DIRECTORY.resolve(CAPTCHA_IMAGE_FILE_NAME);
            if (Files.notExists(PLUGIN_DATA_DIRECTORY)) {
                Files.createDirectories(PLUGIN_DATA_DIRECTORY);
            }
            if (Files.notExists(configPath)) {
                try (InputStream inputStream = this.openBundledResource(CONFIG_FILE_NAME)) {
                    if (inputStream == null) {
                        throw new IOException("Missing embedded resource: " + CONFIG_FILE_NAME);
                    }
                    Files.copy(inputStream, configPath);
                }
            }
            if (Files.notExists(captchaImagePath)) {
                try (InputStream inputStream = this.openBundledResource(CAPTCHA_IMAGE_FILE_NAME)) {
                    if (inputStream == null) {
                        throw new IOException("Missing embedded resource: " + CAPTCHA_IMAGE_FILE_NAME);
                    }
                    Files.copy(inputStream, captchaImagePath);
                }
            }
        }
        catch (IOException ioException) {
            throw new IllegalStateException("Could not initialize backend resources", ioException);
        }
    }

    private InputStream openBundledResource(Path resourceFileName) {
        return this.getClass().getClassLoader().getResourceAsStream(BUNDLED_RESOURCE_DIRECTORY.resolve(resourceFileName).toString().replace("\\", "/"));
    }

    private FileConfiguration loadFileConfiguration() {
        return YamlConfiguration.loadConfiguration(PLUGIN_DATA_DIRECTORY.resolve(CONFIG_FILE_NAME).toFile());
    }

    private void loadBackendConfig() {
        try {
            FileConfiguration fileConfiguration = this.loadFileConfiguration();
            this.captchaImage = ImageIO.read(PLUGIN_DATA_DIRECTORY.resolve(CAPTCHA_IMAGE_FILE_NAME).toFile());
            this.accessToken = fileConfiguration.getString("accessToken", "");
            this.spawnLocation = fileConfiguration.getString("spawnLocation", "");
            this.captchaMessage = BackendTextUtils.sanitizeCaptchaMessage(fileConfiguration.getString("captchaMessage", "Your captcha is %captcha_code%"));
            this.disconnectionMessage = BackendTextUtils.translateColorCodes(fileConfiguration.getString("disconnectionMessage", "&8[&c&l\u00bb&8] &7Please join to the server via &amc.example.com&7!"));
            this.accessTokenDisabled = fileConfiguration.getBoolean("accessTokenDisabled", false);
            this.restrictedMovement = fileConfiguration.getBoolean("restrictedMovement", true);
            this.restrictedInteractions = fileConfiguration.getBoolean("restrictedInteractions", true);
            this.blindnessEffect = fileConfiguration.getBoolean("blindnessEffect", true);
            this.captchaMapSlot = BackendTextUtils.normalizeMapSlot(fileConfiguration.getInt("captchaMapSlot", 0));
        }
        catch (IOException ioException) {
            throw new IllegalStateException("Could not load backend configuration assets", ioException);
        }
    }
}
