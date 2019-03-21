package de.codingair.warpsystem.spigot.features.tempwarps.guis;

import com.earth2me.essentials.Warps;
import de.codingair.codingapi.player.gui.anvil.AnvilClickEvent;
import de.codingair.codingapi.player.gui.anvil.AnvilCloseEvent;
import de.codingair.codingapi.player.gui.anvil.AnvilGUI;
import de.codingair.codingapi.player.gui.anvil.AnvilListener;
import de.codingair.codingapi.player.gui.inventory.gui.GUI;
import de.codingair.codingapi.player.gui.inventory.gui.GUIListener;
import de.codingair.codingapi.player.gui.inventory.gui.Skull;
import de.codingair.codingapi.player.gui.inventory.gui.itembutton.ItemButton;
import de.codingair.codingapi.player.gui.inventory.gui.itembutton.ItemButtonOption;
import de.codingair.codingapi.player.gui.inventory.gui.simple.SyncButton;
import de.codingair.codingapi.server.Sound;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.tools.items.XMaterial;
import de.codingair.warpsystem.spigot.api.players.Head;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.language.Lang;
import de.codingair.warpsystem.spigot.features.FeatureType;
import de.codingair.warpsystem.spigot.features.effectportals.PortalEditor;
import de.codingair.warpsystem.spigot.features.effectportals.managers.PortalManager;
import de.codingair.warpsystem.spigot.features.effectportals.utils.Portal;
import de.codingair.warpsystem.spigot.features.tempwarps.managers.TempWarpManager;
import de.codingair.warpsystem.spigot.features.tempwarps.utils.TempWarp;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GTempWarpList extends GUI {
    private static int MAX_PAGE(Player player) {
        List<TempWarp> warps = TempWarpManager.getManager().getActiveWarps(false);
        List<TempWarp> temp = TempWarpManager.getManager().getWarps(player, true);
        warps.addAll(temp);
        temp.clear();
        int size = warps.size();
        warps.clear();

        if(size == 0) return 0;

        return (int) (Math.ceil((double) size / 44.0) - 1);
    }

    private static String TITLE(Player player, int page) {
        return ChatColor.RED + Lang.get("TempWarps") + " " + ChatColor.GRAY + "- " + ChatColor.RED + "List " + ChatColor.GRAY + "(" + page + "/" + (MAX_PAGE(player) + 1) + ")";
    }

    private static void add(GTempWarpList gui, TempWarp tempWarp, String underline) {
        ItemButtonOption option = new ItemButtonOption();
        option.setClickSound(Sound.CLICK.bukkitSound());
        option.setCloseOnClick(true);

        int slot = gui.firstEmpty();
        if(slot < 0 || slot > 53) return;

        Head head = WarpSystem.getInstance().getHeadManager().getHead(tempWarp.getOwner());

        gui.addButton(new SyncButton(slot) {
            @Override
            public ItemStack craftItem() {
                ItemBuilder builder = new ItemBuilder(head.buildItem());
                String name = "§3" + Lang.get("Name")+": " + de.codingair.codingapi.utils.ChatColor.highlight("§b" + tempWarp.getName(), underline, "§e§n", "§r", true);
                builder.setName(name);

                builder.setLore("§3" + Lang.get("Owner") + ": " +  de.codingair.codingapi.utils.ChatColor.highlight("§7" + tempWarp.getLastKnownName(), underline, "§e§n", "§r", true));
                builder.addLore("§3" + Lang.get("Online") + ": §7" + TempWarpManager.getManager().convertInTimeFormat(new Date().getTime() - tempWarp.getBornDate().getTime(), TimeUnit.MILLISECONDS));
                if(tempWarp.getTeleportCosts() > 0) builder.addLore("§3" + Lang.get("Costs") + ": §7" + tempWarp.getTeleportCosts() + " " + Lang.get("Coins"));
                builder.addLore("", "§7» " + Lang.get("Teleport"));
                return builder.getItem();
            }

            @Override
            public void onClick(InventoryClickEvent e, Player player) {
                TempWarpManager.getManager().getTeleportManager().tryToTeleport((Player) e.getWhoClicked(), tempWarp);
            }
        }.setOption(option));
    }

    private String searching;
    private int page = 0;

    public GTempWarpList(Player p) {
        this(p, null);
    }

    public GTempWarpList(Player p, String search) {
        super(p, TITLE(p, 1), 54, WarpSystem.getInstance(), false);

        this.searching = search;
        setBuffering(true);

        addListener(new GUIListener() {
            private BukkitTask task = null;

            @Override
            public void onInvClickEvent(InventoryClickEvent e) {

            }

            @Override
            public void onInvOpenEvent(InventoryOpenEvent e) {
                task = Bukkit.getScheduler().runTaskTimer(WarpSystem.getInstance(), () -> {
                    for(ItemButton value : getButtons().values()) {
                        if(value instanceof SyncButton) {
                            ((SyncButton) value).update();
                        }
                    }

                    p.updateInventory();
                }, 20, 20);
            }

            @Override
            public void onInvCloseEvent(InventoryCloseEvent e) {
                if(task != null) task.cancel();
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

        initialize(p);
    }

    @Override
    public void initialize(Player p) {
        TempWarpManager manager = TempWarpManager.getManager();

        ItemStack ph = new ItemBuilder(XMaterial.BLACK_STAINED_GLASS_PANE).setHideName(true).getItem();
        ItemBuilder search = new ItemBuilder(Material.COMPASS).setName(ChatColor.RED.toString() + (searching == null ? "" : ChatColor.UNDERLINE) + Lang.get("Search"));
        if(searching != null) {
            search.addLore("", ChatColor.GRAY + "» " + Lang.get("Current") + ": '" + ChatColor.YELLOW + searching + ChatColor.GRAY + "'",
                    ChatColor.GRAY + "» " + Lang.get("Rightclick_To_Reset"));
        }

        setItem(2, ph);
        setItem(6, ph);
        addLine(2, 1, 6, 1, ph, true);

        ItemButtonOption option = new ItemButtonOption();
        option.setClickSound(Sound.CLICK.bukkitSound());
        option.setCloseOnClick(true);

        addButton(new ItemButton(3, new ItemBuilder(Skull.ArrowLeft).setName(ChatColor.GRAY + Lang.get("Previous_Page")).getItem()) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if(page == 0) return;
                page--;
                reinitialize(TITLE(p, page + 1));
            }
        });

        addButton(new ItemButton(5, new ItemBuilder(Skull.ArrowRight).setName(ChatColor.GRAY + Lang.get("Next_Page")).getItem()) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if(page == MAX_PAGE(p)) return;
                page++;
                reinitialize(TITLE(p, page + 1));
            }
        });

        addButton(new ItemButton(4, search.getItem()) {
            @Override
            public void onClick(InventoryClickEvent e) {
                if(e.isRightClick()) {
                    searching = null;
                    reinitialize();
                } else if(e.isLeftClick()) {
                    p.closeInventory();

                    AnvilGUI.openAnvil(WarpSystem.getInstance(), p, new AnvilListener() {
                        @Override
                        public void onClick(AnvilClickEvent e) {
                            e.setCancelled(true);
                            e.setClose(true);
                        }

                        @Override
                        public void onClose(AnvilCloseEvent e) {
                            e.setPost(() -> {
                                if(e.isSubmitted()) {
                                    searching = e.getSubmittedText();
                                    page = 0;
                                    reinitialize();
                                }

                                open();
                            });
                        }
                    }, new ItemBuilder(Material.PAPER).setName(Lang.get("Search")).getItem());
                }
            }
        }.setOption(option).setCloseOnClick(false));

        List<TempWarp> warpList;
        if(searching == null) {
            warpList = manager.getActiveWarps(false);
            List<TempWarp> temp = manager.getWarps(getPlayer(), true);
            warpList.addAll(temp);
            temp.clear();
        } else {
            warpList = new ArrayList<>();

            List<TempWarp> temp = manager.getActiveWarps(false);
            List<TempWarp> temp2 = manager.getWarps(getPlayer(), true);
            temp.addAll(temp2);
            temp2.clear();

            for(TempWarp warp : temp) {
                if(ChatColor.stripColor(warp.getName()).toLowerCase().contains(searching.toLowerCase()) || warp.getLastKnownName().toLowerCase().contains(searching.toLowerCase()))
                    warpList.add(warp);
            }

            temp.clear();
        }

        for(int i = page * 44; i < 44 + page * 44; i++) {
            if(warpList.size() <= i) break;

            add(this, warpList.get(i), searching);
        }

        warpList.clear();
    }
}
