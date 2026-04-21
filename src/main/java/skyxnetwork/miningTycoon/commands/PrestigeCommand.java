package skyxnetwork.miningTycoon.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.data.PlayerData;
import skyxnetwork.miningTycoon.managers.PrestigeManager;

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

        player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§d§l⚡ PRESTIGE INFORMATION ⚡");
        player.sendMessage("");
        player.sendMessage("§7Current Level: §6" + data.getLevel());
        player.sendMessage("§7Current Prestige: §d" + data.getPrestige());
        player.sendMessage("§7Rebirth Points: §6" + data.getRebirthPoints());
        player.sendMessage("§7EXP Multiplier: §e" + data.getExpMultiplierDisplay());
        player.sendMessage("");
        player.sendMessage("§eHow to prestige:");
        player.sendMessage("§f1. §7Enter a prestige portal");
        player.sendMessage("§f2. §7The GUI will open automatically");
        player.sendMessage("§f3. §7Type §a/prestige confirm §7to proceed");
        player.sendMessage("");

        int rebirthCount = plugin.getPrestigeManager().getRebirthCount();
        for (int i = 1; i <= rebirthCount; i++) {
            PrestigeManager.RebirthConfig config = plugin.getPrestigeManager().getRebirthConfig(i);
            if (config != null) {
                boolean canRebirth = data.getLevel() >= config.getLevelRequirement();
                String status = canRebirth ? "§a✓" : "§c✗";
                int multiplier = (int) (config.getExpMultiplierBonus() * 100);
                int points = config.getRewards().getOrDefault("rebirth-points", 0);
                player.sendMessage("§f" + i + ". §dRebirth " + i + " §7(Level " + config.getLevelRequirement() + "+)" + status);
                player.sendMessage("    §7EXP Bonus: §e+" + multiplier + "%  §7Points: §6+" + points);
            }
        }

        player.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        return true;
    }
}