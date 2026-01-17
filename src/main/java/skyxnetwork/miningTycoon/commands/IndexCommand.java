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
        player.sendMessage("§e/level §7- Check your level and XP");
        player.sendMessage("§e/prestige [confirm] §7- Prestige system");
        player.sendMessage("§e/afk §7- Teleport to AFK zone");
        player.sendMessage("§e/lobby §7- Return to lobby");
        player.sendMessage("§e/fasttp zone <1-18> §7- Fast teleport to zones");
        player.sendMessage("§e/booststatus §7- Show if a Global Boost is active");
        player.sendMessage("§8§m-----------------------------");

        return true;
    }
}

