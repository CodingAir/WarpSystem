package de.codingair.warpsystem.spigot.features.tempwarps.commands;

import de.codingair.codingapi.server.commands.BaseComponent;
import de.codingair.codingapi.server.commands.CommandBuilder;
import de.codingair.codingapi.server.commands.CommandComponent;
import de.codingair.codingapi.server.commands.MultiCommandComponent;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.language.Lang;
import de.codingair.warpsystem.spigot.features.tempwarps.managers.TempWarpManager;
import de.codingair.warpsystem.spigot.features.tempwarps.utils.TempWarp;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CTempWarps extends CommandBuilder {
    public CTempWarps() {
        super("TempWarps", new BaseComponent(WarpSystem.PERMISSION_USE) {
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
                sender.sendMessage(Lang.getPrefix() + "§7" + Lang.get("Use") + ": /" + label + " §e<create, delete, edit, list, renew>");
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + "§7" + Lang.get("Use") + ": /" + label + " §e<create, delete, edit, list, renew>");
                return false;
            }
        }, true);

        getBaseComponent().addChild(new CommandComponent("create") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                List<TempWarp> list = TempWarpManager.getManager().getWarps((Player) sender);
                int current = list.size();
                list.clear();
                if(!sender.hasPermission(TempWarpManager.PERMISSION(current + 1))) {
                    if(current == 0) {
                        sender.sendMessage(Lang.getPrefix() + Lang.get("No_Permission"));
                    } else sender.sendMessage(Lang.getPrefix() + Lang.get("TempWarp_Maximum_of_Warps").replace("%AMOUNT%", current + ""));
                    return false;
                }

                TempWarpManager.getManager().create((Player) sender);
                return false;
            }
        });

        getComponent("create").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, List<String> suggestions) {
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                List<TempWarp> list = TempWarpManager.getManager().getWarps((Player) sender);
                int current = list.size();
                list.clear();
                if(!sender.hasPermission(TempWarpManager.PERMISSION(current + 1))) {
                    if(current == 0) {
                        sender.sendMessage(Lang.getPrefix() + Lang.get("No_Permission"));
                    } else sender.sendMessage(Lang.getPrefix() + Lang.get("TempWarp_Maximum_of_Warps").replace("%AMOUNT%", current + ""));
                    return false;
                }

                TempWarpManager.getManager().create((Player) sender, argument);
                return false;
            }
        });


        getBaseComponent().addChild(new CommandComponent("delete") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + "§7" + Lang.get("Use") + ": /" + label + " delete §e<warp>");
                return false;
            }
        });

        getComponent("delete").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, List<String> suggestions) {
                List<TempWarp> warps = TempWarpManager.getManager().getWarps((Player) sender);

                for(TempWarp warp : warps) {
                    suggestions.add(warp.getIdentifier());
                }

                warps.clear();
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                TempWarpManager.getManager().delete((Player) sender, argument);
                return false;
            }
        });


        getBaseComponent().addChild(new CommandComponent("edit") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + "§7" + Lang.get("Use") + ": /" + label + " edit §e<warp>");
                return false;
            }
        });

        getComponent("edit").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, List<String> suggestions) {
                List<TempWarp> warps = TempWarpManager.getManager().getWarps((Player) sender);

                for(TempWarp warp : warps) {
                    suggestions.add(warp.getIdentifier());
                }

                warps.clear();
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                TempWarpManager.getManager().edit((Player) sender, argument);
                return false;
            }
        });

        getBaseComponent().addChild(new CommandComponent("list") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                List<TempWarp> list = TempWarpManager.getManager().getWarps((Player) sender);
                if(list.isEmpty()) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("TempWarp_No_Warps"));
                    return false;
                }

                List<String> l = new ArrayList<>();

                l.add(" ");
                l.add("§7§l§m------------------------------------------------");
                l.add(" ");
                l.add("  " + Lang.get("TempWarp_You_have_n_Warps").replace("%AMOUNT%", list.size() + ""));
                l.add(" ");
                for(TempWarp warp : list) {
                    l.add("  §7\"§f" + warp.getName() + "§7\" §8(" + (warp.isPublic() ? "§a" + Lang.get("Public") : "§c" + Lang.get("Private")) + "§8)§7: §b" + TempWarpManager.getManager().convertInTimeFormat(warp.getLeftTime(), TimeUnit.MILLISECONDS) + " §7" + Lang.get("Remaining"));
                }

                l.add(" ");
                l.add("§7§l§m------------------------------------------------");

                sender.sendMessage(l.toArray(new String[0]));

                l.clear();
                list.clear();
                return false;
            }
        });

        getBaseComponent().addChild(new CommandComponent("renew") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + "§7" + Lang.get("Use") + ": /" + label + " renew §e<warp>");
                return false;
            }
        });

        getComponent("renew").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, List<String> suggestions) {
                List<TempWarp> warps = TempWarpManager.getManager().getWarps((Player) sender);

                for(TempWarp warp : warps) {
                    if(warp.isExpired()) suggestions.add(warp.getIdentifier());
                }

                warps.clear();
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                TempWarpManager.getManager().reactivate((Player) sender, argument);
                return false;
            }
        });
    }

    private static class test extends TextComponent {
        public test(String text) {
            super(text);
        }
    }
}
