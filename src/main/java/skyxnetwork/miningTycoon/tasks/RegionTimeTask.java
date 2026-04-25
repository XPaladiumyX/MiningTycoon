package skyxnetwork.miningTycoon.tasks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;
import skyxnetwork.miningTycoon.MiningTycoon;

public class RegionTimeTask extends BukkitRunnable {

    private final MiningTycoon plugin;

    public RegionTimeTask(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().getName().equals("mining_tycoon")) {
                PotionEffect effect = new PotionEffect(PotionEffectType.NIGHT_VISION, 400, 0, false, false);

                if (player.getLocation().getX() <= 40){
                    player.addPotionEffect(effect);
                    player.setPlayerTime(1000, false);
                } else{
                    player.setPlayerTime(20000, false);
                }
            }
        }
    }
}

