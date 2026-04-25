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

    public void reload() {
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
        
        if (!zoneSection.isList("rewards")) {
            plugin.getLogger().warning("Rewards is not a list for zone: " + zoneSection.getName());
            return rewards;
        }
        
        List<?> rewardsList = zoneSection.getList("rewards");

        if (rewardsList == null) {
            return rewards;
        }

        for (Object obj : rewardsList) {
            if (!(obj instanceof Map)) {
                plugin.getLogger().warning("Reward is not a Map: " + obj);
                continue;
            }
            
            Map<?, ?> rewardMap = (Map<?, ?>) obj;
            
            Object typeObj = rewardMap.get("type");
            if (typeObj == null) {
                continue;
            }
            String typeStr = typeObj.toString().toUpperCase();
            
            CommunityReward.RewardType type;
            try {
                type = CommunityReward.RewardType.valueOf(typeStr);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid reward type: " + typeStr);
                continue;
            }

            double chance = 0.0;
            Object chanceObj = rewardMap.get("chance");
            if (chanceObj instanceof Number) {
                chance = ((Number) chanceObj).doubleValue();
            }

            int min = 0;
            Object minObj = rewardMap.get("min");
            if (minObj instanceof Number) {
                min = ((Number) minObj).intValue();
            }

            int max = 0;
            Object maxObj = rewardMap.get("max");
            if (maxObj instanceof Number) {
                max = ((Number) maxObj).intValue();
            }

            int amount = 1;
            Object amountObj = rewardMap.get("amount");
            if (amountObj instanceof Number) {
                amount = ((Number) amountObj).intValue();
            }
            
            List<String> commands = new ArrayList<>();
            Object commandsObj = rewardMap.get("commands");
            if (commandsObj instanceof List) {
                for (Object cmd : (List<?>) commandsObj) {
                    if (cmd != null) {
                        commands.add(cmd.toString());
                    }
                }
            }

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