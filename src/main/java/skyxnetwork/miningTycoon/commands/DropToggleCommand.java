package skyxnetwork.miningTycoon.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.data.PlayerData;

public class DropToggleCommand implements CommandExecutor {

    private final MiningTycoon plugin;

    public DropToggleCommand(MiningTycoon plugin) {
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

        data.setDropMessagesEnabled(!data.isDropMessagesEnabled());

        if (data.isDropMessagesEnabled()) {
            player.sendMessage("§a✔ Drop messages & sounds enabled.");
        } else {
            player.sendMessage("§c✖ Drop messages & sounds disabled.");
        }

        return true;
    }
}
