package cn.frogdevour;

/** Integer food units avoid rounding away a small meal at later stages. */
public final class GrowthRules {
    public static final int MAX_UNITS = 1_000_000_000;
    public static final int[] THRESHOLDS = {0, 10, 40, 130};
    private GrowthRules() { }

    public enum Stage {
        SMALL(0, "小型", 1.0f, 6, 6.0, 1, 1, 24),
        MEDIUM(1, "中型", 1.25f, 12, 12.0, 3, 1, 24),
        LARGE(2, "大型", 1.5f, 20, 24.0, 9, 9, 30),
        GIANT(3, "巨型", 1.8f, 30, 40.0, 27, 27, 40);

        public final int index;
        public final String chineseName;
        public final float modelScale;
        /** Two health points make one heart. */
        public final int maxHealth;
        public final double reach;
        public final int mouthCapacity;
        public final int maxTongues;
        public final int cooldownTicks;

        Stage(int index, String name, float scale, int maxHealth, double reach, int capacity, int tongues, int cooldown) {
            this.index = index;
            this.chineseName = name;
            this.modelScale = scale;
            this.maxHealth = maxHealth;
            this.reach = reach;
            this.mouthCapacity = capacity;
            this.maxTongues = tongues;
            this.cooldownTicks = cooldown;
        }
        public float width() { return 0.5f * modelScale; }
        public float height() { return 0.5f * modelScale; }
        public float eyeHeight() { return height() * 0.8f; }
    }

    public enum PreySize {
        SMALL(0, 1), MEDIUM(1, 3), LARGE(2, 9), BOSS(3, 27);
        public final int level;
        public final int units;
        PreySize(int level, int units) { this.level = level; this.units = units; }
    }

    public static Stage stage(int units) {
        if (units >= THRESHOLDS[3]) return Stage.GIANT;
        if (units >= THRESHOLDS[2]) return Stage.LARGE;
        if (units >= THRESHOLDS[1]) return Stage.MEDIUM;
        return Stage.SMALL;
    }
    public static int add(int units, PreySize prey) {
        return (int) Math.min(MAX_UNITS, (long) Math.max(0, units) + prey.units);
    }
    public static boolean canEat(Stage stage, PreySize prey) { return stage.index >= prey.level; }
    public static int progress(int units) { return Math.max(0, units - THRESHOLDS[stage(units).index]); }
    public static int required(int units) {
        Stage stage = stage(units);
        return stage == Stage.GIANT ? 0 : THRESHOLDS[stage.index + 1] - THRESHOLDS[stage.index];
    }
}
