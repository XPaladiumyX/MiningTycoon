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
import skyxnetwork.miningTycoon.managers.WorldGuardManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerMoveListener implements Listener {

    private final MiningTycoon plugin;
    private final WorldGuardManager worldGuardManager;
    private final Map<UUID, Long> lastPushTime = new HashMap<>();
    private final Map<UUID, Integer> pushCount = new HashMap<>();
    private final Map<UUID, String> lastWarning = new HashMap<>();
    private static final long PUSH_COOLDOWN = 100;
    private static final int MAX_PUSHES = 3;

    public PlayerMoveListener(MiningTycoon plugin) {
        this.plugin = plugin;
        this.worldGuardManager = new WorldGuardManager(plugin);
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

        // Staff mode bypass
        if (data.getPlayerMode().equals("staff")) {
            return;
        }

        checkZoneRestriction(player, event);
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

        // Try WorldGuard first
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
            // Fallback to coordinate-based system
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
                if (safe != null) {
                    Vector direction = safe.toVector().subtract(loc.toVector()).normalize();
                    player.setVelocity(direction.multiply(1.0).setY(0.1));
                }

                int count = pushCount.getOrDefault(uuid, 0) + 1;
                pushCount.put(uuid, count);

                if (count >= MAX_PUSHES) {
                    player.sendMessage("§cYou were teleported to spawn after too many attempts in a restricted area!");
                    Location spawn = new Location(Bukkit.getWorld("mining_tycoon"), -36.5, 124, 16.5);
                    player.teleport(spawn);
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

    // Fallback coordinate-based zone detection
    private ZoneInfo getZoneFromCoordinates(Location loc) {
        int x = loc.getBlockX();
        int z = loc.getBlockZ();

        // Define your zone boundaries here
        // Example zones (customize these to match your server layout):

        // Zone 2 (Level 5 required)
        if (x >= -50 && x <= -40 && z >= 5 && z <= 15) {
            return new ZoneInfo("zone_2", 5);
        }

        // Zone 3 (Level 10 required)
        if (x >= -45 && x <= -35 && z >= -10 && z <= -5) {
            return new ZoneInfo("zone_3", 10);
        }

        // Zone 4 (Level 26 required)
        if (x >= -40 && x <= -30 && z >= -25 && z <= -20) {
            return new ZoneInfo("zone_4", 26);
        }

        // Add more zones as needed...
        // Copy the pattern above for zones 5-18 with your actual coordinates

        return null;
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