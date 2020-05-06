package de.codingair.warpsystem.spigot.features.teleportcommand.commands;

import de.codingair.codingapi.server.commands.builder.BaseComponent;
import de.codingair.codingapi.server.commands.builder.CommandBuilder;
import de.codingair.codingapi.server.commands.builder.CommandComponent;
import de.codingair.codingapi.server.commands.builder.MultiCommandComponent;
import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.utils.ChatColor;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.language.Lang;
import de.codingair.warpsystem.spigot.features.teleportcommand.TeleportCommandManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CTpaHere extends CommandBuilder {
    public CTpaHere() {
        super("tpahere", "A WarpSystem-Command", new BaseComponent(WarpSystem.PERMISSION_USE_TELEPORT_COMMAND_TPA_HERE) {
            @Override
            public void noPermission(CommandSender sender, String label, CommandComponent child) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("No_Permission"));
            }

            @Override
            public void onlyFor(boolean player, CommandSender sender, String label, CommandComponent child) {
                sender.sendMessage(Lang.getPrefix() + Lang.get("Only_For_Players"));
            }

            @Override
            public void unknownSubCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + "§7" + Lang.get("Use") + ": /tpa <§eplayer§7>");
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + "§7" + Lang.get("Use") + ": /tpaHere <§eplayer§7>");
                return false;
            }
        }.setOnlyPlayers(true), true);

        setHighestPriority(true);

        getBaseComponent().addChild(new MultiCommandComponent() {
            @Override
            public boolean matchTabComplete(CommandSender sender, String suggestion, String argument) {
                return WarpSystem.getInstance().isOnBungeeCord() || super.matchTabComplete(sender, suggestion, argument);
            }

            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                Player p = (Player) sender;
                if(WarpSystem.getInstance().isOnBungeeCord()) {
                    suggestions.add("§WARPSYSTEM"); //key for bungeecord

                    StringBuilder builder = new StringBuilder("tpahere");
                    for(String arg : args) {
                        builder.append(" ").append(arg);
                    }
                    suggestions.add(builder.toString());

                    for(Player player : Bukkit.getOnlinePlayers()) {
                        if(!p.canSee(player)) {
                            suggestions.add("-" + player.getName());
                        }
                    }
                } else {
                    for(Player player : Bukkit.getOnlinePlayers()) {
                        if(player.getName().equals(sender.getName()) || !p.canSee(player)) continue;
                        suggestions.add(ChatColor.stripColor(player.getName()));
                    }
                }
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                if(argument == null) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Player_is_not_online"));
                    return false;
                }

                if(argument.equalsIgnoreCase(sender.getName())) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("TeleportRequest_Cant_Teleport_Yourself"));
                    return false;
                }

                TeleportCommandManager.getInstance().invite(sender.getName(), true, new Callback<Long>() {
                    @Override
                    public void accept(Long result) {
                        int handled = (int) (result >> 32);
                        int sent = result.intValue();

                        if(handled == 0) sender.sendMessage(Lang.getPrefix() + Lang.get("Player_is_not_online"));
                        else if(handled == -1) sender.sendMessage(Lang.getPrefix() + Lang.get("TeleportRequest_denied_sender").replace("%PLAYER%", ChatColor.stripColor(argument)));
                        else if(sent == 0) sender.sendMessage(Lang.getPrefix() + Lang.get("TeleportRequest_already_sent"));
                        else sender.sendMessage(Lang.getPrefix() + Lang.get("TeleportRequest_sent").replace("%PLAYER%", ChatColor.stripColor(argument)));
                    }
                }, argument);
                return false;
            }
        });
    }
}
