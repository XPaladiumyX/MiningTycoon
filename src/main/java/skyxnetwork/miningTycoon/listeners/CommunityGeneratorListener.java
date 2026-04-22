package skyxnetwork.miningTycoon.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.config.CommunityGeneratorConfig;
import skyxnetwork.miningTycoon.models.CommunityGeneratorZone;
import skyxnetwork.miningTycoon.models.CommunityReward;
import skyxnetwork.miningTycoon.data.PlayerData;
import skyxnetwork.miningTycoon.utils.ActionBarUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class CommunityGeneratorListener implements Listener {

    private final MiningTycoon plugin;
    private final CommunityGeneratorConfig config;
    private final Map<UUID, Map<String, Long>> cooldowns;

    public CommunityGeneratorListener(MiningTycoon plugin, CommunityGeneratorConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.cooldowns = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!config.isEnabled()) {
            return;
        }

        Block block = event.getBlock();
        Material blockType = block.getType();
        CommunityGeneratorZone zone = config.getZoneByMaterial(blockType);

        if (zone == null) {
            return;
        }

        Player player = event.getPlayer();

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (data != null && data.getPlayerMode().equals("staff")) {
            return;
        }

        int zoneNumber = getZoneNumber(zone.getZoneName());
        String regionName = "zone" + zoneNumber + "_mid";

        if (!plugin.getWorldGuardManager().isInRegion(player, regionName)) {
            return;
        }

        int effectiveCooldown = getEffectiveCooldown(player, zone);
        double reductionPercent = getCooldownReduction(player);

        event.setCancelled(true);

        if (checkCooldown(player.getUniqueId(), zone.getZoneName(), effectiveCooldown)) {
            long remaining = getRemainingCooldown(player.getUniqueId(), zone.getZoneName(), effectiveCooldown);
            ActionBarUtil.sendActionBar(player, ChatColor.RED + "Wait " + remaining + "s... " + ChatColor.GRAY + "(-" + (int)(reductionPercent * 100) + "% cooldown reduction)");
            return;
        }

        ActionBarUtil.sendActionBar(player, ChatColor.LIGHT_PURPLE + "⚒ " + ChatColor.GRAY + "Mining the " + ChatColor.RED + "Community Generator" + ChatColor.GRAY + "...");

        updateCooldown(player.getUniqueId(), zone.getZoneName());

        List<CommunityReward> rewards = zone.getRewards();
        List<String> receivedRewards = new java.util.ArrayList<>();

        boolean atLeastOneReward = false;
        for (CommunityReward reward : rewards) {
            if (rollReward(reward.getChance())) {
                atLeastOneReward = true;
                switch (reward.getType()) {
                    case MONEY:
                        int moneyAmount = reward.getRandomAmount();
                        plugin.getEconomyManager().giveMoney(player, moneyAmount);
                        receivedRewards.add(ChatColor.GOLD + "+" + moneyAmount + " coins");
                        break;

                    case EXP:
                        int expAmount = reward.getRandomAmount();
                        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
                        if (data != null) {
                            data.addExperience(expAmount);
                        }
                        receivedRewards.add(ChatColor.AQUA + "+" + expAmount + " XP");
                        break;

                    case ZENTIUM:
                        int zentiumAmount = reward.getAmount();
                        giveZentium(player, zentiumAmount);
                        receivedRewards.add(ChatColor.LIGHT_PURPLE + "+" + zentiumAmount + " Zentium");
                        break;

                    case COMMAND:
                        executeCommands(player, reward.getCommands());
                        break;
                }
            }
        }

        if (receivedRewards.isEmpty()) {
            player.sendMessage(ChatColor.GRAY + "You didn't receive anything this time. Try again!");
        } else {
            String message = ChatColor.GREEN + "=== Community Generator ===" + "\n";
            message += String.join("\n", receivedRewards);
            player.sendMessage(message);
        }
    }

    private int getZoneNumber(String zoneName) {
        try {
            return Integer.parseInt(zoneName.replace("zone", ""));
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private boolean rollReward(double chance) {
        return ThreadLocalRandom.current().nextDouble(100.0) < chance;
    }

    private void giveZentium(Player player, int amount) {
        String command = "zentium give " + player.getName() + " " + amount;
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    private void executeCommands(Player player, List<String> commands) {
        for (String command : commands) {
            String executed = command.replace("%player%", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), executed);
        }
    }

    private void updateCooldown(UUID playerId, String zoneName) {
        cooldowns.computeIfAbsent(playerId, k -> new HashMap<>()).put(zoneName, System.currentTimeMillis());
    }

    private double getCooldownReduction(Player player) {
        ItemStack tool = player.getInventory().getItemInMainHand();
        int tempoLevel = plugin.getItemManager().getPickaxeCooldownReductionLevel(tool);
        if (tempoLevel == 0) {
            return 0.0;
        }
        return plugin.getItemManager().getCooldownReductionPercent(tempoLevel);
    }

    private int getEffectiveCooldown(Player player, CommunityGeneratorZone zone) {
        int baseCooldown = zone.getCooldown();
        double reduction = getCooldownReduction(player);
        int reducedCooldown = (int) (baseCooldown * (1 - reduction));
        return Math.max(1, reducedCooldown);
    }

    private boolean checkCooldown(UUID playerId, String zoneName, int effectiveCooldown) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        if (playerCooldowns == null) {
            return false;
        }

        Long lastUse = playerCooldowns.get(zoneName);
        if (lastUse == null) {
            return false;
        }

        return System.currentTimeMillis() - lastUse < effectiveCooldown * 1000L;
    }

    private long getRemainingCooldown(UUID playerId, String zoneName, int effectiveCooldown) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        if (playerCooldowns == null) {
            return 0;
        }

        Long lastUse = playerCooldowns.get(zoneName);
        if (lastUse == null) {
            return 0;
        }

        long remaining = (effectiveCooldown * 1000L) - (System.currentTimeMillis() - lastUse);
        return Math.max(0, remaining / 1000L);
    }
}