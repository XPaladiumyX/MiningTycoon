package skyxnetwork.miningTycoon.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.commands.GiveMenuCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InventoryClickListener implements Listener {

    private final MiningTycoon plugin;
    private final Map<UUID, Long> petEquipCooldown = new HashMap<>();
    private final Map<UUID, Long> menuCooldown = new HashMap<>();
    private static final long COOLDOWN_MS = 500;

    public InventoryClickListener(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.isCancelled()) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();

        InventoryType topInvType = event.getView().getTopInventory().getType();
        boolean isInteractingWithContainer = isContainerInventory(topInvType);

        if (clickedItem != null && GiveMenuCommand.isMenuItem(clickedItem)) {
            if (isInteractingWithContainer) {
                event.setCancelled(true);
                player.sendMessage("§c§oYou cannot put the menu item in a container!");
            }
        }

        if (cursorItem != null && GiveMenuCommand.isMenuItem(cursorItem)) {
            if (isInteractingWithContainer) {
                event.setCancelled(true);
                player.sendMessage("§c§oYou cannot put the menu item in a container!");
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

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

    private boolean isContainerInventory(InventoryType type) {
        return type == InventoryType.CHEST
                || type == InventoryType.ENDER_CHEST
                || type == InventoryType.BARREL
                || type == InventoryType.FURNACE
                || type == InventoryType.DISPENSER
                || type == InventoryType.DROPPER
                || type == InventoryType.HOPPER
                || type == InventoryType.SHULKER_BOX;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType() == Material.AIR) return;

        if (GiveMenuCommand.isMenuItem(item)) {
            Action action = event.getAction();

            boolean isValidAction = action == Action.RIGHT_CLICK_AIR
                    || action == Action.RIGHT_CLICK_BLOCK
                    || action == Action.LEFT_CLICK_AIR
                    || action == Action.LEFT_CLICK_BLOCK;

            if (!isValidAction) return;

            UUID uuid = player.getUniqueId();
            long now = System.currentTimeMillis();
            Long lastUse = menuCooldown.get(uuid);
            if (lastUse != null && (now - lastUse) < COOLDOWN_MS) {
                return;
            }
            menuCooldown.put(uuid, now);

            event.setCancelled(true);
            player.chat("/menu");
            return;
        }

        Action action = event.getAction();
        boolean isValidAction = action == Action.RIGHT_CLICK_AIR
                || action == Action.RIGHT_CLICK_BLOCK
                || action == Action.LEFT_CLICK_AIR
                || action == Action.LEFT_CLICK_BLOCK;

        if (!isValidAction) return;
        if (item.getType() != Material.PLAYER_HEAD) return;

        String petId = plugin.getItemManager().getPetId(item);
        if (petId == null) return;

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        if (petEquipCooldown.containsKey(uuid)) {
            long timeSince = now - petEquipCooldown.get(uuid);
            if (timeSince < COOLDOWN_MS) {
                return;
            }
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