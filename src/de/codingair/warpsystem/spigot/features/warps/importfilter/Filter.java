package de.codingair.warpsystem.spigot.features.warps.importfilter;

import de.codingair.warpsystem.spigot.features.warps.hiddenwarps.HiddenWarp;

import java.util.List;

public interface Filter {
    Result importData();
    List<String> loadWarpNames();
    HiddenWarp loadWarp(String link);
}
