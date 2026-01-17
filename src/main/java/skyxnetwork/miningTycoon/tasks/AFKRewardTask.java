package skyxnetwork.miningTycoon.tasks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.data.PlayerData;

public class AFKRewardTask extends BukkitRunnable {

    private final MiningTycoon plugin;

    public AFKRewardTask(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getWorld().getName().equals("mining_tycoon")) continue;

            Location loc = player.getLocation();
            int x = loc.getBlockX();
            int y = loc.getBlockY();
            int z = loc.getBlockZ();

            // Check if in AFK zone (coordinates 8-11, y=108, z=18-21)
            if (x >= 8 && x <= 11 && z >= 18 && z <= 21 && y == 108) {
                PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

                if (!data.isInAFKZone()) {
                    data.setInAFKZone(true);
                    // Teleport up
                    player.teleport(new Location(player.getWorld(), x, 125, z));
                }

                // Give rewards every tick
                double exp = 1;
                double money = 1;

                // Add bonuses (simplified)
                data.addExperience(exp);
                data.addAfkTime(1);

                // Send subtitle
                player.sendTitle("", "§f[§6+" + (int) money + "⛁§f] §f[§3+" + (int) exp + "✦§f]", 0, 20, 0);
            } else {
                PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
                data.setInAFKZone(false);
            }
        }
    }
}
