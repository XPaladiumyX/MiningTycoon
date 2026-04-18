package skyxnetwork.miningTycoon.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.data.PlayerData;

import java.util.HashMap;
import java.util.Map;

public class PrestigeManager {

    private final MiningTycoon plugin;
    private final Map<Integer, RebirthConfig> rebirthConfigs;

    public PrestigeManager(MiningTycoon plugin) {
        this.plugin = plugin;
        this.rebirthConfigs = new HashMap<>();
        loadRebirthConfigs();
    }

    private void loadRebirthConfigs() {
        rebirthConfigs.clear();

        for (int i = 1; i <= 5; i++) {
            ConfigurationSection section = plugin.getConfig().getConfigurationSection("prestige.rebirth_" + i);
            if (section == null) {
                plugin.getLogger().warning("No rebirth_" + i + " configuration found");
                continue;
            }

            int levelRequirement = section.getInt("level-requirement");
            boolean resetLevel = section.getBoolean("reset-level", true);
            double expMultiplierBonus = section.getDouble("exp-multiplier-bonus", 0.0);

            Map<String, Integer> rewards = new HashMap<>();
            ConfigurationSection rewardsSection = section.getConfigurationSection("rewards");
            if (rewardsSection != null) {
                for (String key : rewardsSection.getKeys(false)) {
                    rewards.put(key, rewardsSection.getInt(key));
                }
            }

            RebirthConfig config = new RebirthConfig(
                    i,
                    levelRequirement,
                    resetLevel,
                    expMultiplierBonus,
                    rewards
            );

            rebirthConfigs.put(i, config);
            plugin.getLogger().info("Loaded rebirth_" + i + ": level=" + levelRequirement +
                    ", multiplier=" + (expMultiplierBonus * 100) + "%");
        }
    }

    public boolean canPrestige(Player player, int rebirthNumber) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        RebirthConfig config = rebirthConfigs.get(rebirthNumber);

        if (config == null) {
            return false;
        }

        return data.getLevel() >= config.getLevelRequirement();
    }

    public void performPrestige(Player player, int rebirthNumber) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        RebirthConfig config = rebirthConfigs.get(rebirthNumber);

        if (config == null) {
            player.sendMessage("§c⛔ Invalid rebirth number!");
            return;
        }

        if (config.isResetLevel()) {
            // Reset player money to 0 on rebirth
            if (plugin.getEconomyManager().isEnabled()) {
                plugin.getEconomyManager().setMoney(player, 0);
            }
            
            data.resetForPrestige();
        }

        data.addPrestige(1);
        data.addExpMultiplierBonus(config.getExpMultiplierBonus());

        for (Map.Entry<String, Integer> reward : config.getRewards().entrySet()) {
            String rewardType = reward.getKey();
            int amount = reward.getValue();

            giveReward(player, rewardType, amount);
        }

        String message = plugin.getConfig().getString("messages.prestige-broadcast", "§8[§d⚡ Prestige §8] §6%player% §ehas reached §dPrestige %prestige%§e!");
        message = message.replace("%player%", player.getName()).replace("%prestige%", String.valueOf(data.getPrestige()));
        Bukkit.broadcastMessage(message);

        player.sendMessage("§aCongratulations! You are now Prestige §d" + data.getPrestige() + "§a!");
        player.sendMessage("§7Your EXP multiplier: §e" + data.getExpMultiplierDisplay());
    }

    private void giveReward(Player player, String rewardType, int amount) {
        if (rewardType == null || amount <= 0) return;

        Material material;
        String command = null;

        switch (rewardType.toLowerCase()) {
            case "diamonds":
                material = Material.DIAMOND;
                break;
            case "zentium":
                material = Material.DIAMOND;
                command = "zentium give " + player.getName() + " " + amount;
                break;
            case "rebirth-points":
                plugin.getPlayerDataManager().getPlayerData(player).addRebirthPoints(amount);
                player.sendMessage("§d✨ You gained §6" + amount + " §drebirth point(s)!");
                return;
            default:
                plugin.getLogger().warning("Unknown reward type: " + rewardType);
                return;
        }

        if (material != null) {
            ItemStack item = new ItemStack(material, amount);
            player.getInventory().addItem(item);
        }

        if (command != null) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }

    public RebirthConfig getRebirthConfig(int rebirthNumber) {
        return rebirthConfigs.get(rebirthNumber);
    }

    public int getRebirthCount() {
        return rebirthConfigs.size();
    }

    public String getPrestigeDisplay(int prestige) {
        if (prestige == 0) return "§7[-]";

        String value = prestige >= 1000 ? (Math.round(prestige / 10.0) / 100.0) + "k" : String.valueOf(prestige);

        if (prestige < 10) return "§7[" + value + "✾]";
        if (prestige < 50) return "§a[" + value + "✾]";
        if (prestige < 100) return "§b[" + value + "✾]";
        if (prestige < 200) return "§d[" + value + "✾]";
        if (prestige < 300) return "§6[" + value + "✾]";
        if (prestige < 500) return "§c[" + value + "✾]";
        if (prestige < 800) return "§e[" + value + "✾]";
        if (prestige < 1000) return "§5[" + value + "✾]";
        if (prestige < 1300) return "§9[" + value + "✾]";
        if (prestige < 1500) return "§d[" + value + "✾]";
        return "§d✴[" + value + "✾]§d✴";
    }

    public void reload() {
        loadRebirthConfigs();
    }

    public static class RebirthConfig {
        private final int rebirthNumber;
        private final int levelRequirement;
        private final boolean resetLevel;
        private final double expMultiplierBonus;
        private final Map<String, Integer> rewards;

        public RebirthConfig(int rebirthNumber, int levelRequirement, boolean resetLevel,
                      double expMultiplierBonus, Map<String, Integer> rewards) {
            this.rebirthNumber = rebirthNumber;
            this.levelRequirement = levelRequirement;
            this.resetLevel = resetLevel;
            this.expMultiplierBonus = expMultiplierBonus;
            this.rewards = rewards;
        }

        public int getRebirthNumber() {
            return rebirthNumber;
        }

        public int getLevelRequirement() {
            return levelRequirement;
        }

        public boolean isResetLevel() {
            return resetLevel;
        }

        public double getExpMultiplierBonus() {
            return expMultiplierBonus;
        }

        public Map<String, Integer> getRewards() {
            return rewards;
        }
    }
}