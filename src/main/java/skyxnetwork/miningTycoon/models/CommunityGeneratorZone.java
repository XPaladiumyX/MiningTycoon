package skyxnetwork.miningTycoon.models;

import org.bukkit.Material;

import java.util.List;

public class CommunityGeneratorZone {

    private final String zoneName;
    private final Material block;
    private final int cooldown;
    private final List<CommunityReward> rewards;

    public CommunityGeneratorZone(String zoneName, Material block, int cooldown, List<CommunityReward> rewards) {
        this.zoneName = zoneName;
        this.block = block;
        this.cooldown = cooldown;
        this.rewards = rewards;
    }

    public String getZoneName() {
        return zoneName;
    }

    public Material getBlock() {
        return block;
    }

    public int getCooldown() {
        return cooldown;
    }

    public List<CommunityReward> getRewards() {
        return rewards;
    }
}
