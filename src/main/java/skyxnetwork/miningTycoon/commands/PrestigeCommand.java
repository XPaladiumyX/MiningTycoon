package skyxnetwork.miningTycoon.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.managers.PrestigePortalManager;

public class PrestigeCommand implements CommandExecutor {

    private final MiningTycoon plugin;

    public PrestigeCommand(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        // Check if player is confirming prestige
        if (args.length > 0 && args[0].equalsIgnoreCase("confirm")) {
            // Check if player is in a prestige portal
            PrestigePortalManager.PrestigePortal portal =
                    plugin.getPrestigePortalManager().getPortalAtLocation(player.getLocation());

            if (portal == null) {
                player.sendMessage("§c⛔ You must be inside a prestige portal to use this command!");
                return true;
            }

            // Execute prestige
            plugin.getPrestigePortalManager().executePrestige(player, portal.getType());
            return true;
        }

        // Show prestige info
        showPrestigeInfo(player);
        return true;
    }

    private void showPrestigeInfo(Player player) {
        PrestigePortalManager.PrestigePortal portal =
                plugin.getPrestigePortalManager().getPortalAtLocation(player.getLocation());

        if (portal != null) {
            player.sendMessage("§e§lYou are in a prestige portal!");
            player.sendMessage("§7Use §e/prestige confirm §7to prestige");
        } else {
            player.sendMessage("§c⛔ You must enter a prestige portal to prestige!");
            player.sendMessage("§7Prestige portals are special areas where you can reset");
            player.sendMessage("§7your level in exchange for prestige points and rewards.");
        }
    }
}