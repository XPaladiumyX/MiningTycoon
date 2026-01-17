package skyxnetwork.miningTycoon.managers;

import org.bukkit.entity.Player;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.data.PlayerData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {

    private final MiningTycoon plugin;
    private final Map<UUID, PlayerData> playerDataMap;

    public PlayerDataManager(MiningTycoon plugin) {
        this.plugin = plugin;
        this.playerDataMap = new HashMap<>();
    }

    public PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.computeIfAbsent(uuid, PlayerData::new);
    }

    public void removePlayerData(UUID uuid) {
        playerDataMap.remove(uuid);
    }

    public Map<UUID, PlayerData> getAllPlayerData() {
        return playerDataMap;
    }

    public void addExperience(Player player, double amount) {
        PlayerData data = getPlayerData(player);
        data.addExperience(amount);
        checkLevelUp(player);
    }

    public void checkLevelUp(Player player) {
        PlayerData data = getPlayerData(player);
        while (data.canLevelUp()) {
            data.levelUp();
            player.sendMessage("§6§lLEVEL UP! §eYou are now level §6" + data.getLevel());
            player.playSound(player.getLocation(), "entity.player.levelup", 1.0f, 1.0f);
        }
    }
}
