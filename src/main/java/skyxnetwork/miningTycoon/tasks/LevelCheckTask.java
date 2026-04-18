package skyxnetwork.miningTycoon.tasks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.data.PlayerData;
import skyxnetwork.miningTycoon.utils.ConfigUtil;

public class LevelCheckTask extends BukkitRunnable {

    private final MiningTycoon plugin;

    public LevelCheckTask(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        int maxLevel = ConfigUtil.getMaxLevel(plugin);
        
        for (PlayerData data : plugin.getPlayerDataManager().getAllPlayerData().values()) {
            if (data.getLevel() >= maxLevel) continue;

            if (data.getExperience() >= data.getExperienceNeeded()) {
                Player player = Bukkit.getPlayer(data.getUuid());
                if (player != null && player.isOnline()) {
                    plugin.getPlayerDataManager().checkLevelUp(player);
                }
            }

            Player player = Bukkit.getPlayer(data.getUuid());
            if (player != null && player.isOnline()) {
                updateExperienceBar(player, data);
            }
        }
    }

    private void updateExperienceBar(Player player, PlayerData data) {
        player.setLevel(data.getLevel());

        double progress = data.getExperience() / data.getExperienceNeeded();
        progress = Math.max(0.0, Math.min(1.0, progress));
        player.setExp((float) progress);
    }
}
