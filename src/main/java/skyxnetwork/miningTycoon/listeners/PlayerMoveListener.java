package skyxnetwork.miningTycoon.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.data.PlayerData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerMoveListener implements Listener {

    private final MiningTycoon plugin;
    private final Map<UUID, Long> lastPushTime = new HashMap<>();
    private final Map<UUID, Integer> pushCount = new HashMap<>();
    private static final long PUSH_COOLDOWN = 100; // milliseconds

    public PlayerMoveListener(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

        // Staff mode bypass
        if (data.getPlayerMode().equals("staff")) {
            return;
        }

        // Check zone restrictions (using WorldGuard regions if available)
        // For now, using coordinate-based system
        checkZoneRestriction(player, event);
    }

    private void checkZoneRestriction(Player player, PlayerMoveEvent event) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        int playerLevel = data.getLevel();
        Location loc = event.getTo();

        // Save last safe location
        if (data.getLastSafeLocation() == null) {
            data.setLastSafeLocation(loc);
        }

        // Check zone requirements (simplified version)
        // In production, integrate with WorldGuard
        boolean blocked = false;
        int requiredLevel = 0;

        // Example zone check (customize based on your server layout)
        // This is placeholder logic - replace with actual WorldGuard integration

        if (blocked) {
            event.setCancelled(true);

            long now = System.currentTimeMillis();
            UUID uuid = player.getUniqueId();

            if (!lastPushTime.containsKey(uuid) || now - lastPushTime.get(uuid) > PUSH_COOLDOWN) {
                lastPushTime.put(uuid, now);

                // Push player back
                Location safe = data.getLastSafeLocation();
                Vector direction = safe.toVector().subtract(loc.toVector()).normalize();
                player.setVelocity(direction.multiply(1.0).setY(0.1));

                // Increment push count
                int count = pushCount.getOrDefault(uuid, 0) + 1;
                pushCount.put(uuid, count);

                // Teleport to hub after 3 pushes
                if (count >= 3) {
                    player.sendMessage("§cYou were teleported to the hub after too many attempts in a restricted area!");
                    player.performCommand("hub");
                    pushCount.remove(uuid);
                }
            }
        } else {
            data.setLastSafeLocation(loc);
            pushCount.remove(player.getUniqueId());
        }
    }
}
