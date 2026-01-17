package skyxnetwork.miningTycoon.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import skyxnetwork.miningTycoon.data.PlayerData;

public class ActionBarUtil {

    public static void sendActionBar(Player player, double expGained, double moneyGained, PlayerData data) {
        double exp = data.getExperience();
        double expNeeded = data.getExperienceNeeded();
        double expRemaining = expNeeded - exp;

        int percent = (int) ((exp / expNeeded) * 100);
        if (percent > 100) percent = 100;

        String formattedExpGained = NumberFormatter.format(expGained);
        String formattedMoney = NumberFormatter.format(moneyGained);
        String formattedExpRemaining = NumberFormatter.format(expRemaining);

        // Progress bar
        StringBuilder progress = new StringBuilder("§8[§d");
        int filledBars = percent / 10;

        // Check if Bedrock player (simplified check)
        boolean isBedrock = player.getUniqueId().toString().contains("00000000");

        if (isBedrock) {
            for (int i = 0; i < filledBars; i++) progress.append("•");
            progress.append("§7");
            for (int i = 0; i < (10 - filledBars); i++) progress.append("•");
        } else {
            for (int i = 0; i < filledBars; i++) progress.append("■");
            progress.append("§7");
            for (int i = 0; i < (10 - filledBars); i++) progress.append("■");
        }
        progress.append("§8]");

        String message = String.format("§3+%s✦ §6+%s⛁ §7| §8[§b%d✐§8] %s §7(%d%%",
                formattedExpGained, formattedMoney, data.getLevel(), progress, percent);

        if (!isBedrock) {
            message += " | " + formattedExpRemaining + " EXP left)";
        } else {
            message += ")";
        }

        player.sendActionBar(Component.text(message));
    }
}
