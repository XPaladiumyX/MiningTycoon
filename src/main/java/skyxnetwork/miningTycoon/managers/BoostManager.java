package skyxnetwork.miningTycoon.managers;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import skyxnetwork.miningTycoon.MiningTycoon;

public class BoostManager {

    private final MiningTycoon plugin;
    private boolean boostActive;
    private String boostType; // "exp", "coins", or "both"
    private double expMultiplier;
    private double coinsMultiplier;
    private int boostDuration;
    private int timeRemaining;
    private BossBar bossBar;

    public BoostManager(MiningTycoon plugin) {
        this.plugin = plugin;
        this.boostActive = false;
        this.expMultiplier = 1.0;
        this.coinsMultiplier = 1.0;
    }

    public void startBoost(String type, Player triggeredBy) {
        if (boostActive) {
            triggeredBy.sendMessage("§c⚠ A Global Boost is already active! You received a compensation item instead.");
            triggeredBy.getInventory().addItem(new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIRT));
            return;
        }

        boostActive = true;
        boostType = type;

        switch (type) {
            case "exp":
                int superRoll = (int) (Math.random() * 200) + 1;
                if (superRoll == 1) {
                    expMultiplier = 3.5;
                    boostDuration = 3600;
                } else {
                    expMultiplier = ((int) (Math.random() * 21) + 10) / 10.0;
                    boostDuration = (int) (Math.random() * 241) + 60;
                }
                coinsMultiplier = 1.0;
                Bukkit.broadcastMessage("§b☄ §dA Global EXP Boost is now active! §7(x" + expMultiplier + " EXP for " + boostDuration + " seconds)");
                createBossBar("§d☄ Global EXP Boost Active! §7(x" + expMultiplier + ")", BarColor.PURPLE);
                break;

            case "coins":
                coinsMultiplier = 2.0;
                expMultiplier = 1.0;
                boostDuration = (int) (Math.random() * 241) + 60;
                Bukkit.broadcastMessage("§b☄ §6A Global Coins Boost is now active! §7(x" + coinsMultiplier + " Coins for " + boostDuration + " seconds)");
                createBossBar("§6☄ Global Coins Boost Active! §7(x" + coinsMultiplier + ")", BarColor.YELLOW);
                break;

            case "both":
                expMultiplier = 1.5;
                coinsMultiplier = 2.0;
                boostDuration = (int) (Math.random() * 241) + 60;
                Bukkit.broadcastMessage("§b☄ §dA Global EXP & Coins Boost is now active! §7(x" + expMultiplier + " EXP, x" + coinsMultiplier + " Coins for " + boostDuration + " seconds)");
                createBossBar("§b☄ Global EXP & Coins Boost Active! §7(x" + expMultiplier + ", x" + coinsMultiplier + ")", BarColor.BLUE);
                break;
        }

        timeRemaining = boostDuration;
        startBoostTask();
    }

    private void createBossBar(String title, BarColor color) {
        if (bossBar != null) {
            bossBar.removeAll();
        }
        bossBar = Bukkit.createBossBar(title, color, BarStyle.SOLID);
        for (Player player : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(player);
        }
        bossBar.setVisible(true);
    }

    private void startBoostTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            if (!boostActive || timeRemaining <= 0) {
                endBoost();
                task.cancel();
                return;
            }

            timeRemaining--;
            double progress = (double) timeRemaining / boostDuration;
            if (bossBar != null) {
                bossBar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
                bossBar.setTitle(bossBar.getTitle().split(" §7\\(")[0] + " §7(" + timeRemaining + "s left)");
            }
        }, 0L, 20L);
    }

    private void endBoost() {
        boostActive = false;
        expMultiplier = 1.0;
        coinsMultiplier = 1.0;
        Bukkit.broadcastMessage("§7☄ The Global Boost has ended.");
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }
    }

    public boolean isBoostActive() {
        return boostActive;
    }

    public double getExpMultiplier() {
        return expMultiplier;
    }

    public double getCoinsMultiplier() {
        return coinsMultiplier;
    }

    public int getTimeRemaining() {
        return timeRemaining;
    }

    public String getBoostType() {
        return boostType;
    }

    public void addPlayerToBossBar(Player player) {
        if (bossBar != null && boostActive) {
            bossBar.addPlayer(player);
        }
    }
}
