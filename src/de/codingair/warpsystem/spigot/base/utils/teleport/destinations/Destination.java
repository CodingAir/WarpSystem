package de.codingair.warpsystem.spigot.base.utils.teleport.destinations;

import de.codingair.codingapi.tools.Callback;
import de.codingair.warpsystem.spigot.base.utils.teleport.SimulatedTeleportResult;
import de.codingair.warpsystem.spigot.base.utils.teleport.TeleportResult;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.adapters.DestinationAdapter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

public class Destination {
    private String id;
    private DestinationType type;
    private DestinationAdapter adapter;

    public Destination() {
        id = null;
        type = DestinationType.UNKNOWN;
        adapter = null;
    }

    public Destination(String id, DestinationType type) {
        this.id = id;
        this.type = type;
        this.adapter = type.getInstance();
    }

    public Destination(String id, DestinationAdapter adapter) {
        this(adapter);
        this.id = id;
    }

    public Destination(DestinationAdapter adapter) {
        this.id = null;
        this.type = DestinationType.UNKNOWN;
        this.adapter = adapter;
    }

    public Destination(String data) {
        try {
            JSONArray json = (JSONArray) new JSONParser().parse(data);

            this.type = json.get(0) == null ? null : DestinationType.valueOf((String) json.get(0));
            this.id = json.get(1) == null ? null : (String) json.get(1);
            this.adapter = type.getInstance();
        } catch(Exception ex) {
            throw new IllegalArgumentException("Wrong serialized data!");
        }
    }

    public Destination apply(Destination destination) {
        if(destination == null) {
            this.id = null;
            this.adapter = null;
            this.type = null;
            return this;
        }

        this.id = destination.id;
        this.adapter = destination.adapter;
        this.type = destination.type;
        return this;
    }

    public boolean teleport(Player player, String message, String displayName, boolean checkPermission, boolean silent, double costs, Callback<TeleportResult> callback) {
        if(adapter == null) return false;
        player.setFallDistance(0F);
        return adapter.teleport(player, id, displayName, checkPermission, message, silent, costs, callback);
    }

    public Location buildLocation() {
        return adapter.buildLocation(id);
    }

    public double getCosts() {
        return adapter == null ? 0 : adapter.getCosts(id);
    }

    public SimulatedTeleportResult simulate(Player player, boolean checkPermission) {
        if(adapter == null) return new SimulatedTeleportResult(null, TeleportResult.NO_ADAPTER);
        return adapter.simulate(player, this.id, checkPermission);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DestinationType getType() {
        return type;
    }

    public void setType(DestinationType type) {
        this.type = type;
    }

    public DestinationAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(DestinationAdapter adapter) {
        this.adapter = adapter;
    }

    public String toJSONString() {
        if(this.type == DestinationType.UNKNOWN) throw new IllegalArgumentException("Cannot serialize unknown destination!");

        JSONArray json = new JSONArray();
        json.add(type == null ? null : type.name());
        json.add(id);

        return json.toJSONString();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        Destination that = (Destination) o;
        if(id == null) return that.id == null && type == that.type;
        return id.equals(that.id) &&
                type == that.type;
    }

    public Destination clone() {
        Destination destination = new Destination();
        destination.id = id;
        destination.type = type;
        destination.adapter = adapter;
        return destination;
    }
}
