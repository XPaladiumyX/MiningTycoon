package skyxnetwork.miningTycoon.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.managers.BoostManager;

public class BoostItemListener implements Listener {

    private final MiningTycoon plugin;

    public BoostItemListener(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBoostItemClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null) return;

        BoostManager.BoostItemType type = BoostManager.getBoostItemType(item);
        if (type == null) return;

        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        event.setCancelled(true);

        if (plugin.getBoostManager().isBoostActive()) {
            int remaining = plugin.getBoostManager().getTimeRemaining();
            player.sendMessage(ChatColor.RED + "⚠ A Global Boost is already active!");
            player.sendMessage(ChatColor.GRAY + "Wait " + remaining + " seconds before activating another boost.");
            return;
        }

        item.setAmount(item.getAmount() - 1);
        plugin.getBoostManager().startGlobalBoost(type.getId(), player);
        player.sendMessage(ChatColor.GREEN + "✓ You activated a Global " + type.getName() + ChatColor.GREEN + "!");
    }
}