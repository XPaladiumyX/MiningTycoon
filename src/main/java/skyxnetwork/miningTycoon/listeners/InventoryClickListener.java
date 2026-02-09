package skyxnetwork.miningTycoon.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import skyxnetwork.miningTycoon.MiningTycoon;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InventoryClickListener implements Listener {

    private final MiningTycoon plugin;
    private final Map<UUID, Long> petEquipCooldown = new HashMap<>();
    private static final long COOLDOWN_MS = 500; // 500ms cooldown

    public InventoryClickListener(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        // Handle pet equipping in inventory
        if (clicked != null && clicked.getType() == Material.PLAYER_HEAD) {
            if (event.getSlot() == 39) { // Helmet slot
                String petId = plugin.getItemManager().getPetId(clicked);
                if (petId != null) {
                    String name = clicked.getItemMeta().getDisplayName();
                    player.sendMessage("§8[§dPet Equipped§8] " + name);
                }
            }
        }
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        // Only handle main hand interactions
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if it's a right click (in air or on block)
        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
                event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // Check if item is a pet (PLAYER_HEAD)
        if (item == null || item.getType() != Material.PLAYER_HEAD) {
            return;
        }

        // Verify it's actually a pet from our plugin
        String petId = plugin.getItemManager().getPetId(item);
        if (petId == null) {
            return;
        }

        // Check cooldown
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        if (petEquipCooldown.containsKey(uuid)) {
            long timeSince = now - petEquipCooldown.get(uuid);
            if (timeSince < COOLDOWN_MS) {
                return; // Still on cooldown
            }
        }

        // Cancel the event to prevent other interactions
        event.setCancelled(true);

        // Update cooldown
        petEquipCooldown.put(uuid, now);

        // Get current helmet
        ItemStack currentHelmet = player.getInventory().getHelmet();

        // Create a single pet item (not the whole stack)
        ItemStack singlePet = item.clone();
        singlePet.setAmount(1);

        // Equip the pet on the head
        player.getInventory().setHelmet(singlePet);

        // Handle the item in hand
        if (item.getAmount() > 1) {
            // If there's more than 1, decrease the stack by 1
            item.setAmount(item.getAmount() - 1);
            player.getInventory().setItemInMainHand(item);
        } else {
            // If it's the last one, clear the hand
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        }

        // Put old helmet back in inventory if there was one
        if (currentHelmet != null && currentHelmet.getType() != Material.AIR) {
            // Try to add to inventory, drop if full
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(currentHelmet);
            if (!leftover.isEmpty()) {
                for (ItemStack drop : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), drop);
                }
            }
        }

        // Send confirmation message
        String petName = singlePet.getItemMeta().getDisplayName();
        player.sendMessage("§8[§dPet Equipped§8] " + petName);
        player.playSound(player.getLocation(), "entity.player.levelup", 0.5f, 2.0f);
    }
}