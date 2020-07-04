package de.codingair.warpsystem.spigot.features.teleportcommand;

import de.codingair.codingapi.files.ConfigFile;
import de.codingair.codingapi.player.chat.ChatButtonManager;
import de.codingair.codingapi.tools.Callback;
import de.codingair.warpsystem.bungee.features.teleport.utils.TeleportCommandOptions;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.language.Lang;
import de.codingair.warpsystem.spigot.base.setupassistant.annotations.AvailableForSetupAssistant;
import de.codingair.warpsystem.spigot.base.setupassistant.annotations.Function;
import de.codingair.warpsystem.spigot.base.utils.BungeeFeature;
import de.codingair.warpsystem.spigot.base.utils.teleport.TeleportOptions;
import de.codingair.warpsystem.spigot.base.utils.teleport.TeleportResult;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.Destination;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.adapters.LocationAdapter;
import de.codingair.warpsystem.spigot.bstats.Collectible;
import de.codingair.warpsystem.spigot.features.FeatureType;
import de.codingair.warpsystem.spigot.features.teleportcommand.commands.*;
import de.codingair.warpsystem.spigot.features.teleportcommand.listeners.BackListener;
import de.codingair.warpsystem.spigot.features.teleportcommand.listeners.TeleportListener;
import de.codingair.warpsystem.spigot.features.teleportcommand.listeners.TeleportPacketListener;
import de.codingair.warpsystem.spigot.features.teleportcommand.packets.ToggleForceTeleportsPacket;
import de.codingair.warpsystem.utils.Manager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AvailableForSetupAssistant(type = "TeleportCommands", config = "Config")
@Function(name = "Enabled", defaultValue = "true", configPath = "WarpSystem.Functions.TeleportCommand", clazz = Boolean.class)
@Function(name = "BungeeCord", defaultValue = "true", configPath = "WarpSystem.TeleportCommands.BungeeCord", clazz = Boolean.class)
@Function(name = "Teleport requests costs", defaultValue = "0", configPath = "WarpSystem.TeleportCommands.TeleportRequests.Teleport_Costs", clazz = Double.class)
@Function(name = "Teleport requests expire delay (seconds)", defaultValue = "30", configPath = "WarpSystem.TeleportCommands.TeleportRequests.ExpireDelay", clazz = Integer.class)
@Function(name = "Back", defaultValue = "true", configPath = "WarpSystem.TeleportCommands.Back.Enabled", clazz = Boolean.class)
@Function(name = "Tp", defaultValue = "true", configPath = "WarpSystem.TeleportCommands.Tp", clazz = Boolean.class)
@Function(name = "TpAll", defaultValue = "true", configPath = "WarpSystem.TeleportCommands.TpAll", clazz = Boolean.class)
@Function(name = "TpToggle", defaultValue = "true", configPath = "WarpSystem.TeleportCommands.TpToggle", clazz = Boolean.class)
@Function(name = "Tpa", defaultValue = "true", configPath = "WarpSystem.TeleportCommands.Tpa", clazz = Boolean.class)
@Function(name = "TpaHere", defaultValue = "true", configPath = "WarpSystem.TeleportCommands.TpaHere", clazz = Boolean.class)
@Function(name = "TpaAll", defaultValue = "true", configPath = "WarpSystem.TeleportCommands.TpaAll", clazz = Boolean.class)
@Function(name = "TpaToggle", defaultValue = "true", configPath = "WarpSystem.TeleportCommands.TpaToggle", clazz = Boolean.class)
public class TeleportCommandManager implements Manager, BungeeFeature, Collectible {
    private final HashMap<String, List<Invitation>> invites = new HashMap<>();

    private final List<String> denyTpa = new ArrayList<>();
    private final List<String> denyForceTps = new ArrayList<>();

    private final HashMap<String, List<Location>> backHistory = new HashMap<>();
    private final List<String> usingBackCommand = new ArrayList<>();

    private TeleportPacketListener packetListener;

    private int expireDelay = 30;
    private int backHistorySize = 1;
    private int tpaCosts = 0;
    private boolean bungeeCord = false;

    private CTeleport tp;
    private CTpHere tpHere;
    private CTpToggle tpToggle;
    private CTpa tpa;
    private CTpAccept tpAccept;
    private CTpDeny tpDeny;
    private CTpaHere tpaHere;
    private CTpaToggle tpaToggle;
    private CTpaAll tpaAll;
    private CTpAll tpAll;
    private CBack back;

    public static TeleportCommandManager getInstance() {
        return WarpSystem.getInstance().getDataManager().getManager(FeatureType.TELEPORT_COMMAND);
    }

    @Override
    public void collectOptionStatistics(Map<String, Integer> entry) {
    }

    @Override
    public boolean load(boolean loader) {
        Bukkit.getPluginManager().registerEvents(new TeleportListener(), WarpSystem.getInstance());
        Bukkit.getPluginManager().registerEvents(new BackListener(), WarpSystem.getInstance());

        ConfigFile file = WarpSystem.getInstance().getFileManager().getFile("Config");

        if(file.getConfig().getBoolean("WarpSystem.Functions.TeleportCommand", true)) {
            expireDelay = file.getConfig().getInt("WarpSystem.TeleportCommands.TeleportRequests.ExpireDelay", 30);
            tpaCosts = file.getConfig().getInt("WarpSystem.TeleportCommands.TeleportRequests.Teleport_Costs", 0);
            file.getConfig().set("WarpSystem.TeleportCommands.BungeeCord", false);

            if(file.getConfig().getBoolean("WarpSystem.TeleportCommands.Tp", true)) {
                (tp = new CTeleport()).register();
                (tpHere = new CTpHere(tp)).register();
            }

            if(file.getConfig().getBoolean("WarpSystem.TeleportCommands.TpToggle", true)) (tpToggle = new CTpToggle()).register();
            if(file.getConfig().getBoolean("WarpSystem.TeleportCommands.Tpa", true)) {
                (tpa = new CTpa()).register();
                (tpAccept = new CTpAccept()).register();
                (tpDeny = new CTpDeny()).register();
            }
            if(file.getConfig().getBoolean("WarpSystem.TeleportCommands.TpaHere", true)) {
                (tpaHere = new CTpaHere()).register();
                if(tpAccept == null) (tpAccept = new CTpAccept()).register();
                if(tpDeny == null) (tpDeny = new CTpDeny()).register();
            }
            if(file.getConfig().getBoolean("WarpSystem.TeleportCommands.TpaToggle", true)) (tpaToggle = new CTpaToggle()).register();
            if(file.getConfig().getBoolean("WarpSystem.TeleportCommands.TpaAll", true)) (tpaAll = new CTpaAll()).register();
            if(file.getConfig().getBoolean("WarpSystem.TeleportCommands.TpAll", true)) (tpAll = new CTpAll()).register();
            if(file.getConfig().getBoolean("WarpSystem.TeleportCommands.Back.Enabled", true)) {
                (back = new CBack()).register();
                this.backHistorySize = file.getConfig().getInt("WarpSystem.TeleportCommands.Back.History_Size", 3);
                if(backHistorySize < 1) {
                    backHistorySize = 1;
                    file.getConfig().set("WarpSystem.TeleportCommands.Back.History_Size", 1);
                }
            }
        }

        ChatButtonManager.getInstance().addListener((player, id, type) -> {
            if(type != null && type.equalsIgnoreCase("TP")) {
                player.sendMessage(Lang.getPrefix() + Lang.get("TeleportRequest_not_valid_general"));
            }
        });

        file.saveConfig();
        return true;
    }

    @Override
    public void save(boolean saver) {
    }

    @Override
    public void destroy() {
        tp = null;
        tpHere = null;
        tpToggle = null;
        tpa = null;
        tpAccept = null;
        tpDeny = null;
        tpaHere = null;
        tpaToggle = null;
        tpaAll = null;
        tpAll = null;
        back = null;
    }

    @Override
    public void onConnect() {
        this.packetListener = new TeleportPacketListener();
        WarpSystem.getInstance().getDataHandler().register(this.packetListener);
        Bukkit.getPluginManager().registerEvents(this.packetListener, WarpSystem.getInstance());
    }

    @Override
    public void onDisconnect() {
        if(this.packetListener != null) {
            HandlerList.unregisterAll(this.packetListener);
            WarpSystem.getInstance().getDataHandler().unregister(this.packetListener);
            this.packetListener = null;
        }
    }

    public boolean usingBackCommand(Player player) {
        return this.usingBackCommand.contains(player.getName());
    }

    public void addToBackHistory(Player player, Location location) {
        List<Location> locations = this.backHistory.computeIfAbsent(player.getName(), k -> new ArrayList<>());
        locations.add(0, location);
        if(locations.size() > backHistorySize) locations.remove(locations.size() - 1);
    }

    public boolean teleportToLastBackLocation(Player player) {
        List<Location> locations = this.backHistory.get(player.getName());
        if(locations == null) return false;

        Location l = locations.remove(0);

        TeleportOptions options = new TeleportOptions(new Destination(new LocationAdapter(l)), Lang.get("Last_Position"));
        options.addCallback(new Callback<TeleportResult>() {
            @Override
            public void accept(TeleportResult result) {
                if(result != TeleportResult.SUCCESS) {
                    locations.add(0, l);
                } else if(locations.isEmpty()) backHistory.remove(player.getName());

                usingBackCommand.remove(player.getName());
            }
        });

        this.usingBackCommand.add(player.getName());
        WarpSystem.getInstance().getTeleportManager().teleport(player, options);
        return true;
    }

    public boolean deniesTpaRequests(String player) {
        return this.denyTpa.contains(player);
    }

    public boolean toggleDenyTpaRequest(Player player) {
        if(this.denyTpa.contains(player.getName())) {
            if(WarpSystem.getInstance().isOnBungeeCord()) WarpSystem.getInstance().getDataHandler().send(new ToggleForceTeleportsPacket(player.getName(), deniesForceTps(player), false));
            this.denyTpa.remove(player.getName());
            return false;
        } else {
            if(WarpSystem.getInstance().isOnBungeeCord()) WarpSystem.getInstance().getDataHandler().send(new ToggleForceTeleportsPacket(player.getName(), deniesForceTps(player), true));
            this.denyTpa.add(player.getName());
            return true;
        }
    }

    public boolean deniesForceTps(Player player) {
        return this.denyForceTps.contains(player.getName());
    }

    public boolean toggleDenyForceTps(Player player) {
        if(this.denyForceTps.contains(player.getName())) {
            if(WarpSystem.getInstance().isOnBungeeCord()) WarpSystem.getInstance().getDataHandler().send(new ToggleForceTeleportsPacket(player.getName(), false, deniesTpaRequests(player.getName())));
            this.denyForceTps.remove(player.getName());
            return false;
        } else {
            if(WarpSystem.getInstance().isOnBungeeCord()) WarpSystem.getInstance().getDataHandler().send(new ToggleForceTeleportsPacket(player.getName(), true, deniesTpaRequests(player.getName())));
            this.denyForceTps.add(player.getName());
            return true;
        }
    }

    public void setDenyForceTps(Player player, boolean deny) {
        if(deny) {
            if(!this.denyForceTps.contains(player.getName())) this.denyForceTps.add(player.getName());
        } else this.denyForceTps.remove(player.getName());
    }

    public boolean isInvitedBy(String sender, String recipient) {
        return getInvitation(sender, recipient) != null;
    }

    public List<Invitation> getReceivedInvites(String player) {
        List<Invitation> invites = new ArrayList<>();

        List<List<Invitation>> data = new ArrayList<>(this.invites.values());
        for(List<Invitation> value : data) {
            for(Invitation i : value) {
                if(i.isRecipient(player)) invites.add(i);
            }
        }
        data.clear();

        return invites;
    }

    public void checkDestructionOf(Invitation inv) {
        if(inv.canBeDestroyed()) {
            List<Invitation> l = this.invites.get(inv.getSender());

            if(l != null) l.remove(inv);
            inv.destroy();
        }
    }

    public Invitation getInvitation(String sender, String recipient) {
        if(sender.equalsIgnoreCase(recipient)) return null;

        List<Invitation> l = this.invites.get(sender);
        if(l == null) return null;

        for(Invitation invitation : l) {
            if(invitation.isRecipient(recipient)) return invitation;
        }

        return null;
    }

    public void invite(String sender, boolean tpToSender, Callback<Long> callback, String recipient) {
        invite(sender, tpToSender, callback, recipient, true);
    }

    public void invite(String sender, boolean tpToSender, Callback<Long> callback, String recipient, boolean bukkitOnly) {
        if(recipient != null) {
            List<Invitation> l = this.invites.get(sender);

            if(l == null) {
                l = new ArrayList<>();
                this.invites.put(sender, l);
            } else if(isInvitedBy(sender, recipient)) {
                if(callback != null) callback.accept(1L << 32);
                return;
            } else if(deniesTpaRequests(recipient)) {
                if(callback != null) callback.accept(-1L << 32);
                return;
            }

            Invitation inv = new Invitation(sender, tpToSender, recipient, true);
            l.add(inv);
            inv.send(new Callback<Long>() {
                @Override
                public void accept(Long result) {
                    callback.accept(result);
                    if(result.intValue() == 0) checkDestructionOf(inv);
                }
            });
        } else {
            List<Invitation> l = this.invites.computeIfAbsent(sender, k -> new ArrayList<>());

            Invitation old = null;
            for(Invitation i : l) {
                if(i.getRecipient() == null) {
                    old = i;
                    break;
                }
            }
            l.remove(old);

            //invite all
            Invitation inv = new Invitation(sender, true);
            l.add(inv);
            inv.send(new Callback<Long>() {
                @Override
                public void accept(Long result) {
                    callback.accept(result);
                    if(result.intValue() == 0) checkDestructionOf(inv);
                }
            });
        }
    }

    public void clear(Player player) {
        //clear own/foreign invitations
        List<Invitation> invites = this.invites.remove(player.getName());
        if(invites != null) {
            for(Invitation invite : invites) {
                invite.destroy();
            }

            invites.clear();
        }

        invites = getReceivedInvites(player.getName());

        for(Invitation invite : invites) {
            invite.timeOut(player.getName());
        }

        invites.clear();

        //remove from auto deny list
        this.denyTpa.remove(player.getName());
        this.denyForceTps.remove(player.getName());

        this.backHistory.remove(player.getName());
    }

    public int getExpireDelay() {
        return expireDelay;
    }

    public int getTpaCosts() {
        return this.tpaCosts;
    }

    public int getBackHistorySize() {
        return backHistorySize;
    }
}
