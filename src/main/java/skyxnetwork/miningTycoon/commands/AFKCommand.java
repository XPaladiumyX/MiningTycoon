package skyxnetwork.miningTycoon.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import skyxnetwork.miningTycoon.MiningTycoon;

public class AFKCommand implements CommandExecutor {

    private final MiningTycoon plugin;

    public AFKCommand(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Player player = (Player) sender;
        Location afkLoc = new Location(Bukkit.getWorld("mining_tycoon"), 9, 125, 19);
        afkLoc.setYaw(-270);
        afkLoc.setPitch(0);

        player.teleport(afkLoc);
        player.sendMessage("§aTeleporting to AFK zone...");

        return true;
    }
}
