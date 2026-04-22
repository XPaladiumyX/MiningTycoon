package skyxnetwork.miningTycoon.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.utils.ItemBuilder;

import java.util.Arrays;
import java.util.List;

public class BoostManager {

    public enum BoostItemType {
        EXP("exp", "§dEXP Boost", "§7Activates a Global §dEXP Boost§7!"),
        COINS("coins", "§6Coins Boost", "§7Activates a Global §6Coins Boost§7!"),
        BOTH("both", "§dEXP & Coins Boost", "§7Activates a Global §dEXP & Coins Boost§7!");

        private final String id;
        private final String name;
        private final String lore;

        BoostItemType(String id, String name, String lore) {
            this.id = id;
            this.name = name;
            this.lore = lore;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getLore() { return lore; }

        public static BoostItemType fromId(String id) {
            for (BoostItemType type : values()) {
                if (type.id.equals(id)) return type;
            }
            return null;
        }
    }

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

    public ItemStack createBoostItem(BoostItemType type) {
        return new ItemBuilder(Material.NETHER_STAR)
                .setName(type.getName())
                .setLore(
                        "",
                        type.getLore(),
                        "",
                        "§8\u00bb §7Click to activate",
                        "§8\u00bb §7Cooldown: " + (coinsMaxDuration / 60) + " min max"
                )
                .addEnchantGlint()
                .build();
    }

    public static BoostItemType getBoostItemType(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) return null;
        
        List<String> lore = meta.getLore();
        for (String line : lore) {
            if (line.contains("EXP Boost") && line.contains("Coins")) {
                return BoostItemType.BOTH;
            } else if (line.contains("EXP Boost")) {
                return BoostItemType.EXP;
            } else if (line.contains("Coins Boost")) {
                return BoostItemType.COINS;
            }
        }
        return null;
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

    public ItemStack giveFallbackItem(Player player, BoostItemType type) {
        ItemStack item = createBoostItem(type);
        player.getInventory().addItem(item);
        player.sendMessage("§7§l\u00bb §dA boost item dropped! §7(No boost active, so you got an item instead)");
        return item;
    }
}

    public String getBoostType() {
        return boostType;
    }

    public void addPlayerToBossBar(Player player) {
        if (bossBar != null && boostActive) {
            bossBar.addPlayer(player);
        }
    }

    public void startGlobalBoost(String type, Player triggeredBy) {
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
                Bukkit.broadcastMessage("§b☄ §d" + triggeredBy.getName() + " §dtriggered a Global EXP Boost! §7(x" + expMultiplier + " EXP for " + boostDuration + " seconds)");
                createBossBar("§d☄ Global EXP Boost Active! §7(x" + expMultiplier + ")", BarColor.PURPLE);
                break;
            case "coins":
                Bukkit.broadcastMessage("§b☄ §6" + triggeredBy.getName() + " §6triggered a Global Coins Boost! §7(x" + coinsMultiplier + " Coins for " + boostDuration + " seconds)");
                createBossBar("§6☄ Global Coins Boost Active! §7(x" + coinsMultiplier + ")", BarColor.YELLOW);
                break;
            case "both":
                Bukkit.broadcastMessage("§b☄ §d" + triggeredBy.getName() + " §dtriggered a Global EXP & Coins Boost! §7(x" + expMultiplier + " EXP, x" + coinsMultiplier + " Coins for " + boostDuration + " seconds)");
                createBossBar("§b☄ Global EXP & Coins Boost Active! §7(x" + expMultiplier + ", x" + coinsMultiplier + ")", BarColor.BLUE);
                break;
        }
        
        startBoostTask();
    }
}