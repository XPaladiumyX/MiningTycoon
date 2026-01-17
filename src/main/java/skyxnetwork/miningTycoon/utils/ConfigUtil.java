package skyxnetwork.miningTycoon.utils;

import skyxnetwork.miningTycoon.MiningTycoon;

public class ConfigUtil {

    public static void loadConfigurations(MiningTycoon plugin) {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        plugin.getLogger().info("Configuration loaded successfully!");
    }

    public static int getMaxLevel(MiningTycoon plugin) {
        return plugin.getConfig().getInt("settings.max-level", 500);
    }

    public static double getLevelUpMultiplier(MiningTycoon plugin) {
        return plugin.getConfig().getDouble("settings.level-up-multiplier", 1.1);
    }

    public static String getWorldName(MiningTycoon plugin) {
        return plugin.getConfig().getString("settings.world-name", "mining_tycoon");
    }
}
