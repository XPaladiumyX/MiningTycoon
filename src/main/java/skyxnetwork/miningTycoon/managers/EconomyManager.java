package skyxnetwork.miningTycoon.managers;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import skyxnetwork.miningTycoon.MiningTycoon;

/**
 * Manages economy integration with support for:
 * - Vault Economy API
 * - Custom command-based currency systems
 * - Fallback to console commands if Vault is unavailable
 */
public class EconomyManager {

    private final MiningTycoon plugin;
    private Economy vaultEconomy = null;
    private boolean vaultEnabled = false;
    private String currencyCommand;
    private EconomyType economyType;

    public enum EconomyType {
        VAULT,          // Using Vault API
        COMMAND,        // Using custom command (e.g., "coins give %player% %amount%")
        NONE            // No economy integration
    }

    public EconomyManager(MiningTycoon plugin) {
        this.plugin = plugin;
        this.currencyCommand = plugin.getConfig().getString("economy.command", "coins give %player% %amount%");

        setupEconomy();
    }

    /**
     * Setup economy system - tries Vault first, falls back to commands
     */
    private void setupEconomy() {
        // Try to setup Vault
        if (setupVaultEconomy()) {
            economyType = EconomyType.VAULT;
            plugin.getLogger().info("Hooked into Vault Economy successfully!");
            return;
        }

        // Check if command-based economy is configured
        if (plugin.getConfig().getBoolean("economy.use-commands", true)) {
            economyType = EconomyType.COMMAND;
            plugin.getLogger().info("Using command-based economy: " + currencyCommand);
            plugin.getLogger().info("Configure in config.yml under 'economy.command'");
        } else {
            economyType = EconomyType.NONE;
            plugin.getLogger().warning("No economy system configured! Players won't receive coins.");
        }
    }

    /**
     * Try to hook into Vault Economy
     */
    private boolean setupVaultEconomy() {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        try {
            RegisteredServiceProvider<Economy> rsp = plugin.getServer()
                    .getServicesManager()
                    .getRegistration(Economy.class);

            if (rsp == null) {
                return false;
            }

            vaultEconomy = rsp.getProvider();
            vaultEnabled = true;
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to hook into Vault: " + e.getMessage());
            return false;
        }
    }

    /**
     * Give money to a player
     *
     * @param player The player to give money to
     * @param amount The amount of money to give
     * @return true if successful, false otherwise
     */
    public boolean giveMoney(Player player, double amount) {
        if (amount <= 0) {
            return false;
        }

        switch (economyType) {
            case VAULT:
                return giveMoneyVault(player, amount);

            case COMMAND:
                return giveMoneyCommand(player, amount);

            case NONE:
            default:
                return false;
        }
    }

    /**
     * Give money using Vault
     */
    private boolean giveMoneyVault(Player player, double amount) {
        if (!vaultEnabled || vaultEconomy == null) {
            return false;
        }

        try {
            vaultEconomy.depositPlayer(player, amount);
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to give money via Vault: " + e.getMessage());
            return false;
        }
    }

    /**
     * Give money using custom command
     */
    private boolean giveMoneyCommand(Player player, double amount) {
        try {
            String command = currencyCommand
                    .replace("%player%", player.getName())
                    .replace("%amount%", String.valueOf((int) amount));

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to execute economy command: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get player's balance (Vault only)
     */
    public double getBalance(Player player) {
        if (economyType == EconomyType.VAULT && vaultEnabled && vaultEconomy != null) {
            try {
                return vaultEconomy.getBalance(player);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to get balance: " + e.getMessage());
            }
        }
        return 0.0;
    }

    /**
     * Take money from a player (Vault only)
     */
    public boolean takeMoney(Player player, double amount) {
        if (economyType == EconomyType.VAULT && vaultEnabled && vaultEconomy != null) {
            try {
                if (vaultEconomy.has(player, amount)) {
                    vaultEconomy.withdrawPlayer(player, amount);
                    return true;
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to take money: " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * Check if economy is enabled
     */
    public boolean isEnabled() {
        return economyType != EconomyType.NONE;
    }

    /**
     * Get the current economy type
     */
    public EconomyType getEconomyType() {
        return economyType;
    }

    /**
     * Get the currency symbol or name
     */
    public String getCurrencySymbol() {
        if (economyType == EconomyType.VAULT && vaultEnabled && vaultEconomy != null) {
            return vaultEconomy.currencyNamePlural();
        }
        return plugin.getConfig().getString("economy.currency-symbol", "⛁");
    }

    /**
     * Format money amount for display
     */
    public String formatMoney(double amount) {
        if (economyType == EconomyType.VAULT && vaultEnabled && vaultEconomy != null) {
            return vaultEconomy.format(amount);
        }
        return String.format("%.0f%s", amount, getCurrencySymbol());
    }
}