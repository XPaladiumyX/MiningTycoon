package skyxnetwork.miningTycoon.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.data.PlayerData;
import skyxnetwork.miningTycoon.commands.*;
import skyxnetwork.miningTycoon.utils.ItemBuilder;

public class PlayerJoinListener implements Listener {

    private final MiningTycoon plugin;
    private static final int MENU_SLOT = 8;
    private static final int MENU_CUSTOM_MODEL_DATA = 1234;

    public PlayerJoinListener(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Load player data
        PlayerData data = plugin.getDataStorage().loadPlayerData(player.getUniqueId());
        plugin.getPlayerDataManager().getAllPlayerData().put(player.getUniqueId(), data);

        // Add to boost boss bar if active
        plugin.getBoostManager().addPlayerToBossBar(player);

        // Check if player has menu item, if not give it
        giveMenuItemIfMissing(player);

        // Send welcome message after 3 seconds
        org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.sendMessage("§dSky X §9Network §8●⏺  §e90%% §fof the system was coded with love by §biiXPaladiumyXii §f. Thank you for supporting him by subscribing or donating to SkyXNetwork!");
        }, 60L);
    }

    private void giveMenuItemIfMissing(Player player) {
        // Check if player already has the menu item anywhere in inventory
        if (hasMenuItemInInventory(player)) {
            return; // Player already has it
        }

        // Player doesn't have it, give them one
        ItemStack menuItem = createMenuItem();
        
        // Check slot 8 first
        ItemStack slot8 = player.getInventory().getItem(MENU_SLOT);
        if (slot8 == null || slot8.getType() == Material.AIR) {
            // Slot 8 is empty, put it there
            player.getInventory().setItem(MENU_SLOT, menuItem);
        } else {
            // Slot 8 is occupied, try to find empty slot
            player.getInventory().addItem(menuItem);
        }
    }

    private boolean hasMenuItemInInventory(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isMenuItem(item)) {
                return true;
            }
        }
        return false;
    }

    public static ItemStack createMenuItem() {
        return new ItemBuilder(Material.NETHER_STAR)
                .setName("§b§lMenu")
                .setLore("§7Click to open the menu")
                .setCustomModelData(MENU_CUSTOM_MODEL_DATA)
                .build();
    }

    public static boolean isMenuItem(ItemStack item) {
        if (item == null || item.getType() != Material.NETHER_STAR) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasCustomModelData()) {
            return false;
        }
        return meta.getCustomModelData() == MENU_CUSTOM_MODEL_DATA;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

        // Save player data
        plugin.getDataStorage().savePlayerData(player.getUniqueId(), data);

        // Remove from memory
        plugin.getPermissionCommand().removeAttachment(player);
        plugin.getPlayerDataManager().removePlayerData(player.getUniqueId());
    }
}
