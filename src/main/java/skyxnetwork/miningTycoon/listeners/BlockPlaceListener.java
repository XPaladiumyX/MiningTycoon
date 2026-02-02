package skyxnetwork.miningTycoon.listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import skyxnetwork.miningTycoon.MiningTycoon;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class BlockPlaceListener implements Listener {

    private final MiningTycoon plugin;
    //private final Set<Material> protectedBlocks;

    public BlockPlaceListener(MiningTycoon plugin) {
        this.plugin = plugin;

    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();

        // Check if block is protected
        if (!BlockBreakListener.blockRewards.containsKey(blockType)) {
            // Allow only if player has bypass permission or is OP in creative
            if (!player.hasPermission("antiblock.bypass") &&
                    !(player.isOp() && player.getGameMode() == GameMode.CREATIVE)) {
                event.setCancelled(true);
                player.sendMessage("§cYou do not have permission to break this block!");
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        // Only allow block placement if player has permission or is OP in creative
        if (!player.hasPermission("antiblock.bypass") &&
                !(player.isOp() && player.getGameMode() == GameMode.CREATIVE)) {
            event.setCancelled(true);
            player.sendMessage("§cYou do not have permission to place blocks!");
        }
    }
}
