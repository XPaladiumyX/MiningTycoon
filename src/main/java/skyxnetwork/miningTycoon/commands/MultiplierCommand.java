package skyxnetwork.miningTycoon.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.data.PlayerData;

public class MultiplierCommand implements CommandExecutor {

    private final MiningTycoon plugin;

    public MultiplierCommand(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

        double multiplier = data.getExpMultiplierBonus();
        int percent = (int) (multiplier * 100);

        if (percent == 0) {
            player.sendMessage("§7Your EXP multiplier: §e+0%");
            player.sendMessage("§7 Prestige to earn EXP multipliers!");
        } else {
            player.sendMessage("§7Your EXP multiplier: §e+" + percent + "%");
            player.sendMessage("§7Your current prestige: §d" + data.getPrestige());
        }

        return true;
    }
}