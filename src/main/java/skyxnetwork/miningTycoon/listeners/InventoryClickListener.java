package skyxnetwork.miningTycoon.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
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
    private static final long COOLDOWN_MS = 500;

    public InventoryClickListener(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        InventoryType clickedInventoryType = event.getClickedInventory() != null ?
                event.getClickedInventory().getType() : null;

        // Check if clicking on the menu item
        if (clicked != null && PlayerJoinListener.isMenuItem(clicked)) {
            if (clickedInventoryType != null && clickedInventoryType != InventoryType.PLAYER) {
                event.setCancelled(true);
                player.sendMessage("§7[§e!§7] §cYou can't put the menu in a container!");
                return;
            }
            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                event.setCancelled(true);
                player.sendMessage("§7[§e!§7] §cYou can't move the menu to a container!");
                return;
            }
        }

        // Check if cursor has the menu item (dragging)
        if (cursor != null && PlayerJoinListener.isMenuItem(cursor)) {
            if (clickedInventoryType != null && clickedInventoryType != InventoryType.PLAYER) {
                event.setCancelled(true);
                player.sendMessage("§7[§e!§7] §cYou can't put the menu in a container!");
                return;
            }
            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                event.setCancelled(true);
                player.sendMessage("§7[§e!§7] §cYou can't move the menu to a container!");
                return;
            }
        }

        // Handle pet equipping in inventory (slot 39 = helmet slot)
        if (clicked != null && clicked.getType() == Material.PLAYER_HEAD) {
            if (event.getSlot() == 39) {
                String petId = plugin.getItemManager().getPetId(clicked);
                if (petId != null) {
                    String name = clicked.getItemMeta().getDisplayName();
                    player.sendMessage("§8[§dPet Equipped§8] " + name);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // On intercepte les deux mains mais on traite une seule fois
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        Action action = event.getAction();

        // ✅ Fix : on gère clic droit ET clic gauche, dans l'air ET sur un bloc
        boolean isRightClick = (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK);
        boolean isLeftClick = (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK);

        if (!isRightClick && !isLeftClick) return;

        ItemStack item = player.getInventory().getItemInMainHand();

        // ✅ Fix menu item : clic droit OU gauche sur la star ouvre le menu
        if (item != null && PlayerJoinListener.isMenuItem(item)) {
            event.setCancelled(true);
            player.performCommand("menu");
            return;
        }

        // Vérification offhand aussi
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (offhand != null && PlayerJoinListener.isMenuItem(offhand)) {
            event.setCancelled(true);
            player.performCommand("menu");
            return;
        }

        // À partir d'ici, on ne traite l'équipement du pet que sur clic droit
        if (!isRightClick) return;

        // Check if item is a pet (PLAYER_HEAD)
        if (item == null || item.getType() != Material.PLAYER_HEAD) return;

        // Verify it's actually a pet from our plugin
        String petId = plugin.getItemManager().getPetId(item);
        if (petId == null) return;

        // Check cooldown
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        if (petEquipCooldown.containsKey(uuid)) {
            long timeSince = now - petEquipCooldown.get(uuid);
            if (timeSince < COOLDOWN_MS) return;
        }

        event.setCancelled(true);
        petEquipCooldown.put(uuid, now);

        // Get current helmet
        ItemStack currentHelmet = player.getInventory().getHelmet();

        // Create a single pet item
        ItemStack singlePet = item.clone();
        singlePet.setAmount(1);

        // Equip the pet on the head
        player.getInventory().setHelmet(singlePet);

        // Handle the item in hand
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
            player.getInventory().setItemInMainHand(item);
        } else {
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        }

        // Put old helmet back in inventory if there was one
        if (currentHelmet != null && currentHelmet.getType() != Material.AIR) {
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(currentHelmet);
            if (!leftover.isEmpty()) {
                for (ItemStack drop : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), drop);
                }
            }
        }

        String petName = singlePet.getItemMeta().getDisplayName();
        player.sendMessage("§8[§dPet Equipped§8] " + petName);
        player.playSound(player.getLocation(), "entity.player.levelup", 0.5f, 2.0f);
    }
}