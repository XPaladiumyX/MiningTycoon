package skyxnetwork.miningTycoon.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.data.PlayerData;

public class PrestigeAdminCommand implements CommandExecutor {

    private final MiningTycoon plugin;

    public PrestigeAdminCommand(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("miningtycoon.prestige.admin")) {
            sender.sendMessage("§c§lYou don't have permission to use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§c§lUsage: §7/prestigeadmin <set/reset> <player> [prestige]");
            return true;
        }

        String subcommand = args[0].toLowerCase();
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

        if (subcommand.equals("set")) {
            if (args.length < 3) {
                sender.sendMessage("§c§lUsage: §7/prestigeadmin set <player> <prestige>");
                return true;
            }

            int value;
            try {
                value = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§c§lError: §7Prestige must be a valid number.");
                return true;
            }

            if (value < 0) {
                sender.sendMessage("§c§lError: §7Prestige must be 0 or higher.");
                return true;
            }

            PlayerData data = plugin.getPlayerDataManager().getPlayerData(target.getUniqueId());
            data.setPrestige(value);

            sender.sendMessage("§a✔ §aThe prestige of §b§l" + target.getName() + " §ahas been set to §d§l" + value + "§a.");

            if (target.isOnline()) {
                ((Player) target).sendMessage("§aYour prestige has been set to §d§l" + value + " §aby an administrator.");
            }

        } else if (subcommand.equals("reset")) {
            PlayerData data = plugin.getPlayerDataManager().getPlayerData(target.getUniqueId());
            data.setPrestige(0);

            sender.sendMessage("§a✔ §aThe prestige of §b§l" + target.getName() + " §ahas been reset to §d§l0§a.");

            if (target.isOnline()) {
                ((Player) target).sendMessage("§aYour prestige has been reset by an administrator.");
            }

        } else {
            sender.sendMessage("§c§lUsage: §7/prestigeadmin <set/reset> <player> [prestige]");
        }

        return true;
    }
}
