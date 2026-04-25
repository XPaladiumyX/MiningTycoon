package skyxnetwork.miningTycoon.models;

import java.util.List;

public class CommunityReward {

    public enum RewardType {
        MONEY,
        EXP,
        ZENTIUM,
        COMMAND,
        GLOBAL_EXP_BOOST,
        GLOBAL_COINS_BOOST,
        GLOBAL_BOTH_BOOST
    }

    private final RewardType type;
    private final double chance;
    private final int min;
    private final int max;
    private final int amount;
    private final List<String> commands;

    public CommunityReward(RewardType type, double chance, int min, int max, int amount, List<String> commands) {
        this.type = type;
        this.chance = chance;
        this.min = min;
        this.max = max;
        this.amount = amount;
        this.commands = commands;
    }

    public RewardType getType() {
        return type;
    }

    public double getChance() {
        return chance;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public int getAmount() {
        return amount;
    }

    public List<String> getCommands() {
        return commands;
    }

    public int getRandomAmount() {
        if (min == max) {
            return min;
        }
        return min + (int) (Math.random() * (max - min + 1));
    }
}
