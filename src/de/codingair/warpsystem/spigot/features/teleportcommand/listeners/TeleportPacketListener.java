package de.codingair.warpsystem.spigot.features.teleportcommand.listeners;

import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.utils.ChatColor;
import de.codingair.warpsystem.spigot.api.players.BungeePlayer;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.language.Lang;
import de.codingair.warpsystem.spigot.base.listeners.TeleportListener;
import de.codingair.warpsystem.spigot.base.utils.teleport.Origin;
import de.codingair.warpsystem.spigot.base.utils.teleport.TeleportOptions;
import de.codingair.warpsystem.spigot.base.utils.teleport.Result;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.Destination;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.adapters.EmptyAdapter;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.adapters.LocationAdapter;
import de.codingair.warpsystem.spigot.features.teleportcommand.Invitation;
import de.codingair.warpsystem.spigot.features.teleportcommand.TeleportCommandManager;
import de.codingair.warpsystem.spigot.features.teleportcommand.packets.PrepareTeleportRequestPacket;
import de.codingair.warpsystem.spigot.features.teleportcommand.packets.TeleportRequestHandledPacket;
import de.codingair.warpsystem.spigot.features.teleportcommand.packets.ToggleForceTeleportsPacket;
import de.codingair.warpsystem.transfer.packets.bungee.TeleportPlayerToCoordsPacket;
import de.codingair.warpsystem.transfer.packets.bungee.TeleportPlayerToPlayerPacket;
import de.codingair.warpsystem.transfer.packets.general.IntegerPacket;
import de.codingair.warpsystem.transfer.packets.general.LongPacket;
import de.codingair.warpsystem.transfer.packets.general.StartTeleportToPlayerPacket;
import de.codingair.warpsystem.transfer.packets.spigot.PrepareTeleportPlayerToPlayerPacket;
import de.codingair.warpsystem.transfer.packets.utils.Packet;
import de.codingair.warpsystem.transfer.packets.utils.PacketType;
import de.codingair.warpsystem.transfer.utils.PacketListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.text.DecimalFormat;

public class TeleportPacketListener implements Listener, PacketListener {

    @Override
    public void onReceive(Packet packet, String extra) {
        switch(PacketType.getByObject(packet)) {
            case TeleportPlayerToPlayerPacket: {
                TeleportPlayerToPlayerPacket tpPacket = (TeleportPlayerToPlayerPacket) packet;

                Player gate = Bukkit.getPlayer(tpPacket.getGate());
                Player player = Bukkit.getPlayer(tpPacket.getPlayer());
                Player other = Bukkit.getPlayer(tpPacket.getTarget());

                if(other == null) return;

                TeleportOptions options = new TeleportOptions(new Destination(new LocationAdapter(other.getLocation())), other.getName());
                options.setCosts(Math.max(tpPacket.getCosts(), 0));
                options.setSkip(true);
                options.setConfirmPayment(false);
                options.setOrigin(Origin.TeleportCommand);
                options.setMessage(Lang.getPrefix() + (gate == player ? Lang.get("Teleported_To") : Lang.get("Teleported_To_By").replace("%gate%", gate.getName())));

                if(gate != null && gate != player && tpPacket.isMessageToGate())
                    gate.sendMessage(Lang.getPrefix() + Lang.get("Teleported_Player_Info").replace("%player%", tpPacket.getPlayer()).replace("%warp%", other.getName()));

                TeleportListener.setSpawnPositionOrTeleport(tpPacket.getPlayer(), options);
                break;
            }

            case PrepareTeleportRequestPacket: {
                PrepareTeleportRequestPacket tpPacket = (PrepareTeleportRequestPacket) packet;

                TeleportCommandManager.getInstance().invite(tpPacket.getSender(), tpPacket.isTpToSender(), new Callback<Long>() {
                    @Override
                    public void accept(Long result) {
                        LongPacket answer = new LongPacket(result);
                        tpPacket.applyAsAnswer(answer);
                        WarpSystem.getInstance().getDataHandler().send(answer);
                    }
                }, tpPacket.getRecipient(), true);
                break;
            }

            case StartTeleportToPlayerPacket: {
                StartTeleportToPlayerPacket tpPacket = (StartTeleportToPlayerPacket) packet;
                Player player = Bukkit.getPlayerExact(tpPacket.getPlayer());

                IntegerPacket answer = new IntegerPacket(0);
                tpPacket.applyAsAnswer(answer);

                if(player == null) {
                    answer.setValue(1);
                    WarpSystem.getInstance().getDataHandler().send(answer);
                    return;
                }

                WarpSystem.getInstance().getDataHandler().send(answer);

                TeleportOptions options = new TeleportOptions(new Destination(new EmptyAdapter()), tpPacket.getToDisplayName());
                options.setOrigin(Origin.TeleportRequest);
                options.setWaitForTeleport(true);
                options.setMessage(null);
                options.setPayMessage(null);
                options.setCosts(TeleportCommandManager.getInstance().getTpaCosts());
                options.addCallback(new Callback<Result>() {
                    @Override
                    public void accept(Result result) {
                        //move
                        if(result == Result.SUCCESS) {
                            WarpSystem.getInstance().getDataHandler().send(new PrepareTeleportPlayerToPlayerPacket(player.getName(), tpPacket.getTo(), new Callback<Integer>() {
                                @Override
                                public void accept(Integer result) {
                                    if(result == 0) {
                                        //teleported
                                    } else {
                                        player.sendMessage(Lang.getPrefix() + Lang.get("TeleportRequest_not_valid").replace("%PLAYER%", ChatColor.stripColor(tpPacket.getToDisplayName())));
                                    }
                                }
                            }).setCosts(TeleportCommandManager.getInstance().getTpaCosts()));
                        }
                    }
                });

                WarpSystem.getInstance().getTeleportManager().teleport(player, options);
                break;
            }

            case TeleportRequestHandledPacket: {
                TeleportRequestHandledPacket tpPacket = (TeleportRequestHandledPacket) packet;

                Invitation invitation = TeleportCommandManager.getInstance().getInvitation(tpPacket.getSender(), tpPacket.getRecipient());
                if(invitation != null) {
                    invitation.handle(tpPacket.getRecipient(), tpPacket.isAccepted());
                }
                break;
            }

            case TeleportPlayerToCoordsPacket: {
                TeleportPlayerToCoordsPacket tpPacket = (TeleportPlayerToCoordsPacket) packet;

                Player gate = Bukkit.getPlayer(tpPacket.getGate());
                Player player = Bukkit.getPlayer(tpPacket.getPlayer());
                String world = tpPacket.getWorld() != null ? tpPacket.getWorld() : player == null ? gate.getWorld().getName() : player.getWorld().getName();
                double x = (tpPacket.isRelativeX() ? gate.getLocation().getX() : 0) + tpPacket.getX();
                double y = (tpPacket.isRelativeY() ? gate.getLocation().getY() : 0) + tpPacket.getY();
                double z = (tpPacket.isRelativeZ() ? gate.getLocation().getZ() : 0) + tpPacket.getZ();
                float yaw = tpPacket.getYaw();
                float pitch = tpPacket.getPitch();
                String destination = tpPacket.getDestinationName() != null ? tpPacket.getDestinationName() : "x=" + cut(x) + ", y=" + cut(y) + ", z=" + cut(z);

                de.codingair.codingapi.tools.Location l = new de.codingair.codingapi.tools.Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
                TeleportOptions options = new TeleportOptions(new Destination(new LocationAdapter(l)), destination);

                if(tpPacket.getGate() != null && tpPacket.getPlayer() != null) {
                    if(!tpPacket.getGate().equals(tpPacket.getPlayer())) {
                        BungeePlayer end = new BungeePlayer(tpPacket.getGate(), tpPacket.getGate());
                        end.sendMessage(Lang.getPrefix() + Lang.get("Teleported_Player_Info").replace("%player%", tpPacket.getPlayer()).replace("%warp%", "x=" + cut(x) + ", y=" + cut(y) + ", z=" + cut(z)));
                    }

                    options.setMessage(Lang.getPrefix() + (tpPacket.getGate().equals(tpPacket.getPlayer()) ? Lang.get("Teleported_To") :
                            Lang.get("Teleported_To_By").replace("%gate%", tpPacket.getGate())));
                }

                if(tpPacket.getDestinationName() == null) options.setOrigin(Origin.TeleportCommand);
                options.setSkip(true);

                TeleportListener.setSpawnPositionOrTeleport(tpPacket.getPlayer(), options);
                break;
            }

            case ToggleForceTeleportsPacket: {
                ToggleForceTeleportsPacket tpPacket = (ToggleForceTeleportsPacket) packet;

                Player player = Bukkit.getPlayer(tpPacket.getPlayer());
                if(player != null) TeleportCommandManager.getInstance().setDenyForceTps(player, tpPacket.isAutoDenyTp());
                break;
            }
        }
    }

    public static Number cut(double n) {
        double d = Double.parseDouble(new DecimalFormat("#.##").format(n).replace(",", "."));
        if(d == (int) d) return (int) d;
        else return d;
    }

    @Override
    public boolean onSend(Packet packet) {
        return false;
    }
}
