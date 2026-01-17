package skyxnetwork.miningTycoon.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.data.PlayerData;

public class PrestigeManager {

    private final MiningTycoon plugin;

    public PrestigeManager(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    public boolean canPrestige(Player player, String zone) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

        if (zone.equals("basic")) {
            return data.getLevel() >= 120;
        } else if (zone.equals("elite")) {
            return data.getLevel() >= 150;
        }
        return false;
    }

    public void performPrestige(Player player, String zone) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

        // Reset level and exp
        data.resetForPrestige();

        // Add prestige
        data.addPrestige(1);

        // Give rewards
        player.getInventory().addItem(new ItemStack(Material.DIAMOND, 1));
        // Add 10000 coins (assuming economy plugin integration)
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "coins give " + player.getName() + " 10000");

        if (zone.equals("basic")) {
            // Give Zentium for basic prestige
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "zentium give " + player.getName() + " 1");
        }

        // Broadcast
        int prestigeLevel = data.getPrestige();
        Bukkit.broadcastMessage("§8[§d⚡ Prestige §8] §6" + player.getName() + " §ehas reached §dPrestige " + prestigeLevel + "§e!");
        player.sendMessage("§aCongratulations! You are now Prestige §d" + prestigeLevel + "§a!");
    }

    public String getPrestigeDisplay(int prestige) {
        if (prestige == 0) return "§7[-]";

        String value = prestige >= 1000 ? (Math.round(prestige / 10.0) / 100.0) + "k" : String.valueOf(prestige);

        if (prestige < 10) return "§7[" + value + "✾]";
        if (prestige < 50) return "§a[" + value + "✾]";
        if (prestige < 100) return "§b[" + value + "✾]";
        if (prestige < 200) return "§d[" + value + "✾]";
        if (prestige < 300) return "§6[" + value + "✾]";
        if (prestige < 500) return "§c[" + value + "✾]";
        if (prestige < 800) return "§e[" + value + "✾]";
        if (prestige < 1000) return "§5[" + value + "✾]";
        if (prestige < 1300) return "§9[" + value + "✾]";
        if (prestige < 1500) return "§d[" + value + "✾]";
        return "§d✴[" + value + "✾]§d✴";
    }
}