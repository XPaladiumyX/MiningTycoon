package skyxnetwork.miningTycoon.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.data.PlayerData;

import java.util.HashMap;
import java.util.Map;

public class FastTeleportCommand implements CommandExecutor {

    private final MiningTycoon plugin;
    private final Map<Integer, Location> zoneLocations = new HashMap<>();
    private final Map<Integer, Integer> zoneRequirements = new HashMap<>();

    public FastTeleportCommand(MiningTycoon plugin) {
        this.plugin = plugin;
        initializeZones();
    }

    private void initializeZones() {
        org.bukkit.World world = Bukkit.getWorld("mining_tycoon");
        zoneLocations.put(2, new Location(world, -45, 122, 7));
        zoneLocations.put(3, new Location(world, -42, 123, -7));
        zoneLocations.put(4, new Location(world, -34, 125, -22));
        zoneLocations.put(5, new Location(world, -16, 125, -28));
        zoneLocations.put(6, new Location(world, 3, 126, -31));
        zoneLocations.put(7, new Location(world, 17, 126, -31));
        zoneLocations.put(8, new Location(world, 31, 126, -15));
        zoneLocations.put(9, new Location(world, 28, 128, 5));

        zoneRequirements.putAll(plugin.getZoneManager().getAllZoneRequirements());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 2 || !args[0].equalsIgnoreCase("zone")) {
            player.sendMessage("§cUsage: /fasttp zone <zone_number>");
            return true;
        }

        int zone;
        try {
            zone = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage("§cInvalid zone number!");
            return true;
        }

        if (!zoneLocations.containsKey(zone)) {
            player.sendMessage("§cZone " + zone + " doesn't exist.");
            return true;
        }

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
        int required = zoneRequirements.getOrDefault(zone, 0);

        if (data.getLevel() < required) {
            player.sendMessage("§7[§e!§7] §cYou need to be level §6" + required + " §cto teleport to zone " + zone + "!");
            return true;
        }

        player.teleport(zoneLocations.get(zone));
        player.sendMessage("§7[§e!§7] §aTeleporting to zone " + zone + "...");

        return true;
    }
}
