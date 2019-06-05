package de.codingair.warpsystem.spigot.features.animations.editor;

import de.codingair.codingapi.particles.Particle;
import de.codingair.codingapi.particles.animations.PlayerAnimation;
import de.codingair.codingapi.player.MessageAPI;
import de.codingair.codingapi.player.gui.hotbar.ClickType;
import de.codingair.codingapi.player.gui.hotbar.HotbarGUI;
import de.codingair.codingapi.player.gui.hotbar.ItemComponent;
import de.codingair.codingapi.player.gui.hotbar.ItemListener;
import de.codingair.codingapi.player.gui.inventory.gui.Skull;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.language.Lang;
import de.codingair.warpsystem.spigot.features.animations.utils.AnimationType;
import de.codingair.warpsystem.spigot.features.animations.utils.ParticlePart;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Particles extends HotbarGUI {
    private Menu menu;
    private List<ParticlePart> parts;
    private AnimationPart[] animations = new AnimationPart[5];

    public Particles(Player player, Menu menu) {
        super(player, WarpSystem.getInstance());

        this.menu = menu;
        this.parts = menu.getClone().getParticleParts();

        for(int i = 0; i < 5; i++) {
            this.animations[i] = new AnimationPart(player, i, menu);
        }

        init(player);
    }

    private void init(Player p) {
        setItem(0, new ItemComponent(new ItemBuilder(Skull.ArrowLeft).setName("§7» §c" + Lang.get("Back") + "§7 «").getItem()).setLink(menu), false);
        setItem(1, new ItemComponent(new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE).setHideName(true).getItem()));

        for(int i = 0; i < 5; i++) {
            int id = i;

            if(i < parts.size() + 1) {
                setItem(id + 2, new ItemComponent(new ItemBuilder(parts.size() >= id + 1 ? XMaterial.NETHER_STAR : XMaterial.BARRIER)
                        .setName("§c" + Lang.get("Animation") + " #" + (id + 1))
                        .getItem(), new ItemListener() {
                    @Override
                    public void onClick(HotbarGUI gui, ItemComponent ic, Player player, ClickType clickType) {
                        if(clickType == ClickType.LEFT_CLICK) {
                            ic.setLink(animations[id]);

                            if(menu.getClone().getParticleParts().size() == id) {
                                menu.getClone().getParticleParts().add(new ParticlePart(AnimationType.CIRCLE, Particle.FLAME, 1, 1, PlayerAnimation.MAX_SPEED));
                                animations[id].init(p);
                                menu.getAnimPlayer().update();

                                ic.setItem(new ItemBuilder(parts.size() >= id + 1 ? XMaterial.NETHER_STAR : XMaterial.BARRIER)
                                        .setName("§c" + Lang.get("Animation") + " #" + (id + 1))
                                        .getItem());

                                init(p);
                            } else animations[id].init(p);
                        } else {
                            ic.setLink(null);
                            if(clickType == ClickType.RIGHT_CLICK && parts.size() >= id + 1) {
                                parts.remove(id);
                                menu.getAnimPlayer().update();
                                onHover(gui, ic, ic, player);
                                init(p);
                            }
                        }
                    }

                    @Override
                    public void onHover(HotbarGUI gui, ItemComponent old, ItemComponent current, Player player) {
                        if(parts.size() >= id + 1) {
                            MessageAPI.sendActionBar(getPlayer(), Menu.ACTION_BAR(parts.get(id).getAnimation().getDisplayName(), "§e" + Lang.get("Edit"), "§c" + Lang.get("Delete")), WarpSystem.getInstance(), Integer.MAX_VALUE);
                        } else MessageAPI.sendActionBar(getPlayer(), "§3" + Lang.get("Leftclick") + ": §a" + Lang.get("Add"), WarpSystem.getInstance(), Integer.MAX_VALUE);
                    }

                    @Override
                    public void onUnhover(HotbarGUI gui, ItemComponent current, ItemComponent newItem, Player player) {
                        MessageAPI.stopSendingActionBar(getPlayer());
                    }
                }).setLink(this.animations[id]));
            } else setItem(id + 2, new ItemComponent(new ItemStack(Material.AIR)));
        }
    }

    public Menu getMenuGUI() {
        return menu;
    }
}