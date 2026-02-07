package skyxnetwork.miningTycoon.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.data.PlayerData;
import skyxnetwork.miningTycoon.managers.PrestigePortalManager;
import skyxnetwork.miningTycoon.utils.ItemBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * GUI for prestige portal confirmation
 */
public class PrestigePortalGUI implements Listener {

    private final MiningTycoon plugin;
    private final Map<UUID, String> pendingPrestige = new HashMap<>();

    public PrestigePortalGUI(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    /**
     * Open the initial prestige confirmation GUI
     */
    public void openPrestigeGUI(Player player, PrestigePortalManager.PrestigePortal portal) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

        Inventory inv = Bukkit.createInventory(null, 27, "§d§l⚡ Prestige Portal ⚡");

        // Portal info
        inv.setItem(4, new ItemBuilder(Material.NETHER_STAR)
                .setName("§d§lPrestige Portal")
                .setLore(
                        "§7Type: §e" + portal.getType().toUpperCase(),
                        "§7Required Level: §6" + portal.getLevelRequirement(),
                        "",
                        "§7Your Level: §6" + data.getLevel(),
                        "§7Your Prestige: §d" + data.getPrestige()
                )
                .build());

        // Get rewards from config
        ConfigurationSection rewards = plugin.getConfig()
                .getConfigurationSection("prestige." + portal.getType() + ".rewards");

        if (rewards != null) {
            // Diamond reward
            if (rewards.contains("diamonds")) {
                inv.setItem(10, new ItemBuilder(Material.DIAMOND)
                        .setName("§b§l◆ Diamond Reward")
                        .setLore(
                                "§7You will receive:",
                                "§f  " + rewards.getInt("diamonds") + " Diamond(s)"
                        )
                        .build());
            }

            // Coins reward
            if (rewards.contains("coins")) {
                inv.setItem(12, new ItemBuilder(Material.GOLD_INGOT)
                        .setName("§6§l⛁ Coins Reward")
                        .setLore(
                                "§7You will receive:",
                                "§f  " + rewards.getInt("coins") + " Coins"
                        )
                        .build());
            }

            // Zentium reward
            if (rewards.contains("zentium")) {
                inv.setItem(14, new ItemBuilder(Material.AMETHYST_SHARD)
                        .setName("§d§l✦ Zentium Reward")
                        .setLore(
                                "§7You will receive:",
                                "§f  " + rewards.getInt("zentium") + " Zentium"
                        )
                        .build());
            }
        }

        // Warning
        inv.setItem(16, new ItemBuilder(Material.BARRIER)
                .setName("§c§l⚠ WARNING")
                .setLore(
                        "§7You will be reset to:",
                        "§f  • §6Level 1",
                        "§f  • §30 EXP",
                        "",
                        "§7You will keep:",
                        "§f  • §dAll your items",
                        "§f  • §dAll your stats"
                )
                .build());

        // Accept button
        inv.setItem(21, new ItemBuilder(Material.LIME_TERRACOTTA)
                .setName("§a§l✔ YES, I WANT TO PRESTIGE")
                .setLore(
                        "§7Click to continue",
                        "§7to final confirmation"
                )
                .build());

        // Decline button
        inv.setItem(23, new ItemBuilder(Material.RED_TERRACOTTA)
                .setName("§c§l✖ NO, CANCEL")
                .setLore(
                        "§7Click to cancel",
                        "§7and exit the portal"
                )
                .build());

        // Store pending prestige
        pendingPrestige.put(player.getUniqueId(), portal.getType());

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);
    }

    /**
     * Open the final confirmation GUI
     */
    private void openFinalConfirmation(Player player, String portalType) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

        Inventory inv = Bukkit.createInventory(null, 27, "§c§l⚠ FINAL CONFIRMATION ⚠");

        // Warning icon
        inv.setItem(4, new ItemBuilder(Material.TNT)
                .setName("§c§l⚠ FINAL WARNING ⚠")
                .setLore(
                        "§7This action is §c§lIRREVERSIBLE§7!",
                        "",
                        "§7You are about to:",
                        "§f  • §6Reset to Level 1",
                        "§f  • §3Lose all your EXP",
                        "§f  • §dGain +1 Prestige",
                        "",
                        "§7Current Stats:",
                        "§f  Level: §6" + data.getLevel(),
                        "§f  Prestige: §d" + data.getPrestige(),
                        "",
                        "§7After Prestige:",
                        "§f  Level: §61",
                        "§f  Prestige: §d" + (data.getPrestige() + 1)
                )
                .build());

        // Confirm button
        inv.setItem(11, new ItemBuilder(Material.LIME_WOOL)
                .setName("§a§l✔ CONFIRM PRESTIGE")
                .setLore(
                        "§7Click to §a§lCONFIRM",
                        "§7and execute prestige"
                )
                .build());

        // Cancel button
        inv.setItem(15, new ItemBuilder(Material.RED_WOOL)
                .setName("§c§l✖ CANCEL")
                .setLore(
                        "§7Click to §c§lCANCEL",
                        "§7and keep your progress"
                )
                .build());

        player.openInventory(inv);
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 1.0f);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        String title = event.getView().getTitle();

        // First confirmation GUI
        if (title.equals("§d§l⚡ Prestige Portal ⚡")) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null) return;

            switch (event.getSlot()) {
                case 21: // YES button
                    String portalType = pendingPrestige.get(player.getUniqueId());
                    if (portalType != null) {
                        player.closeInventory();
                        openFinalConfirmation(player, portalType);
                    }
                    break;

                case 23: // NO button
                    player.closeInventory();
                    pendingPrestige.remove(player.getUniqueId());
                    player.sendMessage("§7Prestige cancelled.");
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                    break;
            }
        }

        // Final confirmation GUI
        else if (title.equals("§c§l⚠ FINAL CONFIRMATION ⚠")) {
            event.setCancelled(true);

            if (event.getCurrentItem() == null) return;

            String portalType = pendingPrestige.get(player.getUniqueId());
            if (portalType == null) {
                player.closeInventory();
                return;
            }

            switch (event.getSlot()) {
                case 11: // CONFIRM button
                    player.closeInventory();
                    pendingPrestige.remove(player.getUniqueId());

                    // Execute prestige
                    plugin.getPrestigePortalManager().executePrestige(player, portalType);
                    break;

                case 15: // CANCEL button
                    player.closeInventory();
                    pendingPrestige.remove(player.getUniqueId());
                    player.sendMessage("§7Prestige cancelled.");
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                    break;
            }
        }
    }

    /**
     * Clear pending prestige for a player
     */
    public void clearPending(UUID uuid) {
        pendingPrestige.remove(uuid);
    }

    /**
     * Check if player has pending prestige
     */
    public boolean hasPending(UUID uuid) {
        return pendingPrestige.containsKey(uuid);
    }
}