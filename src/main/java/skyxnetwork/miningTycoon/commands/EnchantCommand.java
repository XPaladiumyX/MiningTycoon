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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EnchantCommand implements CommandExecutor, TabCompleter {

    private final MiningTycoon plugin;
    private static final List<String> ENCHANTS = Arrays.asList("Tempo");
    private static final int MAX_TEMPO_LEVEL = 5;

    public EnchantCommand(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("miningtycoon.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " enchant <enchant> <level> <player>");
            sender.sendMessage(ChatColor.GRAY + "Enchants: " + String.join(", ", ENCHANTS));
            sender.sendMessage(ChatColor.GRAY + "Example: /" + label + " enchant Tempo 3 Notch");
            return true;
        }

        String enchantName = args[0].toLowerCase();
        Player target = null;
        int level;

        try {
            level = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid level: " + args[1]);
            return true;
        }

        String playerName = args[2];
        target = plugin.getServer().getPlayer(playerName);

        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player '" + playerName + "' not found.");
            return true;
        }

        ItemStack item = target.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            target.sendMessage(ChatColor.RED + "You must be holding an item!");
            sender.sendMessage(ChatColor.RED + "Target player must be holding an item.");
            return true;
        }

        ItemManager manager = plugin.getItemManager();
        String pickaxeId = manager.getPickaxeId(item);

        switch (enchantName) {
            case "tempo":
                if (level < 1 || level > MAX_TEMPO_LEVEL) {
                    sender.sendMessage(ChatColor.RED + "Tempo level must be 1-" + MAX_TEMPO_LEVEL + ".");
                    return true;
                }

                if (pickaxeId != null && !manager.canHaveTempo(pickaxeId)) {
                    sender.sendMessage(ChatColor.RED + "This pickaxe cannot have Tempo enchant (set tempoEnabled: true in config).");
                    return true;
                }

                manager.applyTempoEnchant(item, level);

                StringBuilder tempoRoman = new StringBuilder();
                for (int i = 0; i < level; i++) {
                    tempoRoman.append("I");
                    if (i < level - 1) tempoRoman.append(" ");
                }

                target.sendMessage(ChatColor.GREEN + "Applied Tempo " + tempoRoman + " to your item!");
                target.sendMessage(ChatColor.GRAY + "Community Generator cooldown reduced by " + (int)(manager.getCooldownReductionPercent(level) * 100) + "%");
                sender.sendMessage(ChatColor.GREEN + "Applied Tempo " + tempoRoman + " to " + target.getName() + "'s item.");
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown enchant: " + enchantName);
                sender.sendMessage(ChatColor.GRAY + "Available: " + String.join(", ", ENCHANTS));
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(ENCHANTS);
        } else if (args.length == 2) {
            for (int i = 1; i <= MAX_TEMPO_LEVEL; i++) {
                completions.add(String.valueOf(i));
            }
        } else if (args.length == 3) {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                completions.add(player.getName());
            }
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}