package skyxnetwork.miningTycoon.listeners;

import org.bukkit.event.Listener;
import skyxnetwork.miningTycoon.MiningTycoon;

public class AFKListener implements Listener {
    private final MiningTycoon plugin;

    public AFKListener(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    // AFKHandling is done in AFKRewardTask
}
