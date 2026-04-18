package skyxnetwork.miningTycoon.managers;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import skyxnetwork.miningTycoon.MiningTycoon;

import java.util.*;

public class MineManager {

    private final MiningTycoon plugin;
    private final Map<Material, BlockRewardConfig> blockRewards;
    private final Map<Material, BlockRewardConfig> defaultBlockRewards;
    private final Map<Integer, List<String>> zoneBlocks;

    public MineManager(MiningTycoon plugin) {
        this.plugin = plugin;
        this.blockRewards = new HashMap<>();
        this.defaultBlockRewards = new HashMap<>();
        this.zoneBlocks = new HashMap<>();
        loadMines();
    }

    public void loadMines() {
        blockRewards.clear();
        defaultBlockRewards.clear();
        zoneBlocks.clear();

        ConfigurationSection minesSection = plugin.getConfig().getConfigurationSection("mines");
        if (minesSection == null) {
            plugin.getLogger().warning("No 'mines' section found in config.yml!");
            return;
        }

        ConfigurationSection defaultSection = minesSection.getConfigurationSection("default");
        if (defaultSection != null) {
            for (String blockKey : defaultSection.getKeys(false)) {
                ConfigurationSection blockConfig = defaultSection.getConfigurationSection(blockKey);
                if (blockConfig == null) continue;

                String blockType = blockConfig.getString("block");
                if (blockType == null) continue;

                Material material = Material.getMaterial(blockType);
                if (material == null) {
                    continue;
                }

                double exp = blockConfig.getDouble("exp", 0);
                double money = blockConfig.getDouble("money", 0);

                BlockRewardConfig reward = new BlockRewardConfig(
                        blockKey,
                        material,
                        0,
                        exp,
                        money
                );

                defaultBlockRewards.put(material, reward);
            }
            plugin.getLogger().info("Loaded " + defaultBlockRewards.size() + " default blocks");
        }

        Set<Integer> processedZones = new HashSet<>();
        int totalLoaded = 0;

        for (String zoneKey : minesSection.getKeys(false)) {
            if (zoneKey.equals("default")) continue;

            ConfigurationSection zoneSection = minesSection.getConfigurationSection(zoneKey);
            if (zoneSection == null) continue;

            int zoneNum = parseZoneNumber(zoneKey);
            if (zoneNum < 1) continue;

            if (processedZones.contains(zoneNum)) continue;
            processedZones.add(zoneNum);

            List<String> blockListForZone = new ArrayList<>();

            for (String blockKey : zoneSection.getKeys(false)) {
                ConfigurationSection blockConfig = zoneSection.getConfigurationSection(blockKey);
                if (blockConfig == null) continue;

                String blockType = blockConfig.getString("block");
                if (blockType == null) continue;

                Material material = Material.getMaterial(blockType);
                if (material == null) {
                    plugin.getLogger().warning("Unknown material: " + blockType);
                    continue;
                }

                int zone = blockConfig.getInt("zone", zoneNum);
                double exp = blockConfig.getDouble("exp", 0);
                double money = blockConfig.getDouble("money", 0);

                BlockRewardConfig reward = new BlockRewardConfig(
                        blockKey,
                        material,
                        zone,
                        exp,
                        money
                );

                blockRewards.put(material, reward);
                blockListForZone.add(blockKey);
                totalLoaded++;
            }

            if (!blockListForZone.isEmpty()) {
                zoneBlocks.put(zoneNum, blockListForZone);
            }
        }

        plugin.getLogger().info("Loaded " + totalLoaded + " zone-specific block rewards from " + zoneBlocks.size() + " zones");
    }

    private int parseZoneNumber(String zoneKey) {
        try {
            if (zoneKey.equals("zone1")) return 1;
            if (zoneKey.equals("zone2")) return 2;
            if (zoneKey.equals("zone3")) return 3;
            if (zoneKey.equals("zone4")) return 4;
            if (zoneKey.equals("zone5")) return 5;

            return Integer.parseInt(zoneKey.replace("zone", ""));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public BlockRewardConfig getBlockReward(Material material) {
        BlockRewardConfig reward = blockRewards.get(material);
        if (reward == null) {
            reward = defaultBlockRewards.get(material);
        }
        return reward;
    }

    public boolean isValidMineBlock(Material material) {
        return blockRewards.containsKey(material) || defaultBlockRewards.containsKey(material);
    }

    public boolean isDefaultBlock(Material material) {
        return defaultBlockRewards.containsKey(material);
    }

    public List<String> getBlocksForZone(int zone) {
        return zoneBlocks.getOrDefault(zone, Collections.emptyList());
    }

    public Set<Material> getAllBlocks() {
        Set<Material> allBlocks = new HashSet<>(blockRewards.keySet());
        allBlocks.addAll(defaultBlockRewards.keySet());
        return allBlocks;
    }

    public Map<Integer, List<String>> getZoneBlocks() {
        return new HashMap<>(zoneBlocks);
    }

    public int getTotalBlockCount() {
        return blockRewards.size() + defaultBlockRewards.size();
    }

    public void reload() {
        loadMines();
    }

    public static class BlockRewardConfig {
        private final String id;
        private final Material material;
        private final int zone;
        private final double exp;
        private final double money;

        public BlockRewardConfig(String id, Material material, int zone, double exp, double money) {
            this.id = id;
            this.material = material;
            this.zone = zone;
            this.exp = exp;
            this.money = money;
        }

        public String getId() {
            return id;
        }

        public Material getMaterial() {
            return material;
        }

        public int getZone() {
            return zone;
        }

        public double getExp() {
            return exp;
        }

        public double getMoney() {
            return money;
        }
    }
}