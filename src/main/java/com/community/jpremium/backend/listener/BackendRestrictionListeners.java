package com.community.jpremium.backend.listener;

import com.community.jpremium.backend.listener.AbstractBackendRestrictionListener;
import com.community.jpremium.backend.service.BackendStateRegistry;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class BackendRestrictionListeners {
    private BackendRestrictionListeners() {
        throw new AssertionError();
    }

    public static class ModernInteractionRestrictionListener
    extends AbstractBackendRestrictionListener {
        public ModernInteractionRestrictionListener(BackendStateRegistry stateRegistry) {
            super(stateRegistry);
        }

        @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
        public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent playerSwapHandItemsEvent) {
            this.cancelIfUnauthorized((Cancellable)playerSwapHandItemsEvent, (Entity)playerSwapHandItemsEvent.getPlayer());
        }

        @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
        public void onEntityToggleGlide(EntityToggleGlideEvent entityToggleGlideEvent) {
            this.cancelIfUnauthorized((Cancellable)entityToggleGlideEvent, entityToggleGlideEvent.getEntity());
        }
    }

    public static class LegacyInteractionRestrictionListener
    extends AbstractBackendRestrictionListener {
        public LegacyInteractionRestrictionListener(BackendStateRegistry stateRegistry) {
            super(stateRegistry);
        }

        @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
        public void onBlockBreak(BlockBreakEvent blockBreakEvent) {
            this.cancelIfUnauthorized((Cancellable)blockBreakEvent, (Entity)blockBreakEvent.getPlayer());
        }

        @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
        public void onBlockPlace(BlockPlaceEvent blockPlaceEvent) {
            this.cancelIfUnauthorized((Cancellable)blockPlaceEvent, (Entity)blockPlaceEvent.getPlayer());
        }

        @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
        public void onEntityDamage(EntityDamageEvent entityDamageEvent) {
            this.cancelIfUnauthorized((Cancellable)entityDamageEvent, entityDamageEvent.getEntity());
        }

        @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
        public void onEntityDamageByEntity(EntityDamageByEntityEvent entityDamageByEntityEvent) {
            this.cancelIfUnauthorized((Cancellable)entityDamageByEntityEvent, entityDamageByEntityEvent.getDamager());
        }

        @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
        public void onEntityTargetLivingEntity(EntityTargetLivingEntityEvent entityTargetLivingEntityEvent) {
            this.cancelIfUnauthorized((Cancellable)entityTargetLivingEntityEvent, (Entity)entityTargetLivingEntityEvent.getTarget());
        }

        @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
        public void onFoodLevelChange(FoodLevelChangeEvent foodLevelChangeEvent) {
            this.cancelIfUnauthorized((Cancellable)foodLevelChangeEvent, (Entity)foodLevelChangeEvent.getEntity());
        }

        @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
        public void onInventoryOpen(InventoryOpenEvent inventoryOpenEvent) {
            this.cancelIfUnauthorized((Cancellable)inventoryOpenEvent, (Entity)inventoryOpenEvent.getPlayer());
        }

        @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
        public void onInventoryClick(InventoryClickEvent inventoryClickEvent) {
            this.cancelIfUnauthorized((Cancellable)inventoryClickEvent, (Entity)inventoryClickEvent.getWhoClicked());
        }

        @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
        public void onAsyncPlayerChat(AsyncPlayerChatEvent asyncPlayerChatEvent) {
            this.cancelIfUnauthorized((Cancellable)asyncPlayerChatEvent, (Entity)asyncPlayerChatEvent.getPlayer());
        }

        @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
        public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent playerCommandPreprocessEvent) {
            this.cancelIfUnauthorized((Cancellable)playerCommandPreprocessEvent, (Entity)playerCommandPreprocessEvent.getPlayer());
        }

        @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
        public void onPlayerInteract(PlayerInteractEvent playerInteractEvent) {
            this.cancelIfUnauthorized((Cancellable)playerInteractEvent, (Entity)playerInteractEvent.getPlayer());
        }

        @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
        public void onPlayerInteractEntity(PlayerInteractEntityEvent playerInteractEntityEvent) {
            this.cancelIfUnauthorized((Cancellable)playerInteractEntityEvent, (Entity)playerInteractEntityEvent.getPlayer());
        }

        @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
        public void onPlayerItemHeld(PlayerItemHeldEvent playerItemHeldEvent) {
            this.cancelIfUnauthorized((Cancellable)playerItemHeldEvent, (Entity)playerItemHeldEvent.getPlayer());
        }

        @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
        public void onPlayerDropItem(PlayerDropItemEvent playerDropItemEvent) {
            this.cancelIfUnauthorized((Cancellable)playerDropItemEvent, (Entity)playerDropItemEvent.getPlayer());
        }

        @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
        public void onPlayerPickupItem(PlayerPickupItemEvent playerPickupItemEvent) {
            this.cancelIfUnauthorized((Cancellable)playerPickupItemEvent, (Entity)playerPickupItemEvent.getPlayer());
        }
    }
}

