package skyxnetwork.miningTycoon.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.data.PlayerData;

public class ModeCommand implements CommandExecutor {

    private final MiningTycoon plugin;

    public ModeCommand(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Player player = (Player) sender;
        if (!player.isOp()) {
            player.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

        if (data.getPlayerMode().equals("staff")) {
            data.setPlayerMode("player");
            player.sendActionBar(net.kyori.adventure.text.Component.text("§aMode: §lPlayer §7(Break = Rewards)"));
        } else {
            data.setPlayerMode("staff");
            player.sendActionBar(net.kyori.adventure.text.Component.text("§cMode: §lStaff §7(Break = No Rewards)"));
        }

        return true;
    }
}
