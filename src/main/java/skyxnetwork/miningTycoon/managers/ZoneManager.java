package skyxnetwork.miningTycoon.managers;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import skyxnetwork.miningTycoon.MiningTycoon;

import java.util.HashMap;
import java.util.Map;

public class ZoneManager {

    private final MiningTycoon plugin;
    private final Map<Integer, Integer> zoneRequirements;
    private final Map<Integer, Location> zoneLocations;

    public ZoneManager(MiningTycoon plugin) {
        this.plugin = plugin;
        this.zoneRequirements = new HashMap<>();
        this.zoneLocations = new HashMap<>();
        initializeZones();
    }

    private void initializeZones() {
        // Zone level requirements
        zoneRequirements.put(2, 5);
        zoneRequirements.put(3, 10);
        zoneRequirements.put(4, 26);
        zoneRequirements.put(5, 42);
        zoneRequirements.put(6, 58);
        zoneRequirements.put(7, 74);
        zoneRequirements.put(8, 90);
        zoneRequirements.put(9, 100);
        zoneRequirements.put(10, 150);
        zoneRequirements.put(11, 212);
        zoneRequirements.put(12, 289);
        zoneRequirements.put(13, 325);
        zoneRequirements.put(14, 395);
        zoneRequirements.put(15, 420);
        zoneRequirements.put(16, 447);
        zoneRequirements.put(17, 486);
        zoneRequirements.put(18, 500);
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
}
