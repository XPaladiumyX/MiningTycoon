package skyxnetwork.miningTycoon.managers;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import skyxnetwork.miningTycoon.MiningTycoon;

import java.util.ArrayList;
import java.util.List;

public class WorldGuardManager {

    private final MiningTycoon plugin;
    private boolean worldGuardEnabled = false;

    public WorldGuardManager(MiningTycoon plugin) {
        this.plugin = plugin;

        try {
            Class.forName("com.sk89q.worldguard.WorldGuard");
            worldGuardEnabled = true;
            plugin.getLogger().info("WorldGuard integration enabled!");
        } catch (ClassNotFoundException e) {
            plugin.getLogger().warning("WorldGuard not found! Zone restrictions will use fallback system.");
        }
    }

    public List<String> getRegionsAtLocation(Location location) {
        List<String> regionNames = new ArrayList<>();

        if (!worldGuardEnabled) {
            return regionNames;
        }

        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(location));

            for (ProtectedRegion region : set) {
                regionNames.add(region.getId());
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting regions at location: " + e.getMessage());
        }

        return regionNames;
    }

    public boolean isInRegion(Player player, String regionName) {
        if (!worldGuardEnabled) {
            return false;
        }

        return getRegionsAtLocation(player.getLocation()).contains(regionName);
    }

    public boolean isWorldGuardEnabled() {
        return worldGuardEnabled;
    }

    // Get zone number from region name
    public Integer getZoneNumberFromRegions(List<String> regions) {
        for (String region : regions) {
            if (region.startsWith("zone_")) {
                try {
                    return Integer.parseInt(region.substring(5));
                } catch (NumberFormatException e) {
                    // Ignore invalid zone names
                }
            }
        }
        return null;
    }
}
