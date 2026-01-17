package skyxnetwork.miningTycoon.tasks;

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
            if (data.getExperience() > data.getExperienceNeeded()) {
                org.bukkit.entity.Player player = org.bukkit.Bukkit.getPlayer(data.getUuid());
                if (player != null && player.isOnline()) {
                    plugin.getPlayerDataManager().checkLevelUp(player);
                }
            }
        }
    }
}
