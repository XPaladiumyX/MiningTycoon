package skyxnetwork.miningTycoon.commands;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import skyxnetwork.miningTycoon.MiningTycoon;

import java.util.HashMap;
import java.util.Map;

public class GiveArmorCommand implements CommandExecutor {

    private final MiningTycoon plugin;
    private final Map<String, ArmorData> armors = new HashMap<>();

    public GiveArmorCommand(MiningTycoon plugin) {
        this.plugin = plugin;
        initializeArmors();
    }

    private void initializeArmors() {
        // Basic Armor - Miner Set
        armors.put("miner_chestguard", new ArmorData(Material.LEATHER_CHESTPLATE, 2001,
                "§7Miner's Chestguard", Color.RED, "§3+5✦ §8| §6+3⛁", "§7§lBASIC"));

        armors.put("miner_workpants", new ArmorData(Material.LEATHER_LEGGINGS, 2002,
                "§7Miner's Workpants", Color.RED, "§3+3✦ §8| §6+3⛁", "§7§lBASIC"));

        armors.put("miner_boots", new ArmorData(Material.LEATHER_BOOTS, 2003,
                "§7Miner's Boots", Color.RED, "§3+3✦ §8| §6+1⛁", "§7§lBASIC"));

        // Legendary Armor - Eternal Warden Set
        armors.put("eternal_warden_chestplate", new ArmorData(Material.LEATHER_CHESTPLATE, 2086,
                "§b§lE§1§lt§b§le§1§lr§b§ln§1§la§b§ll §1§lW§b§la§1§lr§b§ld§1§le§b§ln §f§lChestplate",
                Color.BLUE, "§3+33333✦ §8| §6+33333⛁", "§e§lLEGENDARY"));

        armors.put("eternal_warden_leggings", new ArmorData(Material.LEATHER_LEGGINGS, 2087,
                "§b§lE§1§lt§b§le§1§lr§b§ln§1§la§b§ll §1§lW§b§la§1§lr§b§ld§1§le§b§ln §f§lLeggings",
                Color.AQUA, "§3+33333✦ §8| §6+33333⛁", "§e§lLEGENDARY"));

        armors.put("eternal_warden_boots", new ArmorData(Material.LEATHER_BOOTS, 2088,
                "§b§lE§1§lt§b§le§1§lr§b§ln§1§la§b§ll §1§lW§b§la§1§lr§b§ld§1§le§b§ln §f§lBoots",
                Color.BLUE, "§3+33333✦ §8| §6+33333⛁", "§e§lLEGENDARY"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("miningtycoon.admin")) {
            sender.sendMessage("§cYou are not allowed to use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /givearmor <armor> <player>");
            return true;
        }

        String armorName = args[0].toLowerCase();
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

        if (!target.isOnline()) {
            sender.sendMessage("§cPlayer " + args[1] + " is not online!");
            return true;
        }

        Player player = (Player) target;
        ArmorData armorData = armors.get(armorName);

        if (armorData == null) {
            sender.sendMessage("§cUnknown armor: " + armorName);
            sender.sendMessage("§eAvailable armors: " + String.join(", ", armors.keySet()));
            return true;
        }

        ItemStack item = new ItemStack(armorData.material);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();

        meta.setDisplayName(armorData.name);
        meta.setLore(java.util.Arrays.asList(
                "§8Armor",
                "",
                "§d§lBonus:",
                "§8« " + armorData.bonus + " §8»",
                "",
                armorData.rarity
        ));
        meta.setColor(armorData.color);
        meta.setCustomModelData(armorData.customModelData);
        meta.setUnbreakable(true);
        meta.addEnchant(Enchantment.UNBREAKING, 255, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_DYE);

        item.setItemMeta(meta);
        player.getInventory().addItem(item);

        sender.sendMessage("§a" + armorName + " has been given to " + player.getName());
        player.sendMessage("§aYou received: " + armorData.name);

        return true;
    }

    private static class ArmorData {
        Material material;
        int customModelData;
        String name;
        Color color;
        String bonus;
        String rarity;

        ArmorData(Material material, int customModelData, String name, Color color, String bonus, String rarity) {
            this.material = material;
            this.customModelData = customModelData;
            this.name = name;
            this.color = color;
            this.bonus = bonus;
            this.rarity = rarity;
        }
    }
}
