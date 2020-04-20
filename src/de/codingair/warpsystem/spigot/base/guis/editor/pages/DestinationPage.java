package de.codingair.warpsystem.spigot.base.guis.editor.pages;

import de.codingair.codingapi.player.gui.inventory.gui.itembutton.ItemButtonOption;
import de.codingair.codingapi.player.gui.inventory.gui.simple.Button;
import de.codingair.codingapi.player.gui.inventory.gui.simple.SimpleGUI;
import de.codingair.codingapi.player.gui.inventory.gui.simple.SyncButton;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.guis.editor.Editor;
import de.codingair.warpsystem.spigot.base.guis.editor.PageItem;
import de.codingair.warpsystem.spigot.base.guis.editor.StandardButtonOption;
import de.codingair.warpsystem.spigot.base.language.Lang;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.Destination;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.DestinationType;
import de.codingair.warpsystem.spigot.features.globalwarps.guis.GGlobalWarpList;
import de.codingair.warpsystem.spigot.features.simplewarps.SimpleWarp;
import de.codingair.warpsystem.spigot.features.simplewarps.guis.GSimpleWarpList;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class DestinationPage extends PageItem {
    private Destination destination;
    private Button[] extra;

    public DestinationPage(Player player, String title, Destination destination, Button... extra) {
        super(player, title, new ItemBuilder(XMaterial.ENDER_PEARL).setName(Editor.ITEM_TITLE_COLOR + Lang.get("Destination")).getItem(), false);
        this.destination = destination;
        this.extra = extra;
        initialize(player);
    }

    @Override
    public boolean initialize(SimpleGUI gui) {
        boolean result = super.initialize(gui);
        updateDestinationButtons();
        return result;
    }

    @Override
    public void initialize(Player p) {
        ItemButtonOption option = new StandardButtonOption();

        int slot = 1;
        if(extra != null && extra.length > 0) {
            for(Button button : extra) {
                if(slot == 7) break;
                button.setSlot(slot++ + 18);
                button.setOption(option);
                addButton(button);
            }
        }

        addButton(new SyncButton(slot++, 2) {
            private int editingOffset = 0;

            @Override
            public ItemStack craftItem() {
                String name = null;
                if(destination.getType() == DestinationType.SimpleWarp) name = destination.getId();

                List<String> lore = new ArrayList<>();
                if(name != null) lore.add("§3" + Lang.get("Rightclick") + ": §c" + Lang.get("Remove"));
                else lore.add("§3" + Lang.get("Shift_Leftclick") + ": §a" + Lang.get("Create") + Lang.PREMIUM_LORE);

                ItemBuilder builder = new ItemBuilder(XMaterial.ENDER_PEARL).setName(Editor.ITEM_TITLE_COLOR + Lang.get("SimpleWarps"))
                        .setLore("§3" + Lang.get("Current") + ": " + (name == null ? "§c" + Lang.get("Not_Set") : "§7'§r" + ChatColor.translateAlternateColorCodes('&', name.replace("_", " ")) + "§7'"),
                                "", "§3" + Lang.get("Leftclick") + ": §a" + (name == null ? Lang.get("Set") : Lang.get("Change")))
                        .addLore(lore);

                builder.addLore(" ");
                builder.addLore("§6" + Lang.get("Max_Random_Offset") + Lang.PREMIUM_LORE);
                builder.addLore("§3" + Lang.get("Shift_Rightclick") + ": §b" + Lang.get("Edit"));
                builder.addLore("  §8» §7X: §e" + (destination.getOffsetX() == 0 ? "0" : destination.getSignedX() == 1 ? "0 - " + destination.getOffsetX() : destination.getSignedX() == 0 ? "-" + destination.getOffsetX() + " - " + destination.getOffsetX() : "-" + +destination.getOffsetX() + " - 0"));
                builder.addLore("  §8» §7Y: §e" + (destination.getOffsetY() == 0 ? "0" : destination.getSignedY() == 1 ? "0 - " + destination.getOffsetY() : destination.getSignedY() == 0 ? "-" + destination.getOffsetY() + " - " + destination.getOffsetY() : "-" + +destination.getOffsetY() + " - 0"));
                builder.addLore("  §8» §7Z: §e" + (destination.getOffsetZ() == 0 ? "0" : destination.getSignedZ() == 1 ? "0 - " + destination.getOffsetZ() : destination.getSignedZ() == 0 ? "-" + destination.getOffsetZ() + " - " + destination.getOffsetZ() : "-" + +destination.getOffsetZ() + " - 0"));

                return builder.getItem();
            }

            @Override
            public void onClick(InventoryClickEvent e, Player player) {
                if(e.isLeftClick()) {
                    if(e.isShiftClick()) {
                        Lang.PREMIUM_CHAT(player);
                    } else {

                        getLast().changeGUI(new GSimpleWarpList(p) {
                            @Override
                            public void onClick(SimpleWarp value, ClickType clickType) {
                                destination.setId(value.getName());
                                destination.setType(DestinationType.SimpleWarp);
                                destination.setAdapter(DestinationType.SimpleWarp.getInstance());
                                updateDestinationButtons();

                                fallBack();
                            }

                            @Override
                            public void onClose() {
                                fallBack();
                            }

                            @Override
                            public void buildItemDescription(List<String> lore) {
                                lore.add("");
                                lore.add("§3" + Lang.get("Leftclick") + ": §b" + Lang.get("Choose"));
                            }
                        }, true);
                    }
                } else if(e.isRightClick()) {
                    if(e.isShiftClick()) {
                        Lang.PREMIUM_CHAT(player);
                    } else {
                        destination.setId(null);
                        destination.setAdapter(null);
                        destination.setType(null);

                        updateDestinationButtons();
                    }
                }
            }
        }.setOption(option));

        if(WarpSystem.getInstance().isOnBungeeCord()) {
            addButton(new SyncButton(slot++, 2) {
                @Override
                public ItemStack craftItem() {
                    String name = null;
                    if(destination.getType() == DestinationType.GlobalWarp) name = destination.getId();

                    List<String> lore = name == null ? null : new ArrayList<>();
                    if(lore != null) lore.add("§3" + Lang.get("Rightclick") + ": §c" + Lang.get("Remove"));

                    return new ItemBuilder(XMaterial.ENDER_EYE).setName(Editor.ITEM_TITLE_COLOR + Lang.get("GlobalWarps"))
                            .setLore("§3" + Lang.get("Current") + ": " + (name == null ? "§c" + Lang.get("Not_Set") : "§7'§r" + ChatColor.translateAlternateColorCodes('&', name) + "§7'"),
                                    "", "§3" + Lang.get("Leftclick") + ": §a" + (name == null ? Lang.get("Set") : Lang.get("Change")))
                            .addLore(lore)
                            .getItem();
                }

                @Override
                public void onClick(InventoryClickEvent e, Player player) {
                    if(e.isLeftClick()) {
                        getLast().setClosingForGUI(true);
                        getLast().changeGUI(new GGlobalWarpList(player) {
                            @Override
                            public void onClick(String warp, ClickType clickType) {
                                destination.setId(warp);
                                destination.setType(DestinationType.GlobalWarp);
                                destination.setAdapter(DestinationType.GlobalWarp.getInstance());
                                updateDestinationButtons();

                                this.setClosingForGUI(true);
                                getLast().open();
                            }

                            @Override
                            public void onClose() {
                            }

                            @Override
                            public void buildItemDescription(List<String> lore) {
                                lore.add("");
                                lore.add("§3" + Lang.get("Leftclick") + ": §b" + Lang.get("Choose"));
                            }
                        }, true);
                    } else if(e.isRightClick()) {
                        destination.setId(null);
                        destination.setAdapter(null);
                        destination.setType(null);

                        updateDestinationButtons();
                    }
                }
            }.setOption(option));

            addButton(new SyncButton(slot++, 2) {
                @Override
                public ItemStack craftItem() {
                    if(destination.getType() == DestinationType.Server) ;

                    return new ItemBuilder(XMaterial.ENDER_CHEST).setName(Editor.ITEM_TITLE_COLOR + Lang.get("Server") + Lang.PREMIUM_LORE)
                            .setLore("§3" + Lang.get("Current") + ": " + "§c" + Lang.get("Not_Set"))
                            .addLore("", "§3" + Lang.get("Leftclick") + ": §a" + (Lang.get("Set")))
                            .getItem();
                }

                @Override
                public void onClick(InventoryClickEvent e, Player player) {
                    Lang.PREMIUM_CHAT(player);
                }
            }.setOption(option));
        }
    }

    public void updateDestinationButtons() {
        for(int i = 1; i < 8; i++) {
            Button button = getButton(i, 2);
            if(button instanceof SyncButton) {
                ((SyncButton) button).update();
            }
        }
    }

    public Destination getDestination() {
        return destination;
    }

    private double trim(double d) {
        return ((double) (int) (d * 100)) / 100;
    }
}
