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

public class MiningTycoonCommand implements CommandExecutor {

    private final MiningTycoon plugin;

    public MiningTycoonCommand(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("miningtycoon.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                sender.sendMessage("§7[§6MiningTycoon§7] §eReloading plugin...");

                // Reload config
                plugin.reloadConfig();

                // Reload zone requirements
                plugin.getZoneManager().reload();

                // Reload mines (blocks)
                plugin.getMineManager().reload();

                // Reload all items
                plugin.getItemManager().loadAllItems();

                // Reload prestige portals and rebirth configs
                plugin.getPrestigePortalManager().reload();
                plugin.getPrestigeManager().reload();

                // Reload area gates
                plugin.getAreaGateManager().reload();

                // Reload community generator
                plugin.getCommunityGeneratorConfig().reload();

                // Save all player data
                plugin.getDataStorage().saveAllData();

                sender.sendMessage("§7[§6MiningTycoon§7] §aPlugin reloaded successfully!");
                sender.sendMessage("§7[§6MiningTycoon§7] §aLoaded " +
                        plugin.getZoneManager().getZoneCount() + " zones");
                sender.sendMessage("§7[§6MiningTycoon§7] §aLoaded " +
                        plugin.getMineManager().getTotalBlockCount() + " mine blocks");
                sender.sendMessage("§7[§6MiningTycoon§7] §aLoaded " +
                        plugin.getItemManager().getAllPickaxeIds().size() + " pickaxes, " +
                        plugin.getItemManager().getAllArmorIds().size() + " armor pieces, and " +
                        plugin.getItemManager().getAllPetIds().size() + " pets");
                sender.sendMessage("§7[§6MiningTycoon§7] §aLoaded " +
                        plugin.getPrestigePortalManager().getPortals().size() + " prestige portal(s)");
                break;

            case "save":
                sender.sendMessage("§7[§6MiningTycoon§7] §eSaving all data...");
                plugin.getDataStorage().saveAllData();
                sender.sendMessage("§7[§6MiningTycoon§7] §aAll data saved successfully!");
                break;

            case "version":
                sender.sendMessage("§7[§6MiningTycoon§7] §eVersion: §a" + plugin.getDescription().getVersion());
                sender.sendMessage("§7[§6MiningTycoon§7] §eAuthor: §aXPaladiumyX");
                sender.sendMessage("§7[§6MiningTycoon§7] §eAPI: §a" + plugin.getDescription().getAPIVersion());
                break;

            case "enchant":
                return handleEnchant(sender, args);

            case "help":
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        sender.sendMessage("§6§lMiningTycoon §7- Admin Commands");
        sender.sendMessage("");
        sender.sendMessage("§e/miningtycoon reload §7- Reload the plugin");
        sender.sendMessage("§e/miningtycoon save §7- Save all data");
        sender.sendMessage("§e/miningtycoon version §7- Show version info");
        sender.sendMessage("§e/miningtycoon enchant §7<add|remove> <enchant> <level> <player>");
        sender.sendMessage("§e/miningtycoon help §7- Show this help");
        sender.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    private boolean handleEnchant(CommandSender sender, String[] args) {
        String action = args[1].toLowerCase();
        
        if (action.equals("add")) {
            if (args.length < 5) {
                sender.sendMessage(ChatColor.RED + "Usage: /miningtycoon enchant add <enchant> <level> <player>");
                sender.sendMessage(ChatColor.GRAY + "Example: /miningtycoon enchant add tempo 3 Notch");
                return true;
            }
        } else if (action.equals("remove")) {
            if (args.length < 4) {
                sender.sendMessage(ChatColor.RED + "Usage: /miningtycoon enchant remove <enchant> <player>");
                sender.sendMessage(ChatColor.GRAY + "Example: /miningtycoon enchant remove tempo Notch");
                return true;
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Usage: /miningtycoon enchant <add|remove> ...");
            return true;
        }

        String enchantName = args[2].toLowerCase();
        int level;
        Player target;

        if (action.equals("add")) {
            try {
                level = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid level: " + args[3]);
                return true;
            }

            target = plugin.getServer().getPlayer(args[4]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player '" + args[4] + "' not found.");
                return true;
            }
        } else {
            level = 0;
            target = plugin.getServer().getPlayer(args[3]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player '" + args[3] + "' not found.");
                return true;
            }
        }

        ItemStack item = target.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            sender.sendMessage(ChatColor.RED + "Player must be holding an item.");
            return true;
        }

        ItemManager manager = plugin.getItemManager();

        switch (enchantName) {
            case "tempo":
                if (action.equals("add")) {
                    if (level < 1 || level > 5) {
                        sender.sendMessage(ChatColor.RED + "Tempo level must be 1-5.");
                        return true;
                    }
                    if (manager.getPickaxeCooldownReductionLevel(item) > 0) {
                        sender.sendMessage(ChatColor.RED + "This item already has Tempo enchant. Use /mt enchant remove tempo first.");
                        return true;
                    }
                    manager.applyTempoEnchant(item, level);
                    String[] roman = {"", "I", "II", "III", "IV", "V"};
                    target.sendMessage(ChatColor.GREEN + "Applied Tempo " + roman[level] + "!");
                    sender.sendMessage(ChatColor.GREEN + "Applied Tempo " + roman[level] + " to " + target.getName());
                } else {
                    if (manager.getPickaxeCooldownReductionLevel(item) == 0) {
                        sender.sendMessage(ChatColor.RED + "This item doesn't have Tempo enchant.");
                        return true;
                    }
                    manager.removeTempoEnchant(item);
                    target.sendMessage(ChatColor.GREEN + "Tempo enchant removed!");
                    sender.sendMessage(ChatColor.GREEN + "Removed Tempo enchant from " + target.getName());
                }
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown enchant: " + enchantName);
                break;
        }
        return true;
    }
}