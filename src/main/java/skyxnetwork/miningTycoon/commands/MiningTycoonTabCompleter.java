package skyxnetwork.miningTycoon.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import skyxnetwork.miningTycoon.MiningTycoon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MiningTycoonTabCompleter implements TabCompleter {

    private final MiningTycoon plugin;

    public MiningTycoonTabCompleter(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        switch (command.getName().toLowerCase()) {
            case "fasttp":
                if (args.length == 1) {
                    completions.add("zone");
                } else if (args.length == 2 && args[0].equalsIgnoreCase("zone")) {
                    for (int i = 1; i <= 18; i++) {
                        completions.add(String.valueOf(i));
                    }
                }
                break;

            case "giveitem":
                if (args.length == 1) {
                    completions.addAll(plugin.getItemManager().getAllPickaxeIds());
                } else if (args.length == 2) {
                    return getOnlinePlayerNames();
                }
                break;

            case "givearmor":
                if (args.length == 1) {
                    completions.addAll(plugin.getItemManager().getAllArmorIds());
                } else if (args.length == 2) {
                    return getOnlinePlayerNames();
                }
                break;

            case "givepet":
                if (args.length == 1) {
                    completions.addAll(plugin.getItemManager().getAllPetIds());
                } else if (args.length == 2) {
                    return getOnlinePlayerNames();
                }
                break;

            case "leveladmin":
                if (args.length == 1) {
                    completions.addAll(Arrays.asList("set", "reset"));
                } else if (args.length == 2) {
                    return getOnlinePlayerNames();
                } else if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
                    completions.addAll(Arrays.asList("1", "10", "50", "100", "200", "500"));
                }
                break;

            case "prestigeadmin":
                if (args.length == 1) {
                    completions.addAll(Arrays.asList("set", "reset"));
                } else if (args.length == 2) {
                    return getOnlinePlayerNames();
                } else if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
                    completions.addAll(Arrays.asList("1", "5", "10", "50", "100"));
                }
                break;

            case "prestige":
                if (args.length == 1) {
                    completions.add("confirm");
                }
                break;

            case "permconfig":
                if (args.length == 1) {
                    completions.addAll(Arrays.asList("add", "remove", "check"));
                } else if (args.length == 2) {
                    return getOnlinePlayerNames();
                } else if (args.length == 3) {
                    completions.addAll(Arrays.asList(
                            "miningtycoon.admin",
                            "miningtycoon.prestige.admin",
                            "miningtycoon.level.admin",
                            "miningtycoon.permhandle",
                            "antiblock.bypass"
                    ));
                }
                break;

            case "miningtycoon":
                if (args.length == 1) {
                    completions.addAll(Arrays.asList("reload", "save", "help", "version"));
                }
                break;
        }

        // Filter completions based on what the player has typed
        String input = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(input))
                .sorted()
                .collect(Collectors.toList());
    }

    private List<String> getOnlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .sorted()
                .collect(Collectors.toList());
    }
}