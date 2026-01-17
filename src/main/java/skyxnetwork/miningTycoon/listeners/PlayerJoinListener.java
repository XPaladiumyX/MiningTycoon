package skyxnetwork.miningTycoon.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.data.PlayerData;

public class PlayerJoinListener implements Listener {

    private final MiningTycoon plugin;

    public PlayerJoinListener(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Load player data
        PlayerData data = plugin.getDataStorage().loadPlayerData(player.getUniqueId());
        plugin.getPlayerDataManager().getAllPlayerData().put(player.getUniqueId(), data);

        // Add to boost boss bar if active
        plugin.getBoostManager().addPlayerToBossBar(player);

        // Send welcome message after 3 seconds
        org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.sendMessage("§dSky X §9Network §8●⏺  §e90%% §fof the system was coded with love by §biiXPaladiumyXii §f. Thank you for supporting him by subscribing or donating to SkyXNetwork!");
        }, 60L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

        // Save player data
        plugin.getDataStorage().savePlayerData(player.getUniqueId(), data);

        // Remove from memory
        plugin.getPlayerDataManager().removePlayerData(player.getUniqueId());
    }
}
