package skyxnetwork.miningTycoon.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.managers.PrestigePortalManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Listens for players entering prestige portals
 */
public class PrestigePortalListener implements Listener {

    private final MiningTycoon plugin;
    private final Map<UUID, String> playerInPortal;
    private final Map<UUID, Long> lastNotification;
    private static final long NOTIFICATION_COOLDOWN = 5000; // 5 seconds

    public PrestigePortalListener(MiningTycoon plugin) {
        this.plugin = plugin;
        this.playerInPortal = new HashMap<>();
        this.lastNotification = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only check if player actually moved blocks
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Check if player is in a prestige portal
        PrestigePortalManager.PrestigePortal portal =
                plugin.getPrestigePortalManager().getPortalAtLocation(event.getTo());

        if (portal != null) {
            // Player entered or is in a portal
            String currentPortalId = playerInPortal.get(uuid);

            if (currentPortalId == null || !currentPortalId.equals(portal.getId())) {
                // Player just entered this portal
                playerInPortal.put(uuid, portal.getId());
                onPortalEnter(player, portal);
            } else {
                // Player is still in the same portal - send periodic reminders
                long now = System.currentTimeMillis();
                Long lastNotif = lastNotification.get(uuid);

                if (lastNotif == null || (now - lastNotif) > NOTIFICATION_COOLDOWN) {
                    lastNotification.put(uuid, now);
                    sendPortalReminder(player, portal);
                }
            }
        } else {
            // Player is not in any portal
            if (playerInPortal.containsKey(uuid)) {
                // Player just left a portal
                playerInPortal.remove(uuid);
                lastNotification.remove(uuid);
                onPortalExit(player);
            }
        }
    }

    /**
     * Called when player enters a prestige portal
     */
    private void onPortalEnter(Player player, PrestigePortalManager.PrestigePortal portal) {
        plugin.getPrestigePortalManager().handlePortalEntry(player, portal);
    }

    /**
     * Send reminder to player while in portal
     */
    private void sendPortalReminder(Player player, PrestigePortalManager.PrestigePortal portal) {
        if (plugin.getPrestigePortalManager().canPrestige(player, portal)) {
            player.sendActionBar(
                    net.kyori.adventure.text.Component.text(
                            "§d⚡ §eType §a/prestige confirm §eto prestige §d⚡"
                    )
            );
        }
    }

    /**
     * Called when player exits a prestige portal
     */
    private void onPortalExit(Player player) {
        player.sendActionBar(
                net.kyori.adventure.text.Component.text("§7Prestige cancelled")
        );
    }

    /**
     * Get players currently in portals (for debugging)
     */
    public Map<UUID, String> getPlayersInPortals() {
        return new HashMap<>(playerInPortal);
    }

    /**
     * Clear portal tracking for a player
     */
    public void clearPlayer(UUID uuid) {
        playerInPortal.remove(uuid);
        lastNotification.remove(uuid);
    }
}