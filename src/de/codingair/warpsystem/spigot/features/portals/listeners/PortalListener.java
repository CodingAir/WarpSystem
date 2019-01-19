package de.codingair.warpsystem.spigot.features.portals.listeners;

import de.codingair.codingapi.API;
import de.codingair.codingapi.server.events.PlayerWalkEvent;
import de.codingair.codingapi.tools.Callback;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.language.Lang;
import de.codingair.warpsystem.spigot.features.FeatureType;
import de.codingair.warpsystem.spigot.features.portals.PortalDeleteGUI;
import de.codingair.warpsystem.spigot.features.portals.PortalEditor;
import de.codingair.warpsystem.spigot.features.portals.commands.CPortal;
import de.codingair.warpsystem.spigot.features.portals.managers.PortalManager;
import de.codingair.warpsystem.spigot.features.portals.utils.Portal;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PortalListener implements Listener {

    @EventHandler
    public void onWalk(PlayerWalkEvent e) {
        PortalManager manager = WarpSystem.getInstance().getDataManager().getManager(FeatureType.PORTALS);

        Player player = e.getPlayer();

        boolean aboutToEdit = CPortal.aboutToEdit.contains(player.getName());
        boolean aboutToDelete = CPortal.aboutToDelete.contains(player.getName());

        for(Portal portal : API.getRemovables(Portal.class)) {
            if(!portal.isRegistered()) continue;

            try {
                if(player.getWorld() == portal.getStart().getWorld() && e.getFrom().distance(portal.getStart()) > portal.getTeleportRadius() && e.getTo().distance(portal.getStart()) <= portal.getTeleportRadius()) {

                    if(aboutToEdit) {
                        CPortal.aboutToEdit.remove(player.getName());
                        new PortalEditor(player, portal).start();
                        return;
                    } else if(aboutToDelete) {
                        CPortal.aboutToDelete.remove(player.getName());
                        new PortalDeleteGUI(player, new Callback<Boolean>() {
                            @Override
                            public void accept(Boolean delete) {
                                if(delete) {
                                    manager.getPortals().remove(portal);
                                    portal.setRunning(false);

                                    player.sendMessage(Lang.getPrefix() + Lang.get("Delete_Portal_Successfully"));
                                } else {
                                    player.sendMessage(Lang.getPrefix() + Lang.get("Delete_Portal_Cancel"));
                                }
                            }
                        }).open();
                        return;
                    }

                    if(!player.hasPermission(WarpSystem.PERMISSION_USE_PORTALS)) {
                        player.sendMessage(Lang.getPrefix() + Lang.get("No_Permission"));
                        return;
                    } else portal.teleportToDestination(player);
                } else if(player.getWorld() == portal.getDestination().getWorld() && e.getFrom().distance(portal.getDestination()) > portal.getTeleportRadius() && e.getTo().distance(portal.getDestination()) <= portal.getTeleportRadius()) {

                    if(aboutToEdit) {
                        CPortal.aboutToEdit.remove(player.getName());
                        new PortalEditor(player, portal).start();
                        return;
                    } else if(aboutToDelete) {
                        CPortal.aboutToDelete.remove(player.getName());
                        new PortalDeleteGUI(player, new Callback<Boolean>() {
                            @Override
                            public void accept(Boolean delete) {
                                if(delete) {
                                    manager.getPortals().remove(portal);
                                    portal.setRunning(false);

                                    player.sendMessage(Lang.getPrefix() + Lang.get("Delete_Portal_Successfully"));
                                } else {
                                    player.sendMessage(Lang.getPrefix() + Lang.get("Delete_Portal_Cancel"));
                                }
                            }
                        }).open();
                        return;
                    }

                    if(!player.hasPermission(WarpSystem.PERMISSION_USE_PORTALS)) {
                        player.sendMessage(Lang.getPrefix() + Lang.get("No_Permission"));
                        return;
                    } else portal.teleportToStart(player);
                }
            } catch(IllegalArgumentException ignored) {

            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {
        for(Portal portal : API.getRemovables(Portal.class)) {
            portal.add(e.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent e) {
        for(PortalEditor portalEditor : API.getRemovables(e.getPlayer(), PortalEditor.class)) {
            portalEditor.exit();
        }

        for(Portal portal : API.getRemovables(Portal.class)) {
            portal.remove(e.getPlayer());
        }
    }

}
