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
}
