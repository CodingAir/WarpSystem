package de.codingair.warpsystem.spigot.features.playerwarps.commands;

import de.codingair.codingapi.player.chat.ChatButton;
import de.codingair.codingapi.player.chat.SimpleMessage;
import de.codingair.codingapi.player.gui.anvil.*;
import de.codingair.codingapi.player.gui.inventory.gui.GUI;
import de.codingair.codingapi.server.commands.builder.CommandComponent;
import de.codingair.codingapi.server.commands.builder.MultiTextCommandComponent;
import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.language.Lang;
import de.codingair.warpsystem.spigot.base.utils.commands.WarpSystemBaseComponent;
import de.codingair.warpsystem.spigot.base.utils.commands.WarpSystemCommandBuilder;
import de.codingair.warpsystem.spigot.base.utils.money.MoneyAdapterType;
import de.codingair.warpsystem.spigot.features.playerwarps.guis.editor.PWEditor;
import de.codingair.warpsystem.spigot.features.playerwarps.guis.list.PWList;
import de.codingair.warpsystem.spigot.features.playerwarps.managers.PlayerWarpManager;
import de.codingair.warpsystem.spigot.features.playerwarps.utils.PWMultiCommandComponent;
import de.codingair.warpsystem.spigot.features.playerwarps.utils.PlayerWarp;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CPlayerWarps extends WarpSystemCommandBuilder {
    public CPlayerWarps() {
        super("playerwarps", new WarpSystemBaseComponent(WarpSystem.PERMISSION_USE_PLAYER_WARPS) {
            @Override
            public void unknownSubCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + "§7" + Lang.get("Use") + ": /" + label + " §e<create, edit, delete, list>");
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                new PWList((Player) sender).open();
                return false;
            }
        }.setOnlyPlayers(true), "pwarps", "pws");

        //list, create, edit, delete, setAsOwnRespawn

        getBaseComponent().addChild(new CommandComponent("delete") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                sender.sendMessage(Lang.getPrefix() + "§7" + Lang.get("Use") + ": /" + label + " delete §e<warp>");
                return false;
            }
        });

        getComponent("delete").addChild(new MultiTextCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                if(sender.hasPermission(WarpSystem.PERMISSION_MODIFY_PLAYER_WARPS)) {
                    PlayerWarpManager.getManager().interactWithWarps(new Callback<PlayerWarp>() {
                        @Override
                        public void accept(PlayerWarp warp) {
                            suggestions.add(warp.getOwner().getName() + "." + warp.getName(false).replace(" ", "_"));
                            if(!suggestions.contains(warp.getOwner().getName())) suggestions.add(warp.getOwner().getName());
                        }
                    });
                } else {
                    List<PlayerWarp> l = PlayerWarpManager.getManager().getOwnWarps((Player) sender);

                    for(PlayerWarp warp : l) {
                        suggestions.add(warp.getName(false).replace(" ", "_"));
                    }
                }
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                PlayerWarp warp = PlayerWarpManager.getManager().getWarp((Player) sender, argument);

                if(warp == null) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("WARP_DOES_NOT_EXISTS"));
                    return false;
                }

                if(!warp.isOwner((Player) sender) && !sender.hasPermission(WarpSystem.PERMISSION_MODIFY_PLAYER_WARPS)) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Warp_no_access"));
                    return false;
                }

                SimpleMessage message = new SimpleMessage(Lang.getPrefix() + Lang.get("Warp_Delete_Info").replace("%NAME%", warp.getName()), WarpSystem.getInstance());

                List<String> lore = Lang.getStringList("Warp_Delete_Button_Info");
                List<String> prepared = new ArrayList<>();
                for(String s : lore) {
                    if(!PlayerWarpManager.getManager().isEconomy() && s.contains("REFUND")) continue;

                    prepared.add(s
                            .replace("%REFUND%", cut(PlayerWarpManager.getManager().calculateRefund(warp)) + "")
                            .replace("%NAME%", warp.getName())
                    );
                }

                message.replace("%HERE%", new ChatButton(Lang.get("Warp_Delete_Info_Here"), prepared) {
                    @Override
                    public void onClick(Player player) {
                        double refund = PlayerWarpManager.getManager().delete(warp, true);
                        if(refund == -1) return;

                        if(refund > 0 && PlayerWarpManager.getManager().isEconomy() && warp.isOwner(player)) {
                            MoneyAdapterType.getActive().deposit((Player) sender, refund);
                            sender.sendMessage(Lang.getPrefix() + Lang.get("Warp_Deleted_Info").replace("%NAME%", warp.getName(true)).replace("%PRICE%", CPlayerWarps.cut(refund) + ""));
                        } else sender.sendMessage(Lang.getPrefix() + Lang.get("Warp_was_deleted").replace("%NAME%", warp.getName(true)));

                        message.destroy();
                    }
                });

                message.setTimeOut(60);

                message.send(sender);
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

        getComponent("edit").addChild(new PWMultiCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
                if(sender.hasPermission(WarpSystem.PERMISSION_MODIFY_PLAYER_WARPS)) {
                    PlayerWarpManager.getManager().interactWithWarps(new Callback<PlayerWarp>() {
                        @Override
                        public void accept(PlayerWarp warp) {
                            suggestions.add(warp.getOwner().getName() + "." + warp.getName(false).replace(" ", "_"));
                            if(!suggestions.contains(warp.getOwner().getName())) suggestions.add(warp.getOwner().getName());
                        }
                    });
                } else {
                    List<PlayerWarp> l = new ArrayList<>(PlayerWarpManager.getManager().getOwnWarps((Player) sender));

                    for(PlayerWarp warp : l) {
                        suggestions.add(warp.getName(false).replace(" ", "_"));
                    }

                    l.clear();
                }
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                PlayerWarp warp = PlayerWarpManager.getManager().getWarp((Player) sender, argument);

                if(warp == null) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("WARP_DOES_NOT_EXISTS"));
                    return false;
                }

                if(!warp.isOwner((Player) sender) && !sender.hasPermission(WarpSystem.PERMISSION_MODIFY_PLAYER_WARPS)) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Warp_no_access"));
                    return false;
                }

                new PWEditor((Player) sender, warp).open();
                return false;
            }
        });

        getBaseComponent().addChild(new CommandComponent("create") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                createPlayerWarp((Player) sender, null);
                return false;
            }
        });

        getComponent("create").addChild(new MultiTextCommandComponent() {
            @Override
            public void addArguments(CommandSender sender, String[] args, List<String> suggestions) {
            }

            @Override
            public boolean runCommand(CommandSender sender, String label, String argument, String[] args) {
                if(!PlayerWarpManager.hasPermission((Player) sender)) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Warp_Maximum_of_Warps").replace("%AMOUNT%", PlayerWarpManager.getManager().getOwnWarps((Player) sender).size() + ""));
                    return false;
                }

                if(PlayerWarpManager.isProtected((Player) sender)) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Create_Protected"));
                    return false;
                }

                if(argument.length() < PlayerWarpManager.getManager().getNameMinLength() || argument.length() > PlayerWarpManager.getManager().getNameMaxLength()) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Name_Too_Long_Too_Short").replace("%MIN%", PlayerWarpManager.getManager().getNameMinLength() + "").replace("%MAX%", PlayerWarpManager.getManager().getNameMaxLength() + ""));
                    return false;
                }

                String forbidden = PlayerWarpManager.getManager().checkSymbols(argument, "§c", "§f");
                if(forbidden != null) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Forbidden_Symbols").replace("%NAME_HINT%", forbidden));
                    return false;
                }

                if(PlayerWarpManager.getManager().existsOwn((Player) sender, argument)) {
                    sender.sendMessage(Lang.getPrefix() + Lang.get("Name_Already_Exists"));
                    return false;
                }

                new PWEditor((Player) sender, argument).open();
                return false;
            }
        });

        getBaseComponent().addChild(new CommandComponent("list") {
            @Override
            public boolean runCommand(CommandSender sender, String label, String[] args) {
                new PWList((Player) sender).open();
                return false;
            }
        });
    }

    public static Number cut(double n) {
        if(n == (int) n) return (int) n;
        else return ((double) (int) (n * 100)) / 100;
    }

    public static void createPlayerWarp(Player p, GUI fallBack) {
        if(!PlayerWarpManager.hasPermission(p)) {
            p.sendMessage(Lang.getPrefix() + Lang.get("Warp_Maximum_of_Warps").replace("%AMOUNT%", PlayerWarpManager.getManager().getOwnWarps(p).size() + ""));
            return;
        }

        if(PlayerWarpManager.isProtected(p)) {
            p.sendMessage(Lang.getPrefix() + Lang.get("Create_Protected"));
            return;
        }

        AnvilGUI.openAnvil(WarpSystem.getInstance(), p, new AnvilListener() {
            @Override
            public void onClick(AnvilClickEvent e) {
                if(!e.getSlot().equals(AnvilSlot.OUTPUT)) return;
                String input = e.getInput();

                if(input == null) {
                    e.getPlayer().sendMessage(Lang.getPrefix() + Lang.get("Enter_Name"));
                    return;
                }

                String forbidden = PlayerWarpManager.getManager().checkSymbols(input, "§c", "§f");
                if(forbidden != null) {
                    p.sendMessage(Lang.getPrefix() + Lang.get("Forbidden_Symbols").replace("%NAME_HINT%", forbidden));
                    return;
                }

                if(PlayerWarpManager.getManager().existsOwn(p, input)) {
                    e.getPlayer().sendMessage(Lang.getPrefix() + Lang.get("Name_Already_Exists"));
                    return;
                }

                e.setClose(true);
            }

            @Override
            public void onClose(AnvilCloseEvent e) {
                if(e.isSubmitted()) {
                    e.setPost(() -> {
                        if(fallBack != null) {
                            GUI g = new PWEditor(e.getPlayer(), e.getSubmittedText());
                            g.setFallbackGUI(fallBack);
                            fallBack.changeGUI(g, true);
                        } else new PWEditor(e.getPlayer(), e.getSubmittedText()).open();
                    });
                } else if(fallBack != null) fallBack.open();
            }
        }, new ItemBuilder(XMaterial.NAME_TAG).setName(Lang.get("Name") + "...").getItem());
    }
}
