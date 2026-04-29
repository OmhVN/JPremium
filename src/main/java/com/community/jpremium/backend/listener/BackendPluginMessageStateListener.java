package com.community.jpremium.backend.listener;

import com.community.jpremium.backend.service.BackendStateRegistry;
import com.community.jpremium.common.util.ReflectionUtils;
import com.community.jpremium.backend.config.BackendConfigService;
import com.community.jpremium.common.model.UserProfileData;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BackendPluginMessageStateListener
implements PluginMessageListener {
    private static final String GENERIC_KICK_MESSAGE = "An unexpected error occurred! Please join to the server again! If the error still occur, please contact with the staff!";
    private static final String DECODE_WARNING_TEMPLATE = "An unexpected exception occurred during decoding the plugin channel message from %s connection!";
    private static final String ACCESS_TOKEN_WARNING_TEMPLATE = "The server received an incorrect access token (%s) from %s connection!";
    private final Logger logger;
    private final BackendStateRegistry stateRegistry;
    private final BackendConfigService backendConfig;
    private final ItemStack captchaItem;

    public BackendPluginMessageStateListener(Logger logger, BackendStateRegistry stateRegistry, BackendConfigService backendConfig, ItemStack captchaItem) {
        this.logger = logger;
        this.stateRegistry = stateRegistry;
        this.backendConfig = backendConfig;
        this.captchaItem = captchaItem;
    }

    public void onPluginMessageReceived(String channel, Player player, byte[] payload) {
        try {
            this.handleStateMessage(player, payload);
        }
        catch (Throwable throwable) {
            this.disconnectWithWarning(player, DECODE_WARNING_TEMPLATE, player.getName());
            throwable.printStackTrace();
        }
    }

    private void handleStateMessage(Player player, byte[] payload) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(payload);
        try {
            DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);
            String expectedAccessToken = this.backendConfig.getAccessToken();
            String receivedAccessToken = dataInputStream.readUTF();
            String userStateName = dataInputStream.readUTF();
            String captchaCode = dataInputStream.readUTF();
            String profileJson = dataInputStream.readUTF();
            if (!expectedAccessToken.equals(receivedAccessToken)) {
                this.disconnectWithWarning(player, ACCESS_TOKEN_WARNING_TEMPLATE, ReflectionUtils.encodeBase64(receivedAccessToken), player.getName());
                return;
            }
            BackendStateRegistry.AuthState playerState = this.stateRegistry.find(player.getUniqueId()).orElseThrow(() -> new NullPointerException("A user object is not exist!"));
            playerState.setUserState(UserProfileData.AuthState.valueOf(userStateName));
            playerState.setCaptchaCode(captchaCode);
            playerState.setProfileJson(profileJson);
            this.applyMovementState(player, playerState.getUserState());
            this.applyBlindnessState(player, playerState.getUserState());
            this.applyCaptchaSlotState(player, playerState.getUserState());
        }
        catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private void disconnectWithWarning(Player player, String warningTemplate, Object ... warningArguments) {
        player.kickPlayer(GENERIC_KICK_MESSAGE);
        this.logger.warning(String.format(warningTemplate, warningArguments));
        this.stateRegistry.find(player.getUniqueId()).ifPresent(this.stateRegistry::remove);
    }

    private void applyMovementState(Player player, UserProfileData.AuthState authState) {
        if (this.backendConfig.isMovementRestricted()) {
            player.setWalkSpeed(authState.allowsBackendAccess() ? 0.2f : 0.0f);
            player.setFlySpeed(authState.allowsBackendAccess() ? 0.1f : 0.0f);
        }
    }

    private void applyBlindnessState(Player player, UserProfileData.AuthState authState) {
        if (this.backendConfig.isBlindnessEnabled()) {
            if (authState.allowsBackendAccess()) {
                player.removePotionEffect(PotionEffectType.BLINDNESS);
            } else {
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 50000, 1));
            }
        }
    }

    private void applyCaptchaSlotState(Player player, UserProfileData.AuthState authState) {
        int captchaMapSlot = this.backendConfig.getCaptchaMapSlot();
        if (captchaMapSlot >= 0) {
            PlayerInventory playerInventory = player.getInventory();
            ItemStack currentItem = playerInventory.getItem(captchaMapSlot);
            if (authState.equals(UserProfileData.AuthState.UNREGISTERED)) {
                playerInventory.setItem(captchaMapSlot, this.captchaItem);
                playerInventory.setHeldItemSlot(captchaMapSlot);
            } else if (currentItem != null && currentItem.equals(this.captchaItem)) {
                playerInventory.setItem(captchaMapSlot, new ItemStack(Material.AIR));
            }
        }
    }
}
