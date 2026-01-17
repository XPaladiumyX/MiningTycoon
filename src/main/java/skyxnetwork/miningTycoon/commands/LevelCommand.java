package skyxnetwork.miningTycoon.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.data.PlayerData;
import skyxnetwork.miningTycoon.utils.NumberFormatter;

public class LevelCommand implements CommandExecutor {

    private final MiningTycoon plugin;

    public LevelCommand(MiningTycoon plugin) {
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

        double percent = (data.getExperience() / data.getExperienceNeeded()) * 100;

        player.sendMessage("§7§l-------< §d§lLevel§f§lStats §7§l>-------");
        player.sendMessage("§fPlayer: §7" + player.getName());
        player.sendMessage("§fLevel: §6" + data.getLevel() + "§7/§c500");
        player.sendMessage("§fEXP: §b" + NumberFormatter.format(data.getExperience()) + "§7/§b" + NumberFormatter.format(data.getExperienceNeeded()));

        // Progress bar
        StringBuilder progress = new StringBuilder("§8[§d");
        int filledBars = (int) (percent / 10);
        for (int i = 0; i < filledBars; i++) {
            progress.append("■");
        }
        progress.append("§7");
        for (int i = 0; i < (10 - filledBars); i++) {
            progress.append("■");
        }
        progress.append("§8]");

        player.sendMessage(progress.toString());
        player.sendMessage("§7§l--------------------------");

        return true;
    }
}
