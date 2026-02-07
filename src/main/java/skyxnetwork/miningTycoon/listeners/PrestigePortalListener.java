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
    private final Map<UUID, Long> lastGUIOpen;
    private static final long GUI_COOLDOWN = 10000; // 10 seconds cooldown before reopening GUI

    public PrestigePortalListener(MiningTycoon plugin) {
        this.plugin = plugin;
        this.playerInPortal = new HashMap<>();
        this.lastGUIOpen = new HashMap<>();
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
            // Player is in a portal
            String currentPortalId = playerInPortal.get(uuid);

            if (currentPortalId == null || !currentPortalId.equals(portal.getId())) {
                // Player just entered this portal
                playerInPortal.put(uuid, portal.getId());
                onPortalEnter(player, portal);
            } else {
                // Player is still in the same portal
                // Check if enough time has passed to reopen GUI
                long now = System.currentTimeMillis();
                Long lastOpen = lastGUIOpen.get(uuid);

                if (lastOpen == null || (now - lastOpen) > GUI_COOLDOWN) {
                    // Only show reminder if player doesn't have GUI open already
                    if (!plugin.getPrestigePortalGUI().hasPending(uuid)) {
                        sendPortalReminder(player, portal);
                    }
                }
            }
        } else {
            // Player is not in any portal
            if (playerInPortal.containsKey(uuid)) {
                // Player just left a portal
                playerInPortal.remove(uuid);
                onPortalExit(player);
            }
        }
    }

    /**
     * Called when player enters a prestige portal
     */
    private void onPortalEnter(Player player, PrestigePortalManager.PrestigePortal portal) {
        UUID uuid = player.getUniqueId();

        // Check GUI cooldown
        long now = System.currentTimeMillis();
        Long lastOpen = lastGUIOpen.get(uuid);

        if (lastOpen != null && (now - lastOpen) < GUI_COOLDOWN) {
            // Too soon to reopen GUI, just send action bar
            long secondsRemaining = (GUI_COOLDOWN - (now - lastOpen)) / 1000;
            player.sendActionBar(
                    net.kyori.adventure.text.Component.text(
                            "§7Wait §e" + secondsRemaining + "s §7before reopening portal GUI"
                    )
            );
            return;
        }

        // Open GUI and update cooldown
        lastGUIOpen.put(uuid, now);
        plugin.getPrestigePortalManager().handlePortalEntry(player, portal);
    }

    /**
     * Send reminder to player while in portal (only if GUI not open)
     */
    private void sendPortalReminder(Player player, PrestigePortalManager.PrestigePortal portal) {
        if (plugin.getPrestigePortalManager().canPrestige(player, portal)) {
            player.sendActionBar(
                    net.kyori.adventure.text.Component.text(
                            "§d⚡ §eMove to reopen prestige GUI §d⚡"
                    )
            );
        }
    }

    /**
     * Called when player exits a prestige portal
     */
    private void onPortalExit(Player player) {
        UUID uuid = player.getUniqueId();

        // Clear GUI if player has pending prestige
        if (plugin.getPrestigePortalGUI().hasPending(uuid)) {
            player.closeInventory();
            plugin.getPrestigePortalGUI().clearPending(uuid);
            player.sendMessage("§7Prestige cancelled - you left the portal");
        }

        player.sendActionBar(
                net.kyori.adventure.text.Component.text("§7Left prestige portal")
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
        lastGUIOpen.remove(uuid);
    }
}