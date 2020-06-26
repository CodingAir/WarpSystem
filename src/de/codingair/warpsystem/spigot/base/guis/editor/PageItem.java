package de.codingair.warpsystem.spigot.base.guis.editor;

import de.codingair.codingapi.player.gui.inventory.gui.simple.Button;
import de.codingair.codingapi.player.gui.inventory.gui.simple.Page;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public abstract class PageItem extends Page {
    private final ItemStack pageItem;

    public PageItem(Player p, String title, ItemStack pageItem, boolean preInitialize) {
        super(p, title, false);

        this.pageItem = pageItem;

        if(preInitialize) initialize(p);
    }

    public ItemStack getPageItem() {
        return pageItem;
    }

    public Button getPageButton() {
        return new Button(0, getPageItem()) {
            @Override
            public void onClick(InventoryClickEvent e, Player player) {
            }

            @Override
            public boolean canClick(ClickType click) {
                return click == ClickType.LEFT;
            }
        };
    }

    @Override
    public Editor getLast() {
        return (Editor) super.getLast();
    }
}
