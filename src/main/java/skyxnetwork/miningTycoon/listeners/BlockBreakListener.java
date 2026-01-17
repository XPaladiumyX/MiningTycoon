package skyxnetwork.miningTycoon.listeners;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.data.PlayerData;
import skyxnetwork.miningTycoon.utils.ActionBarUtil;
import skyxnetwork.miningTycoon.utils.NumberFormatter;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class BlockBreakListener implements Listener {

    private final MiningTycoon plugin;
    private final Random random = new Random();
    private final Map<Material, BlockReward> blockRewards = new HashMap<>();

    public BlockBreakListener(MiningTycoon plugin) {
        this.plugin = plugin;
        initializeBlockRewards();
    }

    private void initializeBlockRewards() {
        blockRewards.put(Material.STONE, new BlockReward(5, 1, 1));
        blockRewards.put(Material.COAL_ORE, new BlockReward(10, 2, 1));
        blockRewards.put(Material.IRON_ORE, new BlockReward(15, 4, 2));
        blockRewards.put(Material.RAW_IRON_BLOCK, new BlockReward(23, 8, 3));
        blockRewards.put(Material.COPPER_ORE, new BlockReward(38, 13, 4));
        blockRewards.put(Material.RAW_COPPER_BLOCK, new BlockReward(63, 21, 5));
        blockRewards.put(Material.GOLD_ORE, new BlockReward(105, 35, 6));
        blockRewards.put(Material.RAW_GOLD_BLOCK, new BlockReward(175, 58, 7));
        blockRewards.put(Material.LAPIS_ORE, new BlockReward(291, 96, 8));
        blockRewards.put(Material.DIAMOND_ORE, new BlockReward(485, 160, 9));
        blockRewards.put(Material.EMERALD_ORE, new BlockReward(808, 266, 10));
        blockRewards.put(Material.AMETHYST_BLOCK, new BlockReward(1346, 443, 11));
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

        // Staff mode check
        if (data.getPlayerMode().equals("staff")) {
            return;
        }

        // Check if valid block
        BlockReward reward = blockRewards.get(blockType);
        if (reward == null) {
            return;
        }

        // Zone requirement check
        if (data.getLevel() < reward.getMinLevel()) {
            player.sendMessage("§7[§e!§7] §cYou must be level §6" + reward.getMinLevel() + " §cto mine this block.");
            event.setCancelled(true);
            return;
        }

        event.setDropItems(false);

        // Calculate rewards
        ItemStack tool = player.getInventory().getItemInMainHand();
        double totalExp = reward.getBaseExp() + getToolBonus(tool, true);
        double totalMoney = reward.getBaseMoney() + getToolBonus(tool, false);

        // Add pet bonuses
        totalExp += getPetBonus(player, true);
        totalMoney += getPetBonus(player, false);

        // Add armor bonuses
        totalExp += getArmorBonus(player, true);
        totalMoney += getArmorBonus(player, false);

        // Lucky miner enchant
        int luckyLevel = getLuckyMinerLevel(tool);
        boolean luckyDrop = false;
        if (luckyLevel > 0) {
            int chance = luckyLevel == 1 ? 15 : luckyLevel == 2 ? 35 : 50;
            if (random.nextInt(100) < chance) {
                luckyDrop = true;
                totalExp *= 2;
                totalMoney *= 2;
            }
        }

        // Apply global boosts
        if (plugin.getBoostManager().isBoostActive()) {
            String boostType = plugin.getBoostManager().getBoostType();
            if (boostType.equals("exp") || boostType.equals("both")) {
                totalExp *= plugin.getBoostManager().getExpMultiplier();
            }
            if (boostType.equals("coins") || boostType.equals("both")) {
                totalMoney *= plugin.getBoostManager().getCoinsMultiplier();
            }
        }

        // Add rewards
        data.addExperience(totalExp);
        // Add money (using economy plugin)
        org.bukkit.Bukkit.dispatchCommand(org.bukkit.Bukkit.getConsoleSender(),
                "coins give " + player.getName() + " " + (int) totalMoney);

        // Lucky drop message
        if (luckyDrop && data.isDropMessagesEnabled()) {
            player.sendMessage("§e[DROP] §3+" + NumberFormatter.format(totalExp / 2) + "✦ §6+" +
                    NumberFormatter.format(totalMoney / 2) + "⛁ §7(/droptoggle)");
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
        }

        // Haste effect
        int hasteLevel = getHasteLevel(tool);
        if (hasteLevel > 0) {
            int chance = hasteLevel == 1 ? 15 : hasteLevel == 2 ? 35 : 50;
            if (random.nextInt(100) < chance) {
                int duration = hasteLevel == 2 ? 600 : 200;
                int amplifier = hasteLevel == 3 ? 1 : 0;
                player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                        org.bukkit.potion.PotionEffectType.HASTE, duration, amplifier, false, false));
            }
        }

        // Update action bar
        ActionBarUtil.sendActionBar(player, totalExp, totalMoney, data);

        // Check level up
        plugin.getPlayerDataManager().checkLevelUp(player);
    }

    private double getToolBonus(ItemStack tool, boolean isExp) {
        if (tool == null || !tool.hasItemMeta()) return 0;
        int cmd = tool.getItemMeta().getCustomModelData();

        Map<Integer, double[]> toolBonuses = new HashMap<>();
        toolBonuses.put(1234, new double[]{3, 1});     // Stone Pickaxe
        toolBonuses.put(1236, new double[]{7, 3});     // Reinforced
        toolBonuses.put(1237, new double[]{15, 8});    // Rockshredder
        toolBonuses.put(1238, new double[]{40, 23});   // Stone Crusher
        toolBonuses.put(1239, new double[]{92, 53});   // Iron
        toolBonuses.put(1240, new double[]{128, 86});  // Tempered Edge
        toolBonuses.put(1241, new double[]{376, 191}); // Ore Splitter
        toolBonuses.put(1242, new double[]{720, 346}); // Iron Storm
        toolBonuses.put(1243, new double[]{1656, 742}); // Diamond
        toolBonuses.put(1244, new double[]{3704, 1506}); // Crystal Cutter
        toolBonuses.put(1245, new double[]{7159, 3458}); // Shardpiercer
        toolBonuses.put(1246, new double[]{12406, 5846}); // GemReaper
        toolBonuses.put(1247, new double[]{19750, 14987}); // AetherPick

        double[] bonuses = toolBonuses.get(cmd);
        if (bonuses != null) {
            return isExp ? bonuses[0] : bonuses[1];
        }
        return 0;
    }

    private double getPetBonus(Player player, boolean isExp) {
        ItemStack helmet = player.getInventory().getHelmet();
        if (helmet == null || helmet.getType() != Material.PLAYER_HEAD) return 0;

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
        if (armor == null || !armor.hasItemMeta()) return 0;
        int cmd = armor.getItemMeta().getCustomModelData();

        Map<Integer, double[]> armorBonuses = new HashMap<>();
        armorBonuses.put(2001, new double[]{5, 3});   // Miner Chestguard
        armorBonuses.put(2002, new double[]{3, 3});   // Miner Workpants
        armorBonuses.put(2003, new double[]{3, 1});   // Miner Boots
        armorBonuses.put(2086, new double[]{33333, 33333}); // Eternal Warden Chest
        armorBonuses.put(2087, new double[]{33333, 33333}); // Eternal Warden Legs
        armorBonuses.put(2088, new double[]{33333, 33333}); // Eternal Warden Boots

        double[] bonuses = armorBonuses.get(cmd);
        if (bonuses != null) {
            return isExp ? bonuses[0] : bonuses[1];
        }
        return 0;
    }

    private int getLuckyMinerLevel(ItemStack tool) {
        if (tool == null || !tool.hasItemMeta() || !tool.getItemMeta().hasLore()) return 0;
        for (String line : tool.getItemMeta().getLore()) {
            if (line.contains("Lucky Miner I")) return 1;
            if (line.contains("Lucky Miner II")) return 2;
            if (line.contains("Lucky Miner III")) return 3;
        }
        return 0;
    }

    private int getHasteLevel(ItemStack tool) {
        if (tool == null || !tool.hasItemMeta() || !tool.getItemMeta().hasLore()) return 0;
        for (String line : tool.getItemMeta().getLore()) {
            if (line.contains("Haste I")) return 1;
            if (line.contains("Haste II")) return 2;
            if (line.contains("Haste III")) return 3;
        }
        return 0;
    }

    private static class BlockReward {
        private final double baseExp;
        private final double baseMoney;
        private final int minLevel;

        public BlockReward(double baseExp, double baseMoney, int minLevel) {
            this.baseExp = baseExp;
            this.baseMoney = baseMoney;
            this.minLevel = minLevel;
        }

        public double getBaseExp() {
            return baseExp;
        }

        public double getBaseMoney() {
            return baseMoney;
        }

        public int getMinLevel() {
            return minLevel;
        }
    }
}
