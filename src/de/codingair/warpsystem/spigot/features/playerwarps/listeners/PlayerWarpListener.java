package de.codingair.warpsystem.spigot.features.playerwarps.listeners;

import de.codingair.codingapi.player.chat.ChatButton;
import de.codingair.codingapi.player.chat.SimpleMessage;
import de.codingair.codingapi.utils.ImprovedDouble;
import de.codingair.warpsystem.spigot.api.events.PlayerFinalJoinEvent;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.language.Lang;
import de.codingair.warpsystem.spigot.features.playerwarps.guis.list.PWList;
import de.codingair.warpsystem.spigot.features.playerwarps.managers.PlayerWarpManager;
import de.codingair.warpsystem.spigot.features.playerwarps.utils.PlayerWarp;
import de.codingair.warpsystem.spigot.features.playerwarps.utils.PlayerWarpData;
import de.codingair.warpsystem.spigot.features.playerwarps.utils.PlayerWarpUpdate;
import de.codingair.warpsystem.transfer.packets.bungee.SendPlayerWarpOptionsPacket;
import de.codingair.warpsystem.transfer.packets.general.DeletePlayerWarpPacket;
import de.codingair.warpsystem.transfer.packets.general.SendPlayerWarpUpdatePacket;
import de.codingair.warpsystem.transfer.packets.general.SendPlayerWarpsPacket;
import de.codingair.warpsystem.transfer.packets.spigot.PlayerWarpTeleportProcessPacket;
import de.codingair.warpsystem.transfer.packets.utils.Packet;
import de.codingair.warpsystem.transfer.packets.utils.PacketType;
import de.codingair.warpsystem.transfer.utils.PacketListener;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

public class PlayerWarpListener implements PacketListener, Listener {

    @EventHandler
    public void onJoin(PlayerFinalJoinEvent e) {
        PlayerWarpManager.getManager().checkPlayerWarpOwnerNames(e.getPlayer());
        List<PlayerWarp> notify = new ArrayList<>();

        boolean timeDependent = PlayerWarpManager.getManager().isEconomy();
        double money = 0;
        List<PlayerWarp> warps = PlayerWarpManager.getManager().getOwnWarps(e.getPlayer());
        for(PlayerWarp warp : warps) {
            if(timeDependent && warp.isExpired()) {
                notify.add(warp);
            }

            money += warp.getInactiveSales() * warp.getTeleportCosts();
        }

        if(money > 0 || !notify.isEmpty() || (e.getPlayer().hasPermission(WarpSystem.PERMISSION_MODIFY_PLAYER_WARPS) && !PlayerWarpManager.getManager().isHideLimitInfo())) {
            double finalMoney = money;
            Bukkit.getScheduler().runTaskLater(WarpSystem.getInstance(), () -> {
                if(!notify.isEmpty()) {
                    for(PlayerWarp warp : notify) {
                        e.getPlayer().sendMessage(Lang.getPrefix() + Lang.get("Warp_expiring").replace("%NAME%", warp.getName()).replace("%TIME_LEFT%", PlayerWarpManager.convertInTimeFormat(PlayerWarpManager.getManager().getInactiveTime() - (System.currentTimeMillis() - warp.getExpireDate()), 0, "", "")));
                    }
                    notify.clear();
                }

                if(finalMoney > 0) {
                    SimpleMessage message = new SimpleMessage(Lang.getPrefix() + Lang.get("Warp_Money_Available").replace("%AMOUNT%", new ImprovedDouble(finalMoney).toString()), WarpSystem.getInstance());
                    message.replace("%BUTTON%", new ChatButton(Lang.get("Warp_Money_Available_Button")) {
                        @Override
                        public void onClick(Player player) {
                            new PWList(player).open();
                            message.destroy();
                        }
                    }.setHover(Lang.get("Click_Hover")));

                    message.setTimeOut(60);

                    message.send(e.getPlayer());
                }

                if((e.getPlayer().hasPermission(WarpSystem.PERMISSION_MODIFY_PLAYER_WARPS) && !PlayerWarpManager.getManager().isHideLimitInfo())) {
                    SimpleMessage message = new SimpleMessage(Lang.getPrefix() + "§7PlayerWarps are §climited §7to §c2 warps §7per §7player. §8[", WarpSystem.getInstance());

                    TextComponent upgrade = new TextComponent("§6§nPremium");
                    upgrade.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/premium-warps-portals-and-more-warp-teleport-system-1-8-1-14.66035/"));
                    upgrade.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[] {new TextComponent("§8» §6§lClick §8«")}));

                    message.add(upgrade);
                    message.add("§8 | ");

                    message.add(new ChatButton("§7Hide", "§7» Click «") {
                        @Override
                        public void onClick(Player player) {
                            PlayerWarpManager.getManager().setHideLimitInfo(true);
                            PlayerWarpManager.getManager().save(true);
                            player.sendMessage(Lang.getPrefix() + "§7You won't see this message again.");
                            message.destroy();
                        }
                    });

                    message.add("§8]");
                    message.send(e.getPlayer());
                }
            }, 5 * 20L);
        }
    }

    @Override
    public void onReceive(Packet packet, String extra) {
        if(packet.getType() == PacketType.SendPlayerWarpsPacket) {
            List<PlayerWarpData> l = ((SendPlayerWarpsPacket) packet).getData();

            for(PlayerWarpData s : l) {
                PlayerWarp w = new PlayerWarp();
                w.setData(s);
                PlayerWarpManager.getManager().updateWarp(w);
                s.destroy();
            }

            l.clear();
            PlayerWarpManager.getManager().updateGUIs();
        } else if(packet.getType() == PacketType.SendPlayerWarpUpdatesPacket) {
            PlayerWarpUpdate update = ((SendPlayerWarpUpdatePacket) packet).getUpdate();

            PlayerWarp w = PlayerWarpManager.getManager().getWarp(update.getId(), update.getOriginName());
            w.setData(update);
            update.destroy();
            PlayerWarpManager.getManager().updateGUIs();
        } else if(packet.getType() == PacketType.SendPlayerWarpOptionsPacket) {
            SendPlayerWarpOptionsPacket p = (SendPlayerWarpOptionsPacket) packet;
            PlayerWarpManager.getManager().setInactiveTime(p.getInactiveTime());
        } else if(packet.getType() == PacketType.DeletePlayerWarpPacket) {
            DeletePlayerWarpPacket p = (DeletePlayerWarpPacket) packet;

            PlayerWarp warp = PlayerWarpManager.getManager().getWarp(p.getId(), p.getName());
            PlayerWarpManager.getManager().delete(warp, false);
            if(warp != null) warp.setSource(true);
            PlayerWarpManager.getManager().updateGUIs();
        } else if(packet.getType() == PacketType.PlayerWarpTeleportProcessPacket) {
            PlayerWarpTeleportProcessPacket p = (PlayerWarpTeleportProcessPacket) packet;

            PlayerWarp warp = PlayerWarpManager.getManager().getWarp(p.getId(), p.getName());
            if(warp != null) {
                if(p.increaseSales()) warp.increaseInactiveSales();
                if(p.resetSales()) warp.resetInactiveSales();
                if(p.increasePerformed()) warp.increasePerformed();

                PlayerWarpManager.getManager().updateGUIs();
            }
        }
    }

    @Override
    public boolean onSend(Packet packet) {
        return false;
    }
}
