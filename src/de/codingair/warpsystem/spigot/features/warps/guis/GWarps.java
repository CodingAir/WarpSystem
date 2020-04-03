package de.codingair.warpsystem.spigot.features.warps.guis;

import de.codingair.codingapi.player.gui.anvil.*;
import de.codingair.codingapi.player.gui.inventory.gui.GUI;
import de.codingair.codingapi.player.gui.inventory.gui.InterfaceListener;
import de.codingair.codingapi.player.gui.inventory.gui.Skull;
import de.codingair.codingapi.player.gui.inventory.gui.itembutton.ItemButton;
import de.codingair.codingapi.player.gui.inventory.gui.itembutton.ItemButtonOption;
import de.codingair.codingapi.player.gui.inventory.gui.simple.SyncButton;
import de.codingair.codingapi.server.sounds.Sound;
import de.codingair.codingapi.server.sounds.SoundData;
import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.tools.items.ItemBuilder;
import de.codingair.codingapi.utils.TextAlignment;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.guis.editor.StandardButtonOption;
import de.codingair.warpsystem.spigot.base.guis.options.OptionsGUI;
import de.codingair.warpsystem.spigot.base.language.Lang;
import de.codingair.warpsystem.spigot.base.utils.featureobjects.actions.Action;
import de.codingair.warpsystem.spigot.base.utils.featureobjects.actions.types.BoundAction;
import de.codingair.warpsystem.spigot.base.utils.featureobjects.actions.types.CostsAction;
import de.codingair.warpsystem.spigot.base.utils.featureobjects.actions.types.WarpAction;
import de.codingair.warpsystem.spigot.base.utils.money.MoneyAdapterType;
import de.codingair.warpsystem.spigot.base.utils.options.specific.WarpGUIOptions;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.Destination;
import de.codingair.warpsystem.spigot.features.FeatureType;
import de.codingair.warpsystem.spigot.features.warps.guis.editor.GEditor;
import de.codingair.warpsystem.spigot.features.warps.guis.utils.GUIListener;
import de.codingair.warpsystem.spigot.features.warps.guis.utils.Task;
import de.codingair.warpsystem.spigot.features.warps.managers.IconManager;
import de.codingair.warpsystem.spigot.features.warps.nextlevel.utils.Icon;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GWarps extends GUI {
    private Icon page;
    private boolean editing;

    private boolean moving = false, cloning = false;
    private ItemStack cursor = null;
    private int oldSlot = -999;
    private Icon cursorIcon = null;
    private boolean showMenu = true;
    private int emptySlots = 0;

    private GUIListener listener;
    private boolean canEdit;
    private String world;
    private List<Class<? extends Icon>> hide;

    private static String getTitle(Icon page, GUIListener listener, Player player) {
        FileConfiguration config = WarpSystem.getInstance().getFileManager().getFile("Config").getConfig();
        String key = player.hasPermission(WarpSystem.PERMISSION_ADMIN) ? "Admin" : "User";

        return listener == null || listener.getTitle() == null ?
                ChatColor.translateAlternateColorCodes('&', (page == null || page.getName() == null ?
                        config.getString("WarpSystem.GUI." + key + ".Title.Standard", "&c&nWarps&r") :
                        config.getString("WarpSystem.GUI." + key + ".Title.In_Category", "&c&nWarps&r &c@%PAGE%").replace("%PAGE%", page.getNameWithoutColor()).replace("%CATEGORY%", page.getNameWithoutColor())))
                : listener.getTitle();
    }

    private static int getSize(Player player) {
        return player.hasPermission(WarpSystem.PERMISSION_ADMIN) ? WarpSystem.getOptions(WarpGUIOptions.class).getAdminSize().getValue() : WarpSystem.getOptions(WarpGUIOptions.class).getUserSize().getValue();
    }

    public GWarps(Player p, Icon page, boolean editing) {
        this(p, page, editing, null);
    }

    public GWarps(Player p, Icon page, boolean editing, GUIListener guiListener, Class<? extends Icon>... without) {
        this(p, page, editing, guiListener, true, without);
    }

    public GWarps(Player p, Icon page, boolean editing, GUIListener guiListener, boolean canEdit, Class<? extends Icon>... without) {
        this(p, page, editing, guiListener, canEdit, p.getLocation().getWorld().getName(), without);
    }

    public GWarps(Player p, Icon page, boolean editing, GUIListener guiListener, boolean canEdit, String world, Class<? extends Icon>... without) {
        super(p, getTitle(page, guiListener, p), getSize(p), WarpSystem.getInstance(), false);
        this.listener = guiListener;
        this.page = page;
        this.editing = editing;
        this.canEdit = canEdit;
        this.world = IconManager.getInstance().boundToWorld() ? world : null;
        this.hide = without == null ? new ArrayList<>() : Arrays.asList(without);

        setBuffering(true);
        setCanDropItems(true);

        Listener listener;
        Bukkit.getPluginManager().registerEvents(listener = new Listener() {

            @EventHandler
            public void onDrop(PlayerDropItemEvent e) {
                Player p = e.getPlayer();

                if(!p.getName().equals(getPlayer().getName()) || !moving) return;

                if(cursor != null && !cursor.getType().equals(Material.AIR) && cursor.getType().equals(e.getItemDrop().getItemStack().getType())) {
                    e.getItemDrop().remove();
                    HandlerList.unregisterAll(this);
                    cursor = null;
                }
            }

        }, WarpSystem.getInstance());

        addListener(new InterfaceListener() {
            @Override
            public void onInvClickEvent(InventoryClickEvent e) {
                if(!cloning && cursorIcon != null && cursorIcon.getPage() == GWarps.this.page && cursorIcon.getSlot() == e.getSlot()) {
                    e.getView().setCursor(new ItemStack(Material.AIR));
                    setMoving(false, e.getSlot());
                    Sound.CLICK.playSound(getPlayer(), 0.7F, 1F);
                    e.setCancelled(true);
                }
            }

            @Override
            public void onDropItem(InventoryClickEvent e) {
                e.setCancelled(true);

                if(moving) {
                    //cancel
                    setMoving(false, oldSlot);
                    e.getView().setCursor(new ItemStack(Material.AIR));
                } else if(cloning) {
                    //cancel
                    cloning = false;
                    oldSlot = -999;
                    cursor = null;
                    cursorIcon = null;

                    e.getView().setCursor(new ItemStack(Material.AIR));
                }
            }

            @Override
            public void onInvOpenEvent(InventoryOpenEvent e) {

            }

            @Override
            public void onInvCloseEvent(InventoryCloseEvent e) {
                e.getView().setCursor(new ItemStack(Material.AIR));
                if(listener != null) HandlerList.unregisterAll(listener);
                if(isClosingByButton()) return;
                if(GWarps.this.listener != null) GWarps.this.listener.onClose();
            }

            @Override
            public void onInvDragEvent(InventoryDragEvent e) {

            }
        });

        initialize(p);
    }

    public void initialize(Player p) {
        IconManager manager = WarpSystem.getInstance().getDataManager().getManager(FeatureType.WARP_GUI);

        ItemButtonOption option = new ItemButtonOption();
        option.setClickSound(new SoundData(Sound.CLICK, 0.7F, 1F));
        option.setOnlyLeftClick(true);

        ItemBuilder noneBuilder;

        if(editing) {
            noneBuilder = new ItemBuilder(Material.BARRIER).setHideStandardLore(true)
                    .setName("§3" + Lang.get("Leftclick") + ": §b" + Lang.get("Set_Icon"))
                    .setLore("§3" + Lang.get("Rightclick") + ": §b" + Lang.get("Fast_Delete"));
        } else {
            noneBuilder = new ItemBuilder(IconManager.getInstance().getBackground()).setHideName(true).setHideStandardLore(true).setHideEnchantments(true);
        }

        ItemStack none = noneBuilder.getItem();

        if(p.hasPermission(WarpSystem.PERMISSION_MODIFY_WARP_GUI) && showMenu && canEdit) {
            ItemBuilder builder = new ItemBuilder(Material.NETHER_STAR).setName(Lang.get("Menu_Help"));

            if(editing) {
                builder.setLore("§0", "§3" + Lang.get("Leftclick") + ": §b" + Lang.get("Quit_Edit_Mode"));
            } else {
                builder.setLore("§0", "§3" + Lang.get("Leftclick") + ": §b" + Lang.get("Edit_Mode"));
            }
            builder.addLore("§3" + Lang.get("Shift_Leftclick") + ": §b" + Lang.get("Set_Background"));
            builder.addLore("");
            builder.addLore("§3" + Lang.get("Rightclick") + ": §b" + Lang.get("Options"));
            builder.addLore("§3" + Lang.get("Shift_Rightclick") + ": §b" + Lang.get("Show_Icon"));

            builder.addEnchantment(Enchantment.DAMAGE_ALL, 1);
            builder.setHideEnchantments(true);

            addButton(new ItemButton(0, builder.getItem()) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    if(moving || cloning) return;

                    if(e.isLeftClick()) {
                        if(e.isShiftClick()) IconManager.getInstance().setBackground(getPlayer().getInventory().getItem(getPlayer().getInventory().getHeldItemSlot()));
                        else editing = !editing;

                        reinitialize();
                        setTitle(getTitle(GWarps.this.page, listener, getPlayer()));
                    } else {
                        if(e.isShiftClick()) {
                            showMenu = !showMenu;
                            reinitialize();
                            setTitle(getTitle(GWarps.this.page, listener, getPlayer()));
                        } else {
                            setClosingForGUI(true);
                            new OptionsGUI(getPlayer()).open();
                        }
                    }
                }
            }.setOption(option).setOnlyLeftClick(false));
        }

        int size = getSize(getPlayer());
        if(page != null) {
            addButton(new ItemButton(size - 9, new ItemBuilder(Skull.ArrowLeft).setName("§c" + Lang.get("Back") + (page.getDepth() > 0 ? " §8(§7" + Lang.get("Shift") + "§8)" : "")).getItem()) {
                @Override
                public void onClick(InventoryClickEvent e) {
                    GWarps.this.page = e.isShiftClick() ? null : page.getPage();
                    reinitialize();
                    setTitle(getTitle(GWarps.this.page, listener, getPlayer()));
                }
            }.setOption(option));
        }

        List<Icon> icons = manager.getIcons(page);
        for(Icon icon : icons) {
            if(icon.isPage() || (!icon.hasPermission() && (hideAll(p) || hideAll(p, "Warp")) && !editing)) continue;
            BoundAction bound = icon.getAction(Action.BOUND_TO_WORLD);

            if(((bound == null && world == null) || (bound != null && world != null && world.equals(bound.getValue())))
                    && (editing || (!icon.hasPermission() || p.hasPermission(icon.getPermission())))
                    && this.cursorIcon != icon) addToGUI(p, icon);
        }

        List<Icon> cIcons = manager.getPages(page);
        for(Icon icon : cIcons) {
            if(!icon.hasPermission() && (hideAll(p) || hideAll(p, "Page")) && !editing) continue;
            BoundAction bound = icon.getAction(Action.BOUND_TO_WORLD);

            if(((bound == null && world == null) || (bound != null && world != null && world.equals(bound.getValue())))
                    && (editing || (!icon.hasPermission() || p.hasPermission(icon.getPermission())))
                    && this.cursorIcon != icon) addToGUI(p, icon);
        }

        emptySlots = 0;
        for(int i = 0; i < size; i++) {
            if(editing) {
                final int slot = i;
                if(slot == oldSlot && cursorIcon != null && !cursorIcon.isPage() && cursorIcon.getPage() == this.page) continue;

                if(getItem(i) == null || getItem(i).getType().equals(Material.AIR)) {
                    emptySlots++;
                    addButton(new ItemButton(i, none.clone()) {
                        @Override
                        public void onClick(InventoryClickEvent clickEvent) {
                            if(cloning) {
                                if(cursorIcon == null) {
                                    cloning = false;
                                    clickEvent.getView().setCursor(new ItemStack(Material.AIR));
                                    return;
                                }

                                if(clickEvent.isLeftClick()) {
                                    IconManager.getInstance().getIcons().add(cursorIcon);
                                    cursorIcon.setPage(GWarps.this.page);
                                    cursorIcon.setSlot(clickEvent.getSlot());
                                    clickEvent.getView().setCursor(new ItemStack(Material.AIR));

                                    oldSlot = -999;
                                    cursor = null;
                                    cursorIcon = null;
                                    cloning = false;

                                    reinitialize();
                                } else if(clickEvent.isRightClick()) {
                                    IconManager.getInstance().getIcons().add(cursorIcon);
                                    cursorIcon.setPage(GWarps.this.page);
                                    cursorIcon.setSlot(clickEvent.getSlot());

                                    cursor.setAmount(cursor.getAmount() - 1);

                                    if(cursor.getAmount() == 0) {
                                        oldSlot = -999;
                                        cursor = null;
                                        cursorIcon = null;
                                        cloning = false;
                                    } else {
                                        cursorIcon = cursorIcon.clone();
                                        cursorIcon.setName(getCopiedName(cursorIcon.getName()));
                                    }

                                    clickEvent.getView().setCursor(cursor == null ? new ItemStack(Material.AIR) : cursor);
                                    reinitialize();
                                }

                                return;
                            } else if(moving) {
                                if(clickEvent.isLeftClick()) {
                                    cursorIcon.setPage(GWarps.this.page);
                                    cursorIcon.setSlot(clickEvent.getSlot());
                                    clickEvent.getView().setCursor(new ItemStack(Material.AIR));
                                    setMoving(false, clickEvent.getSlot());
                                }

                                return;
                            }

                            if(clickEvent.isRightClick()) {
                                clickEvent.getView().setCursor(none.clone());
                                cloning = true;
                            }

                            if(!clickEvent.isLeftClick()) return;

                            ItemStack item = p.getItemInHand();

                            if(item == null || item.getType().equals(Material.AIR)) {
                                p.sendMessage(Lang.getPrefix() + Lang.get("No_Item_In_Hand"));
                                return;
                            }

                            Callback<Boolean> callback = new Callback<Boolean>() {
                                @Override
                                public void accept(Boolean category) {
                                    if(category == null) {
                                        Bukkit.getScheduler().runTask(WarpSystem.getInstance(), new Runnable() {
                                            @Override
                                            public void run() {
                                                GWarps.this.open();
                                            }
                                        });
                                        return;
                                    }

                                    AnvilGUI.openAnvil(WarpSystem.getInstance(), p, new AnvilListener() {
                                        private String input;

                                        @Override
                                        public void onClick(AnvilClickEvent e) {
                                            e.setCancelled(true);
                                            e.setClose(false);

                                            if(e.getSlot().equals(AnvilSlot.OUTPUT)) {
                                                input = e.getInput();
                                                playSound(e.getClickType(), p);

                                                if(input == null) {
                                                    p.sendMessage(Lang.getPrefix() + Lang.get("Enter_Name"));
                                                    return;
                                                }

                                                if(input.contains("@")) {
                                                    p.sendMessage(Lang.getPrefix() + Lang.get("Enter_Correct_Name"));
                                                    return;
                                                }

                                                input = ChatColor.translateAlternateColorCodes('&', input);

                                                if(clickEvent.isRightClick()) {
                                                    StringBuilder builder = new StringBuilder();

                                                    boolean color = false;
                                                    for(char c : input.toCharArray()) {
                                                        builder.append(c);

                                                        if(c == '§') color = true;
                                                        else if(color) {
                                                            builder.append("§n");
                                                            color = false;
                                                        }
                                                    }

                                                    input = builder.toString();
                                                }

                                                if(category) {
                                                    if(manager.existsPage(input)) {
                                                        p.sendMessage(Lang.getPrefix() + Lang.get("Name_Already_Exists"));
                                                        return;
                                                    }
                                                } else {
                                                    if(manager.existsIcon(input)) {
                                                        p.sendMessage(Lang.getPrefix() + Lang.get("Name_Already_Exists"));
                                                        return;
                                                    }
                                                }

                                                input = input.replace("§", "&");

                                                e.setClose(true);
                                            }
                                        }

                                        @Override
                                        public void onClose(AnvilCloseEvent e) {
                                            if(e.isSubmitted())
                                                e.setPost(() -> {
                                                    Icon icon = new Icon(input, item, GWarps.this.page, slot, null);
                                                    icon.setPage(category);

                                                    Icon clone = icon.clone().addAction(new WarpAction(new Destination()));
                                                    new GEditor(p, icon, clone).setFallbackGUI(GWarps.this).setUseFallbackGUI(true).open();
                                                });
                                            else {
                                                Sound.ITEM_BREAK.playSound(p);
                                                e.setPost(() -> new GWarps(p, GWarps.this.page, editing).open());
                                            }
                                        }
                                    }, new ItemBuilder(Material.PAPER).setName(Lang.get("Name") + "...").getItem());
                                }
                            };

                            new GChooseIconType(p, callback).open();
                        }
                    }.setOption(option).setOnlyLeftClick(false));
                }
            } else {
                if(getItem(i) == null || getItem(i).getType().equals(Material.AIR)) setItem(i, none);
            }
        }
    }

    private void addToGUI(Player p, Icon icon) {
        if(icon.isDisabled() && !editing) return;
        IconManager manager = WarpSystem.getInstance().getDataManager().getManager(FeatureType.WARP_GUI);

        if((icon.getSlot() == 0 && showMenu) || icon.getSlot() >= getSize(getPlayer())) return;

        for(Class<? extends Icon> forbidden : this.hide) {
            if(forbidden.isInstance(icon)) return;
        }

        ItemButtonOption option = new StandardButtonOption();

        if(editing || (!icon.hasPermission() || p.hasPermission(icon.getPermission()))) {
            addButton(new SyncButton(icon.getSlot()) {
                private BukkitRunnable runnable;

                @Override
                public ItemStack craftItem() {
                    ItemBuilder iconBuilder = icon.getItemBuilderWithPlaceholders(getPlayer());

                    if(editing) {
                        List<String> commands = icon.hasAction(Action.COMMAND) ? (List<String>) icon.getAction(Action.COMMAND).getValue() : null;
                        List<String> commandInfo = new ArrayList<>();

                        if(commands != null) {
                            for(String command : commands) {
                                commandInfo.add("§7- '" + command + "'");
                            }
                        }

                        String permission = icon.getPermission() == null ? "-" : icon.getPermission();
                        String costs = (icon.getAction(Action.COSTS) == null ? "0" : icon.getAction(CostsAction.class).getValue()) + " " + Lang.get("Coins");

                        if(icon.isDisabled()) {
                            iconBuilder.addText("§8------------");
                            iconBuilder.addText(Lang.get("Icon_Is_Disabled"));
                        }

                        iconBuilder.addText("§8------------");

                        iconBuilder.addText("§7" + Lang.get("Commands") + ": " + (commandInfo.isEmpty() ? "-" : ""));
                        iconBuilder.addText(commandInfo);
                        iconBuilder.addText("§7" + Lang.get("Permission") + ": " + permission);
                        if(MoneyAdapterType.canEnable()) iconBuilder.addText("§7" + Lang.get("Costs") + ": " + costs);
                        iconBuilder.addText("§8------------");
                        iconBuilder.addText("§3" + Lang.get("Leftclick") + ": §7" + Lang.get("Edit"));
                        iconBuilder.addText("§3" + Lang.get("Shift_Leftclick") + ": §7" + Lang.get("Move"));
                        iconBuilder.addText("§3" + Lang.get("Rightclick") + ": §7" + ChatColor.stripColor(Lang.get("Change_Item")));
                        iconBuilder.addText("§3" + Lang.get("Shift_Rightclick") + ": §7" + (runnable != null ? "§4" : "§7") + ChatColor.stripColor(Lang.get("Delete")) + (runnable != null ? " §7(§c" + ChatColor.stripColor(Lang.get("Confirm")) + "§7)" : ""));
                        iconBuilder.addText("§3" + Lang.get("Pick_Block_Click") + ": §7" + ChatColor.stripColor(Lang.get("Copy")));

                        if(!icon.isPage()) {
                            iconBuilder.addText("§8------------");

                            List<String> list = TextAlignment.lineBreak(Lang.get("Move_Help"), 80);
                            iconBuilder.addText(list);
                        }

                        commandInfo.clear();
                    }

                    return iconBuilder.getItem();
                }

                @Override
                public boolean canClick(ClickType click) {
                    return editing || ((!icon.getActions().isEmpty() || icon.isPage()) && click == ClickType.LEFT);
                }

                @Override
                public void onClick(InventoryClickEvent e, Player player) {
                    if(listener != null) {
                        Task task = listener.onClickOnIcon(icon, editing);

                        if(task != null) {
                            task.runTask(p, editing);
                            return;
                        }
                    }

                    if(editing) {
                        if(cloning && cursorIcon == null) {
                            //fast deleting
                            IconManager.getInstance().remove(icon);
                            reinitialize();

                            List<Icon> icons = IconManager.getInstance().getIcons(page);
                            if(icons.isEmpty()) {
                                cloning = false;
                                e.getView().setCursor(new ItemStack(Material.AIR));
                            }
                            icons.clear();
                            return;
                        }

                        if((e.getClick() == ClickType.UNKNOWN || e.getClick() == ClickType.MIDDLE) && emptySlots > 0) {
                            if(!moving && cursorIcon == null && cursor == null) {
                                cloning = true;
                                cursorIcon = icon.clone();

                                cursorIcon.setName(getCopiedName(cursorIcon.getName()));
                                cursor = e.getCurrentItem().clone();
                                cursor.setAmount(emptySlots);

                                e.setCursor(cursor.clone());
                            }
                        } else if(e.isLeftClick()) {
                            if(cloning) {
                                cloning = false;
                                oldSlot = -999;
                                cursor = null;
                                cursorIcon = null;

                                e.getView().setCursor(new ItemStack(Material.AIR));
                            } else if(moving) {
                                if(icon.isPage() && icon.getPage() != cursorIcon.getPage()) return;
                                Icon otherCat = null;
                                if(!cursorIcon.isPage()) {
                                    otherCat = cursorIcon.getPage();
                                    cursorIcon.setPage(GWarps.this.page);
                                }

                                icon.setSlot(oldSlot);
                                icon.setPage(otherCat);
                                cursorIcon.setSlot(e.getSlot());
                                e.getView().setCursor(new ItemStack(Material.AIR));
                                setMoving(false, e.getSlot());
                            } else {
                                if(e.isShiftClick()) {
                                    cursorIcon = icon;
                                    cursor = e.getCurrentItem().clone();
                                    e.setCursor(cursor.clone());
                                    e.setCurrentItem(new ItemStack(Material.AIR));
                                    setMoving(true, e.getSlot());
                                } else {
                                    Icon clone = icon.clone();
                                    if(!clone.hasAction(Action.WARP)) clone.addAction(new WarpAction(new Destination()));
                                    changeGUI(new GEditor(p, icon, clone), true);
                                }
                            }
                        } else if(e.isRightClick()) {
                            if(cloning) {
                                return;
                            } else if(moving) {
                                if(icon.isPage() && !cursorIcon.isPage()) {
                                    GWarps.this.page = icon;
                                    reinitialize();
                                    setTitle(getTitle(GWarps.this.page, listener, getPlayer()));
                                }
                            } else {
                                if(e.isShiftClick()) {
                                    if(runnable != null) {
                                        //delete
                                        manager.remove(icon);
                                        p.sendMessage(Lang.getPrefix() + Lang.get("Icon_Deleted"));

                                        if(!runnable.isCancelled()) runnable.cancel();
                                        runnable = null;
                                        GWarps.this.reinitialize();
                                    } else {
                                        runnable = new BukkitRunnable() {
                                            @Override
                                            public void run() {
                                                runnable = null;
                                                update();
                                            }
                                        };

                                        runnable.runTaskLater(WarpSystem.getInstance(), 20);
                                        update();
                                    }
                                } else {
                                    if(p.getInventory().getItem(p.getInventory().getHeldItemSlot()) == null || p.getInventory().getItem(p.getInventory().getHeldItemSlot()).getType() == Material.AIR
                                            || icon.getRaw().getType() == p.getInventory().getItem(p.getInventory().getHeldItemSlot()).getType()) {
                                        p.sendMessage(Lang.getPrefix() + Lang.get("No_Item_In_Hand"));
                                        return;
                                    }

                                    icon.changeItem(p.getInventory().getItem(p.getInventory().getHeldItemSlot()));
                                    GWarps.this.reinitialize();
                                    updateInventory(p);
                                }
                            }
                        }
                    } else if(e.isLeftClick()) {
                        if(icon.isPage()) {
                            GWarps.this.page = icon;
                            reinitialize();
                            setTitle(getTitle(GWarps.this.page, listener, getPlayer()));
                        } else {
                            if(listener != null) {
                                Task task = listener.onClickOnIcon(icon, editing);

                                if(task != null) {
                                    task.runTask(p, editing);
                                    return;
                                }
                            }
                        }

                        icon.perform(p);
                    }
                }
            }.setOption(option));
        }
    }

    private String getCopiedName(String name) {
        int num = 1;

        name = name.replaceAll("\\p{Blank}\\([0-9]{1,5}?\\)\\z", "");
        name += " (" + num++ + ")";

        while(IconManager.getInstance().getIcon(name) != null) {
            name = name.replaceAll("\\p{Blank}\\([0-9]{1,5}?\\)\\z", "");
            name += " (" + num++ + ")";
        }

        return name;
    }

    private void setMoving(boolean moving, int slot) {
        if(!moving) {
            if(oldSlot != slot) {
                getPlayer().sendMessage(Lang.getPrefix() + Lang.get("Success_Icon_Moved"));
            }

            oldSlot = -999;
            cursor = null;
            cursorIcon = null;
            reinitialize();
            setTitle(getTitle(GWarps.this.page, listener, getPlayer()));
        }

        this.moving = moving;
        this.oldSlot = slot;

        if(moving) {
            for(int i = 0; i < getSize(); i++) {
                if(i == slot || getItem(i) == null || getItem(i).getType().equals(Material.AIR)) continue;

                setItem(i, new ItemBuilder(getItem(i)).setLore("", "§3" + Lang.get("Leftclick") + ": §b" + Lang.get("Move_Icon")).getItem());
            }
        }
    }

    private boolean hideAll(Player player) {
        for(PermissionAttachmentInfo effectivePermission : player.getEffectivePermissions()) {
            String perm = effectivePermission.getPermission();
            if(perm.equalsIgnoreCase(WarpSystem.PERMISSION_HIDE_ALL_ICONS)) return true;
        }
        return false;
    }

    private boolean hideAll(Player player, String type) {
        for(PermissionAttachmentInfo effectivePermission : player.getEffectivePermissions()) {
            String perm = effectivePermission.getPermission();
            if(perm.equalsIgnoreCase(WarpSystem.PERMISSION_HIDE_ALL_ICONS + "." + type)) return true;
        }
        return false;
    }
}
