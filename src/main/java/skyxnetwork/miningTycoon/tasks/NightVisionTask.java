package skyxnetwork.miningTycoon.tasks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import skyxnetwork.miningTycoon.MiningTycoon;

public class NightVisionTask extends BukkitRunnable {

    private final MiningTycoon plugin;

    public NightVisionTask(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().getName().equals("mining_tycoon")) {
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.NIGHT_VISION, 2400, 0, false, false));
            }
        }
    }
}

