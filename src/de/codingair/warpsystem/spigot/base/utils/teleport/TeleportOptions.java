package de.codingair.warpsystem.spigot.base.utils.teleport;

import de.codingair.codingapi.server.Sound;
import de.codingair.codingapi.server.SoundData;
import de.codingair.codingapi.tools.Callback;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.base.language.Lang;
import de.codingair.warpsystem.spigot.base.utils.money.AdapterType;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.Destination;
import de.codingair.warpsystem.spigot.base.utils.teleport.destinations.adapters.LocationAdapter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TeleportOptions {
    private Origin origin;
    private Destination destination;
    private String displayName;
    private String permission;
    private double costs;
    private boolean skip;
    private boolean canMove;
    private boolean waitForTeleport; //Waiting for walking teleports
    private String payMessage;
    private String message;
    private boolean silent;
    private SoundData teleportSound;
    private boolean afterEffects;
    private Callback<TeleportResult> callback;

    public TeleportOptions(Location location, String displayName) {
        this(new Destination(new LocationAdapter(location)), displayName);
    }

    public TeleportOptions(Destination destination, String displayName) {
        this.origin = Origin.Custom;
        this.destination = destination;
        this.displayName = displayName;
        this.permission = null;
        this.costs = 0;
        this.skip = false;
        this.canMove = WarpSystem.getInstance().getTeleportManager().getOptions().isAllowMove();
        this.waitForTeleport = false;
        this.message = Lang.getPrefix() + Lang.get("Teleported_To");
        this.payMessage = Lang.getPrefix() + Lang.get("Money_Paid");
        this.silent = false;
        this.teleportSound = new SoundData(Sound.ENDERMAN_TELEPORT, 1F, 1F);
        this.afterEffects = true;
        this.callback = null;
    }

    public Origin getOrigin() {
        return origin;
    }

    public void setOrigin(Origin origin) {
        this.origin = origin;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public double getCosts() {
        return costs;
    }

    public double getFinalCosts(Player player) {
        return costs > 0 && AdapterType.getActive() != null && !player.hasPermission(WarpSystem.PERMISSION_ByPass_Teleport_Costs) ? costs : 0;
    }

    public void setCosts(double costs) {
        this.costs = costs;
    }

    public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public boolean isCanMove() {
        return canMove;
    }

    public void setCanMove(boolean canMove) {
        this.canMove = canMove;
    }

    public boolean isWaitForTeleport() {
        return waitForTeleport;
    }

    public void setWaitForTeleport(boolean waitForTeleport) {
        this.waitForTeleport = waitForTeleport;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSilent() {
        return silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public SoundData getTeleportSound() {
        return teleportSound;
    }

    public void setTeleportSound(SoundData teleportSound) {
        this.teleportSound = teleportSound;
    }

    public boolean isAfterEffects() {
        return afterEffects;
    }

    public void setAfterEffects(boolean afterEffects) {
        this.afterEffects = afterEffects;
    }

    public Callback<TeleportResult> getCallback() {
        return callback;
    }

    public void setCallback(Callback<TeleportResult> callback) {
        this.callback = callback;
    }

    public String getPayMessage() {
        return payMessage;
    }

    public void setPayMessage(String payMessage) {
        this.payMessage = payMessage;
    }

    public String getFinalMessage(Player player) {
        return getFinalCosts(player) > 0 ? getPayMessage() : getMessage();
    }
}
