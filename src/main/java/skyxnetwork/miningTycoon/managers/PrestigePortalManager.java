package skyxnetwork.miningTycoon.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.data.PlayerData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages prestige portals with coordinate-based detection
 */
public class PrestigePortalManager {

    private final MiningTycoon plugin;
    private final Map<String, PrestigePortal> portals;
    private final Map<UUID, Long> lastPrestigeAttempt;
    private static final long PRESTIGE_COOLDOWN = 3000; // 3 seconds

    public PrestigePortalManager(MiningTycoon plugin) {
        this.plugin = plugin;
        this.portals = new HashMap<>();
        this.lastPrestigeAttempt = new HashMap<>();

        // Delay initial load by 2 seconds to allow worlds to load
        Bukkit.getScheduler().runTaskLater(plugin, this::loadPortals, 40L);
    }

    /**
     * Load all prestige portals from config
     */
    public void loadPortals() {
        portals.clear();

        ConfigurationSection portalsSection = plugin.getConfig().getConfigurationSection("prestige.portals");
        if (portalsSection == null) {
            plugin.getLogger().warning("No prestige portals configured in config.yml!");
            return;
        }

        int loadedCount = 0;

        for (String portalId : portalsSection.getKeys(false)) {
            ConfigurationSection portalConfig = portalsSection.getConfigurationSection(portalId);
            if (portalConfig == null) continue;

            try {
                loadSinglePortal(portalId, portalConfig);
                loadedCount++;
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load portal '" + portalId + "': " + e.getMessage());
            }
        }

        plugin.getLogger().info("Loaded " + loadedCount + " prestige portal(s)");
    }

    /**
     * Load a single portal from config
     */
    private void loadSinglePortal(String portalId, ConfigurationSection portalConfig) {
        String worldName = portalConfig.getString("world");
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            throw new IllegalStateException("World '" + worldName + "' not found");
        }

        int minX = portalConfig.getInt("region.min.x");
        int minY = portalConfig.getInt("region.min.y");
        int minZ = portalConfig.getInt("region.min.z");
        int maxX = portalConfig.getInt("region.max.x");
        int maxY = portalConfig.getInt("region.max.y");
        int maxZ = portalConfig.getInt("region.max.z");

        int levelRequirement = portalConfig.getInt("level-requirement");
        String type = portalConfig.getString("type", "basic");

        PrestigePortal portal = new PrestigePortal(
                portalId,
                world,
                minX, minY, minZ,
                maxX, maxY, maxZ,
                levelRequirement,
                type
        );

        portals.put(portalId, portal);
        plugin.getLogger().info("Loaded prestige portal: " + portalId +
                " (Type: " + type + ", Level: " + levelRequirement + ")");
    }

    /**
     * Check if player is in any prestige portal
     */
    public PrestigePortal getPortalAtLocation(Location location) {
        for (PrestigePortal portal : portals.values()) {
            if (portal.contains(location)) {
                return portal;
            }
        }
        return null;
    }

    /**
     * Check if player can prestige through this portal
     */
    public boolean canPrestige(Player player, PrestigePortal portal) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        return data.getLevel() >= portal.getLevelRequirement();
    }

    /**
     * Handle player entering prestige portal
     */
    public void handlePortalEntry(Player player, PrestigePortal portal) {
        UUID uuid = player.getUniqueId();

        // Check cooldown
        long now = System.currentTimeMillis();
        if (lastPrestigeAttempt.containsKey(uuid)) {
            long timeSince = now - lastPrestigeAttempt.get(uuid);
            if (timeSince < PRESTIGE_COOLDOWN) {
                return; // Still on cooldown
            }
        }

        lastPrestigeAttempt.put(uuid, now);

        // Check if player can prestige
        if (!canPrestige(player, portal)) {
            // Player doesn't meet requirements - push them back
            PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
            player.sendMessage("§c⛔ You need to be level §6" + portal.getLevelRequirement() +
                    " §cto prestige through this portal!");
            player.sendMessage("§7Your current level: §6" + data.getLevel());
            player.playSound(player.getLocation(), "block.note_block.bass", 1.0f, 0.5f);

            // Push player back (same as zone restriction)
            org.bukkit.Location currentLoc = player.getLocation();
            org.bukkit.util.Vector direction = currentLoc.getDirection().multiply(-1);
            player.setVelocity(direction.multiply(1.0).setY(0.1));
            return;
        }

        // Open GUI via PrestigePortalGUI
        plugin.getPrestigePortalGUI().openPrestigeGUI(player, portal);
    }

    /**
     * Execute prestige for player
     */
    public void executePrestige(Player player, String portalType) {
        // Find portal of this type
        PrestigePortal portal = null;
        for (PrestigePortal p : portals.values()) {
            if (p.getType().equalsIgnoreCase(portalType)) {
                portal = p;
                break;
            }
        }

        if (portal == null) {
            player.sendMessage("§c⛔ Invalid prestige type!");
            return;
        }

        // Verify player is still in the portal
        if (!portal.contains(player.getLocation())) {
            player.sendMessage("§c⛔ You must be inside the prestige portal to confirm!");
            return;
        }

        // Verify level requirement again
        if (!canPrestige(player, portal)) {
            player.sendMessage("§c⛔ You no longer meet the level requirement!");
            return;
        }

        // Execute prestige through PrestigeManager
        plugin.getPrestigeManager().performPrestige(player, portalType);

        // Play effects
        player.playSound(player.getLocation(), "entity.player.levelup", 1.0f, 0.5f);
        player.playSound(player.getLocation(), "entity.ender_dragon.growl", 0.5f, 1.5f);

        // Teleport to spawn after 2 seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            ConfigurationSection spawnConfig = plugin.getConfig().getConfigurationSection("prestige.spawn");
            if (spawnConfig != null) {
                String worldName = spawnConfig.getString("world");
                World world = Bukkit.getWorld(worldName);

                if (world != null) {
                    Location spawn = new Location(
                            world,
                            spawnConfig.getDouble("x"),
                            spawnConfig.getDouble("y"),
                            spawnConfig.getDouble("z")
                    );

                    spawn.setYaw((float) spawnConfig.getDouble("yaw", 0));
                    spawn.setPitch((float) spawnConfig.getDouble("pitch", 0));

                    player.teleport(spawn);
                    player.sendMessage("§aYou have been teleported to spawn!");
                }
            }
        }, 40L);
    }

    /**
     * Reload all portals from config
     */
    public void reload() {
        loadPortals();
        plugin.getLogger().info("Reloaded prestige portals");
    }

    /**
     * Get all loaded portals
     */
    public Map<String, PrestigePortal> getPortals() {
        return new HashMap<>(portals);
    }

    /**
     * Create a new portal
     */
    public boolean createPortal(String id, String worldName, int minX, int minY, int minZ,
                                int maxX, int maxY, int maxZ, int levelRequirement, String type) {
        try {
            // Save to config
            String path = "prestige.portals." + id;
            plugin.getConfig().set(path + ".world", worldName);
            plugin.getConfig().set(path + ".region.min.x", minX);
            plugin.getConfig().set(path + ".region.min.y", minY);
            plugin.getConfig().set(path + ".region.min.z", minZ);
            plugin.getConfig().set(path + ".region.max.x", maxX);
            plugin.getConfig().set(path + ".region.max.y", maxY);
            plugin.getConfig().set(path + ".region.max.z", maxZ);
            plugin.getConfig().set(path + ".level-requirement", levelRequirement);
            plugin.getConfig().set(path + ".type", type);

            plugin.saveConfig();
            reload();
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create portal: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete a portal
     */
    public boolean deletePortal(String id) {
        try {
            plugin.getConfig().set("prestige.portals." + id, null);
            plugin.saveConfig();
            reload();
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to delete portal: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update a portal
     */
    public boolean updatePortal(String id, String worldName, int minX, int minY, int minZ,
                                int maxX, int maxY, int maxZ, int levelRequirement, String type) {
        return createPortal(id, worldName, minX, minY, minZ, maxX, maxY, maxZ, levelRequirement, type);
    }

    /**
     * Represents a prestige portal region
     */
    public static class PrestigePortal {
        private final String id;
        private final World world;
        private final int minX, minY, minZ;
        private final int maxX, maxY, maxZ;
        private final int levelRequirement;
        private final String type;

        public PrestigePortal(String id, World world, int minX, int minY, int minZ,
                              int maxX, int maxY, int maxZ, int levelRequirement, String type) {
            this.id = id;
            this.world = world;
            this.minX = Math.min(minX, maxX);
            this.minY = Math.min(minY, maxY);
            this.minZ = Math.min(minZ, maxZ);
            this.maxX = Math.max(minX, maxX);
            this.maxY = Math.max(minY, maxY);
            this.maxZ = Math.max(minZ, maxZ);
            this.levelRequirement = levelRequirement;
            this.type = type;
        }

        public boolean contains(Location location) {
            if (!location.getWorld().equals(world)) {
                return false;
            }

            int x = location.getBlockX();
            int y = location.getBlockY();
            int z = location.getBlockZ();

            return x >= minX && x <= maxX &&
                    y >= minY && y <= maxY &&
                    z >= minZ && z <= maxZ;
        }

        public String getId() {
            return id;
        }

        public World getWorld() {
            return world;
        }

        public int getMinX() {
            return minX;
        }

        public int getMinY() {
            return minY;
        }

        public int getMinZ() {
            return minZ;
        }

        public int getMaxX() {
            return maxX;
        }

        public int getMaxY() {
            return maxY;
        }

        public int getMaxZ() {
            return maxZ;
        }

        public int getLevelRequirement() {
            return levelRequirement;
        }

        public String getType() {
            return type;
        }

        @Override
        public String toString() {
            return "PrestigePortal{" +
                    "id='" + id + '\'' +
                    ", world=" + world.getName() +
                    ", region=[" + minX + "," + minY + "," + minZ + " to " + maxX + "," + maxY + "," + maxZ + "]" +
                    ", level=" + levelRequirement +
                    ", type='" + type + '\'' +
                    '}';
        }
    }
}