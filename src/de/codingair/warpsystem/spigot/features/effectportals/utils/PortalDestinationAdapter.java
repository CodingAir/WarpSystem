package de.codingair.warpsystem.spigot.features.effectportals.utils;

import de.codingair.codingapi.tools.Callback;
import de.codingair.warpsystem.spigot.base.language.Lang;
import de.codingair.warpsystem.spigot.base.listeners.TeleportListener;
import de.codingair.warpsystem.spigot.base.utils.teleport.SimulatedTeleportResult;
import de.codingair.warpsystem.spigot.base.utils.teleport.TeleportResult;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.adapters.DestinationAdapter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class PortalDestinationAdapter implements DestinationAdapter {
    @Override
    public boolean teleport(Player player, String id, Vector randomOffset, String displayName, boolean checkPermission, String message, boolean silent, double costs, Callback<TeleportResult> callback) {
        Location location = buildLocation(id);

        if(location == null) {
            player.sendMessage(Lang.getPrefix() + Lang.get("WARP_DOES_NOT_EXISTS"));
            if(callback != null) callback.accept(TeleportResult.DESTINATION_DOES_NOT_EXIST);
            return false;
        }

        if(location.getWorld() == null) {
            player.sendMessage(Lang.getPrefix() + Lang.get("World_Not_Exists"));
            if(callback != null) callback.accept(TeleportResult.WORLD_DOES_NOT_EXIST);
            return false;
        } else {
            if(silent) TeleportListener.TELEPORTS.put(player, location);
            player.teleport(location);

            if(callback != null) callback.accept(TeleportResult.TELEPORTED);
            return true;
        }
    }

    @Override
    public SimulatedTeleportResult simulate(Player player, String id, boolean checkPermission) {
        return new SimulatedTeleportResult(null, TeleportResult.TELEPORTED);
    }

    @Override
    public double getCosts(String id) {
        return 0;
    }

    @Override
    public de.codingair.codingapi.tools.Location buildLocation(String id) {
        return de.codingair.codingapi.tools.Location.getByJSONString(id);
    }
}
