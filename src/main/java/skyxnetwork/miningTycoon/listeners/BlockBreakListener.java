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
    int baseExp;
    int baseMoney = 1;
    int minLevel = 1;
    int myLevel = 1;

    Level(int level) {
        myLevel = level;
        if (level <= 1) {
            baseExp = 5;
            baseMoney = 1;
            minLevel = 1;
        } else if (level == 2) {
            baseExp = 10;
            baseMoney = 2;
            minLevel = 1;
        } else if (level == 3) {
            baseExp = 15;
            baseMoney = 4;
            minLevel = 2;
        } else {
            baseExp = 23;
            baseMoney = 8;
            minLevel = 3;

            for (int i = 4; i < level; i++) {
                baseExp = (baseExp * 5) / 3;
                baseMoney = (baseMoney * 5) / 3;
                minLevel++;
            }
        }
    }

    public static Level levelLookup(int lookupLevel) {
        for (Level testLevel : BlockBreakListener.levels) {
            if (testLevel.myLevel == lookupLevel) {
                return testLevel;
            }
        }
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
        blockRewards.put(Material.STONE, new BlockReward(Level.levelLookup(1).baseExp, Level.levelLookup(1).baseMoney, Level.levelLookup(1).minLevel));

        // Ores
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

        // Extras
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
            return;
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

        // Add money
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
        if (tool == null) return 0;

        String toolId = plugin.getItemManager().getPickaxeId(tool);
        if (toolId == null) return 0;

        return isExp ? plugin.getItemManager().getPickaxeExpBonus(toolId) :
                plugin.getItemManager().getPickaxeMoneyBonus(toolId);
    }

    private double getPetBonus(Player player, boolean isExp) {
        ItemStack helmet = player.getInventory().getHelmet();
        if (helmet == null) return 0;

        String petId = plugin.getItemManager().getPetId(helmet);
        if (petId == null) return 0;

        return isExp ? plugin.getItemManager().getPetExpBonus(petId) :
                plugin.getItemManager().getPetMoneyBonus(petId);
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

    private int getLuckyMinerLevel(ItemStack tool) {
        if (tool == null) return 0;

        String toolId = plugin.getItemManager().getPickaxeId(tool);
        if (toolId == null) return 0;

        return plugin.getItemManager().getPickaxeLuckyMinerLevel(toolId);
    }

    private int getHasteLevel(ItemStack tool) {
        if (tool == null) return 0;

        String toolId = plugin.getItemManager().getPickaxeId(tool);
        if (toolId == null) return 0;

        return plugin.getItemManager().getPickaxeHasteLevel(toolId);
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