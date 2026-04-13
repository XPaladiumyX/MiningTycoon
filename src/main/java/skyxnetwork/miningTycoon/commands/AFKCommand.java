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

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("playtime")) {
                String formattedTime = plugin.getAfkManager().getDetailedAfkTime(player.getUniqueId());
                int rank = plugin.getAfkManager().getPlayerRank(player.getUniqueId());
                
                sender.sendMessage("§6§lAFK Playtime");
                sender.sendMessage("§7Total AFK time: §e" + formattedTime);
                sender.sendMessage("§7Rank: §e#" + (rank > 0 ? rank : "N/A"));
                return true;
            }
            
            if (args[0].equalsIgnoreCase("on")) {
                plugin.getAfkManager().setPlayerAfk(player.getUniqueId(), true);
                player.sendMessage("§aYou are now marked as AFK!");
                return true;
            }
            
            if (args[0].equalsIgnoreCase("off")) {
                plugin.getAfkManager().setPlayerAfk(player.getUniqueId(), false);
                player.sendMessage("§aYou are no longer marked as AFK!");
                return true;
            }
        }

        Location afkLoc = new Location(Bukkit.getWorld("mining_tycoon"), 9, 125, 19);
        afkLoc.setYaw(-270);
        afkLoc.setPitch(0);

        player.teleport(afkLoc);
        player.sendMessage("§aTeleporting to AFK zone...");

        return true;
    }
}
