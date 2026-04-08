package skyxnetwork.miningTycoon.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        Action action = event.getAction();

        boolean isRightClick = (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK);
        boolean isLeftClick = (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK);

        if (!isRightClick && !isLeftClick) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        ItemStack offhand = player.getInventory().getItemInOffHand();

        // Check if it's the menu item - use main hand
        if (item != null && PlayerJoinListener.isMenuItem(item)) {
            event.setCancelled(true);
            // Use dispatchCommand instead of performCommand
            Bukkit.dispatchCommand(player, "menu");
            return;
        }

        // Also check offhand
        if (offhand != null && PlayerJoinListener.isMenuItem(offhand)) {
            event.setCancelled(true);
            Bukkit.dispatchCommand(player, "menu");
            return;
        }

        // From here: pet equip on right click only
        if (!isRightClick) return;

        if (item == null || item.getType() != Material.PLAYER_HEAD) return;

        String petId = plugin.getItemManager().getPetId(item);
        if (petId == null) return;

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        if (petEquipCooldown.containsKey(uuid)) {
            if (now - petEquipCooldown.get(uuid) < COOLDOWN_MS) return;
        }

        event.setCancelled(true);
        petEquipCooldown.put(uuid, now);

        ItemStack currentHelmet = player.getInventory().getHelmet();

        ItemStack singlePet = item.clone();
        singlePet.setAmount(1);

        player.getInventory().setHelmet(singlePet);

        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
            player.getInventory().setItemInMainHand(item);
        } else {
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
        }

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