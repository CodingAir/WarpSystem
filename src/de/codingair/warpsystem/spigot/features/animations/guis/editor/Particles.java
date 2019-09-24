package de.codingair.warpsystem.spigot.features.animations.guis.editor;

import de.codingair.codingapi.particles.Particle;
import de.codingair.codingapi.particles.animations.customanimations.CustomAnimation;
import de.codingair.codingapi.player.MessageAPI;
import de.codingair.codingapi.player.gui.hotbar.ClickType;
import de.codingair.codingapi.player.gui.hotbar.HotbarGUI;
import de.codingair.codingapi.player.gui.hotbar.components.ItemComponent;
import de.codingair.codingapi.player.gui.hotbar.ItemListener;
import de.codingair.codingapi.player.gui.inventory.gui.Skull;
import de.codingair.codingapi.server.Sound;
import de.codingair.codingapi.server.SoundData;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.language.Lang;
import de.codingair.codingapi.particles.animations.customanimations.AnimationType;
import de.codingair.warpsystem.spigot.features.animations.utils.ParticlePart;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class Particles extends HotbarGUI {
    private Menu menu;
    private List<ParticlePart> parts;
    private AnimationPart[] animations = new AnimationPart[1];

    public Particles(Player player, Menu menu) {
        super(player, WarpSystem.getInstance(), 2);

        setOpenSound(new SoundData(Sound.LEVEL_UP, 0.5F, 1F));
        setCloseSound(new SoundData(Sound.LEVEL_UP, 0.5F, 0.5F));
        setClickSound(new SoundData(Sound.CLICK, 0.5F, 1F));

        this.menu = menu;
        this.parts = menu.getClone().getParticleParts();

        this.animations[0] = new AnimationPart(player, 0, menu);

        initialize();
    }

    public void initialize() {
        setItem(0, new ItemComponent(new ItemBuilder(Skull.ArrowLeft).setName("§7» §c" + Lang.get("Back") + "§7 «").getItem()).setLink(menu), false);
        setItem(1, new ItemComponent(new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE).setHideName(true).getItem()));

        setItem(2, new ItemComponent(new ItemStack(Material.AIR)));
        setItem(3, new ItemComponent(new ItemStack(Material.AIR)));

        setItem(2, new ItemComponent(new ItemBuilder(parts.size() >= 1 ? XMaterial.NETHER_STAR : XMaterial.BARRIER)
                .setName("§c" + Lang.get("Animation") + " #" + (1))
                .getItem(), new ItemListener() {
            @Override
            public void onClick(HotbarGUI gui, ItemComponent ic, Player player, ClickType clickType) {
                if(clickType == ClickType.LEFT_CLICK) {
                    ic.setLink(animations[0]);

                    if(menu.getClone().getParticleParts().size() == 0) {
                        menu.getClone().getParticleParts().add(new ParticlePart(AnimationType.CIRCLE, Particle.FLAME, 1, 1, CustomAnimation.MAX_SPEED));
                        animations[0].initialize();
                        menu.getAnimPlayer().update();

                        ic.setItem(new ItemBuilder(parts.size() >= 1 ? XMaterial.NETHER_STAR : XMaterial.BARRIER)
                                .setName("§c" + Lang.get("Animation") + " #" + (1))
                                .getItem());

                        initialize();
                    } else animations[0].initialize();
                } else {
                    ic.setLink(null);
                    if(clickType == ClickType.RIGHT_CLICK && parts.size() >= 1) {
                        parts.remove(0);
                        menu.getAnimPlayer().update();
                        onHover(gui, ic, ic, player);
                        initialize();
                    }
                }
            }

            @Override
            public void onHover(HotbarGUI gui, ItemComponent old, ItemComponent current, Player player) {
                if(parts.size() >= 1) {
                    MessageAPI.sendActionBar(getPlayer(), Menu.ACTION_BAR(parts.get(0).getAnimation().getDisplayName(), "§e" + Lang.get("Edit"), "§c" + Lang.get("Delete")), WarpSystem.getInstance(), Integer.MAX_VALUE);
                } else MessageAPI.sendActionBar(getPlayer(), "§3" + Lang.get("Leftclick") + ": §a" + Lang.get("Add"), WarpSystem.getInstance(), Integer.MAX_VALUE);
            }

            @Override
            public void onUnhover(HotbarGUI gui, ItemComponent current, ItemComponent newItem, Player player) {
                MessageAPI.stopSendingActionBar(getPlayer());
            }
        }).setLink(this.animations[0]));

        if(!this.parts.isEmpty()) {
            setItem(3, new ItemComponent(new ItemBuilder(XMaterial.BARRIER)
                    .setName("§c" + Lang.get("Animation") + " #" + 2)
                    .getItem(), new ItemListener() {
                @Override
                public void onClick(HotbarGUI gui, ItemComponent ic, Player player, ClickType clickType) {
                    Lang.PREMIUM_CHAT(player);
                }

                @Override
                public void onHover(HotbarGUI gui, ItemComponent old, ItemComponent current, Player player) {
                    MessageAPI.sendActionBar(player, Lang.PREMIUM_HOTBAR, WarpSystem.getInstance(), Integer.MAX_VALUE);
                }

                @Override
                public void onUnhover(HotbarGUI gui, ItemComponent current, ItemComponent newItem, Player player) {
                    MessageAPI.stopSendingActionBar(player);
                }
            }));
        }
    }

    public Menu getMenuGUI() {
        return menu;
    }

    public AnimationPart[] getAnimations() {
        return animations;
    }
}