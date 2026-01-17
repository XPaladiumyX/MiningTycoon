package skyxnetwork.miningTycoon.listeners;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import skyxnetwork.miningTycoon.MiningTycoon;

public class PortalListener implements Listener {

    private final MiningTycoon plugin;

    public PortalListener(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();

        // Cancel the portal event
        event.setCancelled(true);

        // Teleport to spawn location
        Location spawn = new Location(
                Bukkit.getWorld("mining_tycoon"),
                -36.5, 124, 16.5
        );

        player.teleport(spawn);

        // If player is OP and in creative, restore gamemode after delay
        if (player.isOp() && player.getGameMode() == GameMode.CREATIVE) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.setGameMode(GameMode.CREATIVE);
            }, 16L);
        }
    }
}
