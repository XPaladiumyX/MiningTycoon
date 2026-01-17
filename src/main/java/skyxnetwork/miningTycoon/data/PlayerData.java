package skyxnetwork.miningTycoon.data;

import org.bukkit.Location;

import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private int level;
    private double experience;
    private double experienceNeeded;
    private int prestige;
    private boolean dropMessagesEnabled;
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
        this.dropMessagesEnabled = true;
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

    public boolean isDropMessagesEnabled() {
        return dropMessagesEnabled;
    }

    public void setDropMessagesEnabled(boolean dropMessagesEnabled) {
        this.dropMessagesEnabled = dropMessagesEnabled;
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

    // Level up logic
    public boolean canLevelUp() {
        return experience >= experienceNeeded && level < 500;
    }

    public void levelUp() {
        if (canLevelUp()) {
            experience -= experienceNeeded;
            level++;
            experienceNeeded *= 1.1; // 10% increase each level
        }
    }

    // Reset for prestige
    public void resetForPrestige() {
        this.level = 1;
        this.experience = 0;
        this.experienceNeeded = 100;
    }
}