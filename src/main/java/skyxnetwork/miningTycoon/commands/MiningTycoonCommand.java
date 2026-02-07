package skyxnetwork.miningTycoon.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import skyxnetwork.miningTycoon.MiningTycoon;

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

                // Reload all items
                plugin.getItemManager().loadAllItems();

                // Reload prestige portals
                plugin.getPrestigePortalManager().reload();

                // Save all player data
                plugin.getDataStorage().saveAllData();

                sender.sendMessage("§7[§6MiningTycoon§7] §aPlugin reloaded successfully!");
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
        sender.sendMessage("§e/miningtycoon help §7- Show this help");
        sender.sendMessage("§8§m━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
}