package skyxnetwork.miningTycoon.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import skyxnetwork.miningTycoon.MiningTycoon;

public class MenuCommand implements CommandExecutor {

    private final MiningTycoon plugin;

    private static final String MENU_NAME = "menu";

    public MenuCommand(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Player player = (Player) sender;
        plugin.getLogger().info("[MenuDebug] MenuCommand executed for " + player.getName());

        player.performCommand("dm open " + MENU_NAME);

        return true;
    }

    public static void openDeluxeMenusMenu(Player player) {
        player.performCommand("dm open " + MENU_NAME);
    }
}