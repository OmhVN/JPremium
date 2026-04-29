package com.community.jpremium.backend.listener;

import com.community.jpremium.backend.listener.AbstractBackendRestrictionListener;
import com.community.jpremium.backend.service.BackendStateRegistry;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;

public class BackendMoveRestrictionListener
extends AbstractBackendRestrictionListener {
    public BackendMoveRestrictionListener(BackendStateRegistry stateRegistry) {
        super(stateRegistry);
    }

    @EventHandler(priority=EventPriority.HIGH, ignoreCancelled=true)
    public void onPlayerMove(PlayerMoveEvent playerMoveEvent) {
        Location location = playerMoveEvent.getFrom();
        Location location2 = playerMoveEvent.getTo();
        if (location.getX() == location2.getX() && location.getY() == location2.getY() && location.getZ() == location2.getZ()) {
            return;
        }
        if (this.isUnauthorizedEntity((Entity)playerMoveEvent.getPlayer())) {
            location.setYaw(location2.getYaw());
            location.setPitch(location2.getPitch());
            playerMoveEvent.setTo(location);
        }
    }
}

