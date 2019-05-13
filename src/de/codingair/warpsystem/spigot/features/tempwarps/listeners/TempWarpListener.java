package de.codingair.warpsystem.spigot.features.tempwarps.listeners;

import de.codingair.warpsystem.spigot.api.events.PlayerFinalJoinEvent;
import de.codingair.warpsystem.spigot.features.tempwarps.managers.TempWarpManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class TempWarpListener implements Listener {

    @EventHandler
    public void onJoin(PlayerFinalJoinEvent e) {
        TempWarpManager.getManager().updateWarps(e.getPlayer());
        TempWarpManager.getManager().loadKeys(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        TempWarpManager.getManager().saveAndRemoveKeys(e.getPlayer());
    }

}
