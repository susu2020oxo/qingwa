package cn.frogdevour;

import cn.frogdevour.GrowthRules.PreySize;
import cn.frogdevour.GrowthRules.Stage;

/** Standalone tests also run with javac/java, without Minecraft or Gradle. */
public final class GrowthRulesTest {
    private static int checks;
    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        check(GrowthRules.stage(0) == Stage.SMALL, "Start small");
        int food = 0;
        for (int i = 0; i < 9; i++) food = GrowthRules.add(food, PreySize.SMALL);
        check(GrowthRules.stage(food) == Stage.SMALL, "Nine meals cannot upgrade");
        food = GrowthRules.add(food, PreySize.SMALL);
        check(GrowthRules.stage(food) == Stage.MEDIUM, "Ten small meals upgrade");
        check(GrowthRules.progress(food) == 0, "New stage starts empty");
        check(GrowthRules.required(food) == 30, "Medium needs ten medium meals");
        for (int i = 0; i < 9; i++) food = GrowthRules.add(food, PreySize.MEDIUM);
        check(GrowthRules.stage(food) == Stage.MEDIUM, "Nine medium meals cannot upgrade");
        food = GrowthRules.add(food, PreySize.MEDIUM);
        check(GrowthRules.stage(food) == Stage.LARGE, "Ten medium meals upgrade");
        for (int i = 0; i < 10; i++) food = GrowthRules.add(food, PreySize.LARGE);
        check(GrowthRules.stage(food) == Stage.GIANT, "Ten large meals upgrade");
        check(GrowthRules.required(food) == 0, "No fifth stage");
        check(GrowthRules.add(10, PreySize.MEDIUM) == GrowthRules.add(GrowthRules.add(GrowthRules.add(10,
                PreySize.SMALL), PreySize.SMALL), PreySize.SMALL), "Three small equal one medium");
        check(GrowthRules.add(40, PreySize.LARGE) == GrowthRules.add(GrowthRules.add(GrowthRules.add(40,
                PreySize.MEDIUM), PreySize.MEDIUM), PreySize.MEDIUM), "Three medium equal one large");
        food = 10;
        for (int i = 0; i < 30; i++) food = GrowthRules.add(food, PreySize.SMALL);
        check(GrowthRules.stage(food) == Stage.LARGE, "Small prey remains useful later");
        check(GrowthRules.progress(41) == 1, "Leftover units retained");
        for (Stage stage : Stage.values()) {
            for (PreySize prey : PreySize.values()) {
                check(GrowthRules.canEat(stage, prey) == (stage.index >= prey.level), "Stage gate " + stage + " " + prey);
            }
            check(stage.mouthCapacity >= PreySize.values()[stage.index].units, "Can swallow own class");
        }
        check(Stage.LARGE.maxTongues == 9 && Stage.LARGE.mouthCapacity == 9, "Large has nine small tongues");
        check(Stage.GIANT.maxTongues == 27 && Stage.GIANT.mouthCapacity == 27, "Giant has 27 small tongues");
        for (Stage stage : Stage.values()) {
            FeedingBudget budget = new FeedingBudget(stage);
            for (int i = 0; i < stage.maxTongues; i++) check(budget.take(PreySize.SMALL), "Small batch within capacity");
            check(!budget.take(PreySize.SMALL), "No extra tongue beyond limit");
        }
        FeedingBudget large = new FeedingBudget(Stage.LARGE);
        check(large.take(PreySize.MEDIUM) && large.take(PreySize.MEDIUM) && large.take(PreySize.MEDIUM), "Three medium in large batch");
        check(!large.take(PreySize.SMALL), "Full stomach rejects next target");
        FeedingBudget mixed = new FeedingBudget(Stage.LARGE);
        check(mixed.take(PreySize.MEDIUM), "Mixed batch first medium");
        check(!mixed.take(PreySize.BOSS) && mixed.remaining() == 6 && mixed.count() == 1, "Rejected target consumes no budget");
        for (int i = 0; i < 6; i++) check(mixed.take(PreySize.SMALL), "Mixed batch fills remaining capacity");
        check(!mixed.take(PreySize.SMALL), "Mixed batch stops at nine units");
        FeedingBudget boss = new FeedingBudget(Stage.GIANT);
        check(boss.take(PreySize.BOSS) && !boss.take(PreySize.SMALL), "A boss occupies an entire giant batch");
        FeedingBudget medium = new FeedingBudget(Stage.MEDIUM);
        check(!medium.take(PreySize.LARGE) && medium.take(PreySize.SMALL) && !medium.take(PreySize.SMALL), "Medium keeps a single tongue");
        check(GrowthRules.add(Integer.MAX_VALUE, PreySize.BOSS) == GrowthRules.MAX_UNITS, "Food cannot overflow");
        check(GrowthRules.add(-10, PreySize.SMALL) == 1, "Negative input cannot corrupt progress");
        check(GrowthRules.stage(9) == Stage.SMALL && GrowthRules.stage(39) == Stage.MEDIUM
                && GrowthRules.stage(129) == Stage.LARGE && GrowthRules.stage(130) == Stage.GIANT, "Boundary values");
        for (int units = 0; units < 130; units++) {
            check(GrowthRules.progress(units) >= 0 && GrowthRules.progress(units) < GrowthRules.required(units), "Progress bounds");
        }
        check(Stage.SMALL.width() < Stage.MEDIUM.width() && Stage.MEDIUM.width() < Stage.LARGE.width()
                && Stage.LARGE.width() < Stage.GIANT.width(), "Physical size increases");
        check(Stage.SMALL.maxHealth == 6, "Start with three hearts");
        check(Stage.MEDIUM.maxHealth == 12 && Stage.LARGE.maxHealth == 20 && Stage.GIANT.maxHealth == 30, "Stage health increases");
        for (Stage stage : Stage.values()) {
            check(stage.width() <= 0.9f && stage.height() <= 2.9f, "Fit within Warden dimensions");
            check(stage.eyeHeight() < stage.height(), "Camera stays within frog height");
        }
        check(HealthRules.afterMaxChange(20, 20, 6) == 6, "Initial health capped at three hearts");
        check(HealthRules.afterMaxChange(4, 6, 12) == 10, "Growth keeps two points of existing damage");
        check(HealthRules.afterMaxChange(6, 6, 12) == 12, "Healthy frog gains newly unlocked hearts");
        check(HealthRules.afterMaxChange(4, 6, 6) == 4, "Repeated sync never heals");
        check(HealthRules.afterMaxChange(28, 30, 6) == 6, "Reset clamps health");
        check(HealthRules.afterMaxChange(2, 30, 6) == 2, "Reset does not heal wounds");
        check(HealthRules.afterMaxChange(0, 6, 30) == 0, "Growth cannot resurrect dead player");
        check(KeyConflictRules.suppressReload("iris.keybind.reload", "key.keyboard.r", "key.keyboard.r", true), "R devour must not reload Iris");
        check(!KeyConflictRules.suppressReload("iris.keybind.reload", "key.keyboard.r", "key.keyboard.f", true), "Remapped devour leaves Iris R usable");
        check(!KeyConflictRules.suppressReload("iris.keybind.reload", "key.keyboard.f", "key.keyboard.r", true), "Remapped Iris reload stays usable");
        check(!KeyConflictRules.suppressReload("iris.keybind.reload", "key.keyboard.r", "key.keyboard.r", false), "Menus or non-frog play never suppress reload");
        check(!KeyConflictRules.suppressReload("iris.keybind.toggleShaders", "key.keyboard.r", "key.keyboard.r", true), "Shader toggle not affected");
        check(!KeyConflictRules.suppressReload("iris.keybind.shaderPackSelection", "key.keyboard.r", "key.keyboard.r", true), "Shader pack selector not affected");
        check(!KeyConflictRules.suppressReload("othermod.action", "key.keyboard.r", "key.keyboard.r", true), "Other mods not affected");
        check(!KeyConflictRules.suppressReload("iris.keybind.reload", "key.keyboard.unknown", "key.keyboard.unknown", true), "Unbound key not treated as active conflict");
        check(KeyConflictRules.suppressReload("iris.keybind.reload", "key.mouse.middle", "key.mouse.middle", true), "User mouse rebind also supported");
        check(KeyConflictRules.suppressReload("iris.keybind.reload", "key.keyboard.f", "key.keyboard.f", true), "Conflict follows actual binding rather than hardcoded R");
        check(Stage.SMALL.reach == 6 && Stage.MEDIUM.reach == 12 && Stage.LARGE.reach == 24 && Stage.GIANT.reach == 40, "Tongue ranges follow growth stages");
        check(Stage.SMALL.reach < Stage.MEDIUM.reach && Stage.MEDIUM.reach < Stage.LARGE.reach && Stage.LARGE.reach < Stage.GIANT.reach, "Every growth stage extends tongue reach");
        System.out.println("PASS: " + checks + " gameplay rule checks");
    }
}
