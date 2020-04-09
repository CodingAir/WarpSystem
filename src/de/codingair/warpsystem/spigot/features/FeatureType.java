package de.codingair.warpsystem.spigot.features;

import de.codingair.codingapi.files.ConfigFile;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.bstats.MetricsManager;
import de.codingair.warpsystem.spigot.features.animations.AnimationManager;
import de.codingair.warpsystem.spigot.features.globalwarps.managers.GlobalWarpManager;
import de.codingair.warpsystem.spigot.features.nativeportals.managers.NativePortalManager;
import de.codingair.warpsystem.spigot.features.effectportals.managers.EffectPortalManager;
import de.codingair.warpsystem.spigot.features.playerwarps.managers.PlayerWarpManager;
import de.codingair.warpsystem.spigot.features.randomteleports.managers.RandomTeleporterManager;
import de.codingair.warpsystem.spigot.features.shortcuts.managers.ShortcutManager;
import de.codingair.warpsystem.spigot.features.signs.managers.SignManager;
import de.codingair.warpsystem.spigot.features.teleportcommand.TeleportCommandManager;
import de.codingair.warpsystem.spigot.features.warps.managers.IconManager;
import de.codingair.warpsystem.spigot.features.simplewarps.managers.SimpleWarpManager;
import de.codingair.warpsystem.utils.Manager;

import java.util.ArrayList;
import java.util.List;

public enum FeatureType {
    WARP_GUI(IconManager.class, Priority.HIGHEST, "WarpGUI"),
    GLOBAL_WARPS(GlobalWarpManager.class, Priority.LOW, "GlobalWarps"),
    SIGNS(SignManager.class, Priority.LOWEST, "WarpSigns"),
    EFFECT_PORTALS(EffectPortalManager.class, Priority.LOW, "EffectPortals"),
    NATIVE_PORTALS(NativePortalManager.class, Priority.LOW, "NativePortals"),
    SHORTCUTS(ShortcutManager.class, Priority.LOW, "Shortcuts"),
    SIMPLE_WARPS(SimpleWarpManager.class, Priority.HIGH, "SimpleWarps"),
    RANDOM_TELEPORTS(RandomTeleporterManager.class, Priority.HIGH, "RandomTeleports"),
    TELEPORT_COMMAND(TeleportCommandManager.class, Priority.ALWAYS_ON, "TeleportCommand"),
    ANIMATION_EDITOR(AnimationManager.class, Priority.ALWAYS_ON, "AnimationEditor"),
    PLAYER_WARS(PlayerWarpManager.class, Priority.LOW, "PlayerWarps"),
    METRICS(MetricsManager.class, Priority.LOWEST, "bStats"),
    ;

    private Class<? extends Manager> managerClass;
    private Priority priority;
    private String name;

    FeatureType(Class<? extends Manager> managerClass, Priority priority, String name) {
        this.managerClass = managerClass;
        this.priority = priority;
        this.name = name;
    }

    public Class<? extends Manager> getManagerClass() {
        return managerClass;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        if(getPriority() == Priority.ALWAYS_ON) return true;
        if(getPriority() == Priority.DISABLED) return false;
        ConfigFile file = WarpSystem.getInstance().getFileManager().getFile("Config");
        return file.getConfig().getBoolean("WarpSystem.Functions." + getName(), true);
    }

    public static FeatureType[] values(Priority priority) {
        List<FeatureType> featureTypes = new ArrayList<>();

        for(FeatureType value : values()) {
            if(value.getPriority().equals(priority)) featureTypes.add(value);
        }

        return featureTypes.toArray(new FeatureType[0]);
    }

    public Priority getPriority() {
        return priority;
    }

    public enum Priority {
        ALWAYS_ON,
        HIGHEST,
        HIGH,
        LOW,
        LOWEST,
        DISABLED
    }
}
