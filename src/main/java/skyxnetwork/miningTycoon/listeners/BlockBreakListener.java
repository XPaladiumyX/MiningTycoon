package skyxnetwork.miningTycoon.listeners;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.data.PlayerData;
import skyxnetwork.miningTycoon.managers.MineManager;
import skyxnetwork.miningTycoon.utils.ActionBarUtil;
import skyxnetwork.miningTycoon.utils.NumberFormatter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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

        boolean hasGodPick = hasGodPickFromConfig(player.getInventory().getItemInMainHand());
        boolean godPickDrop = false;
        if (hasGodPick) {
            godPickDrop = true;
            totalExp *= 3;
            totalMoney *= 3;
        }

        int veinMinerLevel = getVeinMinerLevel(player.getInventory().getItemInMainHand());
        if (veinMinerLevel > 0) {
            int chance = getVeinMinerChance(veinMinerLevel);
            if (random.nextInt(100) < chance) {
                Set<Block> nearbyBlocks = getNearbyBlocks(event.getBlock(), player);
                if (!nearbyBlocks.isEmpty()) {
                    // Store original block data BEFORE hiding blocks using BlockData for full state
                    Map<Block, org.bukkit.block.BlockData> originalBlockData = new HashMap<>();
                    for (Block block : nearbyBlocks) {
                        originalBlockData.put(block, block.getBlockData());
                    }
                    // Now hide blocks and start respawn process
                    sendFakeBlockBreak(player, nearbyBlocks);
                    processVeinMinerBlocks(nearbyBlocks, originalBlockData, player, totalExp, totalMoney);
                }
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

        if (godPickDrop && data.isDropMessagesEnabled()) {
            player.sendMessage("§c[GOD] §3+" + NumberFormatter.format(totalExp / 3) + "✦ §6+" +
                    NumberFormatter.format(totalMoney / 3) + "⛁ §7(§cTriple§7)");
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.5f);
        }

        int hasteLevel = getHasteLevel(player.getInventory().getItemInMainHand());
        int hasteDuration = getHasteDuration(player.getInventory().getItemInMainHand());
        if (hasteLevel > 0) {
            int chance = hasteLevel == 1 ? 15 : hasteLevel == 2 ? 35 : 50;
            if (random.nextInt(100) < chance) {
                int duration = hasteDuration > 0 ? hasteDuration * 20 : (hasteLevel == 2 ? 600 : 200);
                int amplifier = hasteLevel == 3 ? 1 : 0;
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.HASTE, duration, amplifier, false, false));
            }
        }

        ActionBarUtil.sendActionBar(player, totalExp, totalMoney, data);

        plugin.getPlayerDataManager().checkLevelUp(player);
    }

    private void sendFakeBlockBreak(Player player, Set<Block> blocks) {
        try {
            org.bukkit.block.BlockData airData = Material.AIR.createBlockData();
            for (Block block : blocks) {
                player.sendBlockChange(block.getLocation(), airData);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send fake block break: " + e.getMessage());
        }
    }

    private void sendBlockRespawn(Player player, Block block) {
        try {
            player.sendBlockChange(block.getLocation(), block.getBlockData());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to send block respawn: " + e.getMessage());
        }
    }

    private void processVeinMinerBlocks(Set<Block> blocks, Map<Block, org.bukkit.block.BlockData> originalBlockData, Player player, double baseExp, double baseMoney) {
        new BukkitRunnable() {
            int index = 0;
            @Override
            public void run() {
                if (index >= blocks.size()) {
                    // Final cleanup - respawn all remaining blocks
                    for (Block block : blocks) {
                        org.bukkit.block.BlockData bd = originalBlockData.get(block);
                        if (bd != null && bd.getMaterial() != Material.AIR) {
                            player.sendBlockChange(block.getLocation(), bd);
                        }
                    }
                    cancel();
                    return;
                }

                Block block = (Block) blocks.toArray()[index];
                
                org.bukkit.block.BlockData bd = originalBlockData.get(block);
                
                // Respawn block with original data using BlockData
                if (bd != null && bd.getMaterial() != Material.AIR) {
                    player.sendBlockChange(block.getLocation(), bd);
                    
                    // Give rewards for valid blocks
                    MineManager.BlockRewardConfig reward = plugin.getMineManager().getBlockReward(bd.getMaterial());
                    if (reward != null) {
                        double exp = reward.getExp();
                        double money = reward.getMoney();

                        exp += getToolBonus(player.getInventory().getItemInMainHand(), true);
                        money += getToolBonus(player.getInventory().getItemInMainHand(), false);

                        exp += getPetBonus(player, true);
                        money += getPetBonus(player, false);

                        exp += getArmorBonus(player, true);
                        money += getArmorBonus(player, false);

                        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
                        data.addExperience(exp);

                        if (plugin.getEconomyManager().isEnabled()) {
                            plugin.getEconomyManager().giveMoney(player, money);
                        }
                    }
                }
                
                index++;
            }
        }.runTaskTimer(plugin, 2L, 2L);
    }

    private Set<Block> getNearbyBlocks(Block centerBlock, Player player) {
        Set<Block> blocks = new HashSet<>();
        
        int cx = centerBlock.getX();
        int cy = centerBlock.getY();
        int cz = centerBlock.getZ();
        
        for (int x = cx - 1; x <= cx + 1; x++) {
            for (int y = cy - 1; y <= cy + 1; y++) {
                for (int z = cz - 1; z <= cz + 1; z++) {
                    if (x == cx && y == cy && z == cz) continue;
                    
                    Block block = centerBlock.getWorld().getBlockAt(x, y, z);
                    if (block.getType() == Material.AIR) continue;
                    
                    MineManager.BlockRewardConfig reward = plugin.getMineManager().getBlockReward(block.getType());
                    if (reward != null) {
                        int requiredZone = reward.getZone();
                        boolean isDefaultBlock = plugin.getMineManager().isDefaultBlock(block.getType());
                        if (isDefaultBlock || plugin.getZoneManager().hasZoneAccess(player, requiredZone)) {
                            blocks.add(block);
                        }
                    }
                }
            }
        }
        
        return blocks;
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

    private int getHasteDuration(ItemStack tool) {
        if (tool == null) return 0;

        String toolId = plugin.getItemManager().getPickaxeId(tool);
        if (toolId == null) return 0;

        return plugin.getItemManager().getPickaxeHasteDuration(toolId);
    }

    private int getVeinMinerLevel(ItemStack tool) {
        if (tool == null) return 0;
        
        String toolId = plugin.getItemManager().getPickaxeId(tool);
        if (toolId != null) {
            int configLevel = plugin.getItemManager().getPickaxeVeinMinerLevel(toolId);
            if (configLevel > 0) {
                return configLevel;
            }
        }
        
        return plugin.getItemManager().getPickaxeVeinMinerLevelFromItem(tool);
    }

    private boolean hasGodPickFromConfig(ItemStack tool) {
        if (tool == null) return false;
        
        String toolId = plugin.getItemManager().getPickaxeId(tool);
        if (toolId != null) {
            int configLevel = plugin.getItemManager().getPickaxeGodPickLevel(toolId);
            if (configLevel > 0) {
                return true;
            }
        }
        
        return plugin.getItemManager().hasGodPickEnchant(tool);
    }

    private int getVeinMinerChance(int level) {
        switch (level) {
            case 1: return 10;
            case 2: return 25;
            case 3: return 50;
            case 4: return 65;
            case 5: return 80;
            case 6: return 100;
            default: return 0;
        }
    }
}