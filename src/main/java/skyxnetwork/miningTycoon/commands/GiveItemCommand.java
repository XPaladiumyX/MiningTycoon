package skyxnetwork.miningTycoon.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.utils.ItemBuilder;

import java.util.HashMap;
import java.util.Map;

public class GiveItemCommand implements CommandExecutor {

    private final MiningTycoon plugin;
    private final Map<String, ToolData> tools = new HashMap<>();

    public GiveItemCommand(MiningTycoon plugin) {
        this.plugin = plugin;
        initializeTools();
    }

    private void initializeTools() {
        // Basic Tools
        tools.put("stone_pickaxe", new ToolData(Material.STONE_PICKAXE, 1234, "§7Stone Pickaxe",
                new String[]{"§8Pickaxe", "", "§d§lBonus:", "§8« §3+3✦ §8| §6+1⛁ §8»", "", "§a§lCOMMON"}, 0));

        tools.put("wooden_pickaxe", new ToolData(Material.WOODEN_PICKAXE, 1235, "§6Starter Pickaxe",
                new String[]{"§8Pickaxe", "", "§7§lBASIC"}, 0));

        // Common Tools
        tools.put("reinforced_pickaxe", new ToolData(Material.STONE_PICKAXE, 1236, "§7Reinforced Pickaxe",
                new String[]{"§8Pickaxe", "", "§d§lBonus:", "§8« §3+7✦ §8| §6+3⛁ §8»", "",
                        "§1Efficiency I", "§7Increases §6Mining Speed §7by §a20%%", "", "§a§lCOMMON"}, 1));

        tools.put("rockshredder_pickaxe", new ToolData(Material.STONE_PICKAXE, 1237, "§7Rockshredder",
                new String[]{"§8Pickaxe", "", "§d§lBonus:", "§8« §3+15✦ §8| §6+8⛁ §8»", "",
                        "§1Efficiency II", "§7Increases §6Mining Speed §7by §a50%%", "", "§a§lCOMMON"}, 2));

        // Rare Tools
        tools.put("stone_crusher", new ToolData(Material.STONE_PICKAXE, 1238, "§eStone Crusher",
                new String[]{"§8Pickaxe", "", "§d§lBonus:", "§8« §3+40✦ §8| §6+23⛁ §8»", "",
                        "§1Efficiency III", "§7Increases §6Mining Speed §7by §a100%%", "",
                        "§eLucky Miner I", "§a15%% §7Chance to earn double XP & money while mining", "", "§e§lRARE"}, 3));

        tools.put("iron_pickaxe", new ToolData(Material.IRON_PICKAXE, 1239, "§7Iron Pickaxe",
                new String[]{"§8Pickaxe", "", "§d§lBonus:", "§8« §3+92✦ §8| §6+53⛁ §8»", "",
                        "§eLucky Miner II", "§a35%% §7Chance to earn double XP & money while mining", "", "§e§lRARE"}, 0));

        // Epic Tools
        tools.put("diamond_pickaxe", new ToolData(Material.DIAMOND_PICKAXE, 1243, "§bDiamond Pickaxe",
                new String[]{"§8Pickaxe", "", "§d§lBonus:", "§8« §3+1656✦ §8| §6+742⛁ §8»", "",
                        "§1Efficiency I", "§7Increases §6Mining Speed §7by §a20%%", "",
                        "§bHaste III", "§a50%% §7Chance to apply §eHaste 2 (10s) §7while mining", "",
                        "§eLucky Miner III", "§a50%% §7Chance to earn double XP & money while mining", "", "§9§lEPIC"}, 1));

        tools.put("aetherpick", new ToolData(Material.DIAMOND_PICKAXE, 1247, "§f§lAetherPick",
                new String[]{"§8Pickaxe", "", "§d§lBonus:", "§8« §3+19,750✦ §8| §6+14,987⛁ §8»", "",
                        "§1Efficiency IV", "§7Increases §6Mining Speed §7by §a150%%", "",
                        "§bHaste III", "§a50%% §7Chance to apply §eHaste 2 (10s) §7while mining", "",
                        "§eLucky Miner III", "§a50%% §7Chance to earn double XP & money while mining", "", "§9§lEPIC"}, 4));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("miningtycoon.admin")) {
            sender.sendMessage("§cYou are not allowed to use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /giveitem <item> <player>");
            return true;
        }

        String itemName = args[0].toLowerCase();
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

        if (!target.isOnline()) {
            sender.sendMessage("§cPlayer " + args[1] + " is not online!");
            return true;
        }

        Player player = (Player) target;
        ToolData toolData = tools.get(itemName);

        if (toolData == null) {
            sender.sendMessage("§cUnknown item: " + itemName);
            sender.sendMessage("§eAvailable items: " + String.join(", ", tools.keySet()));
            return true;
        }

        ItemBuilder builder = new ItemBuilder(toolData.material)
                .setName(toolData.name)
                .setLore(toolData.lore)
                .setCustomModelData(toolData.customModelData)
                .setUnbreakable(true)
                .addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);

        if (toolData.efficiencyLevel > 0) {
            builder.addEnchant(Enchantment.EFFICIENCY, toolData.efficiencyLevel);
        }

        builder.addEnchant(Enchantment.UNBREAKING, 255);

        player.getInventory().addItem(builder.build());
        sender.sendMessage("§a" + itemName + " has been given to " + player.getName());
        player.sendMessage("§aYou received: " + toolData.name);

        return true;
    }

    private static class ToolData {
        Material material;
        int customModelData;
        String name;
        String[] lore;
        int efficiencyLevel;

        ToolData(Material material, int customModelData, String name, String[] lore, int efficiencyLevel) {
            this.material = material;
            this.customModelData = customModelData;
            this.name = name;
            this.lore = lore;
            this.efficiencyLevel = efficiencyLevel;
        }
    }
}
