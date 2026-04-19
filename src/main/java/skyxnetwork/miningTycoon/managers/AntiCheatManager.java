package skyxnetwork.miningTycoon.managers;

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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AntiCheatManager {

    private final MiningTycoon plugin;
    private boolean enabled;
    private boolean debug;

    private SpeedConfig speedConfig;
    private FlyConfig flyConfig;
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
                speedSec != null ? speedSec.getDouble("max-horizontal-speed", 0.8) : 0.8,
                speedSec != null ? speedSec.getInt("tolerance", 20) : 20,
                speedSec != null ? speedSec.getInt("check-interval", 2) : 2
        );

        var flySec = anticheat.getConfigurationSection("fly");
        flyConfig = new FlyConfig(
                flySec != null && flySec.getBoolean("enabled", true),
                flySec != null ? flySec.getInt("max-air-ticks", 40) : 40,
                flySec != null ? flySec.getInt("tolerance", 20) : 20,
                flySec != null ? flySec.getInt("check-interval", 2) : 2
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
                exclusionsSec != null ? exclusionsSec.getStringList("allowed-effects") : Arrays.asList("speed", "slowness", "jump_boost", "levitation", "slow_falling"),
                exclusionsSec != null ? exclusionsSec.getStringList("safe-blocks") : Arrays.asList("WATER", "LAVA", "SLIME_BLOCK", "HONEY_BLOCK", "VINE", "LADDER", "SCAFFOLDING", "SOUL_SAND", "SOIL"),
                exclusionsSec != null && exclusionsSec.getBoolean("ignore-external-velocity", true),
                exclusionsSec != null ? exclusionsSec.getDouble("external-velocity-threshold", 3.0) : 3.0
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

        boolean currentlyOnGround = player.isOnGround();
        boolean isClimbing = isOnClimbable(player);
        boolean isInWater = isInWater(player);
        boolean isOnSlime = isOnSlimeBlock(player);

        boolean wasOnGround = data.lastOnGround;
        boolean justLeftGround = wasOnGround && !currentlyOnGround;

        if (justLeftGround) {
            data.jumpStartTick = data.currentTick;
        }

        data.lastOnGround = currentlyOnGround;
        data.currentTick++;

        int consecutiveAirTicks = currentlyOnGround ? 0 : (int)(data.currentTick - data.jumpStartTick);
        double verticalVelocity = velocity.getY();
        double horizontalSpeed = getHorizontalSpeed(from, to);

        int speedLevel = getSpeedLevel(player);
        int jumpLevel = getJumpBoostLevel(player);

        double adjustedMaxSpeed = calculateMaxSpeed(speedLevel, jumpLevel, currentlyOnGround, horizontalSpeed > 0.3);
        double adjustedMaxAirTicks = calculateMaxAirTicks(jumpLevel);

        boolean hasExternalVelocity = hasExternalVelocity(player, velocity);
        boolean isGliding = player.isGliding();
        boolean isFlying = player.isFlying();

        boolean speedViolation = false;
        boolean flyViolation = false;

        if (speedConfig.enabled && !hasExternalVelocity && !isGliding && !isFlying) {
            double threshold = adjustedMaxSpeed * 1.2;
            
            if (horizontalSpeed > threshold) {
                data.speedViolations++;
                if (debug) {
                    plugin.getLogger().info("[AntiCheat] " + player.getName() + " speed=" + horizontalSpeed +
                            " max=" + threshold + " violations=" + data.speedViolations);
                }
                if (data.speedViolations > speedConfig.tolerance) {
                    speedViolation = true;
                }
            } else if (data.speedViolations > 0) {
                data.speedViolations = Math.max(0, data.speedViolations - 1);
            }
        }

        if (flyConfig.enabled && !currentlyOnGround && !hasExternalVelocity && !isGliding && !isFlying && !isClimbing && !isInWater) {
            double threshold = adjustedMaxAirTicks * 1.2;
            
            boolean suspiciousAirTicks = consecutiveAirTicks > threshold;
            
            boolean suspiciousVerticalControl = Math.abs(verticalVelocity) < 0.15 && verticalVelocity > -0.5;
            
            boolean hovering = false;
            if (!suspiciousAirTicks && verticalVelocity > -0.1 && verticalVelocity < 0.1) {
                data.hoverTicks++;
                if (data.hoverTicks > 20) {
                    hovering = true;
                }
            } else {
                data.hoverTicks = 0;
            }
            
            if (suspiciousAirTicks || suspiciousVerticalControl || hovering) {
                data.flyViolations++;
                if (debug) {
                    plugin.getLogger().info("[AntiCheat] " + player.getName() + " airTicks=" + consecutiveAirTicks +
                            " max=" + threshold + " vertical=" + verticalVelocity + " hover=" + data.hoverTicks +
                            " violations=" + data.flyViolations);
                }
                if (data.flyViolations > flyConfig.tolerance) {
                    flyViolation = true;
                }
            } else if (data.flyViolations > 0) {
                data.flyViolations = Math.max(0, data.flyViolations - 1);
            }
        }

        if (speedViolation || flyViolation) {
            if (debug) {
                plugin.getLogger().info("[AntiCheat] ROLLBACK: " + player.getName() +
                        " speedViol=" + speedViolation + " flyViol=" + flyViolation);
            }
            
            Location safeLoc = data.getLastSafeLocation();
            if (safeLoc == null) {
                safeLoc = from;
            }
            rollbackPlayer(player, safeLoc);
            
            data.speedViolations = 0;
            data.flyViolations = 0;
            data.rollbackCooldown = 20;
            
            return false;
        }

        if (currentlyOnGround || isClimbing || isInWater) {
            data.speedViolations = 0;
            data.flyViolations = 0;
            data.setLastSafeLocation(to);
        } else if (data.getLastSafeLocation() == null) {
            data.setLastSafeLocation(from);
        }

        return true;
    }

    private double calculateMaxSpeed(int speedLevel, int jumpLevel, boolean onGround, boolean isMoving) {
        double baseSpeed = 0.28;

        double speedMultiplier = 1.0 + (speedLevel * 0.20);
        double jumpBoostBonus = jumpLevel * 0.05;

        double maxSpeed = baseSpeed * speedMultiplier;

        if (!onGround && isMoving) {
            maxSpeed *= 1.3;
        }

        maxSpeed += jumpBoostBonus;

        maxSpeed *= 1.5;

        return maxSpeed;
    }

    private int calculateMaxAirTicks(int jumpLevel) {
        int baseTicks = 25;
        return baseTicks + (jumpLevel * 8);
    }

    private int getSpeedLevel(Player player) {
        org.bukkit.potion.PotionEffect speedEffect = player.getPotionEffect(PotionEffectType.SPEED);
        return speedEffect != null ? speedEffect.getAmplifier() + 1 : 0;
    }

    private int getJumpBoostLevel(Player player) {
        org.bukkit.potion.PotionEffect jumpEffect = player.getPotionEffect(PotionEffectType.JUMP_BOOST);
        return jumpEffect != null ? jumpEffect.getAmplifier() + 1 : 0;
    }

    private boolean isOnClimbable(Player player) {
        Location loc = player.getLocation();
        Block block = player.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        Material type = block.getType();
        return type == Material.LADDER || type == Material.VINE || type == Material.SCAFFOLDING;
    }

    private boolean isInWater(Player player) {
        Location loc = player.getLocation();
        Block block = player.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        Material type = block.getType();
        return type == Material.WATER || type == Material.LAVA;
    }

    private boolean isOnSlimeBlock(Player player) {
        Location loc = player.getLocation();
        Block block = player.getWorld().getBlockAt(loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());
        if (block == null) return false;
        Material type = block.getType();
        return type == Material.SLIME_BLOCK || type == Material.HONEY_BLOCK;
    }

    private boolean isExcluded(Player player) {
        if (player.hasPermission(exclusionConfig.bypassPermission)) {
            return true;
        }

        GameMode gamemode = player.getGameMode();
        if (exclusionConfig.excludedGamemodes.contains(gamemode.name().toLowerCase())) {
            return true;
        }

        if (player.isFlying()) {
            return true;
        }

        if (player.getVehicle() != null) {
            return true;
        }

        for (org.bukkit.potion.PotionEffect effect : player.getActivePotionEffects()) {
            if (exclusionConfig.allowedEffects.contains(effect.getType().getName().toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    private double getHorizontalSpeed(Location from, Location to) {
        if (from == null || to == null) return 0;

        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();

        if (!Double.isFinite(dx) || !Double.isFinite(dz)) return 0;

        double distance = Math.sqrt(dx * dx + dz * dz);

        if (distance < 0.001 || distance > 20) {
            return 0;
        }

        return distance;
    }

    private boolean hasExternalVelocity(Player player, Vector velocity) {
        if (!exclusionConfig.ignoreExternalVelocity) return false;

        double magnitude = velocity.length();

        if (magnitude > exclusionConfig.externalVelocityThreshold) {
            return true;
        }

        if (Math.abs(velocity.getY()) > 0.5) {
            return true;
        }

        return false;
    }

    private void rollbackPlayer(Player player, Location safeLocation) {
        Location rollbackLoc;

        if (rollbackConfig.useLastSafePosition && safeLocation != null) {
            rollbackLoc = safeLocation.clone();
        } else {
            rollbackLoc = player.getLocation().clone();
        }

        rollbackLoc.setPitch(player.getLocation().getPitch());
        rollbackLoc.setYaw(player.getLocation().getYaw());

        if (!Double.isFinite(rollbackLoc.getX()) || !Double.isFinite(rollbackLoc.getY()) || !Double.isFinite(rollbackLoc.getZ())) {
            rollbackLoc = player.getWorld().getSpawnLocation();
        }

        player.teleport(rollbackLoc);
        player.setVelocity(new Vector(0, rollbackConfig.verticalPush, 0));

        UUID uuid = player.getUniqueId();
        playerData.remove(uuid);

        if (debug) {
            plugin.getLogger().info("[AntiCheat] Rolled back " + player.getName());
        }
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
        int speedViolations = 0;
        int flyViolations = 0;
        int hoverTicks = 0;
        long lastSpeedCheck = 0;
        long lastFlyCheck = 0;
        Location lastSafeLocation;
        boolean lastOnGround = true;
        long currentTick = 0;
        long jumpStartTick = 0;
        int rollbackCooldown = 0;

        boolean shouldCheck(int flyInterval, int speedInterval) {
            if (rollbackCooldown > 0) {
                rollbackCooldown--;
                return false;
            }
            
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