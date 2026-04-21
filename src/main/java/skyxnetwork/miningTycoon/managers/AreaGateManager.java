package skyxnetwork.miningTycoon.managers;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.data.PlayerData;

import java.util.*;

public class AreaGateManager {

    private final MiningTycoon plugin;
    private final Map<String, AreaGateConfig> gateConfigs;
    private long pushDelay;
    private int maxPushes;
    private boolean placeholderApiEnabled;

    public AreaGateManager(MiningTycoon plugin) {
        this.plugin = plugin;
        this.gateConfigs = new HashMap<>();
        this.placeholderApiEnabled = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
        loadGateConfigs();
    }

    public void loadGateConfigs() {
        gateConfigs.clear();

        ConfigurationSection gatesSection = plugin.getConfig().getConfigurationSection("area_gates");
        if (gatesSection == null) {
            plugin.getLogger().warning("No 'area_gates' section found in config.yml!");
            return;
        }

        pushDelay = gatesSection.getLong("push-delay", 100);
        maxPushes = gatesSection.getInt("max-pushes", 3);

        for (String key : gatesSection.getKeys(false)) {
            if (key.equals("push-delay") || key.equals("max-pushes")) continue;

            ConfigurationSection gateSection = gatesSection.getConfigurationSection(key);
            if (gateSection == null) continue;

            String region = gateSection.getString("region", key);
            int requiredPrestige = gateSection.getInt("required_prestige", 0);
            int requiredLevel = gateSection.getInt("required_level", 0);
            int requiredZentium = gateSection.getInt("required_zentium", 0);
            String pushBackLocation = gateSection.getString("push-back-location", "spawn");

            AreaGateConfig config = new AreaGateConfig(
                    key,
                    region,
                    requiredPrestige,
                    requiredLevel,
                    requiredZentium,
                    pushBackLocation
            );

            gateConfigs.put(key, config);
            plugin.getLogger().info("Loaded area gate: " + key + " (region: " + region + 
                    ", prestige: " + requiredPrestige + 
                    ", level: " + requiredLevel + 
                    ", zentium: " + requiredZentium + ")");
        }

        plugin.getLogger().info("Loaded " + gateConfigs.size() + " area gates from config");
    }

    public boolean checkGateAccess(Player player, String gateKey) {
        AreaGateConfig config = gateConfigs.get(gateKey);
        if (config == null) return true;

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

        if (config.getRequiredPrestige() > 0 && data.getPrestige() < config.getRequiredPrestige()) {
            return false;
        }

        if (config.getRequiredLevel() > 0 && data.getLevel() < config.getRequiredLevel()) {
            return false;
        }

        if (config.getRequiredZentium() > 0 && getPlayerZentium(player) < config.getRequiredZentium()) {
            return false;
        }

        return true;
    }

    public AreaGateConfig getFailedRequirement(Player player, String gateKey) {
        AreaGateConfig config = gateConfigs.get(gateKey);
        if (config == null) return null;

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

        if (config.getRequiredPrestige() > 0 && data.getPrestige() < config.getRequiredPrestige()) {
            return config;
        }

        if (config.getRequiredLevel() > 0 && data.getLevel() < config.getRequiredLevel()) {
            return config;
        }

        if (config.getRequiredZentium() > 0 && getPlayerZentium(player) < config.getRequiredZentium()) {
            return config;
        }

        return null;
    }

    public String getAccessDeniedMessage(Player player, String gateKey) {
        AreaGateConfig config = gateConfigs.get(gateKey);
        if (config == null) return null;

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

        String baseMessage = plugin.getConfig().getString("messages.area-gate.access-denied",
                "§7[§e!§7] §cYou need %requirement% %type% to enter this area!");

        if (config.getRequiredPrestige() > 0 && data.getPrestige() < config.getRequiredPrestige()) {
            int missing = config.getRequiredPrestige() - data.getPrestige();
            return baseMessage.replace("%requirement%", String.valueOf(config.getRequiredPrestige()))
                    .replace("%type%", "Prestige");
        }

        if (config.getRequiredLevel() > 0 && data.getLevel() < config.getRequiredLevel()) {
            return baseMessage.replace("%requirement%", String.valueOf(config.getRequiredLevel()))
                    .replace("%type%", "Level");
        }

        if (config.getRequiredZentium() > 0 && getPlayerZentium(player) < config.getRequiredZentium()) {
            return baseMessage.replace("%requirement%", String.valueOf(config.getRequiredZentium()))
                    .replace("%type%", "Zentium");
        }

        return null;
    }

    private int getPlayerZentium(Player player) {
        if (!placeholderApiEnabled) {
            return 0;
        }

        try {
            String placeholderResult = PlaceholderAPI.setPlaceholders(player, "%coinsengine_balance_raw_zentium%");
            if (placeholderResult != null && !placeholderResult.isEmpty()) {
                return Integer.parseInt(placeholderResult.replaceAll("[^0-9]", ""));
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to get zentium balance: " + e.getMessage());
        }
        return 0;
    }

    public Map<String, AreaGateConfig> getGateConfigs() {
        return new HashMap<>(gateConfigs);
    }

    public AreaGateConfig getGateConfig(String gateKey) {
        return gateConfigs.get(gateKey);
    }

    public long getPushDelay() {
        return pushDelay;
    }

    public int getMaxPushes() {
        return maxPushes;
    }

    public void reload() {
        loadGateConfigs();
    }

    public static class AreaGateConfig {
        private final String gateKey;
        private final String region;
        private final int requiredPrestige;
        private final int requiredLevel;
        private final int requiredZentium;
        private final String pushBackLocation;

        public AreaGateConfig(String gateKey, String region, int requiredPrestige, 
                              int requiredLevel, int requiredZentium, String pushBackLocation) {
            this.gateKey = gateKey;
            this.region = region;
            this.requiredPrestige = requiredPrestige;
            this.requiredLevel = requiredLevel;
            this.requiredZentium = requiredZentium;
            this.pushBackLocation = pushBackLocation;
        }

        public String getGateKey() {
            return gateKey;
        }

        public String getRegion() {
            return region;
        }

        public int getRequiredPrestige() {
            return requiredPrestige;
        }

        public int getRequiredLevel() {
            return requiredLevel;
        }

        public int getRequiredZentium() {
            return requiredZentium;
        }

        public String getPushBackLocation() {
            return pushBackLocation;
        }
    }
}