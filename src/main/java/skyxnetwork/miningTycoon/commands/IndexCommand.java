package skyxnetwork.miningTycoon.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import skyxnetwork.miningTycoon.MiningTycoon;

public class IndexCommand implements CommandExecutor {

    private final MiningTycoon plugin;

    public IndexCommand(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        player.sendMessage("§8§m------§r §6§lCommand Index §8§m------");
        player.sendMessage("§e/droptoggle §7- Toggle drop messages");
        player.sendMessage("§e/shop §7- Open the server shop");
        player.sendMessage("§e/pets §7- Open the server pets shop");
        player.sendMessage("§e/reward §7- Claim your daily reward");
        player.sendMessage("§e/level §7- Check your level and XP");
        player.sendMessage("§e/hub §7- Return to the hub");
        player.sendMessage("§e/spawn §7- Teleport to the spawn");
        player.sendMessage("§e/lobby §7- Sends you to the lobby");
        player.sendMessage("§e/afk §7- Teleport you to the afk area");
        player.sendMessage("§e/playerhide §7- Toggle visibility of other players");
        player.sendMessage("§e/booststatus §7- Show if a Global Boost is active");
        player.sendMessage("§8§m-----------------------------");

        return true;
    }
}

