package com.community.jpremium.backend.listener;

import com.community.jpremium.backend.service.BackendStateRegistry;
import com.community.jpremium.backend.config.BackendConfigService;
import com.community.jpremium.backend.util.BackendTextUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;

public class BackendPlayerLoginListener
implements Listener {
    private final BackendStateRegistry stateRegistry;
    private final BackendConfigService backendConfig;
    private final ItemStack captchaItem;

    public BackendPlayerLoginListener(BackendStateRegistry stateRegistry, BackendConfigService backendConfig, ItemStack itemStack) {
        this.stateRegistry = stateRegistry;
        this.backendConfig = backendConfig;
        this.captchaItem = itemStack;
    }

    @EventHandler(priority=EventPriority.LOWEST)
    public void onPlayerLoginStart(PlayerLoginEvent playerLoginEvent) {
        this.stateRegistry.add(BackendStateRegistry.AuthState.fromPlayer(playerLoginEvent.getPlayer()));
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void onPlayerLoginResult(PlayerLoginEvent playerLoginEvent) {
        if (!playerLoginEvent.getResult().equals(PlayerLoginEvent.Result.ALLOWED)) {
            this.stateRegistry.find(playerLoginEvent.getPlayer().getUniqueId()).ifPresent(this.stateRegistry::remove);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent playerJoinEvent) {
        Location spawnLocation = BackendTextUtils.parseLocation(this.backendConfig.getSpawnLocation());
        if (spawnLocation != null) {
            playerJoinEvent.getPlayer().teleport(spawnLocation);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent playerQuitEvent) {
        Player player = playerQuitEvent.getPlayer();
        this.restoreMovement(player);
        this.restoreBlindness(player);
        this.removeCaptchaItem(player);
        this.stateRegistry.find(player.getUniqueId()).ifPresent(this.stateRegistry::remove);
    }

    private void restoreMovement(Player player) {
        if (this.backendConfig.isMovementRestricted()) {
            player.setWalkSpeed(0.2f);
            player.setFlySpeed(0.1f);
        }
    }

    private void restoreBlindness(Player player) {
        if (this.backendConfig.isBlindnessEnabled()) {
            player.removePotionEffect(PotionEffectType.BLINDNESS);
        }
    }

    private void removeCaptchaItem(Player player) {
        int captchaMapSlot = this.backendConfig.getCaptchaMapSlot();
        if (captchaMapSlot < 0) {
            return;
        }
        PlayerInventory inventory = player.getInventory();
        ItemStack item = inventory.getItem(captchaMapSlot);
        if (item != null && item.equals(this.captchaItem)) {
            inventory.setItem(captchaMapSlot, new ItemStack(Material.AIR));
        }
    }
}
