package skyxnetwork.miningTycoon.tasks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.data.PlayerData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AFKRewardTask extends BukkitRunnable {

    private final MiningTycoon plugin;
    private final Map<UUID, Float> savedYaw = new HashMap<>();
    private final Map<UUID, Float> savedPitch = new HashMap<>();

    public AFKRewardTask(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getWorld().getName().equals("mining_tycoon")) continue;

            Location loc = player.getLocation();
            int x = loc.getBlockX();
            int y = loc.getBlockY();
            int z = loc.getBlockZ();

            // Check if in AFK zone (coordinates 8-11, y=108, z=18-21)
            if (x >= 8 && x <= 11 && z >= 18 && z <= 21 && y == 108) {
                PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

                if (!data.isInAFKZone()) {
                    data.setInAFKZone(true);

                    // Save current rotation
                    savedYaw.put(player.getUniqueId(), loc.getYaw());
                    savedPitch.put(player.getUniqueId(), loc.getPitch());

                    // Teleport up while preserving rotation
                    Location newLoc = new Location(player.getWorld(), x + 0.5, 125, z + 0.5);
                    newLoc.setYaw(savedYaw.get(player.getUniqueId()));
                    newLoc.setPitch(savedPitch.get(player.getUniqueId()));
                    player.teleport(newLoc);
                }

                // Calculate rewards with all bonuses
                double exp = 1;
                double money = 1;

                // Tool bonus
                ItemStack tool = player.getInventory().getItemInMainHand();
                exp += getToolBonus(tool, true);
                money += getToolBonus(tool, false);

                // Pet bonus (helmet)
                ItemStack helmet = player.getInventory().getHelmet();
                exp += getPetBonus(helmet, true);
                money += getPetBonus(helmet, false);

                // Armor bonus
                exp += getArmorBonus(player, true);
                money += getArmorBonus(player, false);

                // Add rewards
                data.addExperience(exp);
                data.addAfkTime(1);

                // Add money via economy
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        "coins give " + player.getName() + " " + (int) money);

                // Send subtitle
                player.sendTitle("", "§f[§6+" + (int) money + "⛁§f] §f[§3+" + (int) exp + "✦§f]", 0, 20, 0);
            } else {
                PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
                if (data.isInAFKZone()) {
                    data.setInAFKZone(false);
                    savedYaw.remove(player.getUniqueId());
                    savedPitch.remove(player.getUniqueId());
                }
            }
        }
    }

    private double getToolBonus(ItemStack tool, boolean isExp) {
        if (tool == null || !tool.hasItemMeta() || !tool.getItemMeta().hasCustomModelData()) {
            return 0;
        }

        int cmd = tool.getItemMeta().getCustomModelData();
        Map<Integer, double[]> toolBonuses = new HashMap<>();
        toolBonuses.put(1234, new double[]{3, 1});
        toolBonuses.put(1236, new double[]{7, 3});
        toolBonuses.put(1237, new double[]{15, 8});
        toolBonuses.put(1238, new double[]{40, 23});
        toolBonuses.put(1239, new double[]{92, 53});
        toolBonuses.put(1240, new double[]{128, 86});
        toolBonuses.put(1241, new double[]{376, 191});
        toolBonuses.put(1242, new double[]{720, 346});
        toolBonuses.put(1243, new double[]{1656, 742});
        toolBonuses.put(1244, new double[]{3704, 1506});
        toolBonuses.put(1245, new double[]{7159, 3458});
        toolBonuses.put(1246, new double[]{12406, 5846});
        toolBonuses.put(1247, new double[]{19750, 14987});

        double[] bonuses = toolBonuses.get(cmd);
        if (bonuses != null) {
            return isExp ? bonuses[0] : bonuses[1];
        }
        return 0;
    }

    private double getPetBonus(ItemStack helmet, boolean isExp) {
        if (helmet == null || helmet.getType() != Material.PLAYER_HEAD || !helmet.hasItemMeta()) {
            return 0;
        }

        String name = helmet.getItemMeta().getDisplayName();
        Map<String, double[]> petBonuses = new HashMap<>();
        petBonuses.put("§7§lRocky Mole", new double[]{7, 2});
        petBonuses.put("§7§lStone Crab", new double[]{14, 7});
        petBonuses.put("§7§lTiny Golem", new double[]{52, 16});
        petBonuses.put("§a§lIron Snail", new double[]{2300, 1980});
        petBonuses.put("§e§lSilver Griffin", new double[]{4500, 4000});
        petBonuses.put("§5§lDrill Core", new double[]{8700, 7400});
        petBonuses.put("§c§lTitanium §6§lDragon", new double[]{15500, 12400});
        petBonuses.put("§c§lGalactic §6§lGolem", new double[]{20000, 17900});

        double[] bonuses = petBonuses.get(name);
        if (bonuses != null) {
            return isExp ? bonuses[0] : bonuses[1];
        }
        return 0;
    }

    private double getArmorBonus(Player player, boolean isExp) {
        double total = 0;
        total += getArmorPieceBonus(player.getInventory().getChestplate(), isExp);
        total += getArmorPieceBonus(player.getInventory().getLeggings(), isExp);
        total += getArmorPieceBonus(player.getInventory().getBoots(), isExp);
        return total;
    }

    private double getArmorPieceBonus(ItemStack armor, boolean isExp) {
        if (armor == null || !armor.hasItemMeta() || !armor.getItemMeta().hasCustomModelData()) {
            return 0;
        }

        int cmd = armor.getItemMeta().getCustomModelData();
        Map<Integer, double[]> armorBonuses = new HashMap<>();
        armorBonuses.put(2001, new double[]{5, 3});
        armorBonuses.put(2002, new double[]{3, 3});
        armorBonuses.put(2003, new double[]{3, 1});
        armorBonuses.put(2086, new double[]{33333, 33333});
        armorBonuses.put(2087, new double[]{33333, 33333});
        armorBonuses.put(2088, new double[]{33333, 33333});

        double[] bonuses = armorBonuses.get(cmd);
        if (bonuses != null) {
            return isExp ? bonuses[0] : bonuses[1];
        }
        return 0;
    }
}