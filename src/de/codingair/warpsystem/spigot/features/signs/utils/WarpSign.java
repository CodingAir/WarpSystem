package de.codingair.warpsystem.spigot.features.signs.utils;

import de.codingair.codingapi.tools.Location;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.utils.featureobjects.FeatureObject;
import de.codingair.warpsystem.spigot.base.utils.featureobjects.actions.Action;
import de.codingair.warpsystem.spigot.base.utils.featureobjects.actions.types.WarpAction;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.Destination;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.DestinationType;
import de.codingair.warpsystem.spigot.features.FeatureType;
import de.codingair.warpsystem.spigot.features.warps.managers.IconManager;
import de.codingair.warpsystem.spigot.features.warps.nextlevel.utils.Icon;
import de.codingair.codingapi.tools.JSON.JSONObject;

import java.util.Objects;

public class WarpSign extends FeatureObject {
    private Location location;

    public WarpSign() {
    }

    public WarpSign(WarpSign sign) {
        super(sign);
        this.location = sign.location;
    }

    public WarpSign(Location location, Destination destination) {
        this(location, destination, null);
    }

    public WarpSign(Location location, Destination destination, String permission) {
        super(permission, false, new WarpAction(destination));
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public Destination getDestination() {
        return hasAction(Action.WARP) ? ((WarpAction) getAction(Action.WARP)).getValue() : null;
    }

    @Override
    public boolean read(JSONObject json) throws Exception {
        super.read(json);

        if(json.get("Loc") != null) {
            this.location = Location.getByJSONString(json.getRaw("Loc"));
        } else if(json.get("location") != null) {
            this.location = Location.getByJSONString(json.getRaw("location"));
        }

        if(json.get("Destination") != null) {
            //New pattern
            Destination destination = new Destination((String) json.get("Destination"));
            addAction(new WarpAction(destination));
        } else if(json.get("Warp") != null) {
            //Old pattern
            Icon warp = ((IconManager) WarpSystem.getInstance().getDataManager().getManager(FeatureType.WARPS)).getIcon(json.get("Warp"));
            if(warp != null) {
                Destination destination = new Destination(warp.getName(), DestinationType.SimpleWarp);
                addAction(new WarpAction(destination));
            }
        }

        if(json.get("Permissions") != null) {
            setPermission(json.get("Permissions"));
        }

        return true;
    }

    @Override
    public void write(JSONObject json) {
        super.write(json);

        json.put("location", this.location.toJSONString(0));
    }

    @Override
    public void destroy() {
        super.destroy();
        this.location = null;
    }

    @Override
    public void apply(FeatureObject object) {
        super.apply(object);

        WarpSign sign = (WarpSign) object;
        this.location = sign.location;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o) &&
                o instanceof WarpSign &&
                Objects.equals(this.location, ((WarpSign) o).location);
    }

    public void setDestination(Destination destination) {
        if(destination == null) removeAction(Action.WARP);
        else addAction(new WarpAction(destination));
    }

    public WarpSign clone() {
        return new WarpSign(this);
    }
}
