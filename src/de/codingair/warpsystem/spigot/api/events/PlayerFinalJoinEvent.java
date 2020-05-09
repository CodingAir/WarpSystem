package de.codingair.warpsystem.spigot.api.events;

import de.codingair.warpsystem.spigot.base.listeners.TeleportListener;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import java.util.UUID;

public class PlayerFinalJoinEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();
    private boolean alreadyTeleported = false;

    private UUID uniqueId;

    public PlayerFinalJoinEvent(Player player, UUID uniqueId) {
        super(player);
        this.uniqueId = uniqueId;
        this.alreadyTeleported = TeleportListener.teleport.getIfPresent(player.getName()) != null;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public boolean alreadyTeleported() {
        return alreadyTeleported;
    }
}
