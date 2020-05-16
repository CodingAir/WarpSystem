package de.codingair.warpsystem.spigot.features.randomteleports.utils;

import de.codingair.codingapi.server.Environment;
import de.codingair.codingapi.tools.Area;
import de.codingair.codingapi.tools.Callback;
import de.codingair.codingapi.tools.Location;
import de.codingair.codingapi.utils.Value;
import de.codingair.warpsystem.spigot.api.players.PermissionPlayer;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.features.randomteleports.managers.RandomTeleporterManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomLocationCalculator implements Runnable {
    private Player player;
    private org.bukkit.Location startLocation;
    private PermissionPlayer check;
    private Callback<Location> callback;
    private long lastReaction = 0;
    private double minRange, maxRange;

    public RandomLocationCalculator(Player player, org.bukkit.Location location, double minRange, double maxRange, Callback<Location> callback) {
        this.player = player;
        this.check = new PermissionPlayer(player);
        this.callback = callback;
        this.startLocation = location;
        this.minRange = minRange;
        this.maxRange = maxRange;
    }

    @Override
    public void run() {
        Location location = null;
        try {
            location = calculate(this.player);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        if(location != null) {
            location.setX(location.getBlockX() + 0.5);
            location.setY(location.getBlockY() + 0.5);
            location.setZ(location.getBlockZ() + 0.5);
        }
        callback.accept(location);
    }

    private Location calculate(Player player) throws InterruptedException {
        long start = System.currentTimeMillis();
        Location location = new Location(startLocation);

        double x = startLocation.getX();
        double z = startLocation.getZ();

        Random r = new Random();

        long maxTime = (long) ((maxRange - minRange) / 2);
        if(maxTime < 1000) maxTime = 1000;
        if(maxTime > 5000) maxTime = 5000;

        do {
            location.setY(startLocation.getY());
            lastReaction = System.currentTimeMillis();

            double xNext = r.nextDouble() * (maxRange - minRange) + minRange;
            double zNext = r.nextDouble() * (maxRange - minRange) + minRange;

            if(r.nextBoolean()) xNext *= -1;
            if(r.nextBoolean()) zNext *= -1;

            location.setX(x + xNext);
            location.setZ(z + zNext);

            if(start + maxTime < System.currentTimeMillis()) {
                return null;
            }

            if(correct(location, false)) location.setY(calculateYCoord(location));
        } while(!checkY(location) || !correct(location, true));
        return location;
    }

    private boolean isSafeLocation(Location location) {
        return Environment.canBeEntered(location.getBlock().getType()) && Environment.canBeEntered(location.clone().add(0, 1, 0).getBlock().getType());
    }

    private boolean checkY(Location location) {
        return location.getY() <= getHighestY(location.getWorld()) && location.getY() > 0;
    }

    private int getHighestY(World w) {
        switch(w.getEnvironment()) {
            case NETHER:
                return RandomTeleporterManager.getInstance().getNetherHeight();
            case THE_END:
                return RandomTeleporterManager.getInstance().getEndHeight();
            default:
                return 72;
        }
    }

    private int calculateYCoord(Location location) {
        Location loc = location.clone();
        if(location.getWorld().getEnvironment() != World.Environment.NORMAL) loc.setY(getHighestY(loc.getWorld()));

        if(location.getWorld().getEnvironment() == World.Environment.NETHER) {
            int free = 0;
            while(free < 2) {
                if(loc.getBlock().getType() == Material.AIR) free++;

                loc.setY(loc.getY() - 1);
            }

            while(loc.getBlock().getType() == Material.AIR && loc.getBlockY() > 0) {
                loc.setY(loc.getY() - 1);
            }

            loc.setY(loc.getY() + 1);
            return loc.getBlockY();
        } else {
            if(loc.getBlock().getType() != Material.AIR) {
                while(loc.getBlock().getType() != Material.AIR) {
                    loc.setY(loc.getY() + 5);
                }

                while(loc.getBlock().getType() == Material.AIR) {
                    loc.setY(loc.getY() - 1);
                }

                loc.setY(loc.getY() + 1);
                return loc.getBlockY();
            } else {
                while(loc.getBlock().getType() == Material.AIR && loc.getBlockY() > 0) {
                    loc.setY(loc.getY() - 5);
                }

                if(loc.getBlockY() > 0) {
                    while(loc.getBlock().getType() != Material.AIR) {
                        loc.setY(loc.getY() + 1);
                    }
                }

                return loc.getBlockY();
            }
        }
    }

    private boolean correct(Location location, boolean safety) throws InterruptedException {
        if(RandomTeleporterManager.getInstance().getBiomeList() != null && !RandomTeleporterManager.getInstance().getBiomeList().contains(location.getWorld().getBiome(location.getBlockX(), location.getBlockZ())))
            return false;
        if(RandomTeleporterManager.getInstance().isProtectedRegions() && isProtected(location)) return false;
        if(RandomTeleporterManager.getInstance().isWorldBorder() && !isInsideOfWorldBorder(location)) return false;
        if(safety) {
            Location above = location.clone();
            above.setY(above.getY() + 1);
            Location below = location.clone();
            below.setY(below.getY() - 1);


            return isSafeLocation(location) && isSafe(above) && isSafe(location) && isSafe(below);
        } else return true;
    }

    private boolean isSafe(Location location) {
        Block b = location.getBlock();

        List<String> unsafe = new ArrayList<>();

        unsafe.add("VOID");
        unsafe.add("LAVA");
        unsafe.add("FIRE");
        unsafe.add("MAGMA");

        for(String s : unsafe) {
            if(b.getType().name().toUpperCase().contains(s)) return false;
        }

        return true;
    }

    private boolean isInsideOfWorldBorder(Location location) {
        WorldBorder border = location.getWorld().getWorldBorder();
        return border == null || Area.isInArea(location, border.getCenter(), border.getSize() / 2, false, 0);
    }

    private boolean isProtected(Location location) throws InterruptedException {
        synchronized(this) {
            Value<BlockBreakEvent> eventValue = new Value<>(null);
            Bukkit.getScheduler().runTask(WarpSystem.getInstance(), () -> {
                BlockBreakEvent event = new BlockBreakEvent(location.getBlock(), this.check); //check is a bukkit/Player instance
                eventValue.setValue(event);
                Bukkit.getPluginManager().callEvent(event);

                synchronized(RandomLocationCalculator.this) {
                    RandomLocationCalculator.this.notify();
                }
            });

            this.wait();
            return eventValue.getValue().isCancelled();
        }
    }

    public long getLastReaction() {
        return lastReaction;
    }
}
