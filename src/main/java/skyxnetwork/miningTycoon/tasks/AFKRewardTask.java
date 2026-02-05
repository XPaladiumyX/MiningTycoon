package skyxnetwork.miningTycoon.tasks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
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
                String toolId = plugin.getItemManager().getPickaxeId(tool);
                if (toolId != null) {
                    exp += plugin.getItemManager().getPickaxeExpBonus(toolId);
                    money += plugin.getItemManager().getPickaxeMoneyBonus(toolId);
                }

                // Pet bonus (helmet)
                ItemStack helmet = player.getInventory().getHelmet();
                String petId = plugin.getItemManager().getPetId(helmet);
                if (petId != null) {
                    exp += plugin.getItemManager().getPetExpBonus(petId);
                    money += plugin.getItemManager().getPetMoneyBonus(petId);
                }

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

    private double getArmorBonus(Player player, boolean isExp) {
        double total = 0;
        total += getArmorPieceBonus(player.getInventory().getChestplate(), isExp);
        total += getArmorPieceBonus(player.getInventory().getLeggings(), isExp);
        total += getArmorPieceBonus(player.getInventory().getBoots(), isExp);
        return total;
    }

    private double getArmorPieceBonus(ItemStack armor, boolean isExp) {
        if (armor == null) return 0;

        String armorId = plugin.getItemManager().getArmorId(armor);
        if (armorId == null) return 0;

        return isExp ? plugin.getItemManager().getArmorExpBonus(armorId) :
                plugin.getItemManager().getArmorMoneyBonus(armorId);
    }
}