package skyxnetwork.miningTycoon.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.managers.ItemManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TempoCommand implements CommandExecutor, TabCompleter {

    private final MiningTycoon plugin;

    public TempoCommand(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("miningtycoon.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /tempo <player> <level> [1-5]");
            sender.sendMessage(ChatColor.GRAY + "Applies Tempo enchant to player's held item.");
            return true;
        }

        String playerName = args[0];
        Player target = plugin.getServer().getPlayer(playerName);

        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player '" + playerName + "' not found.");
            return true;
        }

        int level = 1;
        if (args.length >= 2) {
            try {
                level = Integer.parseInt(args[1]);
                if (level < 1 || level > 5) {
                    sender.sendMessage(ChatColor.RED + "Tempo level must be between 1 and 5.");
                    return true;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid level: " + args[1]);
                return true;
            }
        }

        ItemStack item = target.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            target.sendMessage(ChatColor.RED + "You must be holding an item to apply Tempo!");
            sender.sendMessage(ChatColor.RED + "Target player must be holding an item.");
            return true;
        }

        ItemManager manager = plugin.getItemManager();
        String pickaxeId = manager.getPickaxeId(item);

        if (pickaxeId != null && !manager.canHaveTempo(pickaxeId)) {
            sender.sendMessage(ChatColor.RED + "This pickaxe cannot have Tempo enchant!");
            return true;
        }

        if (pickaxeId == null) {
            sender.sendMessage(ChatColor.YELLOW + "Warning: This item is not a configured pickaxe. Applying anyway...");
        }

        manager.applyTempoEnchant(item, level);

        String tempoRoman = "";
        for (int i = 0; i < level; i++) tempoRoman += "I" + (i < level - 1 ? " " : "");

        target.sendMessage(ChatColor.GREEN + "✓ Tempo " + tempoRoman + " applied!");
        target.sendMessage(ChatColor.GRAY + "Community Generator cooldown reduced by " + (int)(manager.getCooldownReductionPercent(level) * 100) + "%");

        sender.sendMessage(ChatColor.GREEN + "Applied Tempo " + tempoRoman + " to " + target.getName() + "'s item.");

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                completions.add(player.getName());
            }
        } else if (args.length == 2) {
            completions.add("1");
            completions.add("2");
            completions.add("3");
            completions.add("4");
            completions.add("5");
        }

        return completions;
    }
}