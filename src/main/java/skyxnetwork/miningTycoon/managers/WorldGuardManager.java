package skyxnetwork.miningTycoon.managers;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import skyxnetwork.miningTycoon.MiningTycoon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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

    public boolean regionExists(String regionName) {
        if (!worldGuardEnabled) {
            return false;
        }

        try {
            String worldName = plugin.getConfig().getString("settings.world-name", "mining_tycoon");
            World world = plugin.getServer().getWorld(worldName);
            if (world == null) return false;

            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            Location testLoc = new Location(world, 0, 64, 0);
            ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(testLoc));
            
            for (ProtectedRegion region : set) {
                if (region.getId().equalsIgnoreCase(regionName)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public Set<String> getAllRegionNames() {
        if (!worldGuardEnabled) {
            return java.util.Collections.emptySet();
        }

        try {
            String worldName = plugin.getConfig().getString("settings.world-name", "mining_tycoon");
            World world = plugin.getServer().getWorld(worldName);
            if (world == null) return java.util.Collections.emptySet();

            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            
            // Test center of each potential zone (just test 40 positions)
            java.util.Set<String> regionNames = new java.util.HashSet<>();
            for (int i = 1; i <= 80; i++) {
                // Common zone coordinates - adjust as need
                int baseX = (i - 1) * 20;
                Location loc = new Location(world, baseX, 64, 0);
                ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(loc));
                for (ProtectedRegion region : set) {
                    regionNames.add(region.getId());
                }
            }
            
            return regionNames;
        } catch (Exception e) {
            return java.util.Collections.emptySet();
        }
    }
    
    // Simple check - just return true if WG is enabled
    public boolean hasAnyRegion() {
        return worldGuardEnabled;
    }
}
