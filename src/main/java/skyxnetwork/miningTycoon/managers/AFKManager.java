package skyxnetwork.miningTycoon.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import skyxnetwork.miningTycoon.MiningTycoon;
import skyxnetwork.miningTycoon.data.PlayerData;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AFKManager {

    private final MiningTycoon plugin;
    private final Map<UUID, Long> lastActivityTime = new ConcurrentHashMap<>();
    private final Map<UUID, Long> afkStartTime = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> afkStatus = new ConcurrentHashMap<>();
    private final Map<UUID, Long> afkTimeCache = new ConcurrentHashMap<>();
    private final Set<UUID> manualAfkTime = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Map<UUID, Long> lastManualAfkAdd = new ConcurrentHashMap<>();

    private long afkThreshold;
    private final File afkDataFile;
    private FileConfiguration afkDataConfig;

    public AFKManager(MiningTycoon plugin) {
        this.plugin = plugin;
        this.afkDataFile = new File(plugin.getDataFolder(), "afk_data.yml");
        loadAfkData();
        loadAfkThreshold();
    }

    private void loadAfkThreshold() {
        FileConfiguration config = plugin.getConfig();
        afkThreshold = config.getLong("afk-settings.idle-threshold", 300);
        if (afkThreshold < 60) {
            afkThreshold = 60;
        }
    }

    private void loadAfkData() {
        afkDataConfig = new org.bukkit.configuration.file.YamlConfiguration();
        if (afkDataFile.exists()) {
            try {
                afkDataConfig.load(afkDataFile);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to load AFK data file!");
                e.printStackTrace();
            }
        }
    }

    public void saveAfkData() {
        if (afkDataConfig == null) return;

        for (UUID uuid : afkTimeCache.keySet()) {
            afkDataConfig.set(uuid.toString(), afkTimeCache.get(uuid));
        }

        try {
            afkDataConfig.save(afkDataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save AFK data!");
            e.printStackTrace();
        }
    }

    public long getPlayerAfkTime(UUID uuid) {
        return afkTimeCache.getOrDefault(uuid, 0L);
    }

    public void setPlayerAfkTime(UUID uuid, long time) {
        afkTimeCache.put(uuid, time);
    }

    public void addAfkTime(UUID uuid, long time) {
        long current = afkTimeCache.getOrDefault(uuid, 0L);
        afkTimeCache.put(uuid, current + time);
    }

    public void setPlayerAfk(UUID uuid, boolean afk) {
        if (afk) {
            afkStatus.put(uuid, true);
            afkStartTime.put(uuid, System.currentTimeMillis());
            manualAfkTime.add(uuid);
        } else {
            if (Boolean.TRUE.equals(afkStatus.get(uuid))) {
                long afkStart = afkStartTime.getOrDefault(uuid, System.currentTimeMillis());
                long afkDuration = (System.currentTimeMillis() - afkStart) / 1000;
                
                if (!manualAfkTime.contains(uuid)) {
                    addAfkTime(uuid, afkDuration);
                } else {
                    manualAfkTime.remove(uuid);
                    lastManualAfkAdd.remove(uuid);
                }
            }
            afkStatus.put(uuid, false);
            afkStartTime.remove(uuid);
        }
    }

    public boolean isManuallyAfk(UUID uuid) {
        return manualAfkTime.contains(uuid);
    }

    public boolean shouldAddTime(UUID uuid) {
        long now = System.currentTimeMillis();
        Long lastAdd = lastManualAfkAdd.get(uuid);
        if (lastAdd == null || now - lastAdd >= 1000) {
            lastManualAfkAdd.put(uuid, now);
            return true;
        }
        return false;
    }

    public boolean isPlayerAfk(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.getWorld().getName().equals("mining_tycoon")) {
            Location loc = player.getLocation();
            int x = loc.getBlockX();
            int y = loc.getBlockY();
            int z = loc.getBlockZ();
            if (x >= 8 && x <= 11 && z >= 18 && z <= 21 && y <= 125 && y >= 100) {
                return true;
            }
        }
        return afkStatus.getOrDefault(uuid, false);
    }

    public boolean isPlayerInAfkZone(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.getWorld().getName().equals("mining_tycoon")) {
            Location loc = player.getLocation();
            int x = loc.getBlockX();
            int y = loc.getBlockY();
            int z = loc.getBlockZ();
            return x >= 8 && x <= 11 && z >= 18 && z <= 21 && y <= 125 && y >= 100;
        }
        return false;
    }

    public String getAfkStatusForPlaceholder(UUID uuid) {
        return isPlayerAfk(uuid) ? "§cAFK " : "";
    }

    public void updateLastActivity(Player player) {
        UUID uuid = player.getUniqueId();
        lastActivityTime.put(uuid, System.currentTimeMillis());

        if (Boolean.TRUE.equals(afkStatus.get(uuid))) {
            if (manualAfkTime.contains(uuid)) {
                manualAfkTime.remove(uuid);
                afkStatus.put(uuid, false);
                afkStartTime.remove(uuid);
                player.sendMessage("§aYou returned from AFK!");
                return;
            }

            long afkStart = afkStartTime.getOrDefault(uuid, System.currentTimeMillis());
            long afkDuration = (System.currentTimeMillis() - afkStart) / 1000;
            addAfkTime(uuid, afkDuration);

            PlayerData data = plugin.getPlayerDataManager().getPlayerData(player);
            if (data != null) {
                data.addAfkTime(afkDuration);
                plugin.getDataStorage().savePlayerData(uuid, data);
            }

            afkStatus.put(uuid, false);
            afkStartTime.remove(uuid);

            player.sendMessage("§aYou returned from AFK! §7(§e" + formatTime(afkDuration) + "§7)");
        }
    }

    public void checkAfkStatus() {
        long now = System.currentTimeMillis();
        long thresholdMs = afkThreshold * 1000;

        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();

            if (!lastActivityTime.containsKey(uuid)) {
                lastActivityTime.put(uuid, now);
                continue;
            }

            long lastActivity = lastActivityTime.get(uuid);
            boolean currentlyAfk = afkStatus.getOrDefault(uuid, false);

            if ((now - lastActivity) >= thresholdMs && !currentlyAfk) {
                afkStatus.put(uuid, true);
                afkStartTime.put(uuid, now);
                player.sendMessage("§eYou are now AFK!");
            }
        }
    }

    public void onPlayerJoin(UUID uuid) {
        if (afkDataConfig.contains(uuid.toString())) {
            long storedTime = afkDataConfig.getLong(uuid.toString(), 0);
            afkTimeCache.put(uuid, storedTime);
        } else {
            PlayerData data = plugin.getPlayerDataManager().getPlayerData(uuid);
            if (data != null) {
                long savedTime = data.getAfkTime();
                afkTimeCache.put(uuid, savedTime);
            }
        }
        lastActivityTime.put(uuid, System.currentTimeMillis());
        afkStatus.put(uuid, false);
    }

    public void onPlayerQuit(UUID uuid) {
        if (Boolean.TRUE.equals(afkStatus.get(uuid))) {
            long afkStart = afkStartTime.getOrDefault(uuid, System.currentTimeMillis());
            long afkDuration = (System.currentTimeMillis() - afkStart) / 1000;
            addAfkTime(uuid, afkDuration);
        }

        long savedTime = afkTimeCache.getOrDefault(uuid, 0L);
        afkDataConfig.set(uuid.toString(), savedTime);
        saveAfkData();

        lastActivityTime.remove(uuid);
        afkStartTime.remove(uuid);
        afkStatus.remove(uuid);
    }

    public List<Map.Entry<UUID, Long>> getTopAfkPlayers(int limit) {
        List<Map.Entry<UUID, Long>> sorted = new ArrayList<>(afkTimeCache.entrySet());
        sorted.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));

        return sorted.subList(0, Math.min(limit, sorted.size()));
    }

    public int getPlayerRank(UUID uuid) {
        List<Map.Entry<UUID, Long>> sorted = new ArrayList<>(afkTimeCache.entrySet());
        sorted.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));

        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).getKey().equals(uuid)) {
                return i + 1;
            }
        }
        return -1;
    }

    private String formatTime(long seconds) {
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        sb.append(secs).append("s");

        return sb.toString().trim();
    }

    public String getDetailedAfkTime(UUID uuid) {
        long seconds = getPlayerAfkTime(uuid);
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m ");
        sb.append(secs).append("s");

        return sb.toString().trim();
    }

    public String getFormattedAfkTime(UUID uuid) {
        return formatTime(getPlayerAfkTime(uuid));
    }
}