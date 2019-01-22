package de.codingair.warpsystem.spigot.base.destinations;

import de.codingair.warpsystem.spigot.base.destinations.adapters.*;

public enum DestinationType {
    UNKNOWN(-1, null),
    WarpIcon(0, WarpIconAdapter.class),
    HiddenWarp(1, HiddenWarpAdapter.class),
    GlobalWarpIcon(2, GlobalWarpIconAdapter.class),
    GlobalWarp(3, GlobalWarpAdapter.class),
    ;

    private int id;
    private Class<? extends DestinationAdapter> adapter;

    DestinationType(int id, Class<? extends DestinationAdapter> adapter) {
        this.id = id;
        this.adapter = adapter;
    }

    public int getId() {
        return id;
    }

    public Class<? extends DestinationAdapter> getAdapter() {
        return adapter;
    }

    public DestinationAdapter getInstance() {
        if(getAdapter() == null) return null;

        try {
            return getAdapter().newInstance();
        } catch(InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
}
