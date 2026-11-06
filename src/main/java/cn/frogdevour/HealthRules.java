package cn.frogdevour;

public final class HealthRules {
    private HealthRules() { }

    /** Add only newly unlocked hearts. Keep existing wounds, and never revive a dead player. */
    public static float afterMaxChange(float health, float oldMax, float newMax) {
        if (health <= 0) return 0;
        return Math.min(newMax, health + Math.max(0, newMax - oldMax));
    }
}
