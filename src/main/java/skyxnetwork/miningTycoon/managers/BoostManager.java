package skyxnetwork.miningTycoon.managers;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import skyxnetwork.miningTycoon.MiningTycoon;

public class BoostManager {

    private final MiningTycoon plugin;
    private boolean boostActive;
    private String boostType;
    private double expMultiplier;
    private double coinsMultiplier;
    private int boostDuration;
    private int timeRemaining;
    private BossBar bossBar;
    private BukkitTask boostTask;

    private double expMinMultiplier;
    private double expMaxMultiplier;
    private int expMinDuration;
    private int expMaxDuration;
    private int superRareChance;
    private double superRareMultiplier;
    private int superRareDuration;

    private double coinsMultiplierValue;
    private int coinsMinDuration;
    private int coinsMaxDuration;

    public BoostManager(MiningTycoon plugin) {
        this.plugin = plugin;
        this.boostActive = false;
        this.expMultiplier = 1.0;
        this.coinsMultiplier = 1.0;
        loadBoostConfig();
    }

    private void loadBoostConfig() {
        var boostsSection = plugin.getConfig().getConfigurationSection("boosts");
        if (boostsSection == null) {
            plugin.getLogger().warning("No boosts section in config, using defaults");
            setDefaults();
            return;
        }

        var expSection = boostsSection.getConfigurationSection("exp");
        if (expSection != null) {
            expMinMultiplier = expSection.getDouble("min-multiplier", 1.0);
            expMaxMultiplier = expSection.getDouble("max-multiplier", 3.0);
            expMinDuration = expSection.getInt("min-duration", 60);
            expMaxDuration = expSection.getInt("max-duration", 300);
            superRareChance = expSection.getInt("super-rare-chance", 200);
            superRareMultiplier = expSection.getDouble("super-rare-multiplier", 3.5);
            superRareDuration = expSection.getInt("super-rare-duration", 3600);
        } else {
            expMinMultiplier = 1.0;
            expMaxMultiplier = 3.0;
            expMinDuration = 60;
            expMaxDuration = 300;
            superRareChance = 200;
            superRareMultiplier = 3.5;
            superRareDuration = 3600;
        }

        var coinsSection = boostsSection.getConfigurationSection("coins");
        if (coinsSection != null) {
            coinsMultiplierValue = coinsSection.getDouble("multiplier", 2.0);
            coinsMinDuration = coinsSection.getInt("min-duration", 60);
            coinsMaxDuration = coinsSection.getInt("max-duration", 300);
        } else {
            coinsMultiplierValue = 2.0;
            coinsMinDuration = 60;
            coinsMaxDuration = 300;
        }
    }

    private void setDefaults() {
        expMinMultiplier = 1.0;
        expMaxMultiplier = 3.0;
        expMinDuration = 60;
        expMaxDuration = 300;
        superRareChance = 200;
        superRareMultiplier = 3.5;
        superRareDuration = 3600;
        coinsMultiplierValue = 2.0;
        coinsMinDuration = 60;
        coinsMaxDuration = 300;
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
                int superRoll = (int) (Math.random() * superRareChance) + 1;
                if (superRoll == 1) {
                    expMultiplier = superRareMultiplier;
                    boostDuration = superRareDuration;
                } else {
                    expMultiplier = ((int) (Math.random() * (expMaxMultiplier * 10 - expMinMultiplier * 10)) + (int) (expMinMultiplier * 10)) / 10.0;
                    boostDuration = (int) (Math.random() * (expMaxDuration - expMinDuration)) + expMinDuration;
                }
                coinsMultiplier = 1.0;
                Bukkit.broadcastMessage("§b☄ §dA Global EXP Boost is now active! §7(x" + expMultiplier + " EXP for " + boostDuration + " seconds)");
                createBossBar("§d☄ Global EXP Boost Active! §7(x" + expMultiplier + ")", BarColor.PURPLE);
                break;

            case "coins":
                coinsMultiplier = coinsMultiplierValue;
                expMultiplier = 1.0;
                boostDuration = (int) (Math.random() * (coinsMaxDuration - coinsMinDuration)) + coinsMinDuration;
                Bukkit.broadcastMessage("§b☄ §6A Global Coins Boost is now active! §7(x" + coinsMultiplier + " Coins for " + boostDuration + " seconds)");
                createBossBar("§6☄ Global Coins Boost Active! §7(x" + coinsMultiplier + ")", BarColor.YELLOW);
                break;

            case "both":
                expMultiplier = 1.5;
                coinsMultiplier = coinsMultiplierValue;
                boostDuration = (int) (Math.random() * (coinsMaxDuration - coinsMinDuration)) + coinsMinDuration;
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
        boostTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                if (!boostActive || timeRemaining <= 0) {
                    endBoost();
                    return;
                }

                timeRemaining--;
                double progress = (double) timeRemaining / boostDuration;
                if (bossBar != null) {
                    bossBar.setProgress(Math.max(0.0, Math.min(1.0, progress)));
                    bossBar.setTitle(bossBar.getTitle().split(" §7\\(")[0] + " §7(" + timeRemaining + "s left)");
                }
            }
        }, 0L, 20L);
    }

    public void endBoost() {
        boostActive = false;
        expMultiplier = 1.0;
        coinsMultiplier = 1.0;

        if (boostTask != null) {
            boostTask.cancel();
            boostTask = null;
        }

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

    public void startGlobalBoost(String type) {
        boostActive = true;
        boostType = type;
        
        switch (type) {
            case "exp":
                expMultiplier = ((int) (Math.random() * (expMaxMultiplier * 10 - expMinMultiplier * 10)) + (int) (expMinMultiplier * 10)) / 10.0;
                if (expMultiplier < expMinMultiplier) expMultiplier = expMinMultiplier;
                coinsMultiplier = 1.0;
                boostDuration = (int) (Math.random() * (expMaxDuration - expMinDuration)) + expMinDuration;
                break;
            case "coins":
                coinsMultiplier = coinsMultiplierValue;
                expMultiplier = 1.0;
                boostDuration = (int) (Math.random() * (coinsMaxDuration - coinsMinDuration)) + coinsMinDuration;
                break;
            case "both":
                expMultiplier = ((int) (Math.random() * (expMaxMultiplier * 10 - expMinMultiplier * 10)) + (int) (expMinMultiplier * 10)) / 10.0;
                if (expMultiplier < expMinMultiplier) expMultiplier = expMinMultiplier;
                coinsMultiplier = coinsMultiplierValue;
                boostDuration = (int) (Math.random() * (coinsMaxDuration - coinsMinDuration)) + coinsMinDuration;
                break;
            default:
                boostActive = false;
                return;
        }
        
        timeRemaining = boostDuration;
        
        switch (type) {
            case "exp":
                Bukkit.broadcastMessage("§b☄ §dA Global EXP Boost is now active! §7(x" + expMultiplier + " EXP for " + boostDuration + " seconds)");
                createBossBar("§d☄ Global EXP Boost Active! §7(x" + expMultiplier + ")", BarColor.PURPLE);
                break;
            case "coins":
                Bukkit.broadcastMessage("§b☄ §6A Global Coins Boost is now active! §7(x" + coinsMultiplier + " Coins for " + boostDuration + " seconds)");
                createBossBar("§6☄ Global Coins Boost Active! §7(x" + coinsMultiplier + ")", BarColor.YELLOW);
                break;
            case "both":
                Bukkit.broadcastMessage("§b☄ §dA Global EXP & Coins Boost is now active! §7(x" + expMultiplier + " EXP, x" + coinsMultiplier + " Coins for " + boostDuration + " seconds)");
                createBossBar("§b☄ Global EXP & Coins Boost Active! §7(x" + expMultiplier + ", x" + coinsMultiplier + ")", BarColor.BLUE);
                break;
        }
        
        startBoostTask();
    }
}