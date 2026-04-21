package skyxnetwork.miningTycoon.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import skyxnetwork.miningTycoon.MiningTycoon;

import java.util.*;

public class ZoneManager {

    private final MiningTycoon plugin;
    private final Map<Integer, Integer> zoneRequirements;
    private final Map<Integer, String> zoneRegions;
    private final Set<Integer> loadedZones;

    public ZoneManager(MiningTycoon plugin) {
        this.plugin = plugin;
        this.zoneRequirements = new HashMap<>();
        this.zoneRegions = new HashMap<>();
        this.loadedZones = new HashSet<>();
        initializeZones();
    }

    private void initializeZones() {
        zoneRequirements.clear();
        zoneRegions.clear();
        loadedZones.clear();

        var requirementsSection = plugin.getConfig().getConfigurationSection("zones.requirements");
        if (requirementsSection != null) {
            for (String key : requirementsSection.getKeys(false)) {
                try {
                    int zone = Integer.parseInt(key);
                    int requirement = requirementsSection.getInt(key);
                    zoneRequirements.put(zone, requirement);
                    zoneRegions.put(zone, "zone_" + zone);
                    loadedZones.add(zone);
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Invalid zone key in config: " + key);
                }
            }
        }

        plugin.getLogger().info("Loaded " + zoneRequirements.size() + " zone requirements from config");
    }

    public boolean hasZoneAccess(Player player, int zone) {
        int playerLevel = plugin.getPlayerDataManager().getPlayerData(player).getLevel();
        Integer required = zoneRequirements.get(zone);
        return required == null || playerLevel >= required;
    }

    public int getZoneRequirement(int zone) {
        return zoneRequirements.getOrDefault(zone, 0);
    }

    public Map<Integer, Integer> getAllZoneRequirements() {
        return new HashMap<>(zoneRequirements);
    }

    public int getPlayerZone(Player player) {
        if (!plugin.getWorldGuardManager().isWorldGuardEnabled()) {
            return getZoneByLegacyCheck(player);
        }

        Location loc = player.getLocation();
        List<String> regions = plugin.getWorldGuardManager().getRegionsAtLocation(loc);

        Integer zoneNumber = getZoneNumberFromRegions(regions, player);
        if (zoneNumber != null) {
            return zoneNumber;
        }

        return getZoneByLegacyCheck(player);
    }

    private int getZoneByLegacyCheck(Player player) {
        int level = plugin.getPlayerDataManager().getPlayerData(player).getLevel();

        if (level >= 2058) return 40;
        if (level >= 2040) return 39;
        if (level >= 2023) return 38;
        if (level >= 1822) return 37;
        if (level >= 1729) return 36;
        if (level >= 1558) return 35;
        if (level >= 1480) return 34;
        if (level >= 1395) return 33;
        if (level >= 1320) return 32;
        if (level >= 1250) return 31;
        if (level >= 1185) return 30;
        if (level >= 1125) return 29;
        if (level >= 1080) return 28;
        if (level >= 1010) return 27;
        if (level >= 943) return 26;
        if (level >= 880) return 25;
        if (level >= 821) return 24;
        if (level >= 766) return 23;
        if (level >= 715) return 22;
        if (level >= 668) return 21;
        if (level >= 625) return 20;
        if (level >= 563) return 19;
        if (level >= 510) return 18;
        if (level >= 460) return 17;
        if (level >= 413) return 16;
        if (level >= 369) return 15;
        if (level >= 328) return 14;
        if (level >= 290) return 13;
        if (level >= 255) return 12;
        if (level >= 232) return 11;
        if (level >= 188) return 10;
        if (level >= 148) return 9;
        if (level >= 113) return 8;
        if (level >= 82) return 7;
        if (level >= 55) return 6;
        if (level >= 33) return 5;
        if (level >= 15) return 4;
        if (level >= 5) return 3;
        if (level >= 1) return 2;
        return 1;
    }

    private Integer getZoneNumberFromRegions(List<String> regions, Player player) {
        Integer highestZone = null;
        int highestZoneNumber = -1;

        for (String region : regions) {
            if (region.startsWith("zone_")) {
                try {
                    int zoneNum = Integer.parseInt(region.substring(5));
                    if (zoneNum > highestZoneNumber && zoneNum >= 1 && zoneNum <= 40) {
                        highestZone = zoneNum;
                        highestZoneNumber = zoneNum;
                    }
                } catch (NumberFormatException e) {
                    // Not a zone region
                }
            }
        }

        return highestZone;
    }

    public void pushPlayerBack(Player player) {
        Location loc = player.getLocation();
        Vector direction = loc.getDirection().multiply(-1).setY(0.1);
        player.setVelocity(direction);
    }

    public String getZoneMessage(int zone) {
        int requirement = getZoneRequirement(zone);
        return "§7[§e!§7] §cYou must be level §6" + requirement + " §cto enter this zone!";
    }

    public Set<Integer> getLoadedZones() {
        return new HashSet<>(loadedZones);
    }

    public String getRegionName(int zone) {
        return zoneRegions.get(zone);
    }

    public int getZoneCount() {
        return zoneRequirements.size();
    }

    public void reload() {
        initializeZones();
    }
}