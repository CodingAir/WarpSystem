package de.codingair.warpsystem.bungee.features.teleport.commands;

import de.codingair.warpsystem.bungee.base.WarpSystem;
import de.codingair.warpsystem.bungee.base.language.Lang;
import de.codingair.warpsystem.bungee.features.teleport.managers.TeleportManager;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class CTpToggle extends Command {
    public CTpToggle() {
        super("TpToggle", WarpSystem.PERMISSION_USE_TELEPORT_COMMAND_TP_TOGGLE);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(TeleportManager.getInstance().toggleDenyForceTps((ProxiedPlayer) sender))
            sender.sendMessage(Lang.getPrefix() + Lang.get("Teleports_toggled_disabling"));
        else
            sender.sendMessage(Lang.getPrefix() + Lang.get("Teleports_toggled_enabling"));
    }
}
