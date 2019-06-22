package de.codingair.warpsystem.spigot.features.nativeportals;

import de.codingair.codingapi.server.Sound;
import de.codingair.codingapi.server.SoundData;
import de.codingair.codingapi.server.blocks.utils.Axis;
import de.codingair.codingapi.tools.Area;
import de.codingair.warpsystem.spigot.base.utils.featureobjects.FeatureObject;
import de.codingair.warpsystem.spigot.base.utils.featureobjects.actions.Action;
import de.codingair.warpsystem.spigot.base.utils.featureobjects.actions.types.WarpAction;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.Destination;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.DestinationType;
import de.codingair.warpsystem.spigot.features.nativeportals.utils.PortalBlock;
import de.codingair.warpsystem.spigot.features.nativeportals.utils.PortalListener;
import de.codingair.warpsystem.spigot.features.nativeportals.utils.PortalType;
import de.codingair.warpsystem.spigot.features.warps.simplewarps.SimpleWarp;
import de.codingair.warpsystem.spigot.features.warps.simplewarps.managers.SimpleWarpManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.json.simple.JSONArray;
import de.codingair.warpsystem.utils.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Portal extends FeatureObject {
    private PortalType type;
    private boolean editMode = false;

    private List<PortalBlock> blocks;
    private boolean visible = false;
    private List<PortalListener> listeners = new ArrayList<>();

    private Location[] cachedEdges = null;
    private Axis cachedAxis = null;

    private String displayName;

    public Portal() {
        this.type = null;
        this.blocks = new ArrayList<>();
    }

    public Portal(Portal portal) {
        super(portal);
        this.type = portal.getType();
        this.blocks = new ArrayList<>(portal.getBlocks());

        this.listeners.clear();
        this.listeners.addAll(portal.getListeners());

        this.displayName = portal.getDisplayName();
    }

    public Portal(PortalType type, List<PortalBlock> blocks) {
        this.type = type;
        this.blocks = blocks;
    }

    public Portal(PortalType type) {
        this.type = type;
        this.blocks = new ArrayList<>();
    }

    public Portal(PortalType type, Destination destination, String displayName, List<PortalBlock> blocks) {
        super(null, false, new WarpAction(destination));
        this.type = type;
        this.displayName = displayName;
        this.blocks = blocks;
    }

    @Override
    public boolean read(JSONObject json) throws Exception {
        super.read(json);

        if(json.get("Type") != null) {
            this.type = PortalType.valueOf((String) json.get("Type"));
        } else if(json.get("type") != null) {
            this.type = PortalType.valueOf((String) json.get("type"));
        }

        Destination destination = null;

        if(json.get("Destination") != null) {
            //new pattern
            destination = new Destination((String) json.get("Destination"));
        } else if(json.get("Warp") != null || json.get("GlobalWarp") != null) {
            //old pattern
            SimpleWarp warp = json.get("Warp") == null ? null : SimpleWarpManager.getInstance().getWarp((String) json.get("Warp"));
            String globalWarp = json.get("GlobalWarp") == null ? null : (String) json.get("GlobalWarp");

            if(warp != null) {
                destination = new Destination(warp.getName(), DestinationType.SimpleWarp);
            } else {
                destination = new Destination(globalWarp, DestinationType.GlobalWarp);
            }
        }

        if(destination != null) addAction(new WarpAction(destination));

        if(json.get("Name") != null) {
            this.displayName = (String) json.get("Name");
        } else if(json.get("name") != null) {
            this.displayName = (String) json.get("name");
        }

        JSONArray jsonArray = null;
        if(json.get("Blocks") != null) {
            jsonArray = (JSONArray) new JSONParser().parse((String) json.get("Blocks"));
        } else if(json.get("blocks") != null) {
            jsonArray = (JSONArray) new JSONParser().parse((String) json.get("blocks"));
        }

        this.blocks = new ArrayList<>();

        if(jsonArray != null) {
            for(Object o : jsonArray) {
                String data = (String) o;
                Location loc = de.codingair.codingapi.tools.Location.getByJSONString(data);

                if(loc == null || loc.getWorld() == null) {
                    destroy();
                    return false;
                }

                blocks.add(new PortalBlock(loc));
            }

            for(PortalBlock block : blocks) {
                if(block.getLocation().getWorld() == null) {
                    destroy();
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void write(JSONObject json) {
        super.write(json);

        json.put("type", type == null ? null : type.name());
        json.put("name", displayName);

        JSONArray jsonArray = new JSONArray();

        for(PortalBlock block : this.blocks) {
            jsonArray.add(block.getLocation().toJSONString(4));
        }

        json.put("blocks", jsonArray.toJSONString());
    }

    @Override
    public void destroy() {
        super.destroy();

        this.type = null;
        this.cachedEdges = null;
        this.cachedAxis = null;
        this.blocks.clear();
        this.listeners.clear();
    }

    @Override
    public void apply(FeatureObject object) {
        super.apply(object);

        Portal portal = (Portal) object;
        boolean visible = isVisible();
        if(visible) setVisible(false);

        this.cachedEdges = null;
        this.cachedAxis = null;
        this.blocks.clear();
        this.blocks.addAll(portal.getBlocks());
        this.type = portal.getType();
        this.listeners.clear();
        this.listeners.addAll(portal.getListeners());

        if(visible) setVisible(true);
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Portal)) return false;
        Portal portal = (Portal) o;

        return super.equals(o) &&
                blocks.equals(portal.blocks) &&
                listeners.equals(portal.listeners) &&
                type == portal.type;
    }

    @Override
    public FeatureObject perform(Player player) {
        return perform(player, hasAction(Action.WARP) ? getAction(WarpAction.class).getValue().getId() : null, hasAction(Action.WARP) ? getAction(WarpAction.class).getValue() : null, new SoundData(Sound.ENDERMAN_TELEPORT, 1F, 1F), true, true);
    }

    public boolean isInPortal(LivingEntity entity) {
        return isInPortal(entity, entity.getLocation());
    }

    public boolean isInPortal(LivingEntity entity, Location target) {
        if(entity == null || target == null) return false;

        Location[] edges = getCachedEdges();
        if(Area.isInArea(entity, target, edges[0], edges[1])) {
            for(PortalBlock block : getBlocks()) {
                if(block.touches(entity, target)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isAround(Location location, double distance, boolean isExact) {
        if(Area.isInArea(location, getCachedEdges()[0], getCachedEdges()[1], true, distance)) {
            for(PortalBlock block : blocks) {
                if(isExact && block.getLocation().distance(location) == distance) return true;
                else if(block.getLocation().distance(location) <= distance) return true;
            }
        }

        return false;
    }

    public Axis getCachedAxis() {
        if(cachedAxis == null) {
            int x, z;

            Location[] edges = getCachedEdges();

            x = Math.abs(edges[0].getBlockX() - edges[1].getBlockX());
            z = Math.abs(edges[0].getBlockZ() - edges[1].getBlockZ());

            this.cachedAxis = x > z ? Axis.X : Axis.Z;
        }

        return this.cachedAxis;
    }

    public Location[] getCachedEdges() {
        if(cachedEdges != null) return cachedEdges;

        int x0 = 0, y0 = 0, z0 = 0, x1 = 0, y1 = 0, z1 = 0;
        World world = null;

        boolean first = true;
        for(PortalBlock block : this.blocks) {
            if(first) {
                first = false;

                world = block.getLocation().getWorld();

                x0 = block.getLocation().getBlockX();
                y0 = block.getLocation().getBlockY();
                z0 = block.getLocation().getBlockZ();

                x1 = block.getLocation().getBlockX();
                y1 = block.getLocation().getBlockY();
                z1 = block.getLocation().getBlockZ();

                continue;
            }

            if(x0 > block.getLocation().getBlockX()) x0 = block.getLocation().getBlockX();
            else if(x1 < block.getLocation().getBlockX()) x1 = block.getLocation().getBlockX();
            if(y0 > block.getLocation().getBlockY()) y0 = block.getLocation().getBlockY();
            else if(y1 < block.getLocation().getBlockY()) y1 = block.getLocation().getBlockY();
            if(z0 > block.getLocation().getBlockZ()) z0 = block.getLocation().getBlockZ();
            else if(z1 < block.getLocation().getBlockZ()) z1 = block.getLocation().getBlockZ();
        }

        double diff = 0.0;
        return cachedEdges = new Location[] {new Location(world, x0 - diff, y0 - diff, z0 - diff), new Location(world, x1 + 0.999999 + diff, y1 + 0.999999 + diff, z1 + 0.999999 + diff)};
    }

    public boolean isVertically() {
        Vector v = getCachedEdges()[0].toVector().subtract(getCachedEdges()[1].toVector().subtract(new Vector(0.999999, 0.999999, 0.999999)));
        return Math.abs(v.getY()) >= 1;
    }

    public void update() {
        if(getType() == null) return;

        for(PortalBlock block : this.blocks) {
            block.updateBlock(this);
        }
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        if(visible != this.visible) {
            this.visible = visible;
            update();
        }
    }

    public PortalType getType() {
        return type;
    }

    public void setType(PortalType type) {
        if(this.type != type) {
            if(isVisible() && !editMode) {
                setVisible(false);
                this.type = type;
                setVisible(true);
            } else {
                this.type = type;
            }
        }
    }

    public void addPortalBlock(PortalBlock block) {
        this.blocks.add(block);
        this.cachedEdges = null;
        this.cachedAxis = null;
    }

    public void removePortalBlock(PortalBlock block) {
        this.blocks.remove(block);
        this.cachedEdges = null;
        this.cachedAxis = null;
    }

    public void clear() {
        setVisible(false);
        this.blocks.clear();
        this.listeners.clear();
        this.cachedEdges = null;
        this.cachedAxis = null;
    }

    public List<PortalBlock> getBlocks() {
        return Collections.unmodifiableList(this.blocks);
    }

    public List<PortalListener> getListeners() {
        return listeners;
    }

    public Portal clone() {
        return new Portal(this);
    }

    public Destination getDestination() {
        return hasAction(Action.WARP) ? ((WarpAction) getAction(Action.WARP)).getValue() : null;
    }

    public void setDestination(Destination destination) {
        if(destination == null) removeAction(Action.WARP);
        else addAction(new WarpAction(destination));
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
        update();
    }
}
