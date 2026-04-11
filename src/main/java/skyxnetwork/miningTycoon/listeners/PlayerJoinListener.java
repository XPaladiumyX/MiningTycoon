package skyxnetwork.miningTycoon.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.commands.GiveMenuCommand;
import skyxnetwork.miningTycoon.data.PlayerData;

import java.util.HashMap;

public class PlayerJoinListener implements Listener {

    private final MiningTycoon plugin;
    private static final int MENU_SLOT = 8;
    private static final int PICKAXE_SLOT = 0;
    private static final int CHECK_INTERVAL_TICKS = 20;
    private static final String STARTER_PICKAXE_ID = "wooden_pickaxe";

    public PlayerJoinListener(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        PlayerData data = plugin.getDataStorage().loadPlayerData(player.getUniqueId());
        plugin.getPlayerDataManager().getAllPlayerData().put(player.getUniqueId(), data);

        plugin.getBoostManager().addPlayerToBossBar(player);

        boolean menuGiven = giveMenuItemIfMissing(player);
        if (!menuGiven) {
            startMenuItemCheckTask(player);
        }

        boolean pickaxeGiven = giveStarterPickaxeIfMissing(player);
        if (!pickaxeGiven) {
            startStarterPickaxeCheckTask(player);
        }

        org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.sendMessage("§dSky X §9Network §8●⏺  §e90%% §fof the system was coded with love by §biiXPaladiumyXii §f. Thank you for supporting him by subscribing or donating to SkyXNetwork!");
        }, 60L);
    }

    private boolean giveStarterPickaxeIfMissing(Player player) {
        if (hasPickaxeInInventory(player)) {
            return true;
        }

        ItemStack starterPickaxe = plugin.getItemManager().getPickaxe(STARTER_PICKAXE_ID);
        if (starterPickaxe == null) {
            plugin.getLogger().warning("Starter pickaxe not found: " + STARTER_PICKAXE_ID);
            return false;
        }

        ItemStack slot0 = player.getInventory().getItem(PICKAXE_SLOT);
        if (slot0 == null || slot0.getType() == Material.AIR) {
            player.getInventory().setItem(PICKAXE_SLOT, starterPickaxe);
            return true;
        }

        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(starterPickaxe);
        return leftover.isEmpty();
    }

    private boolean hasPickaxeInInventory(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR && isMiningTool(item.getType())) {
                return true;
            }
        }
        return false;
    }

    private boolean isMiningTool(Material material) {
        return material == Material.WOODEN_PICKAXE
                || material == Material.STONE_PICKAXE
                || material == Material.IRON_PICKAXE
                || material == Material.GOLDEN_PICKAXE
                || material == Material.DIAMOND_PICKAXE
                || material == Material.NETHERITE_PICKAXE;
    }

    private void startStarterPickaxeCheckTask(Player player) {
        org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;

            if (hasPickaxeInInventory(player)) return;

            boolean given = giveStarterPickaxeIfMissing(player);
            if (!given) {
                startStarterPickaxeCheckTask(player);
            }
        }, CHECK_INTERVAL_TICKS);
    }

    private boolean giveMenuItemIfMissing(Player player) {
        if (hasMenuItemInInventory(player)) {
            return true;
        }

        ItemStack menuItem = GiveMenuCommand.createMenuItem();

        ItemStack slot8 = player.getInventory().getItem(MENU_SLOT);
        if (slot8 == null || slot8.getType() == Material.AIR) {
            player.getInventory().setItem(MENU_SLOT, menuItem);
            return true;
        }

        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(menuItem);
        return leftover.isEmpty();
    }

    private boolean hasMenuItemInInventory(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (GiveMenuCommand.isMenuItem(item)) return true;
        }
        return false;
    }

    private void startMenuItemCheckTask(Player player) {
        org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            
            if (hasMenuItemInInventory(player)) return;
            
            boolean given = giveMenuItemIfMissing(player);
            if (!given) {
                startMenuItemCheckTask(player);
            }
        }, CHECK_INTERVAL_TICKS);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

        plugin.getDataStorage().savePlayerData(player.getUniqueId(), data);
        plugin.getPermissionCommand().removeAttachment(player);
        plugin.getPlayerDataManager().removePlayerData(player.getUniqueId());
    }
}