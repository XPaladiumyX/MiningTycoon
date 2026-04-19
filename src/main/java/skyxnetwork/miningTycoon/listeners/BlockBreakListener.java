package skyxnetwork.miningTycoon.listeners;

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
import skyxnetwork.miningTycoon.managers.MineManager;
import skyxnetwork.miningTycoon.utils.ActionBarUtil;
import skyxnetwork.miningTycoon.utils.NumberFormatter;

import java.util.Random;

public class BlockBreakListener implements Listener {

    private final MiningTycoon plugin;
    private final Random random = new Random();

    public BlockBreakListener(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

        if (data.getPlayerMode().equals("staff")) {
            return;
        }

        MineManager.BlockRewardConfig reward = plugin.getMineManager().getBlockReward(blockType);
        if (reward == null) {
            return;
        }

        int requiredZone = reward.getZone();
        boolean isDefaultBlock = plugin.getMineManager().isDefaultBlock(blockType);

        if (!isDefaultBlock) {
            if (!plugin.getZoneManager().hasZoneAccess(player, requiredZone)) {
                int requiredLevel = plugin.getZoneManager().getZoneRequirement(requiredZone);
                player.sendMessage("§7[§e!§7] §cYou must be level §6" + requiredLevel + " §cto mine this block!");
                plugin.getZoneManager().pushPlayerBack(player);
                event.setCancelled(true);
                return;
            }
        }

        event.setCancelled(true);

        double totalExp = reward.getExp();
        double totalMoney = reward.getMoney();

        totalExp += getToolBonus(player.getInventory().getItemInMainHand(), true);
        totalMoney += getToolBonus(player.getInventory().getItemInMainHand(), false);

        totalExp += getPetBonus(player, true);
        totalMoney += getPetBonus(player, false);

        totalExp += getArmorBonus(player, true);
        totalMoney += getArmorBonus(player, false);

        int luckyLevel = getLuckyMinerLevel(player.getInventory().getItemInMainHand());
        boolean luckyDrop = false;
        if (luckyLevel > 0) {
            int chance = luckyLevel == 1 ? 15 : luckyLevel == 2 ? 35 : 50;
            if (random.nextInt(100) < chance) {
                luckyDrop = true;
                totalExp *= 2;
                totalMoney *= 2;
            }
        }

        if (plugin.getBoostManager().isBoostActive()) {
            String boostType = plugin.getBoostManager().getBoostType();
            if (boostType.equals("exp") || boostType.equals("both")) {
                totalExp *= plugin.getBoostManager().getExpMultiplier();
            }
            if (boostType.equals("coins") || boostType.equals("both")) {
                totalMoney *= plugin.getBoostManager().getCoinsMultiplier();
            }
        }

        totalExp *= data.getTotalExpMultiplier();

        data.addExperience(totalExp);

        if (plugin.getEconomyManager().isEnabled()) {
            boolean moneyGiven = plugin.getEconomyManager().giveMoney(player, totalMoney);
            if (!moneyGiven) {
                plugin.getLogger().warning("Failed to give " + totalMoney + " coins to " + player.getName());
            }
        }

        if (luckyDrop && data.isDropMessagesEnabled()) {
            player.sendMessage("§e[DROP] §3+" + NumberFormatter.format(totalExp / 2) + "✦ §6+" +
                    NumberFormatter.format(totalMoney / 2) + "⛁ §7(/droptoggle)");
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
        }

        int hasteLevel = getHasteLevel(player.getInventory().getItemInMainHand());
        if (hasteLevel > 0) {
            int chance = hasteLevel == 1 ? 15 : hasteLevel == 2 ? 35 : 50;
            if (random.nextInt(100) < chance) {
                int duration = hasteLevel == 2 ? 600 : 200;
                int amplifier = hasteLevel == 3 ? 1 : 0;
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.HASTE, duration, amplifier, false, false));
            }
        }

        ActionBarUtil.sendActionBar(player, totalExp, totalMoney, data);

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
}