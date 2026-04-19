package skyxnetwork.miningTycoon.data;

import org.bukkit.Location;

import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private int level;
    private double experience;
    private double experienceNeeded;
    private int prestige;
    private int rebirthPoints;
    private double expMultiplierBonus; // Cumulative from rebirths (e.g., 0.36 = 36%)
    private boolean dropMessagesEnabled;
    private boolean levelUpSoundEnabled;
    private String playerMode; // "player" or "staff"
    private Location lastSafeLocation;
    private boolean inAFKZone;
    private long afkTime;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.level = 1;
        this.experience = 0;
        this.experienceNeeded = 100;
        this.prestige = 0;
        this.rebirthPoints = 0;
        this.expMultiplierBonus = 0.0;
        this.dropMessagesEnabled = true;
        this.levelUpSoundEnabled = true;
        this.playerMode = "player";
        this.inAFKZone = false;
        this.afkTime = 0;
    }

    // Getters and Setters
    public UUID getUuid() {
        return uuid;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public double getExperience() {
        return experience;
    }

    public void setExperience(double experience) {
        this.experience = experience;
    }

    public void addExperience(double amount) {
        this.experience += amount;
    }

    public double getExperienceNeeded() {
        return experienceNeeded;
    }

    public void setExperienceNeeded(double experienceNeeded) {
        this.experienceNeeded = experienceNeeded;
    }

    public int getPrestige() {
        return prestige;
    }

    public void setPrestige(int prestige) {
        this.prestige = prestige;
    }

    public void addPrestige(int amount) {
        this.prestige += amount;
    }

    public int getRebirthPoints() {
        return rebirthPoints;
    }

    public void setRebirthPoints(int rebirthPoints) {
        this.rebirthPoints = rebirthPoints;
    }

    public void addRebirthPoints(int amount) {
        this.rebirthPoints += amount;
    }

    public boolean isDropMessagesEnabled() {
        return dropMessagesEnabled;
    }

    public void setDropMessagesEnabled(boolean dropMessagesEnabled) {
        this.dropMessagesEnabled = dropMessagesEnabled;
    }

    public boolean isLevelUpSoundEnabled() {
        return levelUpSoundEnabled;
    }

    public void setLevelUpSoundEnabled(boolean levelUpSoundEnabled) {
        this.levelUpSoundEnabled = levelUpSoundEnabled;
    }

    public String getPlayerMode() {
        return playerMode;
    }

    public void setPlayerMode(String playerMode) {
        this.playerMode = playerMode;
    }

    public Location getLastSafeLocation() {
        return lastSafeLocation;
    }

    public void setLastSafeLocation(Location lastSafeLocation) {
        this.lastSafeLocation = lastSafeLocation;
    }

    public boolean isInAFKZone() {
        return inAFKZone;
    }

    public void setInAFKZone(boolean inAFKZone) {
        this.inAFKZone = inAFKZone;
    }

    public long getAfkTime() {
        return afkTime;
    }

    public void setAfkTime(long afkTime) {
        this.afkTime = afkTime;
    }

    public void addAfkTime(long time) {
        this.afkTime += time;
    }

    // EXP Multiplier from rebirths
    public double getExpMultiplierBonus() {
        return expMultiplierBonus;
    }

    public void setExpMultiplierBonus(double expMultiplierBonus) {
        this.expMultiplierBonus = expMultiplierBonus;
    }

    public void addExpMultiplierBonus(double bonus) {
        this.expMultiplierBonus += bonus;
    }

    public double getTotalExpMultiplier() {
        return 1.0 + expMultiplierBonus;
    }

    public String getExpMultiplierDisplay() {
        int percent = (int) (expMultiplierBonus * 100);
        return "+" + percent + "%";
    }

    // Level up logic
    public boolean canLevelUp() {
        int maxLevel = 2100;
        try {
            maxLevel = skyxnetwork.miningTycoon.MiningTycoon.getInstance().getConfig().getInt("settings.max-level", 2100);
        } catch (Exception e) {
            // Use default
        }
        return experience >= experienceNeeded && level < maxLevel;
    }

    public void levelUp() {
        if (canLevelUp()) {
            experience -= experienceNeeded;
            level++;
            double multiplier = 1.08;
            try {
                multiplier = skyxnetwork.miningTycoon.MiningTycoon.getInstance().getConfig().getDouble("settings.level-up-multiplier", 1.08);
            } catch (Exception e) {
                // Use default
            }
            experienceNeeded *= multiplier;
        }
    }

    // Reset for prestige
    public void resetForPrestige() {
        this.level = 1;
        this.experience = 0;
        this.experienceNeeded = 100;
    }
}