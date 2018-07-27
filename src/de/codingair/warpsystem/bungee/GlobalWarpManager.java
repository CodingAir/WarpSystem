package de.codingair.warpsystem.bungee;

import de.codingair.codingapi.bungeecord.files.ConfigFile;
import de.codingair.warpsystem.transfer.packets.bungee.SendGlobalWarpNamesPacket;
import de.codingair.warpsystem.transfer.packets.bungee.UpdateGlobalWarpPacket;
import de.codingair.warpsystem.transfer.serializeable.SGlobalWarp;
import de.codingair.warpsystem.transfer.serializeable.SLocation;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.config.Configuration;

import java.util.ArrayList;
import java.util.List;

public class GlobalWarpManager {
    private List<SGlobalWarp> globalWarps = new ArrayList<>();

    public void load() {
        ConfigFile file = WarpSystem.getInstance().getFileManager().getFile("GlobalWarps");
        Configuration config = file.getConfig();

        this.globalWarps = new ArrayList<>();

        for(String data : config.getKeys()) {
            SGlobalWarp warp = new SGlobalWarp();

            warp.setName(data);
            warp.setServer(config.getString(data + ".Server"));
            warp.setLoc(new SLocation(
                    config.getString(data + ".Location.World"),
                    config.getDouble(data + ".Location.X"),
                    config.getDouble(data + ".Location.Y"),
                    config.getDouble(data + ".Location.Z"),
                    config.getFloat(data + ".Location.Yaw"),
                    config.getFloat(data + ".Location.Pitch")
            ));

            this.globalWarps.add(warp);
        }
    }

    private void save(SGlobalWarp warp) {
        ConfigFile file = WarpSystem.getInstance().getFileManager().getFile("GlobalWarps");
        Configuration config = file.getConfig();

        config.set(warp.getName() + ".Server", warp.getServer());
        config.set(warp.getName() + ".Location.World", warp.getLoc().getWorld());
        config.set(warp.getName() + ".Location.X", warp.getLoc().getX());
        config.set(warp.getName() + ".Location.Y", warp.getLoc().getY());
        config.set(warp.getName() + ".Location.Z", warp.getLoc().getZ());
        config.set(warp.getName() + ".Location.Yaw", warp.getLoc().getYaw());
        config.set(warp.getName() + ".Location.Pitch", warp.getLoc().getPitch());

        file.save();
    }

    public void synchronize(String name) {
        for(ServerInfo server : WarpSystem.getInstance().getServerManager().getOnlineServer()) {
            WarpSystem.getInstance().getDataHandler().send(new UpdateGlobalWarpPacket(get(name) == null ? UpdateGlobalWarpPacket.Action.DELETE.getId() : UpdateGlobalWarpPacket.Action.ADD.getId(), name), server);
        }
    }

    public void sendData(ServerInfo info) {
        if(this.globalWarps.isEmpty()) {
            System.out.println("Don't have any global warps... nothing to send.");
            return;
        }

        List<List<String>> list = new ArrayList<>();
        List<String> current = new ArrayList<>();
        int currentBytes = 0;

        for(SGlobalWarp warp : this.globalWarps) {
            currentBytes += warp.getName().length();

            if(currentBytes > 32700) {
                currentBytes = warp.getName().length();
                list.add(current);
                current = new ArrayList<>();
            } else {
                current.add(warp.getName());
            }
        }

        list.add(current);

        for(List<String> l : list) {
            WarpSystem.getInstance().getDataHandler().send(new SendGlobalWarpNamesPacket(l), info);
            System.out.println("Sent a list!");
        }

        list.clear();
    }

    private void delete(SGlobalWarp warp) {
        ConfigFile file = WarpSystem.getInstance().getFileManager().getFile("GlobalWarps");
        Configuration config = file.getConfig();

        config.set(warp.getName(), null);

        file.save();
    }

    public List<SGlobalWarp> getGlobalWarps() {
        return globalWarps;
    }

    public SGlobalWarp get(String name) {
        for(SGlobalWarp warp : this.globalWarps) {
            if(warp.getName().equalsIgnoreCase(name)) return warp;
        }

        return null;
    }

    public boolean add(SGlobalWarp warp) {
        if(get(warp.getName()) != null) return false;
        this.globalWarps.add(warp);
        save(warp);
        return true;
    }

    public SGlobalWarp remove(String name) {
        SGlobalWarp warp = get(name);
        if(warp == null) return null;
        this.globalWarps.remove(warp);
        delete(warp);
        return warp;
    }
}
