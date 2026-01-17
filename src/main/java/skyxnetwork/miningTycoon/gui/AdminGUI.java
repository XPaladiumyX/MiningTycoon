package skyxnetwork.miningTycoon.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.data.PlayerData;
import skyxnetwork.miningTycoon.utils.ItemBuilder;

public class AdminGUI implements Listener {

    private static MiningTycoon plugin;

    public AdminGUI(MiningTycoon plugin) {
        AdminGUI.plugin = plugin;
    }

    public static void openMainMenu(Player player, MiningTycoon pluginInstance) {
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

    public static void openPlayerManagement(Player player) {
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

    public static void openOnlinePlayers(Player admin) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8§lOnline Players");

        // Back button
        inv.setItem(0, new ItemBuilder(Material.ARROW)
                .setName("§7← Back")
                .build());

        int slot = 10;
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (slot >= 44) break;

            PlayerData data = plugin.getPlayerDataManager().getPlayerData(onlinePlayer);

            inv.setItem(slot, new ItemBuilder(Material.PLAYER_HEAD)
                    .setName("§e" + onlinePlayer.getName())
                    .setLore(
                            "§7Level: §6" + data.getLevel(),
                            "§7Prestige: §d" + data.getPrestige(),
                            "§7Experience: §b" + String.format("%.0f", data.getExperience()),
                            "",
                            "§eClick to manage player"
                    )
                    .build());
            slot++;
        }

        admin.openInventory(inv);
    }

    public static void openBoostManagement(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8§lBoost Management");

        // Back button
        inv.setItem(0, new ItemBuilder(Material.ARROW)
                .setName("§7← Back")
                .build());

        // Start EXP Boost
        inv.setItem(11, new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .setName("§d§lStart EXP Boost")
                .setLore("§7Click to start a global EXP boost")
                .build());

        // Start Coins Boost
        inv.setItem(13, new ItemBuilder(Material.GOLD_INGOT)
                .setName("§6§lStart Coins Boost")
                .setLore("§7Click to start a global coins boost")
                .build());

        // Start Combined Boost
        inv.setItem(15, new ItemBuilder(Material.NETHER_STAR)
                .setName("§b§lStart Combined Boost")
                .setLore("§7Click to start EXP + Coins boost")
                .build());

        // Current Status
        if (plugin.getBoostManager().isBoostActive()) {
            inv.setItem(22, new ItemBuilder(Material.LIME_DYE)
                    .setName("§a§lBoost Active")
                    .setLore(
                            "§7Type: §e" + plugin.getBoostManager().getBoostType(),
                            "§7Time Left: §e" + plugin.getBoostManager().getTimeRemaining() + "s"
                    )
                    .build());
        } else {
            inv.setItem(22, new ItemBuilder(Material.GRAY_DYE)
                    .setName("§c§lNo Active Boost")
                    .setLore("§7No boost is currently active")
                    .build());
        }

        player.openInventory(inv);
    }

    public static void openServerSettings(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8§lServer Settings");

        // Back button
        inv.setItem(0, new ItemBuilder(Material.ARROW)
                .setName("§7← Back")
                .build());

        // Reload Config
        inv.setItem(11, new ItemBuilder(Material.BOOK)
                .setName("§e§lReload Config")
                .setLore("§7Reload plugin configuration")
                .build());

        // Save All Data
        inv.setItem(13, new ItemBuilder(Material.WRITABLE_BOOK)
                .setName("§a§lSave All Data")
                .setLore("§7Save all player data to disk")
                .build());

        // View Statistics
        inv.setItem(15, new ItemBuilder(Material.KNOWLEDGE_BOOK)
                .setName("§b§lView Statistics")
                .setLore(
                        "§7Total Players: §e" + plugin.getPlayerDataManager().getAllPlayerData().size(),
                        "§7Online Players: §e" + Bukkit.getOnlinePlayers().size()
                )
                .build());

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        String title = event.getView().getTitle();
        if (event.getCurrentItem() == null) return;

        // Main Menu
        if (title.equals("§8§lMiningTycoon Admin")) {
            event.setCancelled(true);

            switch (event.getSlot()) {
                case 10: // Player Management
                    openPlayerManagement(player);
                    break;
                case 12: // Item Management
                    player.sendMessage("§eItem management coming soon!");
                    player.closeInventory();
                    break;
                case 14: // Boost Management
                    openBoostManagement(player);
                    break;
                case 16: // Server Settings
                    openServerSettings(player);
                    break;
                case 26: // Close
                    player.closeInventory();
                    break;
            }
        }

        // Player Management
        else if (title.equals("§8§lPlayer Management")) {
            event.setCancelled(true);

            switch (event.getSlot()) {
                case 0: // Back
                    openMainMenu(player, plugin);
                    break;
                case 11: // Online Players
                    openOnlinePlayers(player);
                    break;
                case 13: // Search Player
                    player.sendMessage("§ePlayer search coming soon!");
                    player.closeInventory();
                    break;
                case 15: // Top Players
                    player.sendMessage("§eLeaderboards coming soon!");
                    player.closeInventory();
                    break;
            }
        }

        // Online Players
        else if (title.equals("§8§lOnline Players")) {
            event.setCancelled(true);

            if (event.getSlot() == 0) {
                openPlayerManagement(player);
            }
        }

        // Boost Management
        else if (title.equals("§8§lBoost Management")) {
            event.setCancelled(true);

            switch (event.getSlot()) {
                case 0: // Back
                    openMainMenu(player, plugin);
                    break;
                case 11: // Start EXP Boost
                    plugin.getBoostManager().startBoost("exp", player);
                    player.closeInventory();
                    player.sendMessage("§aGlobal EXP boost started!");
                    break;
                case 13: // Start Coins Boost
                    plugin.getBoostManager().startBoost("coins", player);
                    player.closeInventory();
                    player.sendMessage("§aGlobal Coins boost started!");
                    break;
                case 15: // Start Combined Boost
                    plugin.getBoostManager().startBoost("both", player);
                    player.closeInventory();
                    player.sendMessage("§aGlobal Combined boost started!");
                    break;
            }
        }

        // Server Settings
        else if (title.equals("§8§lServer Settings")) {
            event.setCancelled(true);

            switch (event.getSlot()) {
                case 0: // Back
                    openMainMenu(player, plugin);
                    break;
                case 11: // Reload Config
                    plugin.reloadConfig();
                    player.sendMessage("§aConfiguration reloaded!");
                    player.closeInventory();
                    break;
                case 13: // Save All Data
                    plugin.getDataStorage().saveAllData();
                    player.sendMessage("§aAll player data saved!");
                    player.closeInventory();
                    break;
                case 15: // View Statistics
                    // Already shows stats in lore
                    break;
            }
        }
    }
}
