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

        inv.setItem(10, new ItemBuilder(Material.PLAYER_HEAD)
                .setName("§6§lPlayer Management")
                .setLore("§7Click to manage player data", "", "§e► View player stats", "§e► Set levels", "§e► Set prestige")
                .build());

        inv.setItem(12, new ItemBuilder(Material.DIAMOND_PICKAXE)
                .setName("§b§lItem Management")
                .setLore("§7Click to manage items", "", "§e► Manage tools", "§e► Manage armor", "§e► Manage pets")
                .build());

        inv.setItem(14, new ItemBuilder(Material.BEACON)
                .setName("§d§lBoost Management")
                .setLore("§7Click to manage boosts", "", "§e► Start global boost", "§e► View boost status", "§e► End current boost")
                .build());

        inv.setItem(16, new ItemBuilder(Material.COMMAND_BLOCK)
                .setName("§c§lServer Settings")
                .setLore("§7Click to configure server", "", "§e► Reload config", "§e► Save all data", "§e► View statistics")
                .build());

        inv.setItem(26, new ItemBuilder(Material.BARRIER)
                .setName("§cClose")
                .build());

        player.openInventory(inv);
    }

    public static void openItemManagement(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8§lItem Management");

        inv.setItem(0, new ItemBuilder(Material.ARROW)
                .setName("§7← Back")
                .build());

        inv.setItem(11, new ItemBuilder(Material.DIAMOND_PICKAXE)
                .setName("§e§lManage Tools")
                .setLore("§7View and manage all pickaxes")
                .build());

        inv.setItem(13, new ItemBuilder(Material.LEATHER_CHESTPLATE)
                .setName("§b§lManage Armor")
                .setLore("§7View and manage all armor sets")
                .build());

        inv.setItem(15, new ItemBuilder(Material.PLAYER_HEAD)
                .setName("§d§lManage Pets")
                .setLore("§7View and manage all pets")
                .build());

        player.openInventory(inv);
    }

    public static void openBoostManagement(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8§lBoost Management");

        inv.setItem(0, new ItemBuilder(Material.ARROW)
                .setName("§7← Back")
                .build());

        inv.setItem(10, new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .setName("§d§lStart EXP Boost")
                .setLore("§7Click to start a global EXP boost")
                .build());

        inv.setItem(12, new ItemBuilder(Material.GOLD_INGOT)
                .setName("§6§lStart Coins Boost")
                .setLore("§7Click to start a global coins boost")
                .build());

        inv.setItem(14, new ItemBuilder(Material.NETHER_STAR)
                .setName("§b§lStart Combined Boost")
                .setLore("§7Click to start EXP + Coins boost")
                .build());

        inv.setItem(16, new ItemBuilder(Material.REDSTONE_BLOCK)
                .setName("§c§lStop Current Boost")
                .setLore("§7Click to stop the active boost")
                .build());

        if (plugin.getBoostManager().isBoostActive()) {
            inv.setItem(22, new ItemBuilder(Material.LIME_DYE)
                    .setName("§a§lBoost Active")
                    .setLore(
                            "§7Type: §e" + plugin.getBoostManager().getBoostType(),
                            "§7Time Left: §e" + plugin.getBoostManager().getTimeRemaining() + "s",
                            "",
                            "§c► Click to stop"
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

    public static void openToolsManagement(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8§lTool Management");

        inv.setItem(0, new ItemBuilder(Material.ARROW)
                .setName("§7← Back")
                .build());

        // Display all tools
        String[] tools = {
                "wooden_pickaxe", "stone_pickaxe", "reinforced_pickaxe", "rockshredder_pickaxe",
                "stone_crusher", "iron_pickaxe", "tempered_edge", "ore_splitter",
                "iron_storm", "diamond_pickaxe", "crystal_cutter", "shardpiercer",
                "gemreaper", "aetherpick"
        };

        int slot = 9;
        for (String tool : tools) {
            inv.setItem(slot++, new ItemBuilder(Material.DIAMOND_PICKAXE)
                    .setName("§e" + tool.replace("_", " ").toUpperCase())
                    .setLore("§7Click to view/edit this tool")
                    .build());
        }

        player.openInventory(inv);
    }

    public static void openArmorManagement(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8§lArmor Management");

        inv.setItem(0, new ItemBuilder(Material.ARROW)
                .setName("§7← Back")
                .build());

        // Display armor sets
        String[] armorSets = {
                "Miner Set", "Prospector Set", "Quarry Set", "Industrial Set",
                "Random Miner Set", "Steel Miner Set", "Coal Extractor Set",
                "Amber Worker Set", "Bronze Digger Set", "Obsidian Laborer Set",
                "Saltstone Scraper Set", "Gold Hunter Set", "Lapis Technician Set",
                "Topaz Artisan Set", "Cobalt Excavator Set", "Granite Breaker Set",
                "Onyx Surveyor Set", "Ruby Prospector Set", "Emerald Harvester Set",
                "Mythril Splitter Set", "Zircon Excavator Set", "Moonstone Diver Set",
                "Starlight Guardian Set", "Void Monarch Set", "Chrono Crusher Set",
                "Celestial Titan Set", "Draconic Reaper Set", "Galactic Annihilator Set",
                "Eternal Warden Set"
        };

        int slot = 9;
        for (String set : armorSets) {
            if (slot >= 53) break;
            inv.setItem(slot++, new ItemBuilder(Material.LEATHER_CHESTPLATE)
                    .setName("§b" + set)
                    .setLore("§7Click to view/edit this armor set")
                    .build());
        }

        player.openInventory(inv);
    }

    public static void openPetsManagement(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8§lPet Management");

        inv.setItem(0, new ItemBuilder(Material.ARROW)
                .setName("§7← Back")
                .build());

        // Display all pets
        String[] pets = {
                "Rocky Mole", "Stone Crab", "Tiny Golem", "Cave Bat", "Dusty Rat",
                "Pebble Slime", "Iron Snail", "Pickaxe Puppy", "Drill Bee",
                "Mine Cat", "Rusty Guardian", "Glow Worm", "Silver Griffin",
                "Ore Hound", "Magnet Crawler", "Smelt Spirit", "Shimmer Beetle",
                "Emerald Fox", "Drill Core", "Crystallion", "Obsidian Beast",
                "Sparkling Phoenix", "Ancient Digger", "Lava Serpent",
                "Titanium Dragon", "Mythic Centipede", "Quantum Ghost",
                "Void Harvester", "Chrono Mole", "Nuclear Wyrm", "Galactic Golem"
        };

        int slot = 9;
        for (String pet : pets) {
            if (slot >= 53) break;
            inv.setItem(slot++, new ItemBuilder(Material.PLAYER_HEAD)
                    .setName("§d" + pet)
                    .setLore("§7Click to view/edit this pet")
                    .build());
        }

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
                    openItemManagement(player);
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

        // Item Management
        else if (title.equals("§8§lItem Management")) {
            event.setCancelled(true);

            switch (event.getSlot()) {
                case 0: // Back
                    openMainMenu(player, plugin);
                    break;
                case 11: // Manage Tools
                    openToolsManagement(player);
                    break;
                case 13: // Manage Armor
                    openArmorManagement(player);
                    break;
                case 15: // Manage Pets
                    openPetsManagement(player);
                    break;
            }
        }

        // Tool Management
        else if (title.equals("§8§lTool Management")) {
            event.setCancelled(true);
            if (event.getSlot() == 0) {
                openItemManagement(player);
            }
        }

        // Armor Management
        else if (title.equals("§8§lArmor Management")) {
            event.setCancelled(true);
            if (event.getSlot() == 0) {
                openItemManagement(player);
            }
        }

        // Pet Management
        else if (title.equals("§8§lPet Management")) {
            event.setCancelled(true);
            if (event.getSlot() == 0) {
                openItemManagement(player);
            }
        }

        // Boost Management
        else if (title.equals("§8§lBoost Management")) {
            event.setCancelled(true);

            switch (event.getSlot()) {
                case 0: // Back
                    openMainMenu(player, plugin);
                    break;
                case 10: // Start EXP Boost
                    plugin.getBoostManager().startBoost("exp", player);
                    player.closeInventory();
                    player.sendMessage("§aGlobal EXP boost started!");
                    break;
                case 12: // Start Coins Boost
                    plugin.getBoostManager().startBoost("coins", player);
                    player.closeInventory();
                    player.sendMessage("§aGlobal Coins boost started!");
                    break;
                case 14: // Start Combined Boost
                    plugin.getBoostManager().startBoost("both", player);
                    player.closeInventory();
                    player.sendMessage("§aGlobal Combined boost started!");
                    break;
                case 16: // Stop Current Boost
                    if (plugin.getBoostManager().isBoostActive()) {
                        plugin.getBoostManager().endBoost();
                        player.sendMessage("§aGlobal boost has been stopped!");
                        openBoostManagement(player); // Refresh
                    } else {
                        player.sendMessage("§cNo active boost to stop!");
                    }
                    break;
                case 22: // Status (also stops if active)
                    if (plugin.getBoostManager().isBoostActive()) {
                        plugin.getBoostManager().endBoost();
                        player.sendMessage("§aGlobal boost has been stopped!");
                        openBoostManagement(player); // Refresh
                    }
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
            }
        }

        // Online Players
        else if (title.equals("§8§lOnline Players")) {
            event.setCancelled(true);
            if (event.getSlot() == 0) {
                openPlayerManagement(player);
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
            }
        }
    }

    public static void openPlayerManagement(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8§lPlayer Management");

        inv.setItem(0, new ItemBuilder(Material.ARROW)
                .setName("§7← Back")
                .build());

        inv.setItem(11, new ItemBuilder(Material.COMPASS)
                .setName("§a§lOnline Players")
                .setLore("§7View and manage online players")
                .build());

        player.openInventory(inv);
    }

    public static void openOnlinePlayers(Player admin) {
        Inventory inv = Bukkit.createInventory(null, 54, "§8§lOnline Players");

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

    public static void openServerSettings(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8§lServer Settings");

        inv.setItem(0, new ItemBuilder(Material.ARROW)
                .setName("§7← Back")
                .build());

        inv.setItem(11, new ItemBuilder(Material.BOOK)
                .setName("§e§lReload Config")
                .setLore("§7Reload plugin configuration")
                .build());

        inv.setItem(13, new ItemBuilder(Material.WRITABLE_BOOK)
                .setName("§a§lSave All Data")
                .setLore("§7Save all player data to disk")
                .build());

        inv.setItem(15, new ItemBuilder(Material.KNOWLEDGE_BOOK)
                .setName("§b§lView Statistics")
                .setLore(
                        "§7Total Players: §e" + plugin.getPlayerDataManager().getAllPlayerData().size(),
                        "§7Online Players: §e" + Bukkit.getOnlinePlayers().size()
                )
                .build());

        player.openInventory(inv);
    }
}