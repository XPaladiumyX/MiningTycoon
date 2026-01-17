package skyxnetwork.miningTycoon.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import skyxnetwork.miningTycoon.MiningTycoon;

public class LobbyCommand implements CommandExecutor {

    private final MiningTycoon plugin;

    public LobbyCommand(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Player player = (Player) sender;
        player.sendMessage("§aConnecting to lobby...");

        // Using BungeeCord/Velocity connection
        org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.sendMessage("§aSending to lobby...");
            // Implement actual proxy connection here
            // player.connect(server);
        }, 60L);

        return true;
    }
}
