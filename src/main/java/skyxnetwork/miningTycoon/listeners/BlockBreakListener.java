package skyxnetwork.miningTycoon.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.data.PlayerData;
import skyxnetwork.miningTycoon.utils.ActionBarUtil;
import skyxnetwork.miningTycoon.utils.NumberFormatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

class Level {
    static double growFactor = 5.0/3.0;
    int baseExp;
    int baseMoney = 1;
    int minLevel = 1;
    int myLevel = 1;
    Level(int level){
        myLevel = level;
        if (level <= 1){
            baseExp = 5;
            baseMoney = 1;
            minLevel = 1;
        } else if (level == 2){
            baseExp = 10;
            baseMoney = 2;
            minLevel = 1;
        } else if (level == 3){
            baseExp = 15;
            baseMoney = 4;
            minLevel = 2;
        } else{
            baseExp = 23;
            baseMoney = 8;
            minLevel = 3;

            for (int i = 4; i < level; i++) {
                baseExp = (int) (baseExp * growFactor);
                baseMoney = (int) (baseMoney * growFactor);
                minLevel++;
            }

        }
    }

    public static Level levelLookup(int lookupLevel){
        for (Level testLevel : BlockBreakListener.levels){
            if (testLevel.myLevel == lookupLevel){
                return testLevel;
            }
        }
        //BlockBreakListener.plugin.getLogger().warning("Level " + lookupLevel + " not found");
        return null;
    }
}

public class BlockBreakListener implements Listener {

    private final MiningTycoon plugin;
    private final Random random = new Random();
    public static final Map<Material, BlockReward> blockRewards = new HashMap<>();
    static ArrayList<Level> levels = new ArrayList<>();

    static {
        for (int i = 1; i <= 100; i++) {
            Level newLevel = new Level(i);
            levels.add(newLevel);
        }
    }

    public BlockBreakListener(MiningTycoon plugin) {
        this.plugin = plugin;
        initializeBlockRewards();
    }


    private void initializeBlockRewards() {
        /*blockRewards.put(Material.STONE, new BlockReward(5, 1, 1));
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
        blockRewards.put(Material.AMETHYST_BLOCK, new BlockReward(1346, 443, 11));*/

        blockRewards.put(Material.STONE, new BlockReward(Level.levelLookup(1).baseExp, Level.levelLookup(1).baseMoney, Level.levelLookup(1).minLevel));



        //  Ores
        blockRewards.put(Material.COAL_ORE, new BlockReward(Level.levelLookup(2).baseExp, Level.levelLookup(2).baseMoney, Level.levelLookup(2).minLevel));
        blockRewards.put(Material.IRON_ORE, new BlockReward(Level.levelLookup(3).baseExp, Level.levelLookup(3).baseMoney, Level.levelLookup(3).minLevel));
        blockRewards.put(Material.RAW_IRON_BLOCK, new BlockReward(Level.levelLookup(4).baseExp, Level.levelLookup(4).baseMoney, Level.levelLookup(4).minLevel));
        blockRewards.put(Material.COPPER_ORE, new BlockReward(Level.levelLookup(5).baseExp, Level.levelLookup(5).baseMoney, Level.levelLookup(5).minLevel));
        blockRewards.put(Material.RAW_COPPER_BLOCK, new BlockReward(Level.levelLookup(6).baseExp, Level.levelLookup(6).baseMoney, Level.levelLookup(6).minLevel));
        blockRewards.put(Material.GOLD_ORE, new BlockReward(Level.levelLookup(7).baseExp, Level.levelLookup(7).baseMoney, Level.levelLookup(7).minLevel));
        blockRewards.put(Material.RAW_GOLD_BLOCK, new BlockReward(Level.levelLookup(8).baseExp, Level.levelLookup(8).baseMoney, Level.levelLookup(8).minLevel));
        blockRewards.put(Material.LAPIS_ORE, new BlockReward(Level.levelLookup(9).baseExp, Level.levelLookup(9).baseMoney, Level.levelLookup(9).minLevel));
        blockRewards.put(Material.DIAMOND_ORE, new BlockReward(Level.levelLookup(10).baseExp, Level.levelLookup(10).baseMoney, Level.levelLookup(10).minLevel));
        blockRewards.put(Material.EMERALD_ORE, new BlockReward(Level.levelLookup(11).baseExp, Level.levelLookup(11).baseMoney, Level.levelLookup(11).minLevel));
        blockRewards.put(Material.AMETHYST_BLOCK, new BlockReward(Level.levelLookup(12).baseExp, Level.levelLookup(12).baseMoney, Level.levelLookup(12).minLevel));
        blockRewards.put(Material.DEEPSLATE_COAL_ORE, new BlockReward(Level.levelLookup(13).baseExp, Level.levelLookup(13).baseMoney, Level.levelLookup(13).minLevel));
        blockRewards.put(Material.DEEPSLATE_IRON_ORE, new BlockReward(Level.levelLookup(14).baseExp, Level.levelLookup(14).baseMoney, Level.levelLookup(14).minLevel));
        blockRewards.put(Material.DEEPSLATE_COPPER_ORE, new BlockReward(Level.levelLookup(15).baseExp, Level.levelLookup(15).baseMoney, Level.levelLookup(15).minLevel));
        blockRewards.put(Material.DEEPSLATE_GOLD_ORE, new BlockReward(Level.levelLookup(16).baseExp, Level.levelLookup(16).baseMoney, Level.levelLookup(16).minLevel));
        blockRewards.put(Material.DEEPSLATE_LAPIS_ORE, new BlockReward(Level.levelLookup(17).baseExp, Level.levelLookup(17).baseMoney, Level.levelLookup(17).minLevel));
        blockRewards.put(Material.DEEPSLATE_DIAMOND_ORE, new BlockReward(Level.levelLookup(18).baseExp, Level.levelLookup(18).baseMoney, Level.levelLookup(18).minLevel));


        //  Extras
        blockRewards.put(Material.STONE, new BlockReward(Level.levelLookup(1).baseExp, Level.levelLookup(1).baseMoney, Level.levelLookup(1).minLevel));
        blockRewards.put(Material.STONE_SLAB, new BlockReward(Level.levelLookup(1).baseExp, Level.levelLookup(1).baseMoney, Level.levelLookup(1).minLevel));
        blockRewards.put(Material.STONE_STAIRS, new BlockReward(Level.levelLookup(1).baseExp, Level.levelLookup(1).baseMoney, Level.levelLookup(1).minLevel));
        blockRewards.put(Material.COBBLESTONE, new BlockReward(Level.levelLookup(2).baseExp, Level.levelLookup(2).baseMoney, Level.levelLookup(2).minLevel));
        blockRewards.put(Material.COBBLESTONE_STAIRS, new BlockReward(Level.levelLookup(2).baseExp, Level.levelLookup(2).baseMoney, Level.levelLookup(2).minLevel));
        blockRewards.put(Material.CALCITE, new BlockReward(Level.levelLookup(4).baseExp, Level.levelLookup(4).baseMoney, Level.levelLookup(4).minLevel));
        blockRewards.put(Material.DEEPSLATE, new BlockReward(Level.levelLookup(10).baseExp, Level.levelLookup(10).baseMoney, Level.levelLookup(10).minLevel));
        blockRewards.put(Material.COBBLED_DEEPSLATE, new BlockReward(Level.levelLookup(10).baseExp, Level.levelLookup(10).baseMoney, Level.levelLookup(10).minLevel));
        blockRewards.put(Material.COBBLED_DEEPSLATE_SLAB, new BlockReward(Level.levelLookup(10).baseExp, Level.levelLookup(10).baseMoney, Level.levelLookup(10).minLevel));
        blockRewards.put(Material.COBBLED_DEEPSLATE_STAIRS, new BlockReward(Level.levelLookup(10).baseExp, Level.levelLookup(10).baseMoney, Level.levelLookup(10).minLevel));


    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

        // Staff mode check - no rewards
        if (data.getPlayerMode().equals("staff")) {
            return;
        }

        // Check if valid mineable block
        BlockReward reward = blockRewards.get(blockType);
        if (reward == null) {
            return; // Not a reward block, let it break normally
        }

        // Zone requirement check
        if (data.getLevel() < reward.getMinLevel()) {
            player.sendMessage("§7[§e!§7] §cYou must be level §6" + reward.getMinLevel() + " §cto mine this block.");
            event.setCancelled(true);
            return;
        }

        // CANCEL THE EVENT - Make blocks unbreakable
        event.setCancelled(true);

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

        // Add money (using economy plugin or internal system)
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
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
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.HASTE, duration, amplifier, false, false));
            }
        }

        // Update action bar
        ActionBarUtil.sendActionBar(player, totalExp, totalMoney, data);

        // Check level up
        plugin.getPlayerDataManager().checkLevelUp(player);
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

    private double getPetBonus(Player player, boolean isExp) {
        ItemStack helmet = player.getInventory().getHelmet();
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

    private int getLuckyMinerLevel(ItemStack tool) {
        if (tool == null || !tool.hasItemMeta() || !tool.getItemMeta().hasLore()) {
            return 0;
        }

        for (String line : tool.getItemMeta().getLore()) {
            if (line.contains("Lucky Miner III")) return 3;
            if (line.contains("Lucky Miner II")) return 2;
            if (line.contains("Lucky Miner I")) return 1;
        }
        return 0;
    }

    private int getHasteLevel(ItemStack tool) {
        if (tool == null || !tool.hasItemMeta() || !tool.getItemMeta().hasLore()) {
            return 0;
        }

        for (String line : tool.getItemMeta().getLore()) {
            if (line.contains("Haste III")) return 3;
            if (line.contains("Haste II")) return 2;
            if (line.contains("Haste I")) return 1;
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