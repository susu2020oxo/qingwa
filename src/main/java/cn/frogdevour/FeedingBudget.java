package cn.frogdevour;

import cn.frogdevour.GrowthRules.PreySize;
import cn.frogdevour.GrowthRules.Stage;

/** One press has a food budget and a separate maximum tongue count. */
public final class FeedingBudget {
    private final Stage stage;
    private int remaining;
    private int count;
    public FeedingBudget(Stage stage) { this.stage = stage; this.remaining = stage.mouthCapacity; }
    public boolean take(PreySize prey) {
        if (!GrowthRules.canEat(stage, prey) || prey.units > remaining || count >= stage.maxTongues) return false;
        remaining -= prey.units;
        count++;
        return true;
    }
    public int remaining() { return remaining; }
    public int count() { return count; }
}
