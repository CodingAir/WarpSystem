package de.codingair.warpsystem.spigot.base.editor;

import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class Layout extends de.codingair.codingapi.player.gui.inventory.gui.simple.Layout {
    public Layout() {
        super(27);
    }

    @Override
    public void initialize() {
        ItemStack dp = new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE).setHideName(true).getItem();
        ItemStack gp = new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE).setHideName(true).getItem();

        addLine(0, 0, 0, 2, dp, false);
        addLine(7, 0, 7, 2, dp, false);
        addLine(1, 0, 6, 0, gp, false);

        addLine(1, 2, 6, 2, new ItemStack(Material.AIR), true);
    }
}
