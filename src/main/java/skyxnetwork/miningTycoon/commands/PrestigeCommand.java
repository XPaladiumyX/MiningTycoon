package skyxnetwork.miningTycoon.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import skyxnetwork.miningTycoon.MiningTycoon;

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

        if (args.length == 0 || !args[0].equalsIgnoreCase("confirm")) {
            player.sendMessage("§c⛔ You can only use this command from a Prestige Portal!");
            return true;
        }

        // Check if player is in prestige region (integrate with WorldGuard)
        // For now, simplified version
        player.sendMessage("§aPrestige system integrated with portal regions");

        return true;
    }
}
