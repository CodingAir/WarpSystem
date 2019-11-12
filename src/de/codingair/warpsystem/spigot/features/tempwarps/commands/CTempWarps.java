package de.codingair.warpsystem.spigot.features.tempwarps.commands;

import de.codingair.codingapi.player.chat.ChatButton;
import de.codingair.codingapi.player.chat.SimpleMessage;
import de.codingair.codingapi.player.gui.anvil.*;
import de.codingair.codingapi.server.commands.builder.BaseComponent;
import de.codingair.codingapi.server.commands.builder.CommandBuilder;
import de.codingair.codingapi.server.commands.builder.CommandComponent;
import de.codingair.codingapi.server.commands.builder.MultiCommandComponent;
import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import de.codingair.warpsystem.spigot.api.players.PermissionPlayer;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.language.Lang;
import de.codingair.warpsystem.spigot.features.tempwarps.guis.GTempWarpList;
import de.codingair.warpsystem.spigot.features.tempwarps.guis.keys.KeyList;
import de.codingair.warpsystem.spigot.features.tempwarps.guis.keys.TemplateGUI;
import de.codingair.warpsystem.spigot.features.tempwarps.managers.TempWarpManager;
import de.codingair.warpsystem.spigot.features.tempwarps.utils.Key;
import de.codingair.warpsystem.spigot.features.tempwarps.utils.TempWarp;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CTempWarps extends CommandBuilder {
    public CTempWarps() {
        super("tempwarps", "A WarpSystem-Command", new BaseComponent(WarpSystem.PERMISSION_USE_TEMP_WARPS) {
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
                sender.sendMessage(Lang.getPrefix() + "§7" + Lang.get("Use") + ": /" + label + " §e<" + (TempWarpManager.getManager().isKeys() && sender.hasPermission(WarpSystem.PERMISSION_MODIFY_TEMP_WARPS) ? "keys, " : "") + "create, delete, edit, list, info, renew>");
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + "§7" + Lang.get("Use") + ": /" + label + " §e<" + (TempWarpManager.getManager().isKeys() && sender.hasPermission(WarpSystem.PERMISSION_MODIFY_TEMP_WARPS) ? "keys, " : "") + "create, delete, edit, list, info, renew>");
                return false;
            }
        }, true, "tws", "twarps");

        if(TempWarpManager.getManager().isKeys()) {
            getBaseComponent().addChild(new CommandComponent("keys") {
                @Override
                public boolean runCommand(CommandSender sender, String label, String[] args) {

                    if(sender instanceof Player) {
                        if(sender.hasPermission(WarpSystem.PERMISSION_MODIFY_TEMP_WARPS))
                            sender.sendMessage(Lang.getPrefix() + "§7" + Lang.get("Use") + ": /" + label + " keys §e<create, edit, give, delete, list>");

                        List<String> keys = TempWarpManager.getManager().getKeys((Player) sender);
                        sender.sendMessage(Lang.getPrefix() + Lang.get("TempWarps_Keys_Info").replace("%AMOUNT%", (keys == null ? 0 : keys.size()) + ""));
                    } else sender.sendMessage(Lang.getPrefix() + "§7" + Lang.get("Use") + ": /" + label + " keys §e<give>");
                    return false;
                }
            });

            getComponent("keys").addChild(new CommandComponent("create", WarpSystem.PERMISSION_MODIFY_TEMP_WARPS) {
                @Override
                public boolean runCommand(CommandSender sender, String label, String[] args) {
                    if(sender instanceof Player) {
                        Player player = (Player) sender;

                        ItemStack item = player.getInventory().getItem(player.getInventory().getHeldItemSlot());

                        if(item == null || item.getType() == Material.AIR) item = TempWarpManager.KEY_ITEM.clone();
                        else item = item.clone();
                        ItemStack finalItem = item;

                        AnvilGUI.openAnvil(WarpSystem.getInstance(), (Player) sender, new AnvilListener() {
                            @Override
                            public void onClick(AnvilClickEvent e) {
                                if(!e.getSlot().equals(AnvilSlot.OUTPUT)) return;
                                String input = e.getInput();

                                if(input == null) {
                                    e.getPlayer().sendMessage(Lang.getPrefix() + Lang.get("Enter_Name"));
                                    return;
                                }

                                if(TempWarpManager.getManager().getTemplate(ChatColor.translateAlternateColorCodes('&', input)) != null) {
                                    e.getPlayer().sendMessage(Lang.getPrefix() + Lang.get("Name_Already_Exists"));
                                    return;
                                }

                                e.setClose(true);
                            }

                            @Override
                            public void onClose(AnvilCloseEvent e) {
                                if(e.isSubmitted()) {
                                    Key key = new Key(ChatColor.translateAlternateColorCodes('&', e.getSubmittedText()), TempWarpManager.getManager().getMinTime(), finalItem);
                                    e.setPost(() -> new TemplateGUI(e.getPlayer(), key, key.clone()).open());
                                }
                            }
                        }, new ItemBuilder(XMaterial.NAME_TAG).setName(Lang.get("Name") + "...").getItem());
                    } else sender.sendMessage(Lang.getPrefix() + "§cThe console can only give keys away.");

                    return false;
                }
            });

            getComponent("keys", "create").addChild(new MultiCommandComponent() {
                @Override
                public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                }

                @Override
                public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                    if(sender instanceof Player) {
                        if(TempWarpManager.getManager().getTemplate(ChatColor.translateAlternateColorCodes('&', argument)) != null) {
                            sender.sendMessage(Lang.getPrefix() + Lang.get("Name_Already_Exists"));
                            return false;
                        }

                        ItemStack item = ((Player) sender).getInventory().getItem(((Player) sender).getInventory().getHeldItemSlot());

                        if(item == null || item.getType() == Material.AIR) item = TempWarpManager.KEY_ITEM.clone();
                        else item = item.clone();

                        Key key = new Key(ChatColor.translateAlternateColorCodes('&', argument), TempWarpManager.getManager().getMinTime(), item);
                        new TemplateGUI((Player) sender, key, key.clone()).open();
                    } else sender.sendMessage(Lang.getPrefix() + "§cThe console can only give keys away.");
                    return false;
                }
            });

            getComponent("keys").addChild(new CommandComponent("edit", WarpSystem.PERMISSION_MODIFY_TEMP_WARPS) {
                @Override
                public boolean runCommand(CommandSender sender, String label, String[] args) {
                    if(sender instanceof Player)
                        sender.sendMessage(Lang.getPrefix() + "§7" + Lang.get("Use") + ": /" + label + " keys edit §e<name>");
                    else sender.sendMessage(Lang.getPrefix() + "§cThe console can only give keys away.");
                    return false;
                }
            });

            getComponent("keys", "edit").addChild(new MultiCommandComponent() {
                @Override
                public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                    for(Key template : TempWarpManager.getManager().getTemplates()) {
                        suggestions.add(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', template.getName())));
                    }
                }

                @Override
                public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                    if(sender instanceof Player) {
                        Key key = TempWarpManager.getManager().getTemplate(argument);

                        if(key == null) {
                            sender.sendMessage(Lang.getPrefix() + Lang.get("Key_Name_Does_Not_Exist"));
                            return false;
                        }

                        new TemplateGUI((Player) sender, key, key.clone()).open();
                    } else sender.sendMessage(Lang.getPrefix() + "§cThe console can only give keys away.");
                    return false;
                }
            });

            getComponent("keys").addChild(new CommandComponent("give", WarpSystem.PERMISSION_MODIFY_TEMP_WARPS) {
                @Override
                public boolean runCommand(CommandSender sender, String label, String[] args) {
                    sender.sendMessage(Lang.getPrefix() + "§7" + Lang.get("Use") + ": /" + label + " keys give §e<player>");
                    return false;
                }
            });

            getComponent("keys", "give").addChild(new MultiCommandComponent() {
                @Override
                public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                    for(Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        suggestions.add(onlinePlayer.getName());
                    }
                }

                @Override
                public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                    sender.sendMessage(Lang.getPrefix() + "§7" + Lang.get("Use") + ": /" + label + " keys give " + argument + " §e<key>");
                    return false;
                }
            });

            getComponent("keys", "give", null).addChild(new MultiCommandComponent() {
                @Override
                public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                    for(Key template : TempWarpManager.getManager().getTemplates()) {
                        suggestions.add(template.getStrippedName());
                    }
                }

                @Override
                public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                    Key template = TempWarpManager.getManager().getTemplate(argument);

                    if(template == null) {
                        sender.sendMessage(Lang.getPrefix() + Lang.get("Key_Name_Does_Not_Exist"));
                        return false;
                    }

                    Player player = Bukkit.getPlayer(args[2]);

                    if(player == null) {
                        sender.sendMessage(Lang.getPrefix() + Lang.get("Player_is_not_online"));
                        return false;
                    }

                    if(!TempWarpManager.getManager().giveKey(WarpSystem.getInstance().getUUIDManager().get(player), 1, template.getStrippedName())) {
                        sender.sendMessage(Lang.getPrefix() + Lang.get("Keys_Not_More_Than").replace("%AMOUNT%", TempWarpManager.MAX_KEYS + ""));
                        return false;
                    }

                    player.sendMessage(Lang.getPrefix() + Lang.get("TempWarps_Player_Got_Key")
                            .replace("%KEY%", template.getName())
                            .replace("%DURATION%", TempWarpManager.getManager().convertInTimeFormat(template.getTime(), TempWarpManager.getManager().getConfig().getUnit())));
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Key_sent_to_player").replace("%PLAYER%", player.getName()));
                    return false;
                }
            });

            getComponent("keys").addChild(new CommandComponent("delete", WarpSystem.PERMISSION_MODIFY_TEMP_WARPS) {
                @Override
                public boolean runCommand(CommandSender sender, String label, String[] args) {
                    sender.sendMessage(Lang.getPrefix() + "§7" + Lang.get("Use") + ": /" + label + " keys delete §e<name>");
                    return false;
                }
            });

            getComponent("keys", "delete").addChild(new MultiCommandComponent() {
                @Override
                public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                    for(Key template : TempWarpManager.getManager().getTemplates()) {
                        suggestions.add(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', template.getName())));
                    }
                }

                @Override
                public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                    if(sender instanceof Player) {
                        Key key = TempWarpManager.getManager().getTemplate(argument);

                        if(key == null) {
                            sender.sendMessage(Lang.getPrefix() + Lang.get("Key_Name_Does_Not_Exist"));
                            return false;
                        }

                        SimpleMessage message = new SimpleMessage(Lang.getPrefix() + Lang.get("Key_Template_Delete_Confirmation").replace("%NAME%", key.getStrippedName()), WarpSystem.getInstance());

                        message.replace("%NO%", new ChatButton(Lang.get("No_Keep"), Lang.get("Click_Hover")) {
                            @Override
                            public void onClick(Player player) {
                                message.destroy();
                                sender.sendMessage(Lang.getPrefix() + Lang.get("Key_Template_Not_Deleted"));
                            }
                        });

                        message.replace("%YES%", new ChatButton(Lang.get("Yes_Delete"), Lang.get("Click_Hover")) {
                            @Override
                            public void onClick(Player player) {
                                message.destroy();
                                TempWarpManager.getManager().removeTemplate(key);
                                sender.sendMessage(Lang.getPrefix() + Lang.get("Key_Template_Deleted"));
                            }
                        });

                        message.setTimeOut(20);
                        message.send((Player) sender);
                    } else sender.sendMessage(Lang.getPrefix() + "§cThe console can only give keys away.");
                    return false;
                }
            });

            getComponent("keys").addChild(new CommandComponent("list", WarpSystem.PERMISSION_MODIFY_TEMP_WARPS) {
                @Override
                public boolean runCommand(CommandSender sender, String label, String[] args) {
                    if(sender instanceof Player) {

                        new KeyList((Player) sender) {
                            @Override
                            public void onClick(Key key, ClickType clickType) {
                                new TemplateGUI((Player) sender, key, key.clone()).open();
                            }

                            @Override
                            public void buildItemDescription(List<String> lore) {
                                lore.add("");
                                lore.add("§8» §7" + Lang.get("Edit"));
                            }

                            @Override
                            public void onClose() {
                            }
                        }.open();
                    } else sender.sendMessage(Lang.getPrefix() + "§cThe console can only give keys away.");
                    return false;
                }
            });
        }

        getBaseComponent().addChild(new CommandComponent("create") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                List<TempWarp> list = TempWarpManager.getManager().getWarps((Player) sender);
                int current = list.size();
                list.clear();
                if(!TempWarpManager.hasPermission((Player) sender)) {
                    if(current == 0) {
                        sender.sendMessage(Lang.getPrefix() + Lang.get("No_Permission"));
                    } else sender.sendMessage(Lang.getPrefix() + Lang.get("TempWarp_Maximum_of_Warps").replace("%AMOUNT%", current + ""));
                    return false;
                }

                if(TempWarpManager.getManager().isProtectedRegions() && isProtected((Player) sender)) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("TempWarp_Create_Protected"));
                    return false;
                }

                TempWarpManager.getManager().create((Player) sender);
                return false;
            }
        });

        getComponent("create").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                return getComponent("create").runCommand(sender, label, args);
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
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
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
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                List<TempWarp> warps = TempWarpManager.getManager().getWarps((Player) sender);

                for(TempWarp warp : warps) {
                    if(warp.isExpired()) continue;
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
                new GTempWarpList((Player) sender).open();
//                List<TempWarp> list = TempWarpManager.getManager().getWarps((Player) sender);
//                drawList((Player) sender, sender.getName(), list);
//                list.clear();
                return false;
            }
        });

        getComponent("list").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                for(Player player : Bukkit.getOnlinePlayers()) {
                    suggestions.add(player.getName());
                }

                suggestions.remove(sender.getName());
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                Player player = Bukkit.getPlayer(argument);

                if(player == null) {
                    if(sender.hasPermission(WarpSystem.PERMISSION_ADMIN)) {
                        if(WarpSystem.getInstance().getUUIDManager().isCached(argument)) {
                            List<TempWarp> warps = TempWarpManager.getManager().getWarps(WarpSystem.getInstance().getUUIDManager().getCached(argument));
                            drawList((Player) sender, argument, warps);
                            warps.clear();
                        } else {
                            SimpleMessage message = new SimpleMessage(Lang.getPrefix() + Lang.get("Ask_to_download_uuid"), WarpSystem.getInstance());
                            message.replace("%YES%", new ChatButton("§a" + Lang.get("Yes")) {
                                @Override
                                public void onClick(Player clicked) {
                                    WarpSystem.getInstance().getUUIDManager().downloadFromMojang(argument, new Callback<UUID>() {
                                        @Override
                                        public void accept(UUID id) {
                                            if(id == null) {
                                                sender.sendMessage(Lang.getPrefix() + Lang.get("Player_does_not_exist"));
                                            } else {
                                                List<TempWarp> warps = TempWarpManager.getManager().getWarps(id);
                                                drawList((Player) sender, argument, warps);
                                                warps.clear();
                                            }
                                        }
                                    });
                                }
                            }.setHover(Lang.get("Click_Hover")));

                            message.setTimeOut(60);
                            message.send((Player) sender);
                        }
                    } else {
                        sender.sendMessage(Lang.getPrefix() + Lang.get("Player_is_not_online"));
                    }
                } else {
                    if(player.getName().equals(sender.getName())) {
                        getComponent("list").runCommand(sender, label, null);
                    } else {
                        List<TempWarp> warps = TempWarpManager.getManager().getWarps(player);
                        drawList((Player) sender, player.getName(), warps);
                        warps.clear();
                    }
                }

                return false;
            }
        });

        getBaseComponent().addChild(new CommandComponent("info") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + "§7" + Lang.get("Use") + ": /" + label + " info §e<identifier>");
                return false;
            }
        });

        getComponent("info").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                List<TempWarp> list = TempWarpManager.getManager().getWarps();
                for(TempWarp warp : list) {
                    suggestions.add(warp.getIdentifier());
                }
                list.clear();
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                TempWarp warp = TempWarpManager.getManager().getWarp(argument);

                if(warp == null) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("WARP_DOES_NOT_EXISTS"));
                    return false;
                }

                drawInfo((Player) sender, warp);
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
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
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

    private boolean isProtected(Player player) {
        PermissionPlayer check = new PermissionPlayer(player);
        BlockBreakEvent event = new BlockBreakEvent(player.getLocation().getBlock(), check);
        Bukkit.getPluginManager().callEvent(event);
        return event.isCancelled();
    }

    private static void drawList(Player player, String owner, List<TempWarp> list) {
        if(list.isEmpty()) {
            if(player.getName().equals(owner)) {
                player.sendMessage(Lang.getPrefix() + Lang.get("TempWarp_No_Warps"));
            } else {
                player.sendMessage(Lang.getPrefix() + Lang.get("TempWarp_No_Warps_Other"));
            }
            return;
        }


        List<String> l = new ArrayList<>();

        l.add(" ");
        l.add("§7§l§m---------------------------------------------");
        l.add(" ");

        if(player.getName().equals(owner)) {
            l.add("  " + Lang.get("TempWarp_You_have_n_Warps").replace("%AMOUNT%", list.size() + ""));
        } else {
            l.add("  " + Lang.get("TempWarp_Player_have_n_Warps").replace("%AMOUNT%", list.size() + "").replace("%PLAYER%", owner));
        }

        l.add(" ");
        for(TempWarp warp : list) {
            l.add("  §7\"§f" + warp.getName() + "§7\" §8(" + (warp.isPublic() ? "§a" + Lang.get("Public") : "§c" + Lang.get("Private")) + "§8)§7: §b" + (warp.getLeftTime() <= 0 ? "§c" + Lang.get("Expired") + " » " + Lang.get("TempWarp_List_deleted_in").replace("%TIME_LEFT%", "" + TempWarpManager.getManager().convertInTimeFormat(TempWarpManager.getManager().getInactiveTime() * 1000 + warp.getLeftTime(), TimeUnit.MILLISECONDS)) : TempWarpManager.getManager().convertInTimeFormat(warp.getLeftTime(), TimeUnit.MILLISECONDS) + " §7" + Lang.get("Remaining")));
        }

        l.add(" ");
        l.add("§7§l§m---------------------------------------------");

        player.sendMessage(l.toArray(new String[0]));

        l.clear();
    }

    private static void drawInfo(Player player, TempWarp tempWarp) {
        List<String> l = new ArrayList<>();

        l.add(" ");
        l.add("§7§l§m---------------------------------------------");
        l.add(" ");

        //Name, Public/Private, Owner (UUID and Name), BornDate, EndDate, Location, TeleportCosts

        l.add("  §6TempWarp§7: \"§r" + tempWarp.getName() + "§7\"");
        l.add("  §6World§7: §b" + (tempWarp.isAvailable() ? tempWarp.getLocation().getWorld().getName() : tempWarp.getLocation().getWorldName() + " §8(§cMissing§8)"));
        l.add("  §6State§7: " + (tempWarp.isPublic() ? "§aPublic" : "§cPrivate"));
        l.add("  §6Teleport-Costs§7: §c" + tempWarp.getTeleportCosts() + " Coin(s)");
        l.add(" ");
        l.add("  §6Owner§7: \"§b" + tempWarp.getLastKnownName() + "§7\" §8(§7" + tempWarp.getOwner().toString() + "§8)");
        l.add(" ");
        l.add("  §6Created§7: §a" + TempWarpManager.getManager().convertInTimeFormat(new Date().getTime() - tempWarp.getBornDate().getTime(), TimeUnit.MILLISECONDS) + " ago");
        l.add("  §6End in§7: §c" + TempWarpManager.getManager().convertInTimeFormat(tempWarp.getLeftTime(), TimeUnit.MILLISECONDS));

        l.add(" ");
        l.add("§7§l§m---------------------------------------------");

        if(!tempWarp.isAvailable()) {
            l.add(" ");
            l.add(TempWarpManager.ERROR_NOT_AVAILABLE(tempWarp.getIdentifier()));
            l.add(" ");

        }

        player.sendMessage(l.toArray(new String[0]));

        l.clear();
    }
}
