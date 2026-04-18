package skyxnetwork.miningTycoon.managers;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import skyxnetwork.miningTycoon.MiningTycoon;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AntiCheatManager {

    private final MiningTycoon plugin;
    private boolean enabled;
    private boolean debug;

    private SpeedConfig speedConfig;
    private FlyConfig flyConfig;
    private AirWalkConfig airWalkConfig;
    private RollbackConfig rollbackConfig;
    private ExclusionConfig exclusionConfig;

    private final Map<UUID, PlayerAntiCheatData> playerData = new ConcurrentHashMap<>();

    public AntiCheatManager(MiningTycoon plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        ConfigurationSection anticheat = loadSeparateAnticheatFile();
        
        if (anticheat == null) {
            plugin.getLogger().warning("No anticheat configuration found!");
            enabled = false;
            return;
        }

        enabled = anticheat.getBoolean("enabled", true);
        debug = anticheat.getBoolean("debug", false);

        var speedSec = anticheat.getConfigurationSection("speed");
        speedConfig = new SpeedConfig(
                speedSec != null && speedSec.getBoolean("enabled", true),
                speedSec != null ? speedSec.getDouble("max-horizontal-speed", 0.75) : 0.75,
                speedSec != null ? speedSec.getInt("tolerance", 3) : 3,
                speedSec != null ? speedSec.getInt("check-interval", 1) : 1
        );

        var flySec = anticheat.getConfigurationSection("fly");
        flyConfig = new FlyConfig(
                flySec != null && flySec.getBoolean("enabled", true),
                flySec != null ? flySec.getInt("max-air-ticks", 25) : 25,
                flySec != null ? flySec.getInt("tolerance", 3) : 3,
                flySec != null ? flySec.getInt("check-interval", 1) : 1
        );

        var airWalkSec = anticheat.getConfigurationSection("air-walk");
        airWalkConfig = new AirWalkConfig(
                airWalkSec != null && airWalkSec.getBoolean("enabled", true),
                airWalkSec != null ? airWalkSec.getInt("max-air-horizontal-ticks", 15) : 15,
                airWalkSec != null ? airWalkSec.getInt("tolerance", 3) : 3
        );

        var rollbackSec = anticheat.getConfigurationSection("rollback");
        rollbackConfig = new RollbackConfig(
                rollbackSec != null && rollbackSec.getBoolean("use-last-safe-position", true),
                rollbackSec != null ? rollbackSec.getDouble("vertical-push", 0.1) : 0.1
        );

        var exclusionsSec = anticheat.getConfigurationSection("exclusions");
        exclusionConfig = new ExclusionConfig(
                exclusionsSec != null ? exclusionsSec.getString("bypass-permission", "miningtycoon.anticheat.bypass") : "miningtycoon.anticheat.bypass",
                exclusionsSec != null ? exclusionsSec.getStringList("excluded-gamemodes") : Arrays.asList("creative", "spectator"),
                exclusionsSec != null ? exclusionsSec.getStringList("allowed-effects") : Arrays.asList("speed", "slowness", "jump_boost", "levitation"),
                exclusionsSec != null ? exclusionsSec.getStringList("safe-blocks") : Arrays.asList("WATER", "LAVA", "SLIME_BLOCK", "HONEY_BLOCK", "VINE", "LADDER", "SCAFFOLDING"),
                exclusionsSec != null && exclusionsSec.getBoolean("ignore-external-velocity", true),
                exclusionsSec != null ? exclusionsSec.getDouble("external-velocity-threshold", 2.0) : 2.0
        );

        if (enabled) {
            plugin.getLogger().info("Anti-cheat movement protection enabled!");
        }
    }

    private ConfigurationSection loadSeparateAnticheatFile() {
        File anticheatFile = new File(plugin.getDataFolder(), "anticheat.yml");
        if (!anticheatFile.exists()) {
            plugin.saveResource("anticheat.yml", false);
        }
        
        try {
            YamlConfiguration anticheatConfig = YamlConfiguration.loadConfiguration(anticheatFile);
            return anticheatConfig;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load anticheat.yml: " + e.getMessage());
            return null;
        }
    }

    public boolean checkMovement(Player player, Location from, Location to, Vector velocity) {
        if (!enabled) return true;

        if (isExcluded(player)) {
            return true;
        }

        UUID uuid = player.getUniqueId();
        PlayerAntiCheatData data = playerData.computeIfAbsent(uuid, k -> new PlayerAntiCheatData());

        boolean shouldCheck = data.shouldCheck(flyConfig.checkInterval, speedConfig.checkInterval);
        if (!shouldCheck) {
            return true;
        }

        boolean isOnGround = isPlayerOnGround(player);
        double horizontalSpeed = getHorizontalSpeed(from, to);
        int airTicks = getPlayerAirTicks(player);
        boolean hasExternalVel = hasExternalVelocity(player);

        boolean speedViolation = false;
        boolean flyViolation = false;
        boolean airWalkViolation = false;

        if (speedConfig.enabled && !isOnGround && !hasExternalVel && horizontalSpeed > speedConfig.maxHorizontalSpeed) {
            data.speedViolations++;
            if (data.speedViolations > speedConfig.tolerance) {
                speedViolation = true;
                if (debug) {
                    plugin.getLogger().info("[AntiCheat] Speed violation: " + player.getName() + 
                            " speed=" + horizontalSpeed + " max=" + speedConfig.maxHorizontalSpeed);
                }
            }
        }

        if (flyConfig.enabled && !isOnGround && !hasJumpBoost(player) && !hasExternalVel && airTicks > flyConfig.maxAirTicks) {
            data.flyViolations++;
            if (data.flyViolations > flyConfig.tolerance) {
                flyViolation = true;
                if (debug) {
                    plugin.getLogger().info("[AntiCheat] Fly violation: " + player.getName() + 
                            " airTicks=" + airTicks + " max=" + flyConfig.maxAirTicks);
                }
            }
        }

        if (airWalkConfig.enabled && !isOnGround && !hasExternalVel && horizontalSpeed > 0.1 && airTicks > airWalkConfig.maxAirHorizontalTicks) {
            data.airWalkViolations++;
            if (data.airWalkViolations > airWalkConfig.tolerance) {
                airWalkViolation = true;
                if (debug) {
                    plugin.getLogger().info("[AntiCheat] AirWalk violation: " + player.getName() + 
                            " airTicks=" + airTicks + " horizontal=" + horizontalSpeed);
                }
            }
        }

        if (speedViolation || flyViolation || airWalkViolation) {
            rollbackPlayer(player, from);
            return false;
        }

        if (isOnGround) {
            data.resetViolations();
            data.setLastSafeLocation(to);
        }

        return true;
    }

    private boolean isExcluded(Player player) {
        if (player.hasPermission(exclusionConfig.bypassPermission)) {
            return true;
        }

        GameMode gamemode = player.getGameMode();
        if (exclusionConfig.excludedGamemodes.contains(gamemode.name().toLowerCase())) {
            return true;
        }

        for (PotionEffectType effect : player.getActivePotionEffects()) {
            if (exclusionConfig.allowedEffects.contains(effect.getName().toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    private boolean isPlayerOnGround(Player player) {
        Location loc = player.getLocation();
        double y = loc.getY();
        
        Block block = player.getWorld().getBlockAt(loc.getBlockX(), (int) y - 1, loc.getBlockZ());
        if (isSafeBlock(block)) return true;
        
        Block currentBlock = player.getWorld().getBlockAt(loc.getBlockX(), (int) y, loc.getBlockZ());
        if (isSafeBlock(currentBlock)) return true;
        
        return player.isOnGround();
    }

    private boolean isSafeBlock(Block block) {
        if (block == null) return false;
        Material type = block.getType();
        String name = type.name();
        return exclusionConfig.safeBlocks.contains(name);
    }

    private double getHorizontalSpeed(Location from, Location to) {
        if (from == null || to == null) return 0;
        
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        
        if (!Double.isFinite(dx) || !Double.isFinite(dz)) return 0;
        
        return Math.sqrt(dx * dx + dz * dz);
    }

    private int getPlayerAirTicks(Player player) {
        return player.getTicksLived() - player.getLastDamageTicks();
    }

    private boolean hasJumpBoost(Player player) {
        return player.getPotionEffect(PotionEffectType.JUMP) != null;
    }

    private boolean hasExternalVelocity(Player player) {
        if (!exclusionConfig.ignoreExternalVelocity) return false;
        
        Vector vel = player.getVelocity();
        double magnitude = vel.length();
        
        return magnitude > exclusionConfig.externalVelocityThreshold;
    }

    private void rollbackPlayer(Player player, Location safeLocation) {
        if (rollbackConfig.useLastSafePosition && safeLocation != null) {
            Location rollbackLoc = safeLocation.clone();
            rollbackLoc.setPitch(player.getLocation().getPitch());
            rollbackLoc.setYaw(player.getLocation().getYaw());
            rollbackLoc.add(0, rollbackConfig.verticalPush, 0);
            
            player.teleport(rollbackLoc);
        } else {
            player.setVelocity(new Vector(0, rollbackConfig.verticalPush, 0));
        }
        
        UUID uuid = player.getUniqueId();
        playerData.remove(uuid);
    }

    public void setLastSafeLocation(UUID uuid, Location location) {
        playerData.computeIfAbsent(uuid, k -> new PlayerAntiCheatData()).setLastSafeLocation(location);
    }

    public Location getLastSafeLocation(UUID uuid) {
        PlayerAntiCheatData data = playerData.get(uuid);
        return data != null ? data.getLastSafeLocation() : null;
    }

    public void removePlayer(UUID uuid) {
        playerData.remove(uuid);
    }

    public void reload() {
        loadConfig();
    }

    private static class PlayerAntiCheatData {
        private int speedViolations = 0;
        private int flyViolations = 0;
        private int airWalkViolations = 0;
        private long lastSpeedCheck = 0;
        private long lastFlyCheck = 0;
        private Location lastSafeLocation;

        boolean shouldCheck(int flyInterval, int speedInterval) {
            long now = System.currentTimeMillis();
            if (now - lastSpeedCheck > speedInterval * 50) {
                lastSpeedCheck = now;
                return true;
            }
            if (now - lastFlyCheck > flyInterval * 50) {
                lastFlyCheck = now;
                return true;
            }
            return false;
        }

        void resetViolations() {
            speedViolations = 0;
            flyViolations = 0;
            airWalkViolations = 0;
        }

        Location getLastSafeLocation() {
            return lastSafeLocation;
        }

        void setLastSafeLocation(Location lastSafeLocation) {
            this.lastSafeLocation = lastSafeLocation;
        }
    }

    private static class SpeedConfig {
        boolean enabled;
        double maxHorizontalSpeed;
        int tolerance;
        int checkInterval;

        SpeedConfig(boolean enabled, double maxHorizontalSpeed, int tolerance, int checkInterval) {
            this.enabled = enabled;
            this.maxHorizontalSpeed = maxHorizontalSpeed;
            this.tolerance = tolerance;
            this.checkInterval = checkInterval;
        }
    }

    private static class FlyConfig {
        boolean enabled;
        int maxAirTicks;
        int tolerance;
        int checkInterval;

        FlyConfig(boolean enabled, int maxAirTicks, int tolerance, int checkInterval) {
            this.enabled = enabled;
            this.maxAirTicks = maxAirTicks;
            this.tolerance = tolerance;
            this.checkInterval = checkInterval;
        }
    }

    private static class AirWalkConfig {
        boolean enabled;
        int maxAirHorizontalTicks;
        int tolerance;

        AirWalkConfig(boolean enabled, int maxAirHorizontalTicks, int tolerance) {
            this.enabled = enabled;
            this.maxAirHorizontalTicks = maxAirHorizontalTicks;
            this.tolerance = tolerance;
        }
    }

    private static class RollbackConfig {
        boolean useLastSafePosition;
        double verticalPush;

        RollbackConfig(boolean useLastSafePosition, double verticalPush) {
            this.useLastSafePosition = useLastSafePosition;
            this.verticalPush = verticalPush;
        }
    }

    private static class ExclusionConfig {
        String bypassPermission;
        List<String> excludedGamemodes;
        List<String> allowedEffects;
        List<String> safeBlocks;
        boolean ignoreExternalVelocity;
        double externalVelocityThreshold;

        ExclusionConfig(String bypassPermission, List<String> excludedGamemodes,
                       List<String> allowedEffects, List<String> safeBlocks,
                       boolean ignoreExternalVelocity, double externalVelocityThreshold) {
            this.bypassPermission = bypassPermission;
            this.excludedGamemodes = excludedGamemodes;
            this.allowedEffects = allowedEffects;
            this.safeBlocks = safeBlocks;
            this.ignoreExternalVelocity = ignoreExternalVelocity;
            this.externalVelocityThreshold = externalVelocityThreshold;
        }
    }
}