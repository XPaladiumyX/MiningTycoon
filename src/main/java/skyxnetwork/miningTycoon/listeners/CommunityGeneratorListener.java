package skyxnetwork.miningTycoon.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.config.CommunityGeneratorConfig;
import skyxnetwork.miningTycoon.models.CommunityGeneratorZone;
import skyxnetwork.miningTycoon.models.CommunityReward;

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

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!config.isEnabled()) {
            return;
        }

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        Material blockType = block.getType();
        CommunityGeneratorZone zone = config.getZoneByMaterial(blockType);

        if (zone == null) {
            return;
        }

        Player player = event.getPlayer();
        String zoneName = zone.getZoneName();
        String regionName = "community_generator_" + zoneName;

        if (!plugin.getWorldGuardManager().isInRegion(player, regionName)) {
            return;
        }

        if (checkCooldown(player.getUniqueId(), zoneName, zone.getCooldown())) {
            long remaining = getRemainingCooldown(player.getUniqueId(), zoneName);
            player.sendMessage(ChatColor.RED + "You must wait " + remaining + " seconds before using this generator again.");
            return;
        }

        updateCooldown(player.getUniqueId(), zoneName);

        List<CommunityReward> rewards = zone.getRewards();
        List<String> receivedRewards = new java.util.ArrayList<>();

        for (CommunityReward reward : rewards) {
            if (rollReward(reward.getChance())) {
                switch (reward.getType()) {
                    case MONEY:
                        int moneyAmount = reward.getRandomAmount();
                        plugin.getEconomyManager().giveMoney(player, moneyAmount);
                        receivedRewards.add(ChatColor.GOLD + "+" + moneyAmount + " coins");
                        break;

                    case EXP:
                        int expAmount = reward.getRandomAmount();
                        player.giveExp(expAmount);
                        receivedRewards.add(ChatColor.AQUA + "+" + expAmount + " XP");
                        break;

                    case ZENTIUM:
                        int zentiumAmount = reward.getAmount();
                        giveZentium(player, zentiumAmount);
                        receivedRewards.add(ChatColor.LIGHT_PURPLE + "+" + zentiumAmount + " Zentium");
                        break;

                    case COMMAND:
                        executeCommands(player, reward.getCommands());
                        receivedRewards.add(ChatColor.YELLOW + "Command executed!");
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

        event.setCancelled(true);
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

    private boolean checkCooldown(UUID playerId, String zoneName, int cooldownSeconds) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        if (playerCooldowns == null) {
            return false;
        }

        Long lastUse = playerCooldowns.get(zoneName);
        if (lastUse == null) {
            return false;
        }

        return System.currentTimeMillis() - lastUse < cooldownSeconds * 1000L;
    }

    private long getRemainingCooldown(UUID playerId, String zoneName) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        if (playerCooldowns == null) {
            return 0;
        }

        Long lastUse = playerCooldowns.get(zoneName);
        if (lastUse == null) {
            return 0;
        }

        CommunityGeneratorZone zone = config.getZoneByName(zoneName);
        if (zone == null) {
            return 0;
        }

        long remaining = (zone.getCooldown() * 1000L) - (System.currentTimeMillis() - lastUse);
        return Math.max(0, remaining / 1000L);
    }

    private void updateCooldown(UUID playerId, String zoneName) {
        cooldowns.computeIfAbsent(playerId, k -> new HashMap<>()).put(zoneName, System.currentTimeMillis());
    }
}
