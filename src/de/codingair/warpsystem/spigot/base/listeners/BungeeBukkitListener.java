package de.codingair.warpsystem.spigot.base.listeners;

import de.codingair.codingapi.tools.Location;
import de.codingair.codingapi.tools.time.TimeMap;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.transfer.packets.bungee.InitialPacket;
import de.codingair.warpsystem.transfer.packets.bungee.PrepareLoginMessagePacket;
import de.codingair.warpsystem.transfer.packets.general.PrepareCoordinationTeleportPacket;
import de.codingair.warpsystem.transfer.packets.spigot.IsOperatorPacket;
import de.codingair.warpsystem.transfer.packets.utils.Packet;
import de.codingair.warpsystem.transfer.packets.utils.PacketType;
import de.codingair.warpsystem.transfer.utils.PacketListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.logging.Level;

public class BungeeBukkitListener implements PacketListener, Listener {
    private String[] notice = null;
    private TimeMap<String, String> loginMessage = new TimeMap<>();

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if(e.getPlayer().isOp() && WarpSystem.getInstance().isOnBungeeCord()) {
            WarpSystem.getInstance().getDataHandler().send(new IsOperatorPacket(e.getPlayer().getName(), e.getPlayer().isOp()));
        }

        String message = loginMessage.remove(e.getPlayer());
        if(message != null) e.getPlayer().sendMessage(message);

        Bukkit.getScheduler().runTaskLater(WarpSystem.getInstance(), () -> {
            if(notice != null && (e.getPlayer().hasPermission(WarpSystem.PERMISSION_NOTIFY) || e.getPlayer().isOp())) e.getPlayer().sendMessage(notice);
        }, 20 * 4L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if(Bukkit.getOnlinePlayers().size() <= 1) {
            WarpSystem.getInstance().setOnBungeeCord(false);
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        String cmd = e.getMessage().toLowerCase();
        if(cmd.startsWith("/")) cmd = cmd.substring(1);

        if((cmd.startsWith("deop") || cmd.startsWith("op")) && cmd.contains(" ")) {
            String player = cmd.split(" ")[1];

            Player p = Bukkit.getPlayer(player);

            if(p != null) {
                boolean op = p.isOp();

                Bukkit.getScheduler().runTaskLater(WarpSystem.getInstance(), () -> {
                    if(op != p.isOp()) {
                        WarpSystem.getInstance().getDataHandler().send(new IsOperatorPacket(p.getName(), p.isOp()));
                    }
                }, 20);
            }
        }
    }

    @Override
    public void onReceive(Packet packet, String extra) {
        switch(PacketType.getByObject(packet)) {
            case InitialPacket: {
                WarpSystem.getInstance().setCurrentServer(((InitialPacket) packet).getServerName());

                String version = ((InitialPacket) packet).getVersion();
                if(version.equals(WarpSystem.getInstance().getDescription().getVersion())) {
                    if(WarpSystem.getInstance().getBungeePluginVersion() == null || !WarpSystem.getInstance().getBungeePluginVersion().equals(version)) {
                        WarpSystem.getInstance().getLogger().log(Level.INFO, "Found a valid DataCenter > Init BungeeFeatures");
                    }

                    this.notice = null;
                    WarpSystem.getInstance().setOnBungeeCord(true);

                    for(Player player : Bukkit.getOnlinePlayers()) {
                        if(!player.isOp()) continue;
                        WarpSystem.getInstance().getDataHandler().send(new IsOperatorPacket(player.getName(), player.isOp()));
                    }

                } else if(WarpSystem.getInstance().getBungeePluginVersion() == null || WarpSystem.getInstance().getBungeePluginVersion().equals(WarpSystem.getInstance().getDescription().getVersion())) {
                    this.notice = new String[] {
                            "",
                            "§c§l§nWarpSystem",
                            "",
                            "§7WarpSystem §cversion §7of the DataCenter and the SpigotServer are §cdifferent§7!",
                            "§7Please §cupdate §7the WarpSystem §con your BungeeCord §7(" + version + ") §cand on this Server §7(" + WarpSystem.getInstance().getDescription().getVersion() + ")",
                            ""
                    };
                    WarpSystem.getInstance().getLogger().log(Level.WARNING, "WarpSystem version of the DataCenter and the SpigotServer are different!");
                    WarpSystem.getInstance().getLogger().log(Level.INFO, "Please update the WarpSystem on your BungeeCord (" + version + ") and on this Server (" + WarpSystem.getInstance().getDescription().getVersion() + ")");
                }

                WarpSystem.getInstance().setBungeePluginVersion(version);
                break;
            }

            case PrepareLoginMessagePacket: {
                PrepareLoginMessagePacket p = (PrepareLoginMessagePacket) packet;

                if(Bukkit.getPlayer(p.getPlayer()) != null) {
                    Bukkit.getPlayer(p.getPlayer()).sendMessage(p.getMessage());
                } else loginMessage.put(p.getPlayer(), p.getMessage(), 10);
                break;
            }

            case PrepareCoordinationTeleportPacket: {
                PrepareCoordinationTeleportPacket p = (PrepareCoordinationTeleportPacket) packet;
                TeleportListener.setSpawnPositionOrTeleport(p.getPlayer(), new Location(p.getWorld(), p.getX(), p.getY(), p.getZ(), p.getYaw(), p.getPitch()), p.getDestinationName(), p.getMessage());
                break;
            }
        }
    }

    @Override
    public boolean onSend(Packet packet) {
        return false;
    }
}
