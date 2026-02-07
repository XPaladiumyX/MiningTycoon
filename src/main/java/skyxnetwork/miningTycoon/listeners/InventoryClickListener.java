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

public class InventoryClickListener implements Listener {

    private final MiningTycoon plugin;

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

        // Cancel the event to prevent other interactions
        event.setCancelled(true);

        // Get current helmet
        ItemStack currentHelmet = player.getInventory().getHelmet();

        // Equip the pet on the head
        player.getInventory().setHelmet(item.clone());

        // Put old helmet in hand if there was one
        if (currentHelmet != null && currentHelmet.getType() != Material.AIR) {
            player.getInventory().setItemInMainHand(currentHelmet);
        } else {
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        }

        // Send confirmation message
        String petName = item.getItemMeta().getDisplayName();
        player.sendMessage("§8[§dPet Equipped§8] " + petName);
        player.playSound(player.getLocation(), "entity.player.levelup", 0.5f, 2.0f);
    }
}