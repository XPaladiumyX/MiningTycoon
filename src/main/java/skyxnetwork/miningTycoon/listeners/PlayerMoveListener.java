package skyxnetwork.miningTycoon.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.data.PlayerData;
import skyxnetwork.miningTycoon.managers.AreaGateManager;
import skyxnetwork.miningTycoon.managers.WorldGuardManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerMoveListener implements Listener {

    private final MiningTycoon plugin;
    private final WorldGuardManager worldGuardManager;
    private final AreaGateManager areaGateManager;
    private final Map<UUID, Long> lastPushTime = new HashMap<>();
    private final Map<UUID, Integer> pushCount = new HashMap<>();
    private final Map<UUID, String> lastWarning = new HashMap<>();
    private static final long PUSH_COOLDOWN = 100;
    private static final int MAX_PUSHES = 3;

    private final Map<UUID, Long> gateLastPushTime = new HashMap<>();
    private final Map<UUID, Integer> gatePushCount = new HashMap<>();

    public PlayerMoveListener(MiningTycoon plugin) {
        this.plugin = plugin;
        this.worldGuardManager = new WorldGuardManager(plugin);
        this.areaGateManager = plugin.getAreaGateManager();
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

        if (data.getPlayerMode().equals("staff")) {
            return;
        }

        checkZoneRestriction(player, event);
        checkAreaGateRestriction(player, event);
    }

    private void checkZoneRestriction(Player player, PlayerMoveEvent event) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        int playerLevel = data.getLevel();
        Location loc = event.getTo();

        if (data.getLastSafeLocation() == null) {
            data.setLastSafeLocation(loc);
        }

        boolean blocked = false;
        int requiredLevel = 0;
        String zoneName = "";

        if (worldGuardManager.isWorldGuardEnabled()) {
            List<String> regions = worldGuardManager.getRegionsAtLocation(loc);
            Integer zoneNumber = worldGuardManager.getZoneNumberFromRegions(regions);

            if (zoneNumber != null) {
                requiredLevel = plugin.getZoneManager().getZoneRequirement(zoneNumber);
                if (requiredLevel > 0 && playerLevel < requiredLevel) {
                    blocked = true;
                    zoneName = "zone_" + zoneNumber;
                }
            }
        } else {
            ZoneInfo zoneInfo = getZoneFromCoordinates(loc);
            if (zoneInfo != null) {
                requiredLevel = zoneInfo.requiredLevel;
                if (playerLevel < requiredLevel) {
                    blocked = true;
                    zoneName = zoneInfo.name;
                }
            }
        }

        if (blocked) {
            UUID uuid = player.getUniqueId();

            if (!lastWarning.getOrDefault(uuid, "").equals(zoneName)) {
                player.sendMessage("§7[§e!§7] §cYou must be level §6" + requiredLevel + " §cto enter this zone!");
                lastWarning.put(uuid, zoneName);
            }

            long now = System.currentTimeMillis();

            if (!lastPushTime.containsKey(uuid) || now - lastPushTime.get(uuid) > PUSH_COOLDOWN) {
                lastPushTime.put(uuid, now);

                Location safe = data.getLastSafeLocation();
                if (safe != null && isValidLocation(safe) && isValidLocation(loc)) {
                    Vector direction = safe.toVector().subtract(loc.toVector()).normalize();
                    if (isValidVector(direction)) {
                        player.setVelocity(direction.multiply(1.0).setY(0.1));
                    } else {
                        player.setVelocity(new Vector(0, 0.1, 0));
                    }
                } else {
                    player.setVelocity(new Vector(0, 0.1, 0));
                }

                int count = pushCount.getOrDefault(uuid, 0) + 1;
                pushCount.put(uuid, count);

                if (count >= MAX_PUSHES) {
                    player.sendMessage("§cYou were teleported to spawn after too many attempts in a restricted area!");
                    player.teleport(getSpawnLocation());
                    pushCount.remove(uuid);
                    lastWarning.remove(uuid);
                }
            }
        } else {
            data.setLastSafeLocation(loc);
            pushCount.remove(player.getUniqueId());
            lastWarning.remove(player.getUniqueId());
        }
    }

    private void checkAreaGateRestriction(Player player, PlayerMoveEvent event) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        Location loc = event.getTo();
        Location fromLoc = event.getFrom();

        if (!worldGuardManager.isWorldGuardEnabled()) {
            return;
        }

        if (data.getLastSafeLocation() == null) {
            data.setLastSafeLocation(fromLoc);
        }

        List<String> regions = worldGuardManager.getRegionsAtLocation(loc);
        List<String> fromRegions = worldGuardManager.getRegionsAtLocation(fromLoc);

        boolean wasInGate = false;
        for (String region : fromRegions) {
            if (region.startsWith("area_gate_")) {
                wasInGate = true;
                break;
            }
        }

        boolean isInGate = false;
        String matchingGate = null;
        for (String region : regions) {
            if (region.startsWith("area_gate_")) {
                matchingGate = region;
                isInGate = true;
                break;
            }
        }

        if (!wasInGate && isInGate) {
            data.setLastSafeLocation(fromLoc);
        }

        if (matchingGate == null) {
            return;
        }

        boolean hasAccess = areaGateManager.checkGateAccess(player, matchingGate);

        if (!hasAccess) {
            UUID uuid = player.getUniqueId();
            long pushDelay = areaGateManager.getPushDelay();
            int maxPushes = areaGateManager.getMaxPushes();

            String message = areaGateManager.getAccessDeniedMessage(player, matchingGate);
            if (message != null) {
                player.sendMessage(message);
            }

            long now = System.currentTimeMillis();

            if (!gateLastPushTime.containsKey(uuid) || now - gateLastPushTime.get(uuid) > pushDelay) {
                gateLastPushTime.put(uuid, now);

                Location safe = data.getLastSafeLocation();
                if (safe != null && isValidLocation(safe) && isValidLocation(loc)) {
                    Vector direction = safe.toVector().subtract(loc.toVector()).normalize();
                    if (isValidVector(direction)) {
                        player.setVelocity(direction.multiply(1.0).setY(0.1));
                    } else {
                        player.setVelocity(new Vector(0, 0.1, 0));
                    }
                } else {
                    player.setVelocity(new Vector(0, 0.1, 0));
                }

                int count = gatePushCount.getOrDefault(uuid, 0) + 1;
                gatePushCount.put(uuid, count);

                if (count >= maxPushes) {
                    String teleportMsg = plugin.getConfig().getString("messages.area-gate.teleport-warning", 
                            "§cYou were teleported to spawn after too many attempts in a restricted area!");
                    player.sendMessage(teleportMsg);
                    player.teleport(getSpawnLocation());
                    gatePushCount.remove(uuid);
                }
            }
        } else {
            data.setLastSafeLocation(loc);
            gatePushCount.remove(player.getUniqueId());
        }
    }

    private ZoneInfo getZoneFromCoordinates(Location loc) {
        int x = loc.getBlockX();
        int z = loc.getBlockZ();

        if (x >= -50 && x <= -40 && z >= 5 && z <= 15) {
            return new ZoneInfo("zone_2", 5);
        }

        if (x >= -45 && x <= -35 && z >= -10 && z <= -5) {
            return new ZoneInfo("zone_3", 10);
        }

        if (x >= -40 && x <= -30 && z >= -25 && z <= -20) {
            return new ZoneInfo("zone_4", 26);
        }

        return null;
    }

    private Location getSpawnLocation() {
        double x = plugin.getConfig().getDouble("settings.spawn-location.x", -36.5);
        double y = plugin.getConfig().getDouble("settings.spawn-location.y", 124);
        double z = plugin.getConfig().getDouble("settings.spawn-location.z", 16.5);
        String world = plugin.getConfig().getString("settings.world-name", "mining_tycoon");
        
        org.bukkit.World bukkitWorld = Bukkit.getWorld(world);
        if (bukkitWorld == null) {
            bukkitWorld = Bukkit.getWorld("mining_tycoon");
        }
        
        return new Location(bukkitWorld, x, y, z);
    }

    private boolean isValidLocation(Location loc) {
        if (loc == null) return false;
        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();
        return Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z) 
                && !Double.isNaN(x) && !Double.isNaN(y) && !Double.isNaN(z);
    }

    private boolean isValidVector(Vector vec) {
        if (vec == null) return false;
        double x = vec.getX();
        double y = vec.getY();
        double z = vec.getZ();
        return Double.isFinite(x) && Double.isFinite(y) && Double.isFinite(z)
                && !Double.isNaN(x) && !Double.isNaN(y) && !Double.isNaN(z)
                && vec.length() > 0 && vec.length() < 1000;
    }

    private static class ZoneInfo {
        String name;
        int requiredLevel;

        ZoneInfo(String name, int requiredLevel) {
            this.name = name;
            this.requiredLevel = requiredLevel;
        }
    }
}