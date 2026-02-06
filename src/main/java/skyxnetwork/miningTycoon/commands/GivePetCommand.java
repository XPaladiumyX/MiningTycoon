package skyxnetwork.miningTycoon.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import skyxnetwork.miningTycoon.MiningTycoon;

public class GivePetCommand implements CommandExecutor {

    private final MiningTycoon plugin;

    public GivePetCommand(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("miningtycoon.admin")) {
            sender.sendMessage("§cYou are not allowed to use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /givepet <pet_id> <player>");
            sender.sendMessage("§eAvailable pets: " + String.join(", ", plugin.getItemManager().getAllPetIds()));
            return true;
        }

        String petId = args[0].toLowerCase();
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

        if (!target.isOnline()) {
            sender.sendMessage("§cPlayer " + args[1] + " is not online!");
            return true;
        }

        Player player = (Player) target;
        ItemStack pet = plugin.getItemManager().getPet(petId);

        if (pet == null) {
            sender.sendMessage("§cUnknown pet: " + petId);
            sender.sendMessage("§eAvailable pets: " + String.join(", ", plugin.getItemManager().getAllPetIds()));
            return true;
        }

        player.getInventory().addItem(pet);
        sender.sendMessage("§a" + petId + " has been given to " + player.getName());
        player.sendMessage("§aYou received: " + pet.getItemMeta().getDisplayName());

        return true;
    }
}