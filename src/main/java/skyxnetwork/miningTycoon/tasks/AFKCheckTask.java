package skyxnetwork.miningTycoon.tasks;

import org.bukkit.scheduler.BukkitRunnable;
import skyxnetwork.miningTycoon.MiningTycoon;

public class AFKCheckTask extends BukkitRunnable {

    private final MiningTycoon plugin;

    public AFKCheckTask(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.getAfkManager().checkAfkStatus();
    }
}