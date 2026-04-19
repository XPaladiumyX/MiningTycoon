package skyxnetwork.miningTycoon.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.data.PlayerData;

public class MultiplierAdminCommand implements CommandExecutor {

    private final MiningTycoon plugin;

    public MultiplierAdminCommand(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("miningtycoon.multiplier.admin")) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /multiplieradmin <see/set/reset> <player> [multiplier]");
            return true;
        }

        String subcommand = args[0].toLowerCase();
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

        if (!target.hasPlayedBefore() && !target.isOnline()) {
            sender.sendMessage("§cPlayer not found: " + args[1]);
            return true;
        }

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(target.getUniqueId());
        double currentMultiplier = data.getExpMultiplierBonus();
        int currentPercent = (int) (currentMultiplier * 100);

        switch (subcommand) {
            case "see" -> {
                sender.sendMessage("§7EXP Multiplier for §e" + target.getName() + "§7: §e+" + currentPercent + "%");
            }

            case "set" -> {
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /multiplieradmin set <player> <multiplier>");
                    return true;
                }

                int multiplier;
                try {
                    multiplier = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cError: Multiplier must be a valid number (0-100).");
                    return true;
                }

                if (multiplier < 0 || multiplier > 100) {
                    sender.sendMessage("§cError: Multiplier must be between 0 and 100.");
                    return true;
                }

                data.setExpMultiplierBonus(multiplier / 100.0);
                sender.sendMessage("§aThe EXP multiplier of §e" + target.getName() + " §ahas been set to §e+" + multiplier + "%§a.");

                if (target.isOnline()) {
                    ((Player) target).sendMessage("§aYour EXP multiplier has been set to §e+" + multiplier + "% §aby an administrator.");
                }
            }

            case "reset" -> {
                data.setExpMultiplierBonus(0.0);
                sender.sendMessage("§aThe EXP multiplier of §e" + target.getName() + " §ahas been reset.");

                if (target.isOnline()) {
                    ((Player) target).sendMessage("§aYour EXP multiplier has been reset by an administrator.");
                }
            }

            default -> sender.sendMessage("§cUsage: /multiplieradmin <see/set/reset> <player> [multiplier]");
        }

        return true;
    }
}