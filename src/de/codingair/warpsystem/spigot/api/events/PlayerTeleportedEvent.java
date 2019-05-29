package de.codingair.warpsystem.spigot.api.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class PlayerTeleportedEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    public PlayerTeleportedEvent(Player who) {
        super(who);
    }

    @Override
    public HandlerList getHandlers() {
        return getHandlerList();
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
