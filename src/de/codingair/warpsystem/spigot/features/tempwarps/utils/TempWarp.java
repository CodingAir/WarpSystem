package de.codingair.warpsystem.spigot.features.tempwarps.utils;

import de.codingair.codingapi.tools.io.JSON.JSON;
import de.codingair.codingapi.tools.io.JSON.JSONParser;
import de.codingair.codingapi.tools.Location;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import de.codingair.warpsystem.spigot.features.tempwarps.managers.TempWarpManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.json.simple.parser.ParseException;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TempWarp {
    private TempWarp backup = null;

    private String lastKnownName;
    private UUID owner;
    private Location location;
    private String name;

    private Date bornDate;
    private Date startDate;
    private Date endDate;
    private Date expireDate;
    private double duration;
    private boolean isPublic;
    private String message;
    private int teleportCosts;
    private String creatorKey = null;
    private boolean notify = false;

    private int paid;
    private int inactiveSales = 0;

    TempWarp(String lastKnownName, UUID owner, Location location, String name, String message, Date bornDate, Date startDate, Date endDate, Date expireDate, int duration, boolean isPublic, int teleportCosts, int paid, int inactiveSales) {
        this.lastKnownName = lastKnownName;
        this.owner = owner;
        this.location = location;
        this.name = name;
        this.message = message;
        this.bornDate = bornDate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.expireDate = expireDate;
        this.duration = duration;
        this.isPublic = isPublic;
        this.teleportCosts = teleportCosts;
        this.paid = paid;
        this.inactiveSales = inactiveSales;
    }

    private TempWarp(String lastKnownName, UUID owner, Location location, String name, String message, Date bornDate, Date startDate, Date expireDate, int duration, boolean isPublic, int teleportCosts, int paid) {
        this.lastKnownName = lastKnownName;
        this.owner = owner;
        this.location = location;
        this.name = name;
        this.message = message;
        this.bornDate = bornDate;
        this.startDate = startDate;
        this.expireDate = expireDate;
        this.duration = duration;
        this.isPublic = isPublic;
        this.teleportCosts = teleportCosts;
        this.paid = paid;

        this.endDate = calculateEndDate();
    }

    public TempWarp(Player player, Location location, String name, int duration, boolean isPublic, String message, int teleportCosts) {
        this(player.getName(), WarpSystem.getInstance().getUUIDManager().get(player), location, name, message, new Date(), new Date(), null, duration, isPublic, teleportCosts, 0);
        saveCosts();
    }

    public boolean hasAccess(Player player) {
        return isPublic || isOwner(player);
    }

    public boolean isOwner(Player player) {
        Player owner;
        return (owner = getOnlineOwner()) != null && owner.getName().equals(player.getName());
    }

    public Player getOnlineOwner() {
        return WarpSystem.getInstance().getUUIDManager().getPlayerBy(getOwner());
    }

    public boolean isValid() {
        return !isExpired() && this.location != null && this.location.getWorld() != null && isSave();
    }

    public boolean isSave() {
        return TempWarpManager.getManager().isSave(this.location);
    }

    public boolean isExpired() {
        boolean expired = isExpired(new Date());
        if(expired && expireDate == null) this.expireDate = new Date();
        return expired;
    }

    public boolean isExpired(Date date) {
        boolean expired = this.getEndDate().before(date);
        if(expired && this.expireDate == null) this.expireDate = new Date(getEndDate().getTime());
        return expired;
    }

    public long getLeftTime() {
        if(getExpireDate() == null) return getEndDate().getTime() - new Date().getTime();
        return getExpireDate().getTime() - new Date().getTime();
    }

    public Date calculateEndDate() {
        return new Date(this.startDate.getTime() + TimeUnit.MILLISECONDS.convert((int) this.duration, TempWarpManager.getManager().getConfig().getUnit()));
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getBornDate() {
        return bornDate;
    }

    public Date getExpireDate() {
        return expireDate;
    }

    public int getDuration() {
        return (int) duration;
    }

    public double getDurationExact() {
        return duration;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public UUID getOwner() {
        return owner;
    }

    public Location getLocation() {
        return location != null ? location.clone() : null;
    }

    public String getName() {
        return name;
    }

    public String getIdentifier() {
        if(getName() == null) return null;
        return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', isPublic ? getName() : getLastKnownName() + "." + getName())).replace(" ", "_");
    }

    public String getLastKnownName() {
        return lastKnownName;
    }

    public void setLastKnownName(String lastKnownName) {
        this.lastKnownName = lastKnownName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDuration(double duration) {
        this.duration = duration;
        if(this.endDate != null) this.endDate = calculateEndDate();
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public int getCosts() {
        int costs = 0;

        costs += duration * TempWarpManager.getManager().getConfig().getDurationCosts();
        if(isPublic) costs += TempWarpManager.getManager().getConfig().getPublicCosts();
        if(message != null) costs += TempWarpManager.getManager().getConfig().getMessageCosts();
        if(teleportCosts > 0) costs += TempWarpManager.getManager().getTeleportCosts() * this.teleportCosts;
        if(TempWarpManager.getManager().getNameChangeCosts() > 0 && isChangingName()) costs += TempWarpManager.getManager().getNameChangeCosts();
        if(TempWarpManager.getManager().getMessageChangeCosts() > 0 && isChangingMessage()) costs += TempWarpManager.getManager().getMessageChangeCosts();

        return costs;
    }

    public boolean isChangingName() {
        if(backup == null) return false;
        return backup.getName() != null && getName() != null && !backup.getName().equals(getName());
    }

    public boolean isChangingMessage() {
        if(backup == null) return false;
        return backup.getMessage() != null && getMessage() != null && !backup.getMessage().equals(getMessage());
    }

    public long getRemainingTime() {
        return getEndDate().getTime() - new Date().getTime();
    }

    public int getRemainingCosts() {
        TempWarpConfig config = TempWarpManager.getManager().getConfig();
        int rI = (int) config.getUnit().convert(getRemainingTime(), TimeUnit.MILLISECONDS);
        int costs = config.getDurationCosts() * rI;
        return costs < 0 ? 0 : costs;
    }

    public void saveCosts() {
        this.paid = getCosts();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getPaid() {
        return paid;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void backup() {
        this.backup = clone();
    }

    public void restore() {
        apply(this.backup);
    }

    public void renew() {
        this.startDate = new Date();
        this.endDate = calculateEndDate();
        this.expireDate = null;
        setNotify(false);
    }

    public boolean isAvailable() {
        return this.location != null && this.location.getWorld() != null;
    }

    public void apply() {
        if(backup != null) {
            if(!isExpired()) expireDate = null;
            else if(backup.getDuration() != getDuration()) expireDate = new Date();
        }
        this.backup = null;
    }

    private void apply(TempWarp warp) {
        if(warp != null) return;
        this.lastKnownName = warp.lastKnownName;
        this.owner = warp.owner;
        this.location = warp.location;
        this.name = warp.name;
        this.message = warp.message;
        this.startDate = warp.startDate;
        this.endDate = warp.endDate;
        this.expireDate = warp.expireDate;
        this.duration = warp.duration;
        this.isPublic = warp.isPublic;
        this.teleportCosts = warp.teleportCosts;
        this.paid = warp.paid;
    }

    public TempWarp backupped() {
        return this.backup;
    }

    public TempWarp clone() {
        return new TempWarp(this.lastKnownName, this.owner, this.location, this.name, this.message, this.bornDate, this.startDate, this.endDate, this.expireDate, (int) this.duration, this.isPublic, this.teleportCosts, this.paid, this.inactiveSales);
    }

    public String toJSONString() {
        JSON json = new JSON();

        json.put("Owner", this.owner.toString());
        json.put("LastKnownName", this.lastKnownName);
        json.put("Location", this.location);
        json.put("Name", this.name);
        json.put("Message", this.message);
        json.put("BornDate", this.bornDate);
        json.put("StartDate", this.startDate);
        json.put("EndDate", this.endDate);
        json.put("ExpireDate", this.expireDate);
        json.put("Duration", getDuration());
        json.put("isPublic", this.isPublic);
        json.put("TeleportCosts", this.teleportCosts);
        json.put("Paid", this.paid);
        json.put("InactiveSales", this.inactiveSales);
        json.put("Key", this.creatorKey);
        json.put("Notify", this.notify);

        return json.toJSONString();
    }

    public static TempWarp getByJSONString(String s) {
        try {
            JSON json = (JSON) new JSONParser().parse(s);

            UUID owner = UUID.fromString(json.get("Owner"));
            String lastKnownName = json.get("LastKnownName");
            Location location = json.getLocation("Location");
            String name = json.get("Name");
            String teleportMessage = json.get("Message");
            Date bornDate = json.getDate("BornDate");
            Date startDate = json.getDate("StartDate");
            Date endDate = json.getDate("EndDate");
            Date expireDate = json.getDate("ExpireDate");
            int timeIntervals = json.getInteger("Duration");
            boolean isPublic = json.getBoolean("isPublic");
            int teleportCosts = json.getInteger("TeleportCosts");
            int paid = json.getInteger("Paid");
            int inactiveSales = json.getInteger("InactiveSales");
            String creatorKey = json.get("Key");
            boolean notify = json.getBoolean("Notify");

            TempWarp warp = new TempWarp(lastKnownName, owner, location, name, teleportMessage, bornDate, startDate, endDate, expireDate, timeIntervals, isPublic, teleportCosts, paid, inactiveSales);
            warp.setCreatorKey(creatorKey);
            warp.setNotify(notify);
            return warp;
        } catch(ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getTeleportCosts() {
        return teleportCosts;
    }

    public void setTeleportCosts(int teleportCosts) {
        this.teleportCosts = teleportCosts;
    }

    public int getInactiveSales() {
        return inactiveSales;
    }

    public void setInactiveSales(int inactiveSales) {
        this.inactiveSales = inactiveSales;
    }

    public boolean isBeingEdited() {
        return this.backup != null;
    }

    public String getCreatorKey() {
        return creatorKey;
    }

    public void setCreatorKey(String creatorKey) {
        this.creatorKey = creatorKey;
    }

    public boolean isNotify() {
        return notify;
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
    }
}
