package skyxnetwork.miningTycoon.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.gui.AdminGUI;

public class AdminCommand implements CommandExecutor {

    private final MiningTycoon plugin;

    public AdminCommand(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        if (!sender.hasPermission("miningtycoon.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        Player player = (Player) sender;
        AdminGUI.openMainMenu(player, plugin);

        return true;
    }
}
