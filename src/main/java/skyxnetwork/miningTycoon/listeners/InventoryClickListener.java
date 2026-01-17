package skyxnetwork.miningTycoon.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
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

        // Handle pet equipping
        if (clicked != null && clicked.getType() == Material.PLAYER_HEAD) {
            if (event.getSlot() == 39) { // Helmet slot
                String name = clicked.getItemMeta().getDisplayName();
                // Send equip message based on pet
                player.sendMessage("§8[§dPet Equipped§8] " + name);
            }
        }
    }
}

