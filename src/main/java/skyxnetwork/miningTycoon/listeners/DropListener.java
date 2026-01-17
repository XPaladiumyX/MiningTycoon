package skyxnetwork.miningTycoon.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import skyxnetwork.miningTycoon.MiningTycoon;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DropListener implements Listener {

    private final MiningTycoon plugin;
    private final Map<UUID, Long> dropConfirm = new HashMap<>();
    private final Map<UUID, Boolean> dropConfirmed = new HashMap<>();

    public DropListener(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemDrop().getItemStack();

        // Prevent dropping menu item
        if (item.getType() == Material.NETHER_STAR &&
                item.hasItemMeta() &&
                item.getItemMeta().hasCustomModelData() &&
                item.getItemMeta().getCustomModelData() == 1234) {
            event.setCancelled(true);
            player.sendMessage("§7[§e!§7] §cYou can't drop this item: §r" + item.getItemMeta().getDisplayName());
            return;
        }

        UUID uuid = player.getUniqueId();

        // Check if player is confirmed to drop
        if (dropConfirmed.getOrDefault(uuid, false)) {
            return;
        }

        // Check if this is second attempt
        if (dropConfirm.containsKey(uuid)) {
            dropConfirm.remove(uuid);
            dropConfirmed.put(uuid, true);
            player.sendMessage("§7[§e!§7] §aDrop confirmed! You can drop items freely for §e10 seconds.");

            // Reset after 10 seconds
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                dropConfirmed.remove(uuid);
                player.sendMessage("§7[§e!§7] §eSecure drop mode is now re-enabled.");
            }, 200L);
            return;
        }

        // First attempt - cancel and warn
        event.setCancelled(true);
        player.sendMessage("§7[§e!§7] §cPress your drop key again to confirm dropping the item.");
        dropConfirm.put(uuid, System.currentTimeMillis());

        // Reset after 3 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            dropConfirm.remove(uuid);
        }, 60L);
    }
}
