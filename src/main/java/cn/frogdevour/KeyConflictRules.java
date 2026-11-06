package cn.frogdevour;

/** Keep the devour binding usable without triggering Iris' shader reload on the same key. */
public final class KeyConflictRules {
    public static final String IRIS_RELOAD = "iris.keybind.reload";
    private KeyConflictRules() { }

    public static boolean suppressReload(String action, String boundKey, String devourKey, boolean playingFrog) {
        return playingFrog && IRIS_RELOAD.equals(action)
                && !"key.keyboard.unknown".equals(devourKey) && devourKey.equals(boundKey);
    }
}
