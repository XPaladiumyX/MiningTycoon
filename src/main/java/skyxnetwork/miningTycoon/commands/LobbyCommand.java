package skyxnetwork.miningTycoon.commands;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
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

        // Velocity utilise le canal de compatibilité BungeeCord pour les plugin messages
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF("lobby"); // nom du serveur dans velocity.toml

        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
        player.sendMessage("§aConnecting to lobby...");

        return true;
    }
}