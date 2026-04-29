package com.community.jpremium.backend.listener;

import com.community.jpremium.backend.service.BackendStateRegistry;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Listener;

public abstract class AbstractBackendRestrictionListener
implements Listener {
    private final BackendStateRegistry stateRegistry;

    public AbstractBackendRestrictionListener(BackendStateRegistry stateRegistry) {
        this.stateRegistry = stateRegistry;
    }

    protected boolean isUnauthorizedEntity(Entity entity) {
        return entity instanceof Player && this.stateRegistry.find(entity.getUniqueId()).filter(state -> !state.getPlayer().hasMetadata("NPC")).filter(state -> !state.getUserState().allowsBackendAccess()).isPresent();
    }

    protected void cancelIfUnauthorized(Cancellable cancellable, Entity entity) {
        if (this.isUnauthorizedEntity(entity)) {
            cancellable.setCancelled(true);
        }
    }
}

