package skyxnetwork.miningTycoon.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.data.PlayerData;
import skyxnetwork.miningTycoon.utils.ItemBuilder;

import java.util.ArrayList;
import java.util.List;

public class AdminGUINew implements Listener {

    private static MiningTycoon plugin;

    public AdminGUINew(MiningTycoon plugin) {
        AdminGUINew.plugin = plugin;
    }

    // Main Menu
    public static void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8§lMiningTycoon Admin");

        inv.setItem(10, new ItemBuilder(Material.PLAYER_HEAD)
                .setName("§6§lPlayer Management")
                .setLore("§7Manage player data", "", "§e▸ View stats", "§e▸ Edit levels", "§e▸ Edit prestige")
                .build());

        inv.setItem(12, new ItemBuilder(Material.DIAMOND_PICKAXE)
                .setName("§b§lItem Management")
                .setLore("§7Manage all items", "", "§e▸ Edit pickaxes", "§e▸ Edit armor", "§e▸ Edit pets")
                .build());

        inv.setItem(14, new ItemBuilder(Material.BEACON)
                .setName("§d§lBoost Management")
                .setLore("§7Control global boosts", "", "§e▸ Start boost", "§e▸ View status", "§e▸ End boost")
                .build());

        inv.setItem(16, new ItemBuilder(Material.COMMAND_BLOCK)
                .setName("§c§lServer Settings")
                .setLore("§7Plugin configuration", "", "§e▸ Reload plugin", "§e▸ Save data", "§e▸ Statistics")
                .build());

        inv.setItem(26, new ItemBuilder(Material.BARRIER)
                .setName("§cClose")
                .build());

        player.openInventory(inv);
    }

    // Item Management Menu
    public static void openItemManagement(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8§lItem Management");

        inv.setItem(0, new ItemBuilder(Material.ARROW)
                .setName("§7← Back")
                .build());

        inv.setItem(11, new ItemBuilder(Material.DIAMOND_PICKAXE)
                .setName("§e§lPickaxes")
                .setLore(
                        "§7Total: §e" + plugin.getItemManager().getAllPickaxeIds().size(),
                        "",
                        "§e▸ Left Click §7to view all",
                        "§a▸ Right Click §7to create new"
                )
                .build());

        inv.setItem(13, new ItemBuilder(Material.LEATHER_CHESTPLATE)
                .setName("§b§lArmor")
                .setLore(
                        "§7Total: §e" + plugin.getItemManager().getAllArmorIds().size(),
                        "",
                        "§e▸ Left Click §7to view all",
                        "§a▸ Right Click §7to create new"
                )
                .build());

        inv.setItem(15, new ItemBuilder(Material.PLAYER_HEAD)
                .setName("§d§lPets")
                .setLore(
                        "§7Total: §e" + plugin.getItemManager().getAllPetIds().size(),
                        "",
                        "§e▸ Left Click §7to view all",
                        "§a▸ Right Click §7to create new"
                )
                .build());

        player.openInventory(inv);
    }

    // Pickaxe List
    public static void openPickaxeList(Player player, int page) {
        List<String> pickaxes = new ArrayList<>(plugin.getItemManager().getAllPickaxeIds());
        int totalPages = (int) Math.ceil(pickaxes.size() / 45.0);

        Inventory inv = Bukkit.createInventory(null, 54, "§8§lPickaxes (Page " + (page + 1) + "/" + totalPages + ")");

        inv.setItem(0, new ItemBuilder(Material.ARROW)
                .setName("§7← Back")
                .build());

        inv.setItem(8, new ItemBuilder(Material.EMERALD)
                .setName("§a§lCreate New Pickaxe")
                .setLore("§7Click to create a new pickaxe")
                .build());

        // Previous page
        if (page > 0) {
            inv.setItem(45, new ItemBuilder(Material.ARROW)
                    .setName("§7← Previous Page")
                    .build());
        }

        // Next page
        if (page < totalPages - 1) {
            inv.setItem(53, new ItemBuilder(Material.ARROW)
                    .setName("§7Next Page →")
                    .build());
        }

        // Display pickaxes for this page
        int startIndex = page * 45;
        int endIndex = Math.min(startIndex + 45, pickaxes.size());

        for (int i = startIndex; i < endIndex; i++) {
            String id = pickaxes.get(i);
            ItemStack pickaxe = plugin.getItemManager().getPickaxe(id);

            if (pickaxe != null) {
                ItemStack display = pickaxe.clone();
                ItemBuilder builder = new ItemBuilder(display.getType());

                List<String> lore = new ArrayList<>(display.getItemMeta().hasLore() ?
                        display.getItemMeta().getLore() : new ArrayList<>());
                lore.add("");
                lore.add("§7ID: §e" + id);
                lore.add("");
                lore.add("§e▸ Left Click §7to edit");
                lore.add("§c▸ Right Click §7to delete");
                lore.add("§a▸ Shift + Left §7to give yourself");

                builder.setLore(lore);
                inv.setItem(9 + (i - startIndex), builder.build());
            }
        }

        player.openInventory(inv);
    }

    // Armor List
    public static void openArmorList(Player player, int page) {
        List<String> armors = new ArrayList<>(plugin.getItemManager().getAllArmorIds());
        int totalPages = (int) Math.ceil(armors.size() / 45.0);

        Inventory inv = Bukkit.createInventory(null, 54, "§8§lArmor (Page " + (page + 1) + "/" + totalPages + ")");

        inv.setItem(0, new ItemBuilder(Material.ARROW)
                .setName("§7← Back")
                .build());

        inv.setItem(8, new ItemBuilder(Material.EMERALD)
                .setName("§a§lCreate New Armor")
                .setLore("§7Click to create new armor")
                .build());

        // Navigation
        if (page > 0) {
            inv.setItem(45, new ItemBuilder(Material.ARROW)
                    .setName("§7← Previous Page")
                    .build());
        }

        if (page < totalPages - 1) {
            inv.setItem(53, new ItemBuilder(Material.ARROW)
                    .setName("§7Next Page →")
                    .build());
        }

        // Display armor
        int startIndex = page * 45;
        int endIndex = Math.min(startIndex + 45, armors.size());

        for (int i = startIndex; i < endIndex; i++) {
            String id = armors.get(i);
            ItemStack armor = plugin.getItemManager().getArmor(id);

            if (armor != null) {
                ItemStack display = armor.clone();
                ItemBuilder builder = new ItemBuilder(display.getType());

                List<String> lore = new ArrayList<>(display.getItemMeta().hasLore() ?
                        display.getItemMeta().getLore() : new ArrayList<>());
                lore.add("");
                lore.add("§7ID: §e" + id);
                lore.add("");
                lore.add("§e▸ Left Click §7to edit");
                lore.add("§c▸ Right Click §7to delete");
                lore.add("§a▸ Shift + Left §7to give yourself");

                builder.setLore(lore);
                inv.setItem(9 + (i - startIndex), builder.build());
            }
        }

        player.openInventory(inv);
    }

    // Pet List
    public static void openPetList(Player player, int page) {
        List<String> pets = new ArrayList<>(plugin.getItemManager().getAllPetIds());
        int totalPages = (int) Math.ceil(pets.size() / 45.0);

        Inventory inv = Bukkit.createInventory(null, 54, "§8§lPets (Page " + (page + 1) + "/" + totalPages + ")");

        inv.setItem(0, new ItemBuilder(Material.ARROW)
                .setName("§7← Back")
                .build());

        inv.setItem(8, new ItemBuilder(Material.EMERALD)
                .setName("§a§lCreate New Pet")
                .setLore("§7Click to create a new pet")
                .build());

        // Navigation
        if (page > 0) {
            inv.setItem(45, new ItemBuilder(Material.ARROW)
                    .setName("§7← Previous Page")
                    .build());
        }

        if (page < totalPages - 1) {
            inv.setItem(53, new ItemBuilder(Material.ARROW)
                    .setName("§7Next Page →")
                    .build());
        }

        // Display pets
        int startIndex = page * 45;
        int endIndex = Math.min(startIndex + 45, pets.size());

        for (int i = startIndex; i < endIndex; i++) {
            String id = pets.get(i);
            ItemStack pet = plugin.getItemManager().getPet(id);

            if (pet != null) {
                ItemStack display = pet.clone();
                ItemBuilder builder = new ItemBuilder(display.getType());

                List<String> lore = new ArrayList<>(display.getItemMeta().hasLore() ?
                        display.getItemMeta().getLore() : new ArrayList<>());
                lore.add("");
                lore.add("§7ID: §e" + id);
                lore.add("");
                lore.add("§e▸ Left Click §7to edit");
                lore.add("§c▸ Right Click §7to delete");
                lore.add("§a▸ Shift + Left §7to give yourself");

                builder.setLore(lore);
                inv.setItem(9 + (i - startIndex), builder.build());
            }
        }

        player.openInventory(inv);
    }

    // Boost Management
    public static void openBoostManagement(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8§lBoost Management");

        inv.setItem(0, new ItemBuilder(Material.ARROW)
                .setName("§7← Back")
                .build());

        inv.setItem(10, new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .setName("§d§lStart EXP Boost")
                .setLore("§7Start a global EXP boost")
                .build());

        inv.setItem(12, new ItemBuilder(Material.GOLD_INGOT)
                .setName("§6§lStart Coins Boost")
                .setLore("§7Start a global coins boost")
                .build());

        inv.setItem(14, new ItemBuilder(Material.NETHER_STAR)
                .setName("§b§lStart Combined Boost")
                .setLore("§7Start EXP + Coins boost")
                .build());

        inv.setItem(16, new ItemBuilder(Material.REDSTONE_BLOCK)
                .setName("§c§lStop Current Boost")
                .setLore("§7Stop the active boost")
                .build());

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
                    .build());
        }

        player.openInventory(inv);
    }

    // Player Management
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

    // Online Players
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

    // Server Settings
    public static void openServerSettings(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8§lServer Settings");

        inv.setItem(0, new ItemBuilder(Material.ARROW)
                .setName("§7← Back")
                .build());

        inv.setItem(11, new ItemBuilder(Material.BOOK)
                .setName("§e§lReload Plugin")
                .setLore("§7Reload configuration and items")
                .build());

        inv.setItem(13, new ItemBuilder(Material.WRITABLE_BOOK)
                .setName("§a§lSave All Data")
                .setLore("§7Save all player data to disk")
                .build());

        inv.setItem(15, new ItemBuilder(Material.KNOWLEDGE_BOOK)
                .setName("§b§lView Statistics")
                .setLore(
                        "§7Total Players: §e" + plugin.getPlayerDataManager().getAllPlayerData().size(),
                        "§7Online Players: §e" + Bukkit.getOnlinePlayers().size(),
                        "§7Pickaxes: §e" + plugin.getItemManager().getAllPickaxeIds().size(),
                        "§7Armor: §e" + plugin.getItemManager().getAllArmorIds().size(),
                        "§7Pets: §e" + plugin.getItemManager().getAllPetIds().size()
                )
                .build());

        player.openInventory(inv);
    }

    // Event Handler
    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (!title.startsWith("§8§l")) return;

        event.setCancelled(true);

        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
            return;
        }

        handleClick(player, title, event.getSlot(), event.getClick());
    }

    private static void handleClick(Player player, String title, int slot, ClickType clickType) {
        // Main Menu
        if (title.equals("§8§lMiningTycoon Admin")) {
            switch (slot) {
                case 10:
                    openPlayerManagement(player);
                    break;
                case 12:
                    openItemManagement(player);
                    break;
                case 14:
                    openBoostManagement(player);
                    break;
                case 16:
                    openServerSettings(player);
                    break;
                case 26:
                    player.closeInventory();
                    break;
            }
        }
        // Item Management
        else if (title.equals("§8§lItem Management")) {
            switch (slot) {
                case 0:
                    openMainMenu(player);
                    break;
                case 11:
                    if (clickType == ClickType.LEFT) {
                        openPickaxeList(player, 0);
                    } else if (clickType == ClickType.RIGHT) {
                        player.sendMessage("§7[§6Admin§7] §eItem creation GUI coming soon!");
                        player.sendMessage("§7[§6Admin§7] §eFor now, edit items/pickaxes.yml and use /miningtycoon reload");
                    }
                    break;
                case 13:
                    if (clickType == ClickType.LEFT) {
                        openArmorList(player, 0);
                    } else if (clickType == ClickType.RIGHT) {
                        player.sendMessage("§7[§6Admin§7] §eItem creation GUI coming soon!");
                        player.sendMessage("§7[§6Admin§7] §eFor now, edit items/armor.yml and use /miningtycoon reload");
                    }
                    break;
                case 15:
                    if (clickType == ClickType.LEFT) {
                        openPetList(player, 0);
                    } else if (clickType == ClickType.RIGHT) {
                        player.sendMessage("§7[§6Admin§7] §eItem creation GUI coming soon!");
                        player.sendMessage("§7[§6Admin§7] §eFor now, edit items/pets.yml and use /miningtycoon reload");
                    }
                    break;
            }
        }
        // Pickaxe List
        else if (title.startsWith("§8§lPickaxes")) {
            handlePickaxeListClick(player, title, slot, clickType);
        }
        // Armor List
        else if (title.startsWith("§8§lArmor")) {
            handleArmorListClick(player, title, slot, clickType);
        }
        // Pet List
        else if (title.startsWith("§8§lPets")) {
            handlePetListClick(player, title, slot, clickType);
        }
        // Boost Management
        else if (title.equals("§8§lBoost Management")) {
            handleBoostClick(player, slot);
        }
        // Player Management
        else if (title.equals("§8§lPlayer Management")) {
            if (slot == 0) openMainMenu(player);
            else if (slot == 11) openOnlinePlayers(player);
        }
        // Online Players
        else if (title.equals("§8§lOnline Players")) {
            if (slot == 0) openPlayerManagement(player);
        }
        // Server Settings
        else if (title.equals("§8§lServer Settings")) {
            handleServerSettingsClick(player, slot);
        }
    }

    private static void handlePickaxeListClick(Player player, String title, int slot, ClickType clickType) {
        if (slot == 0) {
            openItemManagement(player);
            return;
        }

        if (slot == 8) {
            player.sendMessage("§7[§6Admin§7] §eItem creation coming soon!");
            return;
        }

        // Handle pagination
        if (slot == 45) {
            // Previous page
            int currentPage = extractPage(title);
            if (currentPage > 0) {
                openPickaxeList(player, currentPage - 1);
            }
            return;
        }

        if (slot == 53) {
            // Next page
            int currentPage = extractPage(title);
            openPickaxeList(player, currentPage + 1);
            return;
        }

        // Handle item clicks
        if (slot >= 9 && slot < 54) {
            ItemStack clicked = player.getOpenInventory().getTopInventory().getItem(slot);
            if (clicked == null || !clicked.hasItemMeta()) return;

            List<String> lore = clicked.getItemMeta().getLore();
            if (lore == null) return;

            String id = null;
            for (String line : lore) {
                if (line.startsWith("§7ID: §e")) {
                    id = line.substring(8);
                    break;
                }
            }

            if (id == null) return;

            if (clickType == ClickType.LEFT) {
                player.sendMessage("§7[§6Admin§7] §eEditing " + id);
                player.sendMessage("§7[§6Admin§7] §eEdit items/pickaxes.yml and use /miningtycoon reload");
                player.closeInventory();
            } else if (clickType == ClickType.RIGHT) {
                try {
                    plugin.getItemManager().deletePickaxe(id);
                    player.sendMessage("§7[§6Admin§7] §aDeleted pickaxe: " + id);
                    openPickaxeList(player, extractPage(title));
                } catch (Exception e) {
                    player.sendMessage("§7[§6Admin§7] §cFailed to delete: " + e.getMessage());
                }
            } else if (clickType == ClickType.SHIFT_LEFT) {
                ItemStack item = plugin.getItemManager().getPickaxe(id);
                if (item != null) {
                    player.getInventory().addItem(item);
                    player.sendMessage("§7[§6Admin§7] §aGave you: " + id);
                }
            }
        }
    }

    private static void handleArmorListClick(Player player, String title, int slot, ClickType clickType) {
        if (slot == 0) {
            openItemManagement(player);
            return;
        }

        if (slot == 8) {
            player.sendMessage("§7[§6Admin§7] §eItem creation coming soon!");
            return;
        }

        if (slot == 45) {
            int currentPage = extractPage(title);
            if (currentPage > 0) {
                openArmorList(player, currentPage - 1);
            }
            return;
        }

        if (slot == 53) {
            int currentPage = extractPage(title);
            openArmorList(player, currentPage + 1);
            return;
        }

        if (slot >= 9 && slot < 54) {
            ItemStack clicked = player.getOpenInventory().getTopInventory().getItem(slot);
            if (clicked == null || !clicked.hasItemMeta()) return;

            List<String> lore = clicked.getItemMeta().getLore();
            if (lore == null) return;

            String id = null;
            for (String line : lore) {
                if (line.startsWith("§7ID: §e")) {
                    id = line.substring(8);
                    break;
                }
            }

            if (id == null) return;

            if (clickType == ClickType.LEFT) {
                player.sendMessage("§7[§6Admin§7] §eEditing " + id);
                player.sendMessage("§7[§6Admin§7] §eEdit items/armor.yml and use /miningtycoon reload");
                player.closeInventory();
            } else if (clickType == ClickType.RIGHT) {
                try {
                    plugin.getItemManager().deleteArmor(id);
                    player.sendMessage("§7[§6Admin§7] §aDeleted armor: " + id);
                    openArmorList(player, extractPage(title));
                } catch (Exception e) {
                    player.sendMessage("§7[§6Admin§7] §cFailed to delete: " + e.getMessage());
                }
            } else if (clickType == ClickType.SHIFT_LEFT) {
                ItemStack item = plugin.getItemManager().getArmor(id);
                if (item != null) {
                    player.getInventory().addItem(item);
                    player.sendMessage("§7[§6Admin§7] §aGave you: " + id);
                }
            }
        }
    }

    private static void handlePetListClick(Player player, String title, int slot, ClickType clickType) {
        if (slot == 0) {
            openItemManagement(player);
            return;
        }

        if (slot == 8) {
            player.sendMessage("§7[§6Admin§7] §eItem creation coming soon!");
            return;
        }

        if (slot == 45) {
            int currentPage = extractPage(title);
            if (currentPage > 0) {
                openPetList(player, currentPage - 1);
            }
            return;
        }

        if (slot == 53) {
            int currentPage = extractPage(title);
            openPetList(player, currentPage + 1);
            return;
        }

        if (slot >= 9 && slot < 54) {
            ItemStack clicked = player.getOpenInventory().getTopInventory().getItem(slot);
            if (clicked == null || !clicked.hasItemMeta()) return;

            List<String> lore = clicked.getItemMeta().getLore();
            if (lore == null) return;

            String id = null;
            for (String line : lore) {
                if (line.startsWith("§7ID: §e")) {
                    id = line.substring(8);
                    break;
                }
            }

            if (id == null) return;

            if (clickType == ClickType.LEFT) {
                player.sendMessage("§7[§6Admin§7] §eEditing " + id);
                player.sendMessage("§7[§6Admin§7] §eEdit items/pets.yml and use /miningtycoon reload");
                player.closeInventory();
            } else if (clickType == ClickType.RIGHT) {
                try {
                    plugin.getItemManager().deletePet(id);
                    player.sendMessage("§7[§6Admin§7] §aDeleted pet: " + id);
                    openPetList(player, extractPage(title));
                } catch (Exception e) {
                    player.sendMessage("§7[§6Admin§7] §cFailed to delete: " + e.getMessage());
                }
            } else if (clickType == ClickType.SHIFT_LEFT) {
                ItemStack item = plugin.getItemManager().getPet(id);
                if (item != null) {
                    player.getInventory().addItem(item);
                    player.sendMessage("§7[§6Admin§7] §aGave you: " + id);
                }
            }
        }
    }

    private static void handleBoostClick(Player player, int slot) {
        switch (slot) {
            case 0:
                openMainMenu(player);
                break;
            case 10:
                plugin.getBoostManager().startBoost("exp", player);
                player.closeInventory();
                player.sendMessage("§aGlobal EXP boost started!");
                break;
            case 12:
                plugin.getBoostManager().startBoost("coins", player);
                player.closeInventory();
                player.sendMessage("§aGlobal Coins boost started!");
                break;
            case 14:
                plugin.getBoostManager().startBoost("both", player);
                player.closeInventory();
                player.sendMessage("§aGlobal Combined boost started!");
                break;
            case 16:
            case 22:
                if (plugin.getBoostManager().isBoostActive()) {
                    plugin.getBoostManager().endBoost();
                    player.sendMessage("§aGlobal boost has been stopped!");
                    openBoostManagement(player);
                } else {
                    player.sendMessage("§cNo active boost to stop!");
                }
                break;
        }
    }

    private static void handleServerSettingsClick(Player player, int slot) {
        switch (slot) {
            case 0:
                openMainMenu(player);
                break;
            case 11:
                plugin.reloadConfig();
                plugin.getItemManager().loadAllItems();
                player.sendMessage("§aPlugin reloaded successfully!");
                player.closeInventory();
                break;
            case 13:
                plugin.getDataStorage().saveAllData();
                player.sendMessage("§aAll player data saved!");
                player.closeInventory();
                break;
            case 15:
                // Just display stats, no action needed
                break;
        }
    }

    private static int extractPage(String title) {
        try {
            String[] parts = title.split("Page ");
            if (parts.length > 1) {
                String pageStr = parts[1].split("/")[0];
                return Integer.parseInt(pageStr) - 1;
            }
        } catch (Exception e) {
            // Ignore
        }
        return 0;
    }
}