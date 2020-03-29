package de.codingair.warpsystem.spigot.base.managers;

import de.codingair.codingapi.files.ConfigFile;
import de.codingair.warpsystem.spigot.api.players.Head;
import de.codingair.warpsystem.spigot.base.WarpSystem;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

public class HeadManager {
    private ConfigFile file = null;

    private void checkFile() {
        if(this.file == null) {
            WarpSystem.getInstance().getFileManager().loadFile("PlayerSkins", "Memory/");
            this.file = WarpSystem.getInstance().getFileManager().getFile("PlayerSkins");
        }
    }

    public Head getHead(UUID uuid) {
        return new Head(getSkinId(uuid));
    }

    public String getSkinId(UUID uuid) {
        checkFile();
        return this.file.getConfig().getString(uuid.toString());
    }

    /*
        Called in UUIDListener after getting an unique Id.
     */
    public boolean update(Player player, UUID uuid) {
        if(uuid == null || !player.isOnline()) return false;
        checkFile();

        Head head = new Head(player);

        String id = this.file.getConfig().getString(uuid.toString());
        if(!Objects.equals(head.getId(), id)) {
            this.file.getConfig().set(uuid.toString(), head.getId());
            this.file.saveConfig();
            return true;
        }

        return false;
    }

    public Head getHead(Player player) {
        update(player, WarpSystem.getInstance().getUUIDManager().get(player));
        return new Head(player);
    }
}
