package skyxnetwork.miningTycoon.tasks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.data.PlayerData;

public class LevelCheckTask extends BukkitRunnable {

    private final MiningTycoon plugin;

    public LevelCheckTask(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (PlayerData data : plugin.getPlayerDataManager().getAllPlayerData().values()) {
            // Check for level up
            if (data.getExperience() > data.getExperienceNeeded()) {
                Player player = Bukkit.getPlayer(data.getUuid());
                if (player != null && player.isOnline()) {
                    plugin.getPlayerDataManager().checkLevelUp(player);
                }
            }

            // Update player's experience bar
            Player player = Bukkit.getPlayer(data.getUuid());
            if (player != null && player.isOnline()) {
                updateExperienceBar(player, data);
            }
        }
    }

    private void updateExperienceBar(Player player, PlayerData data) {
        // Set the level (our level 1 = player level 1 in Minecraft)
        player.setLevel(data.getLevel());

        // Calculate progress: current XP / XP needed = percentage (0.0 to 1.0)
        double progress = data.getExperience() / data.getExperienceNeeded();
        // Clamp to valid range
        progress = Math.max(0.0, Math.min(1.0, progress));
        player.setExp((float) progress);
    }
}
