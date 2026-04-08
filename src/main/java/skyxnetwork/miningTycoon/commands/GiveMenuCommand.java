package skyxnetwork.miningTycoon.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.listeners.PlayerJoinListener;

public class GiveMenuCommand implements CommandExecutor {

    private final MiningTycoon plugin;

    public GiveMenuCommand(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("miningtycoon.admin")) {
            sender.sendMessage("§cYou don't have permission!");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /givemenu <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            sender.sendMessage("§cPlayer not found or offline!");
            return true;
        }

        // Check if player already has the menu item
        if (hasMenuItemInInventory(target)) {
            sender.sendMessage("§c" + target.getName() + " already has the menu item!");
            return true;
        }

        // Give the menu item
        ItemStack menuItem = PlayerJoinListener.createMenuItem();
        
        // Try to give at slot 8 first, then find any empty slot
        if (target.getInventory().getItem(8) == null || 
            target.getInventory().getItem(8).getType() == Material.AIR) {
            target.getInventory().setItem(8, menuItem);
        } else {
            target.getInventory().addItem(menuItem);
        }

        sender.sendMessage("§aGiven menu item to " + target.getName() + "!");
        target.sendMessage("§aYou received the menu item!");
        
        return true;
    }

    private boolean hasMenuItemInInventory(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (PlayerJoinListener.isMenuItem(item)) {
                return true;
            }
        }
        return false;
    }
}