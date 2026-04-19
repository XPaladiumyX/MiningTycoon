package skyxnetwork.miningTycoon.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.data.PlayerData;
import skyxnetwork.miningTycoon.utils.NumberFormatter;

public class MiningTycoonPlaceholders extends PlaceholderExpansion {

    private final MiningTycoon plugin;

    public MiningTycoonPlaceholders(MiningTycoon plugin) {
        this.plugin = plugin;
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "miningtycoon";
    }

    @Override
    @NotNull
    public String getAuthor() {
        return "XPaladiumyX";
    }

    @Override
    @NotNull
    public String getVersion() {
        return "2.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);

        switch (identifier.toLowerCase()) {
            case "level_current":
                return String.valueOf(data.getLevel());

            case "exp_display":
                return NumberFormatter.format(data.getExperience()) + "/" +
                        NumberFormatter.format(data.getExperienceNeeded()) + "✦";

            case "exp_progress":
                return getProgressBar(data);

            case "prestige_current":
                return String.valueOf(data.getPrestige());

            case "prestige_current_raw":
            case "prestige":
                return String.valueOf(data.getPrestige());

            case "prestige_display":
                return plugin.getPrestigeManager().getPrestigeDisplay(data.getPrestige());

            case "level_display":
                return getLevelDisplay(data.getLevel());

            case "exp_current":
                return NumberFormatter.format(data.getExperience());

            case "exp_needed":
                return NumberFormatter.format(data.getExperienceNeeded());

            case "exp_percent":
                int percent = (int) ((data.getExperience() / data.getExperienceNeeded()) * 100);
                return String.valueOf(Math.min(percent, 100));

            case "afk_time":
                return plugin.getAfkManager().getFormattedAfkTime(player.getUniqueId());

            case "afk_time_detailed":
                return plugin.getAfkManager().getDetailedAfkTime(player.getUniqueId());

            case "afk_time_seconds":
                return String.valueOf(plugin.getAfkManager().getPlayerAfkTime(player.getUniqueId()));

            case "afk_rank":
                int rank = plugin.getAfkManager().getPlayerRank(player.getUniqueId());
                return rank > 0 ? String.valueOf(rank) : "N/A";

            case "afk_status":
                return plugin.getAfkManager().getAfkStatusForPlaceholder(player.getUniqueId());

            case "afk_top_1":
            case "afk_top_2":
            case "afk_top_3":
            case "afk_top_4":
            case "afk_top_5":
            case "afk_top_6":
            case "afk_top_7":
            case "afk_top_8":
            case "afk_top_9":
            case "afk_top_10":
                return getAfkTopPlaceholder(identifier);

            default:
                return null;
        }
    }

    private String getProgressBar(PlayerData data) {
        double percent = (data.getExperience() / data.getExperienceNeeded()) * 100;
        int filledBars = (int) (percent / 10);

        StringBuilder progress = new StringBuilder("§8[§d");
        for (int i = 0; i < filledBars; i++) {
            progress.append("■");
        }
        progress.append("§7");
        for (int i = 0; i < (10 - filledBars); i++) {
            progress.append("■");
        }
        progress.append("§8]");

        return progress.toString();
    }

    private String getLevelDisplay(int level) {
        String value = level >= 1000 ?
                (Math.round(level / 10.0) / 100.0) + "k" :
                String.valueOf(level);

        if (level < 10) return "§7[" + value + "✐]";
        if (level < 50) return "§e[" + value + "✐]";
        if (level < 100) return "§a[" + value + "✐]";
        if (level < 200) return "§b[" + value + "✐]";
        if (level < 300) return "§d[" + value + "✐]";
        if (level < 500) return "§6[" + value + "✐]";
        if (level < 800) return "§c[" + value + "✐]";
        if (level < 1000) return "§e[" + value + "✐]";
        if (level < 1500) return "§5[" + value + "✐]";
        return "§4✴[" + value + "✐]✴";
    }

    private String getAfkTopPlaceholder(String identifier) {
        int position;
        switch (identifier) {
            case "afk_top_1":
                position = 1;
                break;
            case "afk_top_2":
                position = 2;
                break;
            case "afk_top_3":
                position = 3;
                break;
            case "afk_top_4":
                position = 4;
                break;
            case "afk_top_5":
                position = 5;
                break;
            case "afk_top_6":
                position = 6;
                break;
            case "afk_top_7":
                position = 7;
                break;
            case "afk_top_8":
                position = 8;
                break;
            case "afk_top_9":
                position = 9;
                break;
            case "afk_top_10":
                position = 10;
                break;
            default:
                return "";
        }

        var topList = plugin.getAfkManager().getTopAfkPlayers(10);

        String playerName;
        long seconds = 0;

        if (position <= topList.size()) {
            var entry = topList.get(position - 1);
            playerName = "§e" + plugin.getAfkManager().getPlayerName(entry.getKey());
            seconds = entry.getValue();
        } else {
            playerName = "§c-";
        }

        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        StringBuilder timeStr = new StringBuilder();
        if (days > 0) timeStr.append(days).append("d ");
        if (hours > 0) timeStr.append(hours).append("h ");
        if (minutes > 0) timeStr.append(minutes).append("m ");
        if (secs > 0 || timeStr.length() == 0) timeStr.append(secs).append("s");

        return "§7" + position + ". " + playerName + " " + timeStr.toString();
    }
}
