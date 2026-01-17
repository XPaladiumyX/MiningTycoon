package skyxnetwork.miningTycoon.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.utils.ItemBuilder;

public class AdminGUI {

    public static void openMainMenu(Player player, MiningTycoon plugin) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8§lMiningTycoon Admin");

        // Player Management
        inv.setItem(10, new ItemBuilder(Material.PLAYER_HEAD)
                .setName("§6§lPlayer Management")
                .setLore("§7Click to manage player data", "", "§e► View player stats", "§e► Set levels", "§e► Set prestige")
                .build());

        // Item Management
        inv.setItem(12, new ItemBuilder(Material.DIAMOND_PICKAXE)
                .setName("§b§lItem Management")
                .setLore("§7Click to give items", "", "§e► Give tools", "§e► Give armor", "§e► Give pets")
                .build());

        // Boost Management
        inv.setItem(14, new ItemBuilder(Material.BEACON)
                .setName("§d§lBoost Management")
                .setLore("§7Click to manage boosts", "", "§e► Start global boost", "§e► View boost status", "§e► End current boost")
                .build());

        // Server Settings
        inv.setItem(16, new ItemBuilder(Material.COMMAND_BLOCK)
                .setName("§c§lServer Settings")
                .setLore("§7Click to configure server", "", "§e► Reload config", "§e► Save all data", "§e► View statistics")
                .build());

        // Close button
        inv.setItem(26, new ItemBuilder(Material.BARRIER)
                .setName("§cClose")
                .build());

        player.openInventory(inv);
    }

    public static void openPlayerManagement(Player player, MiningTycoon plugin) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8§lPlayer Management");

        // Back button
        inv.setItem(0, new ItemBuilder(Material.ARROW)
                .setName("§7← Back")
                .build());

        // View Online Players
        inv.setItem(11, new ItemBuilder(Material.COMPASS)
                .setName("§a§lOnline Players")
                .setLore("§7View and manage online players")
                .build());

        // Search Player
        inv.setItem(13, new ItemBuilder(Material.SPYGLASS)
                .setName("§e§lSearch Player")
                .setLore("§7Search for a specific player")
                .build());

        // Top Players
        inv.setItem(15, new ItemBuilder(Material.GOLD_BLOCK)
                .setName("§6§lTop Players")
                .setLore("§7View leaderboards")
                .build());

        player.openInventory(inv);
    }
}
