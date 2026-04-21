package skyxnetwork.miningTycoon.config;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.models.CommunityGeneratorZone;
import skyxnetwork.miningTycoon.models.CommunityReward;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommunityGeneratorConfig {

    private final MiningTycoon plugin;
    private boolean enabled;
    private final Map<String, CommunityGeneratorZone> zones;
    private final Map<Material, CommunityGeneratorZone> materialToZone;

    public CommunityGeneratorConfig(MiningTycoon plugin) {
        this.plugin = plugin;
        this.zones = new HashMap<>();
        this.materialToZone = new HashMap<>();
        load();
    }

    public void load() {
        zones.clear();
        materialToZone.clear();

        if (!plugin.getConfig().contains("community-generator")) {
            plugin.getLogger().warning("No community-generator config found!");
            return;
        }

        enabled = plugin.getConfig().getBoolean("community-generator.enabled", false);
        ConfigurationSection zonesSection = plugin.getConfig().getConfigurationSection("community-generator.zones");

        if (zonesSection == null) {
            plugin.getLogger().warning("No community-generator zones defined!");
            return;
        }

        for (String key : zonesSection.getKeys(false)) {
            ConfigurationSection zoneSection = zonesSection.getConfigurationSection(key);
            if (zoneSection == null) continue;

            String blockName = zoneSection.getString("block", "");
            Material block = Material.getMaterial(blockName);
            if (block == null) {
                plugin.getLogger().warning("Invalid block type: " + blockName + " for community generator zone: " + key);
                continue;
            }

            int cooldown = zoneSection.getInt("cooldown", 60);

            List<CommunityReward> rewards = loadRewards(zoneSection);
            CommunityGeneratorZone zone = new CommunityGeneratorZone(key, block, cooldown, rewards);
            zones.put(key, zone);
            materialToZone.put(block, zone);

            plugin.getLogger().info("Loaded community generator for zone: " + key + " (block: " + blockName + ", cooldown: " + cooldown + "s, rewards: " + rewards.size() + ")");
        }

        plugin.getLogger().info("Community generator config loaded: " + zones.size() + " zones, enabled: " + enabled);
    }

    private List<CommunityReward> loadRewards(ConfigurationSection zoneSection) {
        List<CommunityReward> rewards = new ArrayList<>();
        List<?> rewardsList = zoneSection.getList("rewards");

        if (rewardsList == null) {
            return rewards;
        }

        for (Object obj : rewardsList) {
            if (!(obj instanceof ConfigurationSection rewardSection)) {
                continue;
            }

            String typeStr = rewardSection.getString("type", "").toUpperCase();
            CommunityReward.RewardType type;
            try {
                type = CommunityReward.RewardType.valueOf(typeStr);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid reward type: " + typeStr);
                continue;
            }

            double chance = rewardSection.getDouble("chance", 0.0);
            int min = rewardSection.getInt("min", 0);
            int max = rewardSection.getInt("max", 0);
            int amount = rewardSection.getInt("amount", 1);
            List<String> commands = rewardSection.getStringList("commands");

            rewards.add(new CommunityReward(type, chance, min, max, amount, commands));
        }

        return rewards;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public CommunityGeneratorZone getZoneByMaterial(Material material) {
        return materialToZone.get(material);
    }

    public CommunityGeneratorZone getZoneByName(String zoneName) {
        return zones.get(zoneName);
    }

    public Map<String, CommunityGeneratorZone> getZones() {
        return Collections.unmodifiableMap(zones);
    }
}