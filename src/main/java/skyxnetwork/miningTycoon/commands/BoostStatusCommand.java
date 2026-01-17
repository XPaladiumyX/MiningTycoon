package skyxnetwork.miningTycoon.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import skyxnetwork.miningTycoon.MiningTycoon;

public class BoostStatusCommand implements CommandExecutor {

    private final MiningTycoon plugin;

    public BoostStatusCommand(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (plugin.getBoostManager().isBoostActive()) {
            int timeRemaining = plugin.getBoostManager().getTimeRemaining();
            String type = plugin.getBoostManager().getBoostType();
            player.sendMessage("§b☄ A Global Boost is active! Type: §a" + type + "§7, ends in " + timeRemaining + " seconds.");
        } else {
            player.sendMessage("§7No Global Boost is currently active.");
        }

        return true;
    }
}
