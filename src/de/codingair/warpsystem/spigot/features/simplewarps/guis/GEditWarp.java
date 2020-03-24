package de.codingair.warpsystem.spigot.features.simplewarps.guis;

import de.codingair.codingapi.player.gui.anvil.AnvilClickEvent;
import de.codingair.codingapi.player.gui.anvil.AnvilCloseEvent;
import de.codingair.codingapi.player.gui.inventory.gui.GUIListener;
import de.codingair.codingapi.player.gui.inventory.gui.itembutton.ItemButtonOption;
import de.codingair.codingapi.player.gui.inventory.gui.simple.*;
import de.codingair.codingapi.server.sounds.Sound;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.language.Lang;
import de.codingair.warpsystem.spigot.features.simplewarps.SimpleWarp;
import de.codingair.warpsystem.spigot.features.simplewarps.managers.SimpleWarpManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemStack;

import java.util.Date;
import java.util.List;

public class GEditWarp extends SimpleGUI {
    public GEditWarp(Player p, SimpleWarp warp) {
        super(p, new GLayout(), new GPage(p, warp), WarpSystem.getInstance());

        addListener(new GUIListener() {
            @Override
            public void onInvClickEvent(InventoryClickEvent e) {
            }

            @Override
            public void onInvOpenEvent(InventoryOpenEvent e) {

            }

            @Override
            public void onInvCloseEvent(InventoryCloseEvent e) {
                if(!isClosingForGUI() && !isClosingByButton() && !((GPage) GEditWarp.this.getMain()).saved) p.sendMessage(Lang.getPrefix() + Lang.get("SimpleWarp_Cancel_Edit"));
            }

            @Override
            public void onInvDragEvent(InventoryDragEvent e) {

            }

            @Override
            public void onMoveToTopInventory(ItemStack item, int oldRawSlot, List<Integer> newRawSlots) {

            }

            @Override
            public void onCollectToCursor(ItemStack item, List<Integer> oldRawSlots, int newRawSlot) {

            }
        });
    }

    private static class GPage extends Page {
        private SimpleWarp warp;
        private SimpleWarp clone;
        private boolean saved = false;

        public GPage(Player p, SimpleWarp warp) {
            super(p, Lang.get("SimpleWarp_Edit_Title").replace("%WARP%", ChatColor.translateAlternateColorCodes('&', warp.getName())), false);
            this.warp = warp;
            this.clone = warp.clone();
            initialize(p);
        }

        @Override
        public void initialize(Player p) {
            ItemButtonOption option = new ItemButtonOption();
            option.setClickSound(Sound.CLICK.bukkitSound());

            addButton(new SyncAnvilGUIButton(2) {
                @Override
                public void onClick(AnvilClickEvent e) {
                    e.setCancelled(true);
                    e.setClose(false);
                    String s = e.getInput();
                    if(s != null && (s.isEmpty() || s.equalsIgnoreCase("none") || s.equalsIgnoreCase("-") || s.equalsIgnoreCase("null"))) s = null;

                    if(s == null) {
                        p.sendMessage(Lang.getPrefix() + Lang.get("Enter_Name"));
                        return;
                    }

                    s = s.replace(" ", "_");

                    if(!s.equalsIgnoreCase(warp.getName()) && !SimpleWarpManager.getInstance().reserveName(s)) {
                        p.sendMessage(Lang.getPrefix() + Lang.get("Name_Already_Exists"));
                        return;
                    }

                    clone.setName(s);
                    clone.setLastChange(new Date());
                    clone.setLastChanger(p.getName());
                    update();
                    e.setClose(true);
                }

                @Override
                public void onClose(AnvilCloseEvent e) {
                    getInterface().reinitialize(Lang.get("SimpleWarp_Edit_Title").replace("%WARP%", ChatColor.translateAlternateColorCodes('&', clone.getName())));
                    e.setPost(() -> getInterface().open());
                    getInterface().setClosingForGUI(false);
                }

                @Override
                public ItemStack craftItem() {
                    ItemBuilder changeName = new ItemBuilder(XMaterial.NAME_TAG);
                    changeName.setName("§7" + Lang.get("Name") + ": §7\"§b" + ChatColor.translateAlternateColorCodes('&', clone.getName()) + "§7\"");
                    changeName.addLore("", Lang.get("SimpleWarp_Change_Name"));
                    return changeName.getItem();
                }

                @Override
                public ItemStack craftAnvilItem(ClickType trigger) {
                    ItemBuilder anvilItem = new ItemBuilder(XMaterial.PAPER);
                    anvilItem.setName(clone.getName());
                    return anvilItem.getItem();
                }
            }.setOption(option));

            addButton(new SyncAnvilGUIButton(4) {
                @Override
                public void onClick(AnvilClickEvent e) {
                    e.setCancelled(true);
                    e.setClose(false);
                    String s = e.getInput();
                    if(s != null && (s.isEmpty() || s.equalsIgnoreCase("none") || s.equalsIgnoreCase("-") || s.equalsIgnoreCase("null"))) s = null;

                    clone.setPermission(s);
                    update();
                    e.setClose(true);
                }

                @Override
                public void onClose(AnvilCloseEvent e) {
                }

                @Override
                public ItemStack craftItem() {
                    ItemBuilder permission = new ItemBuilder(XMaterial.REDSTONE);
                    permission.setName("§7" + Lang.get("Permission") + ": " + (clone.getPermission() == null ? "§c§m-" : "§7\"§b" + clone.getPermission() + "§7\""));
                    if(clone.getPermission() != null) permission.addLore(Lang.get("SimpleWarp_Edit_Permission_Hint"));
                    permission.addLore("", Lang.get("SimpleWarp_Change_Permission"));
                    return permission.getItem();
                }

                @Override
                public ItemStack craftAnvilItem(ClickType trigger) {
                    ItemBuilder anvilItem = new ItemBuilder(XMaterial.PAPER);
                    anvilItem.setName(clone.getPermission() == null ? Lang.get("Permission") + "..." : clone.getPermission());
                    return anvilItem.getItem();
                }
            }.setOption(option));

            addButton(new SyncAnvilGUIButton(6) {
                @Override
                public void onClick(AnvilClickEvent e) {
                    e.setCancelled(true);
                    e.setClose(false);
                    String s = e.getInput();
                    if(s != null && s.isEmpty()) s = null;

                    double price;
                    try {
                        s = s.replace(",", ".");
                        price = Double.parseDouble(s);
                    } catch(Exception ex) {
                        p.sendMessage(Lang.getPrefix() + Lang.get("Enter_A_Positive_Number"));
                        return;
                    }

                    if(price < 0) {
                        p.sendMessage(Lang.getPrefix() + Lang.get("Enter_A_Positive_Number"));
                        return;
                    }

                    clone.setCosts(price);
                    update();
                    e.setClose(true);
                }

                @Override
                public void onClose(AnvilCloseEvent e) {
                }

                @Override
                public ItemStack craftItem() {
                    ItemBuilder price = new ItemBuilder(XMaterial.GOLD_NUGGET);
                    price.setName("§7" + Lang.get("Costs") + "§7: §b" + clone.getCosts() + " " + Lang.get("Coins"));
                    price.addLore("", Lang.get("SimpleWarp_Change_Price"));
                    return price.getItem();
                }

                @Override
                public ItemStack craftAnvilItem(ClickType trigger) {
                    ItemBuilder anvilItem = new ItemBuilder(XMaterial.PAPER);
                    anvilItem.setName(clone.getCosts() + "");
                    return anvilItem.getItem();
                }
            }.setOption(option));

            addButton(new Button(0, new ItemBuilder(XMaterial.RED_TERRACOTTA).setName("§8» §c" + Lang.get("Cancel")).getItem()) {
                @Override
                public void onClick(InventoryClickEvent e, Player player) {
                    p.sendMessage(Lang.getPrefix() + Lang.get("SimpleWarp_Cancel_Edit"));
                }
            }.setOption(option).setCloseOnClick(true));

            addButton(new Button(8, new ItemBuilder(XMaterial.LIME_TERRACOTTA).setName("§8» §a" + Lang.get("Save")).getItem()) {
                @Override
                public void onClick(InventoryClickEvent e, Player player) {
                    saved = true;
                    SimpleWarpManager.getInstance().commitNewName(warp, clone.getName());
                    warp.apply(clone);
                    p.sendMessage(Lang.getPrefix() + Lang.get("SimpleWarp_Save_Edit"));
                    p.closeInventory();
                }
            }.setOption(option));

        }
    }

    private static class GLayout extends Layout {
        public GLayout() {
            super(9);
        }

        @Override
        public void initialize() {
            ItemBuilder blackPane = new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE).setHideName(true);

            setItem(1, blackPane.getItem());
            setItem(7, blackPane.getItem());
        }
    }
}
