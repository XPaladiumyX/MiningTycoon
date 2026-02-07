package skyxnetwork.miningTycoon.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.data.PlayerData;

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
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

        // Show prestige info
        player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§d§l⚡ PRESTIGE INFORMATION ⚡");
        player.sendMessage("");
        player.sendMessage("§7Current Level: §6" + data.getLevel());
        player.sendMessage("§7Current Prestige: §d" + data.getPrestige());
        player.sendMessage("");
        player.sendMessage("§eHow to prestige:");
        player.sendMessage("§f1. §7Enter a prestige portal");
        player.sendMessage("§f2. §7The GUI will open automatically");
        player.sendMessage("§f3. §7Follow the confirmation steps");
        player.sendMessage("");
        player.sendMessage("§7Available portals:");
        player.sendMessage("§f  • §dBasic Portal §7(Level 120+)");
        player.sendMessage("§f  • §5Elite Portal §7(Level 150+)");
        player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        return true;
    }
}