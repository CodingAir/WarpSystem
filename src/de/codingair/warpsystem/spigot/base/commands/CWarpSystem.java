package de.codingair.warpsystem.spigot.base.commands;

import de.codingair.codingapi.API;
import de.codingair.codingapi.player.chat.ChatButton;
import de.codingair.codingapi.player.chat.SimpleMessage;
import de.codingair.codingapi.player.gui.anvil.AnvilClickEvent;
import de.codingair.codingapi.player.gui.anvil.AnvilCloseEvent;
import de.codingair.codingapi.player.gui.anvil.AnvilGUI;
import de.codingair.codingapi.player.gui.anvil.AnvilListener;
import de.codingair.codingapi.player.gui.hotbar.HotbarGUI;
import de.codingair.codingapi.server.commands.BaseComponent;
import de.codingair.codingapi.server.commands.CommandBuilder;
import de.codingair.codingapi.server.commands.CommandComponent;
import de.codingair.codingapi.server.commands.MultiCommandComponent;
import de.codingair.codingapi.server.fancymessages.FancyMessage;
import de.codingair.codingapi.server.fancymessages.MessageTypes;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import de.codingair.codingapi.tools.time.TimeList;
import de.codingair.codingapi.utils.TextAlignment;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.guis.options.GOptions;
import de.codingair.warpsystem.spigot.base.guis.options.OptionsGUI;
import de.codingair.warpsystem.spigot.base.language.Lang;
import de.codingair.warpsystem.spigot.base.utils.BungeeFeature;
import de.codingair.warpsystem.spigot.base.utils.featureobjects.actions.types.WarpAction;
import de.codingair.warpsystem.spigot.base.utils.options.OptionBundle;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.Destination;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.DestinationType;
import de.codingair.warpsystem.spigot.features.animations.AnimationManager;
import de.codingair.warpsystem.spigot.features.animations.editor.*;
import de.codingair.warpsystem.spigot.features.animations.utils.Animation;
import de.codingair.warpsystem.spigot.features.animations.utils.AnimationPlayer;
import de.codingair.warpsystem.spigot.features.globalwarps.managers.GlobalWarpManager;
import de.codingair.warpsystem.spigot.features.shortcuts.guis.GEditor;
import de.codingair.warpsystem.spigot.features.shortcuts.managers.ShortcutManager;
import de.codingair.warpsystem.spigot.features.shortcuts.utils.Shortcut;
import de.codingair.warpsystem.spigot.features.warps.importfilter.ImportType;
import de.codingair.warpsystem.spigot.features.warps.importfilter.Result;
import de.codingair.warpsystem.spigot.features.warps.managers.IconManager;
import de.codingair.warpsystem.spigot.features.warps.simplewarps.SimpleWarp;
import de.codingair.warpsystem.spigot.features.warps.simplewarps.managers.SimpleWarpManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CWarpSystem extends CommandBuilder implements BungeeFeature {
    public CWarpSystem() {
        super("WarpSystem", new BaseComponent(WarpSystem.PERMISSION_MODIFY) {
            @Override
            public void noPermission(CommandSender sender, String label, CommandComponent child) {
                Player p = (Player) sender;
                sendInfoMessage(p);
            }

            @Override
            public void onlyFor(boolean player, CommandSender sender, String label, CommandComponent child) {
                if(player) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Only_For_Players"));
                }
            }

            @Override
            public void unknownSubCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + "§7" + Lang.get("Use") + ": /" + label + " §e<info, reload, import, news, report, shortcut, options, animations>");
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + "§7" + Lang.get("Use") + ": /" + label + " §e<info, reload, import, news, report, shortcut, options, animations>");
                return false;
            }
        }, true);

        getBaseComponent().addChild(new CommandComponent("animations") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                return false;
            }
        });

        getComponent("animations").addChild(new CommandComponent("activate") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + "§7" + Lang.get("Use") + ": /" + label + " animations activate §e<name>");
                return false;
            }
        });

        getComponent("animations", "activate").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                for(Animation animation : AnimationManager.getInstance().getAnimationList()) {
                    suggestions.add(animation.getName());
                }
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                Animation animation = AnimationManager.getInstance().getAnimation(argument);

                if(animation == null) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Animation_does_not_exist"));
                    return false;
                }

                AnimationManager.getInstance().setActive(animation);
                sender.sendMessage(Lang.getPrefix() + "§a" + Lang.get("Changes_have_been_saved"));
                return false;
            }
        });

        getComponent("animations").addChild(new CommandComponent("add") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + "§7" + Lang.get("Use") + ": /" + label + " animations add §e<name>");
                return false;
            }
        }.setOnlyPlayers(true));

        getComponent("animations", "add").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                if(AnimationManager.getInstance().existsAnimation(argument)) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Name_Already_Exists"));
                    return false;
                }

                HotbarGUI h = API.getRemovable((Player) sender, HotbarGUI.class);
                if(h != null) {
                    h.destroy();
                    Menu m = null;

                    if(h instanceof AnimationPart) m = ((AnimationPart) h).getMenuGUI();
                    if(h instanceof BuffPart) m = ((BuffPart) h).getMenuGUI();
                    if(h instanceof Buffs) m = ((Buffs) h).getMenuGUI();
                    if(h instanceof Particles) m = ((Particles) h).getMenuGUI();
                    if(h instanceof Sounds) m = ((Sounds) h).getMenuGUI();
                    if(h instanceof Menu) m = (Menu) h;

                    m.getAnimPlayer().setLoop(false);
                    m.getAnimPlayer().setRunning(false);
                }

                new Menu((Player) sender, argument).open(true);
                return false;
            }
        }.setOnlyPlayers(true));

        getComponent("animations").addChild(new CommandComponent("edit") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + "§7" + Lang.get("Use") + ": /" + label + " animations edit §e<name>");
                return false;
            }
        }.setOnlyPlayers(true));

        getComponent("animations", "edit").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                for(Animation animation : AnimationManager.getInstance().getAnimationList()) {
                    suggestions.add(animation.getName());
                }
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                Animation animation = AnimationManager.getInstance().getAnimation(argument);

                if(animation == null) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Animation_does_not_exist"));
                    return false;
                }

                HotbarGUI h = API.getRemovable((Player) sender, HotbarGUI.class);
                if(h != null) {
                    h.destroy();
                    Menu m = null;

                    if(h instanceof AnimationPart) m = ((AnimationPart) h).getMenuGUI();
                    if(h instanceof BuffPart) m = ((BuffPart) h).getMenuGUI();
                    if(h instanceof Buffs) m = ((Buffs) h).getMenuGUI();
                    if(h instanceof Particles) m = ((Particles) h).getMenuGUI();
                    if(h instanceof Sounds) m = ((Sounds) h).getMenuGUI();
                    if(h instanceof Menu) m = (Menu) h;

                    m.getAnimPlayer().setLoop(false);
                    m.getAnimPlayer().setRunning(false);
                }

                new Menu((Player) sender, animation).open(true);
                return false;
            }
        }.setOnlyPlayers(true));

        getComponent("animations").addChild(new CommandComponent("remove") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + "§7" + Lang.get("Use") + ": /" + label + " animations remove §e<name>");
                return false;
            }
        });

        getComponent("animations", "remove").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                for(Animation animation : AnimationManager.getInstance().getAnimationList()) {
                    suggestions.add(animation.getName());
                }
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                Animation animation = AnimationManager.getInstance().getAnimation(argument);

                if(animation == null) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Animation_does_not_exist"));
                    return false;
                }

                AnimationManager.getInstance().removeAnimation(animation);
                sender.sendMessage(Lang.getPrefix() + Lang.get("Animation_was_removed").replace("%ANIMATION%", animation.getName()));
                return false;
            }
        });

        getBaseComponent().addChild(new CommandComponent("options") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                new OptionsGUI((Player) sender).open();
                return false;
            }
        }.setOnlyPlayers(true));

        getBaseComponent().addChild(new CommandComponent("shortcut") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + "§7" + Lang.get("Use") + ": /" + label + " shortcut §e<add, edit, remove, list>");
                return false;
            }
        }.setOnlyPlayers(true));

        getComponent("shortcut").addChild(new CommandComponent("add") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + "§7" + Lang.get("Use") + ": /" + label + " shortcut add §e<name>");
                return false;
            }
        }.setOnlyPlayers(true));

        getComponent("shortcut", "add").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                Shortcut shortcut = new Shortcut(null, argument);
                Shortcut clone = shortcut.clone();
                clone.addAction(new WarpAction(new Destination()));

                new GEditor((Player) sender, shortcut, clone).open();
                return false;
            }
        }.setOnlyPlayers(true));

        getComponent("shortcut").addChild(new CommandComponent("edit") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + "§7" + Lang.get("Use") + ": /" + label + " shortcut edit §e<name>");
                return false;
            }
        }.setOnlyPlayers(true));

        getComponent("shortcut", "edit").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                for(Shortcut shortcut : ShortcutManager.getInstance().getShortcuts()) {
                    suggestions.add(shortcut.getDisplayName());
                }
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                Shortcut shortcut = ShortcutManager.getInstance().getShortcut(argument);

                if(shortcut == null) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Shortcut_does_not_exist"));
                    return false;
                }

                Shortcut clone = shortcut.clone();
                if(clone.getDestination() == null) clone.addAction(new WarpAction(new Destination()));

                new GEditor((Player) sender, shortcut, clone).open();
                return false;
            }
        }.setOnlyPlayers(true));

        getComponent("shortcut").addChild(new CommandComponent("remove") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + "§7" + Lang.get("Use") + ": /" + label + " shortcut remove §e<name>");
                return false;
            }
        }.setOnlyPlayers(true));

        getComponent("shortcut", "remove").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                for(Shortcut shortcut : ShortcutManager.getInstance().getShortcuts()) {
                    suggestions.add(shortcut.getDisplayName());
                }
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                Shortcut shortcut = ShortcutManager.getInstance().getShortcut(argument);

                if(shortcut == null) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Shortcut_does_not_exist"));
                    return false;
                }

                ShortcutManager.getInstance().getShortcuts().remove(shortcut);
                sender.sendMessage(Lang.getPrefix() + Lang.get("Shortcut_was_removed").replace("%SHORTCUT%", shortcut.getDisplayName()));
                return false;
            }
        });

        getComponent("shortcut").addChild(new CommandComponent("list") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                List<String> message = new ArrayList<>();

                if(ShortcutManager.getInstance().getShortcuts().isEmpty()) {
                    message.add(" ");
                    message.add("  §3§lShortcuts: §c-");
                    message.add(" ");

                    sender.sendMessage(message.toArray(new String[0]));
                    return false;
                }

                for(Shortcut shortcut : ShortcutManager.getInstance().getShortcuts()) {
                    message.add("  §7(" + (shortcut.getDestination().getType() + ") §b" + shortcut.getDestination().getId() + " §7« \"§e" + shortcut.getDisplayName() + "§7\""));
                }

                Collections.sort(message);

                message.add(0, " ");
                message.add(1, " ");
                message.add(2, "  §3§l§nShortcuts");
                message.add(3, " ");
                message.add(" ");

                sender.sendMessage(message.toArray(new String[0]));
                return false;
            }
        });


        getBaseComponent().addChild(new CommandComponent("info") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                Player p = (Player) sender;
                sendInfoMessage(p);
                return false;
            }
        });

        getBaseComponent().addChild(new CommandComponent("news") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                Player p = (Player) sender;

                if(WarpSystem.getInstance().getUpdateNotifier().getDownload() == null) {
                    p.sendMessage(Lang.getPrefix() + "§cFetching data... Please try again.");
                    return false;
                }

                TextComponent tc0 = new TextComponent(Lang.getPrefix() + "§7Click »");
                TextComponent click = new TextComponent("§chere");
                TextComponent tc1 = new TextComponent("§7« to read all new stuff!");

                click.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, WarpSystem.getInstance().getUpdateNotifier().getDownload()));
                click.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new net.md_5.bungee.api.chat.BaseComponent[] {new TextComponent("§7»Click«")}));

                tc0.addExtra(click);
                tc0.addExtra(tc1);

                p.spigot().sendMessage(tc0);
                return false;
            }
        }.setOnlyPlayers(true));

        getBaseComponent().addChild(new CommandComponent("report") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + "§7" + Lang.get("Use") + ": /" + label + " report §e<GitHub, Spigot-Forum, Direct>");
                return false;
            }
        }.setOnlyPlayers(true));

        getComponent("report").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                suggestions.add("GitHub");
                suggestions.add("Spigot-Forum");
                suggestions.add("Direct");
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                switch(argument.toLowerCase()) {
                    case "github":
                        TextComponent base = new TextComponent(Lang.getPrefix() + "§7Click »");
                        TextComponent link = new TextComponent("§chere");
                        TextComponent base1 = new TextComponent("§7« to report the bug to §cGitHub§7.");

                        link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/CodingAir/WarpSystem-IssueTracker/issues/new"));
                        link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new net.md_5.bungee.api.chat.BaseComponent[] {new TextComponent("§7»Click«")}));

                        base.addExtra(link);
                        base.addExtra(base1);

                        ((Player) sender).spigot().sendMessage(base);
                        break;

                    case "spigot-forum":
                        base = new TextComponent(Lang.getPrefix() + "§7Click »");
                        link = new TextComponent("§chere");
                        base1 = new TextComponent("§7« to report the bug to the §6SpigotMc-Forum§7.");

                        link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/threads/" + (WarpSystem.getInstance().isPremium() ? "Premium" : "Free") + "-WarpSystem." + (WarpSystem.getInstance().isPremium() ? WarpSystem.PREMIUM_THREAD_ID : WarpSystem.FREE_THREAD_ID) + "/page-9999"));
                        link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new net.md_5.bungee.api.chat.BaseComponent[] {new TextComponent("§7»Click«")}));

                        base.addExtra(link);
                        base.addExtra(base1);

                        ((Player) sender).spigot().sendMessage(base);
                        break;

                    case "direct":
                        base = new TextComponent(Lang.getPrefix() + "§7Click »");
                        link = new TextComponent("§chere");
                        base1 = new TextComponent("§7« to report the bug to §bme §8(§bCodingAir§8)§7.");

                        link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/conversations/add?to=CodingAir&title=WarpSystem-Bug%20(v" + WarpSystem.getInstance().getDescription().getVersion() + ",%20" + System.getProperty("os.name").replace(" ", "%20") + ")"));
                        link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new net.md_5.bungee.api.chat.BaseComponent[] {new TextComponent("§7»Click«")}));

                        base.addExtra(link);
                        base.addExtra(base1);

                        ((Player) sender).spigot().sendMessage(base);
                        break;

                    default:
                        sender.sendMessage(Lang.getPrefix() + "§7" + Lang.get("Use") + ": /" + label + " report <GitHub, Spigot-Forum>");
                        break;
                }
                return false;
            }
        }.setOnlyPlayers(true));

        getBaseComponent().addChild(new CommandComponent("reload") {
            TimeList<CommandSender> confirm = new TimeList<>();

            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                if(confirm.contains(sender)) {
                    try {
                        sender.sendMessage(Lang.getPrefix() + Lang.get("Plugin_Reloading"));
                        WarpSystem.getInstance().reload(false);
                        sender.sendMessage(Lang.getPrefix() + Lang.get("Success_Plugin_Reloaded"));
                    } catch(Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    sender.sendMessage(Lang.getPrefix() + "§7" + Lang.get("Unsaved_Changes"));
                    confirm.add(sender, 10);
                }
                return false;
            }
        });

        getComponent("reload").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                suggestions.add("true");
                suggestions.add("false");
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                if(argument == null || (!argument.equalsIgnoreCase("true") && !argument.equalsIgnoreCase("false"))) {
                    sender.sendMessage(Lang.getPrefix() + "§7" + Lang.get("Use") + ": /" + label + " §e<true, false>");
                    return false;
                }

                boolean save = argument.equalsIgnoreCase("true");
                sender.sendMessage(Lang.getPrefix() + Lang.get("Plugin_Reloading"));
                WarpSystem.getInstance().reload(save);
                if(save)
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Success_Plugin_Reloaded"));
                else
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Success_Plugin_Reloaded_Without_Saving"));
                return false;
            }
        });

        getBaseComponent().addChild(new CommandComponent("import") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + "§7" + Lang.get("Use") + ": /" + label + " import §e<CategoryWarps, Essentials> [Warp]");
                return false;
            }
        });

        getComponent("import").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                suggestions.add("Essentials");
                suggestions.add("CategoryWarps");
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                ImportType type = null;

                switch(argument.toLowerCase()) {
                    case "categorywarps": {
                        type = ImportType.CATEGORY_WARPS;
                        break;
                    }

                    case "essentials": {
                        type = ImportType.ESSENTIALS;
                        break;
                    }
                }

                if(type == null) {
                    sender.sendMessage(Lang.getPrefix() + "§7" + Lang.get("Use") + ": /" + label + " import §e<CategoryWarps, Essentials> [Warp]");
                } else {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Import_Start"));

                    Result result;
                    int size = (IconManager.getInstance() == null ? 0 : IconManager.getInstance().getIcons().size()) + (SimpleWarpManager.getInstance() == null ? 0 : SimpleWarpManager.getInstance().getWarps().size());
                    if((result = type.importData()).isFinished()) {
                        int amount = (IconManager.getInstance() == null ? 0 : IconManager.getInstance().getIcons().size()) + (SimpleWarpManager.getInstance() == null ? 0 : SimpleWarpManager.getInstance().getWarps().size()) - size;
                        sender.sendMessage(Lang.getPrefix() + Lang.get("Import_Finish").replace("%AMOUNT%", amount + ""));
                    } else {
                        sender.sendMessage(Lang.getPrefix() + Lang.get("Import_Finish_With_Errors") + " §8[" + result.name() + "]");
                    }
                }
                return false;
            }
        });

        getComponent("import", null).addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                switch(args[1].toLowerCase()) {
                    case "essentials": {
                        List<String> l = ImportType.ESSENTIALS.loadWarpNames();
                        suggestions.addAll(l);
                        l.clear();
                        break;
                    }
                }
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                switch(args[1].toLowerCase()) {
                    case "essentials": {
                        SimpleWarp warp = ImportType.ESSENTIALS.loadWarp(argument);

                        if(warp == null) {
                            sender.sendMessage(Lang.getPrefix() + Lang.get("Import_Could_Not_Import_Warp"));
                            return false;
                        }

                        if(IconManager.getInstance().getIcon(warp.getName()) != null || SimpleWarpManager.getInstance().existsWarp(warp.getName())) {
                            sender.sendMessage(Lang.getPrefix() + Lang.get("Name_Already_Exists"));

                            SimpleMessage simpleMessage = new SimpleMessage(Lang.getPrefix() + Lang.get("Import_Choose_New_Name"), WarpSystem.getInstance());

                            simpleMessage.replace("%YES%", new ChatButton("§a" + Lang.get("Yes"), Lang.get("Click_Hover")) {
                                @Override
                                public void onClick(Player player) {
                                    AnvilGUI.openAnvil(WarpSystem.getInstance(), (Player) sender, new AnvilListener() {
                                        @Override
                                        public void onClick(AnvilClickEvent e) {
                                            e.setCancelled(true);
                                            e.setClose(false);

                                            String s = e.getInput();
                                            if(s != null && (s.isEmpty() || s.equalsIgnoreCase("none") || s.equalsIgnoreCase("-") || s.equalsIgnoreCase("null"))) s = null;

                                            if(s == null) {
                                                sender.sendMessage(Lang.getPrefix() + Lang.get("Enter_Name"));
                                                return;
                                            }

                                            if(IconManager.getInstance().getIcon(s) != null || SimpleWarpManager.getInstance().existsWarp(s)) {
                                                sender.sendMessage(Lang.getPrefix() + Lang.get("Name_Already_Exists"));
                                                return;
                                            }

                                            warp.setName(s);
                                            SimpleWarpManager.getInstance().addWarp(warp);
                                            sender.sendMessage(Lang.getPrefix() + Lang.get("Import_Warp_Imported").replace("%WARP%", warp.getName()));
                                            e.setClose(true);
                                        }

                                        @Override
                                        public void onClose(AnvilCloseEvent e) {
                                            if(e.getSubmittedText() == null)
                                                sender.sendMessage(Lang.getPrefix() + Lang.get("Import_Could_Not_Import_Warp"));
                                        }
                                    }, new ItemBuilder(XMaterial.NAME_TAG).setName(Lang.get("Name") + "...").getItem());
                                    simpleMessage.destroy();
                                }
                            });

                            simpleMessage.replace("%NO%", new ChatButton("§c" + Lang.get("No"), Lang.get("Click_Hover")) {
                                @Override
                                public void onClick(Player player) {
                                    sender.sendMessage(Lang.getPrefix() + Lang.get("Import_Could_Not_Import_Warp"));
                                    simpleMessage.destroy();
                                }
                            });

                            simpleMessage.send((Player) sender);
                        } else {
                            SimpleWarpManager.getInstance().addWarp(warp);
                            sender.sendMessage(Lang.getPrefix() + Lang.get("Import_Warp_Imported").replace("%WARP%", warp.getName()));
                        }
                        break;
                    }

                    default: {
                        sender.sendMessage(Lang.getPrefix() + Lang.get("Single_Import_Not_Available"));
                    }
                }
                return false;
            }
        });

        WarpSystem.getInstance().getBungeeFeatureList().add(this);
    }

    @Override
    public void onConnect() {
        if(getComponent("shortcut", "add", "globalwarp") != null) return;

        getComponent("shortcut", "add").addChild(new CommandComponent("globalwarp") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + "§7" + Lang.get("Use") + ": /" + label + " shortcut add globalwarp §e<globalwarp>");
                return false;
            }
        }.setOnlyPlayers(true));

        getComponent("shortcut", "add", "globalwarp").addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                suggestions.addAll(GlobalWarpManager.getInstance().getGlobalWarps().keySet());
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                sender.sendMessage(Lang.getPrefix() + "§7" + Lang.get("Use") + ": /" + label + " shortcut add globalwarp " + argument + " §e<shortcut>");
                return false;
            }
        });

        getComponent("shortcut", "add", "globalwarp", null).addChild(new MultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {

            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                String warpId = args[3];
                String shortcut = argument;

                String globalWarp = GlobalWarpManager.getInstance().getCaseCorrectlyName(warpId);

                if(globalWarp == null) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("WARP_DOES_NOT_EXISTS"));
                    return false;
                }

                if(ShortcutManager.getInstance().getShortcut(shortcut) != null) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Shortcut_already_exists"));
                    return false;
                }

                ShortcutManager.getInstance().getShortcuts().add(new Shortcut(new Destination(globalWarp, DestinationType.GlobalWarp), shortcut));
                sender.sendMessage(Lang.getPrefix() + Lang.get("Shortcut_created").replace("%SHORTCUT%", shortcut));
                return false;
            }
        });
    }

    @Override
    public void onDisconnect() {
        getComponent("shortcut", "add").removeChild("globalwarp");
    }

    private static void sendInfoMessage(CommandSender sender) {
        sender.sendMessage(new String[] {
                "",
                "§7§m               §7< §6WarpSystem §7>§m               §7",
                "",
                "     §3Author: §bCodingAir",
                "     §3Version: §bv" + WarpSystem.getInstance().getDescription().getVersion() + " §7["+ (WarpSystem.getInstance().isPremium() ? "§6Premium" : "§bFree") + "§7]",
                "",
                "     §eAvailable on SpigotMc!",
                ""
        });
    }
}