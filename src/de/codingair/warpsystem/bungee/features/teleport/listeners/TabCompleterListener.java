package de.codingair.warpsystem.bungee.features.teleport.listeners;

import de.codingair.warpsystem.bungee.api.Players;
import de.codingair.warpsystem.bungee.base.WarpSystem;
import de.codingair.warpsystem.bungee.features.teleport.managers.TeleportManager;
import de.codingair.warpsystem.bungee.features.teleport.utils.TeleportCommandOptions;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.TabCompleteResponseEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class TabCompleterListener implements Listener {
    public static final String ID = "§WS-TP";
    public static final String ACCESS = "§WS-TP-Access";

    @EventHandler
    public void onResponse(TabCompleteResponseEvent e) {
        if(e.getSuggestions().isEmpty()) return;

        if(e.getSuggestions().remove(ID)) {
            boolean hasAccess = e.getSuggestions().remove(ACCESS);
            String cursor = e.getSuggestions().isEmpty() ? "" : e.getSuggestions().remove(0);

            String cmd = cursor.split(" ")[0];
            String[] args = cursor.split(" ");

            ProxiedPlayer receiver = (ProxiedPlayer) e.getReceiver();
            ServerInfo info = receiver.getServer().getInfo();

            if(cmd.equalsIgnoreCase("teleport") || cmd.equalsIgnoreCase("tp")) {
                e.getSuggestions().clear();
                if(!hasAccess) {
                    for(ProxiedPlayer player : info.getPlayers()) {
                        e.getSuggestions().add(player.getName());
                    }
                    return;
                }

                int deep = args.length - 1;

                if(cursor.endsWith(" ")) {
                    if(deep == 1 && Character.isDigit(args[1].charAt(0)) && Players.getPlayer(args[1]) == null) return;
                    if(deep == 0 || deep == 1) {
                        for(ServerInfo server : BungeeCord.getInstance().getServers().values()) {
                            for(ProxiedPlayer player : server.getPlayers()) {
                                e.getSuggestions().add(player.getName());
                            }
                        }
                    }
                } else {
                    if(deep == 1 || deep == 2) {
                        String last = args[deep];

                        for(ServerInfo server : BungeeCord.getInstance().getServers().values()) {
                            for(ProxiedPlayer player : server.getPlayers()) {
                                if(!player.getName().toLowerCase().startsWith(last.toLowerCase())) continue;
                                e.getSuggestions().add(player.getName());
                            }
                        }
                    }
                }
            } else if(cmd.equalsIgnoreCase("tpa")) {
                e.getSuggestions().clear();
                if(!hasAccess) {
                    for(ProxiedPlayer player : info.getPlayers()) {
                        if(player.getName().equals(receiver.getName())) continue;
                        if(!WarpSystem.getVanishManager().isVanished(player.getName())) e.getSuggestions().add(player.getName()); //check vanished player names
                    }
                    return;
                }

                String last = args[args.length - 1];

                for(ServerInfo server : BungeeCord.getInstance().getServers().values()) {
                    for(ProxiedPlayer player : server.getPlayers()) {
                        if(player.getName().equals(receiver.getName())) continue;
                        if(!cursor.endsWith(" ") && !player.getName().toLowerCase().startsWith(last.toLowerCase())) continue;
                        if(!WarpSystem.getVanishManager().isVanished(player.getName())) e.getSuggestions().add(player.getName()); //check vanished player names
                    }
                }
            } else if(cmd.equalsIgnoreCase("tpahere")) {
                e.getSuggestions().clear();
                if(!hasAccess) {
                    for(ProxiedPlayer player : info.getPlayers()) {
                        if(player.getName().equals(receiver.getName())) continue;
                        if(!WarpSystem.getVanishManager().isVanished(player.getName())) e.getSuggestions().add(player.getName()); //check vanished player names
                    }
                    return;
                }
                String last = args[args.length - 1];

                for(ServerInfo server : BungeeCord.getInstance().getServers().values()) {
                    for(ProxiedPlayer player : server.getPlayers()) {
                        if(player.getName().equals(receiver.getName())) continue;
                        if(!cursor.endsWith(" ") && !player.getName().toLowerCase().startsWith(last.toLowerCase())) continue;
                        if(!WarpSystem.getVanishManager().isVanished(player.getName())) e.getSuggestions().add(player.getName()); //check vanished player names
                    }
                }
            } else if(cmd.equalsIgnoreCase("tphere")) {
                e.getSuggestions().clear();
                if(!hasAccess) {
                    for(ProxiedPlayer player : info.getPlayers()) {
                        e.getSuggestions().add(player.getName());
                    }
                    return;
                }

                String last = args[args.length - 1];

                for(ServerInfo server : BungeeCord.getInstance().getServers().values()) {
                    for(ProxiedPlayer player : server.getPlayers()) {
                        if(!cursor.endsWith(" ") && !player.getName().toLowerCase().startsWith(last.toLowerCase())) continue;
                        e.getSuggestions().add(player.getName());
                    }
                }
            }
        }
    }
}
