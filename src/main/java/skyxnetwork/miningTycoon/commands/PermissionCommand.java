package skyxnetwork.miningTycoon.commands;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import skyxnetwork.miningTycoon.MiningTycoon;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


// entire logic here pulled from my project l
public class PermissionCommand implements CommandExecutor {

    private final MiningTycoon plugin;
    public static final Map<UUID, PermissionAttachment> attachments = new HashMap<>();


    public PermissionCommand(MiningTycoon plugin) {
        this.plugin = plugin;
    }
    public static boolean checkPermission(String perm, Player player){
        return player.hasPermission(perm);
    }

    public PermissionAttachment getAttachment(Player player) {
        PermissionAttachment attachment = attachments.get(player.getUniqueId());
        if (attachment == null) {
            attachment = player.addAttachment(plugin);
            attachments.put(player.getUniqueId(), attachment);
        }
        return attachment;
    }

    public static void removeAttachment(Player player) {
        PermissionAttachment attachment = attachments.remove(player.getUniqueId());

        if (attachment != null) {
            player.removeAttachment(attachment);
        }
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
         if (command.getName().equalsIgnoreCase("permconfig")) {
             if (!(sender instanceof Player)) {
                 sender.sendMessage("only players can use this command, well I mean the logic would work but I'm too lazy to add more if statements");
                 return true;
             }

            Player player = (Player) sender;
            if (!checkPermission("miningtycoon.permhandle", player)){
                player.sendMessage(ChatColor.GOLD + "Hey! U cand do dat :(");
                return true;
            }

            if (args.length != 3){
                player.sendMessage(ChatColor.GOLD + "Command invalid, use format /permconfig <add/remove> <target> <perm>");

            }else{
                String type = args[0];
                Player target = Bukkit.getServer().getPlayerExact(args[1]);
                String permission = args[2];

                if (target != null && (type.equalsIgnoreCase("add") || type.equalsIgnoreCase("remove")|| type.equalsIgnoreCase("check"))){
                    PermissionAttachment attachment = getAttachment(target);

                    switch (type){
                        case "add":
                            if (target.hasPermission(permission)){
                                player.sendMessage( target.getName() + " already has permission " + ChatColor.GOLD + permission);
                            }else{
                                player.sendMessage("Permission " + ChatColor.GREEN + permission + ChatColor.WHITE + " added to " + target.getName());
                                target.sendMessage("You have been granted permission " + ChatColor.GREEN + permission);
                                attachment.setPermission(permission, true);
                            }
                            break;
                        case "remove":
                            if (target.hasPermission(permission)){
                                player.sendMessage("Permission " + ChatColor.GREEN + permission + ChatColor.WHITE + " revoked from " + target.getName());
                                target.sendMessage("Your permission for " + ChatColor.RED + permission + ChatColor.WHITE + " has been revoked");
                                attachment.setPermission(permission, false);
                            }else{
                                player.sendMessage(target.getName() + " doesn't have permission " + ChatColor.GOLD + permission);
                            }
                            break;
                        case "check":
                            if (target.hasPermission(permission)){
                                player.sendMessage(ChatColor.GREEN + target.getName() + " has the " + permission + " permission");
                            }else{
                                player.sendMessage(ChatColor.GOLD + target.getName() + " does not have the " + permission + " permission");
                            }
                    }
                }else if(target == null){
                    player.sendMessage(ChatColor.GOLD + args[1] + " is nil");
                }else{
                    player.sendMessage(ChatColor.GOLD + "Your command was invalid");
                }

            }
        }
        return true;
    }
}
