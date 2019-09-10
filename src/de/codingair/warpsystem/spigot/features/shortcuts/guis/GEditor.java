package de.codingair.warpsystem.spigot.features.shortcuts.guis;

import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import de.codingair.warpsystem.spigot.base.guis.editor.Backup;
import de.codingair.warpsystem.spigot.base.guis.editor.Editor;
import de.codingair.warpsystem.spigot.base.guis.editor.ShowIcon;
import de.codingair.warpsystem.spigot.base.guis.editor.pages.DestinationPage;
import de.codingair.warpsystem.spigot.base.language.Lang;
import de.codingair.warpsystem.spigot.features.shortcuts.guis.pages.POptions;
import de.codingair.warpsystem.spigot.features.shortcuts.managers.ShortcutManager;
import de.codingair.warpsystem.spigot.features.shortcuts.utils.Shortcut;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GEditor extends Editor<Shortcut> {
    public GEditor(Player p, Shortcut shortcut, Shortcut clone) {
        super(p, clone, new Backup<Shortcut>(shortcut) {
            @Override
            public void applyTo(Shortcut clone) {
                boolean reload = !shortcut.getDisplayName().equalsIgnoreCase(clone.getDisplayName());
                shortcut.apply(clone);

                if(ShortcutManager.getInstance().getShortcut(shortcut.getDisplayName()) == null) {
                    reload = true;
                    ShortcutManager.getInstance().getShortcuts().add(shortcut);
                }

                if(reload) ShortcutManager.getInstance().reloadCommand(shortcut);
            }

            @Override
            public void cancel(Shortcut value) {
            }
        }, new ShowIcon() {
            @Override
            public ItemStack buildIcon() {
                return new ItemBuilder(XMaterial.GRAY_STAINED_GLASS_PANE).setHideName(true).getItem();
            }
        }, new POptions(p, clone), new DestinationPage(p, Editor.TITLE_COLOR + Lang.get("Shortcuts"), clone.getDestination()));
    }
}
