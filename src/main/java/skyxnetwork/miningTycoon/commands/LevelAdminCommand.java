package skyxnetwork.miningTycoon.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.data.PlayerData;

public class LevelAdminCommand implements CommandExecutor {

    private final MiningTycoon plugin;

    public LevelAdminCommand(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("miningtycoon.level.admin")) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /leveladmin <set/reset> <player> [level]");
            return true;
        }

        String subcommand = args[0].toLowerCase();
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

        if (subcommand.equals("set")) {
            if (args.length < 3) {
                sender.sendMessage("§cUsage: /leveladmin set <player> <level>");
                return true;
            }

            int newLevel;
            try {
                newLevel = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sender.sendMessage("§cError: Level must be a valid number.");
                return true;
            }

            if (newLevel < 1) {
                sender.sendMessage("§cError: Level must be at least 1.");
                return true;
            }

            PlayerData data = plugin.getPlayerDataManager().getPlayerData(target.getUniqueId());
            data.setLevel(newLevel);
            data.setExperience(0);

            // Recalculate exp needed
            double expNeeded = 100;
            for (int i = 1; i < newLevel; i++) {
                expNeeded *= 1.1;
            }
            data.setExperienceNeeded(expNeeded);

            sender.sendMessage("§aThe level of §e" + target.getName() + " §ahas been set to §6" + newLevel + "§a.");

            if (target.isOnline()) {
                ((Player) target).sendMessage("§aYour level has been set to §6" + newLevel + " §aby an administrator.");
            }

        } else if (subcommand.equals("reset")) {
            PlayerData data = plugin.getPlayerDataManager().getPlayerData(target.getUniqueId());
            data.setLevel(1);
            data.setExperience(0);
            data.setExperienceNeeded(100);

            sender.sendMessage("§aThe level of §e" + target.getName() + " §ahas been reset.");

            if (target.isOnline()) {
                ((Player) target).sendMessage("§aYour level has been reset by an administrator.");
            }

        } else {
            sender.sendMessage("§cUsage: /leveladmin <set/reset> <player> [level]");
        }

        return true;
    }
}
