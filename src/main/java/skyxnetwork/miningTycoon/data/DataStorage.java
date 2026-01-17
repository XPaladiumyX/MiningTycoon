package skyxnetwork.miningTycoon.data;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import skyxnetwork.miningTycoon.MiningTycoon;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class DataStorage {

    private final MiningTycoon plugin;
    private final File dataFolder;

    public DataStorage(MiningTycoon plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    public void savePlayerData(UUID uuid, PlayerData data) {
        File file = new File(dataFolder, uuid.toString() + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        config.set("level", data.getLevel());
        config.set("experience", data.getExperience());
        config.set("experienceNeeded", data.getExperienceNeeded());
        config.set("prestige", data.getPrestige());
        config.set("dropMessagesEnabled", data.isDropMessagesEnabled());
        config.set("playerMode", data.getPlayerMode());
        config.set("afkTime", data.getAfkTime());

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save player data for " + uuid);
            e.printStackTrace();
        }
    }

    public PlayerData loadPlayerData(UUID uuid) {
        File file = new File(dataFolder, uuid.toString() + ".yml");
        if (!file.exists()) {
            return new PlayerData(uuid);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        PlayerData data = new PlayerData(uuid);

        data.setLevel(config.getInt("level", 1));
        data.setExperience(config.getDouble("experience", 0));
        data.setExperienceNeeded(config.getDouble("experienceNeeded", 100));
        data.setPrestige(config.getInt("prestige", 0));
        data.setDropMessagesEnabled(config.getBoolean("dropMessagesEnabled", true));
        data.setPlayerMode(config.getString("playerMode", "player"));
        data.setAfkTime(config.getLong("afkTime", 0));

        return data;
    }

    public void saveAllData() {
        for (PlayerData data : plugin.getPlayerDataManager().getAllPlayerData().values()) {
            savePlayerData(data.getUuid(), data);
        }
        plugin.getLogger().info("Saved all player data");
    }

    public void loadAllData() {
        // Data is loaded on player join
        plugin.getLogger().info("Player data will be loaded on join");
    }
}
