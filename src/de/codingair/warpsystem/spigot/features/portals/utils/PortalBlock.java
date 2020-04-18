package de.codingair.warpsystem.spigot.features.portals.utils;

import de.codingair.codingapi.server.Version;
import de.codingair.codingapi.server.blocks.ModernBlock;
import de.codingair.codingapi.server.blocks.data.Orientable;
import de.codingair.codingapi.tools.Area;
import de.codingair.codingapi.tools.Location;
import de.codingair.codingapi.tools.io.utils.DataWriter;
import de.codingair.codingapi.tools.io.utils.Serializable;
import de.codingair.codingapi.tools.items.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.InvocationTargetException;

public class PortalBlock implements Serializable {
    private Location location;
    private BlockType type;
    private boolean valid = true;
    private de.codingair.warpsystem.spigot.api.blocks.utils.Block instance = null;

    public PortalBlock() {
    }

    public PortalBlock(Location location, BlockType type) {
        this.location = location.clone();
        this.location.trim(0);
        this.type = type;

        try {
            this.location.getBlock();
        } catch(Throwable t) {
            valid = false;
        }
    }

    @Override
    public boolean read(DataWriter d) throws Exception {
        this.location = new Location();
        this.location.read(d);
        this.location.trim(0);

        try {
            this.location.getBlock();
        } catch(Throwable t) {
            valid = false;
        }

        try {
            this.type = BlockType.valueOf(d.getString("type"));
        } catch(Throwable t) {
            this.type = BlockType.WATER;
        }

        return true;
    }

    @Override
    public void write(DataWriter d) {
        this.location.write(d);
        d.put("type", this.type.name());
    }

    @Override
    public void destroy() {
        this.location.destroy();
    }

    public void updateBlock(Portal portal) {
        if(instance != null) {
            instance.destroy();
            instance = null;
        }

        if(!isValid()) return;
        if(portal.isVisible()) {
            if(portal.isEditMode()) {
                if(type.hasEditMaterial()) setEditData(location.getBlock());
            } else {
                if(!type.hasBlockMaterial()) {
                    if(type.getBlock() != null) {
                        try {
                            instance = type.getBlock().getConstructor(org.bukkit.Location.class).newInstance(this.location);
                            instance.create();
                        } catch(NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    if(type == BlockType.NETHER) {
                        new ModernBlock(this.location.getBlock()).setTypeAndData(type.getExactBlockMaterial(), new Orientable(portal.getCachedAxis()));
                    } else if(type == BlockType.END) {
                        if(portal.isVertically() && type.getVerticalBlockMaterial() != null) {
                            this.location.getBlock().setType(type.getExactVerticalBlockMaterial(), true);
                        } else this.location.getBlock().setType(type.getExactBlockMaterial(), false);
                    } else this.location.getBlock().setType(type.getExactBlockMaterial(), false);
                }
            }
        } else if(type.getEditMaterial() != null || type.getBlock() != null || type.getBlockMaterial() != null) this.location.getBlock().setType(Material.AIR, true);
    }

    private void setEditData(Block b) {
        if(Version.getVersion().isBiggerThan(Version.v1_12)) {
            b.setType(type.getExactEditMaterial(), false);
        } else {
            ItemBuilder builder = type.getEditMaterial();
            b.setType(builder.getType(), false);
            b.setData(builder.getData(), false);
        }
    }

    public boolean touches(LivingEntity e) {
        return touches(e, e.getLocation());
    }

    public boolean touches(LivingEntity e, org.bukkit.Location target) {
        return isValid() && Area.isInBlock(e, target, location.getBlock());
    }

    public boolean isValid() {
        return valid;
    }

    public Location getLocation() {
        return location;
    }

    public BlockType getType() {
        return type;
    }
}
