package de.codingair.warpsystem.spigot.features.warps.guis.editor.pages;

import de.codingair.codingapi.player.gui.anvil.AnvilClickEvent;
import de.codingair.codingapi.player.gui.anvil.AnvilCloseEvent;
import de.codingair.codingapi.player.gui.anvil.AnvilSlot;
import de.codingair.codingapi.player.gui.inventory.gui.itembutton.ItemButtonOption;
import de.codingair.codingapi.player.gui.inventory.gui.simple.SyncAnvilGUIButton;
import de.codingair.codingapi.player.gui.inventory.gui.simple.SyncButton;
import de.codingair.codingapi.server.Sound;
import de.codingair.codingapi.server.SoundData;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import de.codingair.codingapi.utils.Value;
import de.codingair.warpsystem.spigot.base.guis.editor.Editor;
import de.codingair.warpsystem.spigot.base.guis.editor.PageItem;
import de.codingair.warpsystem.spigot.base.guis.editor.buttons.NameButton;
import de.codingair.warpsystem.spigot.base.language.Lang;
import de.codingair.warpsystem.spigot.features.warps.managers.IconManager;
import de.codingair.warpsystem.spigot.features.warps.nextlevel.utils.Icon;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PAppearance extends PageItem {
    private String startName;
    private Icon icon;
    
    public PAppearance(Player p, Icon icon) {
        super(p, Editor.TITLE_COLOR + Lang.get("Item_Editing"), new ItemBuilder(XMaterial.ITEM_FRAME).setName(Editor.ITEM_TITLE_COLOR + Lang.get("Appearance")).getItem(), false);
        
        this.icon = icon;
        this.startName = this.icon.getName();
        initialize(p);
    }

    @Override
    public void initialize(Player p) {
        ItemButtonOption option = new ItemButtonOption();
        option.setClickSound(new SoundData(Sound.CLICK, 0.7F, 1F));
        option.setOnlyLeftClick(true);

        addButton(new SyncButton(1, 2) {
            @Override
            public ItemStack craftItem() {
                String info = p.getInventory().getItem(p.getInventory().getHeldItemSlot()) == null || p.getInventory().getItem(p.getInventory().getHeldItemSlot()).getType() == Material.AIR ?
                        "§c" + Lang.get("No_Item_In_Hand") :
                        icon.getItem().getType() == p.getInventory().getItem(p.getInventory().getHeldItemSlot()).getType() ?
                                "§c" + Lang.get("Cant_Change_Item")
                                : "§3" + Lang.get("Leftclick") + ": §a" + Lang.get("Change_Item");


                return new ItemBuilder(XMaterial.ITEM_FRAME)
                        .setName("§6§n" + Lang.get("Item"))
                        .setLore("", info)
                        .getItem();
            }

            @Override
            public void onClick(InventoryClickEvent e, Player player) {
                if(e.isLeftClick()) {
                    if(p.getInventory().getItem(p.getInventory().getHeldItemSlot()) == null || p.getInventory().getItem(p.getInventory().getHeldItemSlot()).getType() == Material.AIR
                            || icon.getItem().getType() == p.getInventory().getItem(p.getInventory().getHeldItemSlot()).getType()) return;

                    icon.changeItem(player.getInventory().getItem(player.getInventory().getHeldItemSlot()));
                    getLast().updateShowIcon();
                    update();
                }
            }
        }.setOption(option));

        addButton(new NameButton(2, 2, true, new Value<>(icon.getName())) {
            @Override
            public String acceptName(String name) {
                if(startName != null && startName.equalsIgnoreCase(name)) return null;

                if(icon.isCategory()) {
                    if((icon.getName() == null || !icon.getName().equalsIgnoreCase(name)) && IconManager.getInstance().existsCategory(name)) {
                        return Lang.getPrefix() + Lang.get("Name_Already_Exists");
                    }
                } else {
                    if((icon.getName() == null || !icon.getName().equalsIgnoreCase(name)) && IconManager.getInstance().existsIcon(name)) {
                        return Lang.getPrefix() + Lang.get("Name_Already_Exists");
                    }
                }

                return null;
            }

            @Override
            public void onChange(String old, String name) {
                icon.setName(name);
                getLast().updateShowIcon();
            }
        });

        addButton(new SyncAnvilGUIButton(3, 2, ClickType.LEFT) {
            @Override
            public ItemStack craftItem() {
                List<String> loreOfItem = icon.getItemBuilder().getLore();
                List<String> lore = new ArrayList<>();
                if(loreOfItem == null) lore = null;
                else {
                    for(String s : loreOfItem) {
                        lore.add("§7- '§r" + s + "§7'");
                    }
                }

                List<String> lore2 = new ArrayList<>();
                if(lore != null && !lore.isEmpty()) lore2.add("§3" + Lang.get("Rightclick") + ": §c" + Lang.get("Reset_Lines"));

                return new ItemBuilder(XMaterial.PAPER)
                        .setName("§6§n" + Lang.get("Description"))
                        .setLore("§3" + Lang.get("Current") + ": " + (lore == null || lore.isEmpty() ? "§c" + Lang.get("Not_Set") : ""))
                        .addLore(lore)
                        .addLore("", "§3" + Lang.get("Leftclick") + ": §a" + Lang.get("Add_Line"))
                        .addLore(lore2)
                        .getItem();
            }

            @Override
            public ItemStack craftAnvilItem(ClickType trigger) {
                return new ItemBuilder(Material.PAPER).setName(Lang.get("Line") + "...").getItem();
            }

            @Override
            public void onOtherClick(InventoryClickEvent e) {
                if(e.getClick() == ClickType.RIGHT) {
                    icon.setItem(icon.getItemBuilder().removeLore().getItem());
                    getLast().updateShowIcon();
                    update();
                }
            }

            @Override
            public void onClick(AnvilClickEvent e) {
                if(!e.getSlot().equals(AnvilSlot.OUTPUT)) return;

                String input = e.getInput();

                if(input == null) {
                    p.sendMessage(Lang.getPrefix() + Lang.get("Enter_Lore"));
                    return;
                }

                e.setClose(true);
                playSound(p);

                icon.setItem(icon.getItemBuilder().addLore(ChatColor.WHITE + ChatColor.translateAlternateColorCodes('&', input)).getItem());
                getLast().updateShowIcon();
                update();
            }

            @Override
            public void onClose(AnvilCloseEvent e) {
            }
        }.setOption(option).setOnlyLeftClick(false));

        addButton(new SyncButton(4, 2) {
            @Override
            public ItemStack craftItem() {
                ItemStack sparkle;
                if(icon.getItemBuilder().getEnchantments() == null || icon.getItemBuilder().getEnchantments().size() == 0) {
                    sparkle = new ItemBuilder(Material.BLAZE_POWDER).setName("§6§n" + Lang.get("Sparkle"))
                            .setLore("", "§3" + Lang.get("Leftclick") + ": §a" + Lang.get("Enable"))
                            .getItem();
                } else {
                    sparkle = new ItemBuilder(Material.BLAZE_POWDER).setName("§6§n" + Lang.get("Sparkle"))
                            .setLore("", "§3" + Lang.get("Leftclick") + ": §c" + Lang.get("Disable"))
                            .getItem();
                }

                return sparkle;
            }

            @Override
            public void onClick(InventoryClickEvent e, Player player) {
                if(icon.getItemBuilder().getEnchantments() == null || icon.getItemBuilder().getEnchantments().size() == 0) {
                    icon.setItem(icon.getItemBuilder().setHideStandardLore(true).addEnchantment(Enchantment.DAMAGE_ALL, 1).setHideEnchantments(true).getItem());
                } else {
                    icon.setItem(icon.getItemBuilder().setHideStandardLore(true).removeEnchantments().getItem());
                }

                getLast().updateShowIcon();
                update();
            }
        }.setOption(option));
    }
}
