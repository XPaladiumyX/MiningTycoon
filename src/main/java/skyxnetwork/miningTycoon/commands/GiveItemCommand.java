package skyxnetwork.miningTycoon.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import skyxnetwork.miningTycoon.MiningTycoon;

public class GiveItemCommand implements CommandExecutor {

    private final MiningTycoon plugin;

    public GiveItemCommand(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("miningtycoon.admin")) {
            sender.sendMessage("§cYou are not allowed to use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /giveitem <pickaxe_id> <player>");
            sender.sendMessage("§eAvailable pickaxes: " + String.join(", ", plugin.getItemManager().getAllPickaxeIds()));
            return true;
        }

        String pickaxeId = args[0].toLowerCase();
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

        if (!target.isOnline()) {
            sender.sendMessage("§cPlayer " + args[1] + " is not online!");
            return true;
        }

        Player player = (Player) target;
        ItemStack pickaxe = plugin.getItemManager().getPickaxe(pickaxeId);

        if (pickaxe == null) {
            sender.sendMessage("§cUnknown pickaxe: " + pickaxeId);
            sender.sendMessage("§eAvailable pickaxes: " + String.join(", ", plugin.getItemManager().getAllPickaxeIds()));
            return true;
        }

        player.getInventory().addItem(pickaxe);
        sender.sendMessage("§a" + pickaxeId + " has been given to " + player.getName());
        player.sendMessage("§aYou received: " + pickaxe.getItemMeta().getDisplayName());

        return true;
    }
}