package skyxnetwork.miningTycoon.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import skyxnetwork.miningTycoon.MiningTycoon;

import java.util.Arrays;

public class GiveMenuCommand implements CommandExecutor {

    private final MiningTycoon plugin;

    public static final int MENU_ITEM_CMD = 1234;
    public static final String MENU_ITEM_NAME = ChatColor.translateAlternateColorCodes('&', "&5&lMenu");
    public static final String MENU_ITEM_LORE = ChatColor.translateAlternateColorCodes('&', "&7Right or left click to open the menu.");

    public GiveMenuCommand(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("miningtycoon.admin")) {
            sender.sendMessage("§cYou don't have permission!");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /givemenu <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null || !target.isOnline()) {
            sender.sendMessage("§cPlayer not found or offline!");
            return true;
        }

        ItemStack menuItem = createMenuItem();

        if (target.getInventory().getItem(8) == null ||
                target.getInventory().getItem(8).getType() == Material.AIR) {
            target.getInventory().setItem(8, menuItem);
        } else {
            target.getInventory().addItem(menuItem);
        }

        sender.sendMessage("§aMenu item given to §d" + target.getName() + "§a!");
        target.sendMessage("§8[§dMenu§8] §aYou received the menu item. Right or left click to open!");

        return true;
    }

    public static ItemStack createMenuItem() {
        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(MENU_ITEM_NAME);
        meta.setLore(Arrays.asList(MENU_ITEM_LORE));
        meta.setCustomModelData(MENU_ITEM_CMD);

        item.setItemMeta(meta);
        return item;
    }

    public static boolean isMenuItem(ItemStack item) {
        if (item == null || item.getType() != Material.NETHER_STAR) return false;
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasCustomModelData()) return false;
        return meta.getCustomModelData() == MENU_ITEM_CMD;
    }

    public boolean hasMenuItem(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isMenuItem(item)) return true;
        }
        return false;
    }
}