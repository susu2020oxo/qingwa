package cn.frogdevour;

import cn.frogdevour.GrowthRules.PreySize;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.registry.Registries;

import java.util.Set;

public final class PreyClassifier {
    private static final Set<String> SMALL = Set.of("chicken", "cod", "salmon", "pufferfish", "tropical_fish",
            "silverfish", "endermite", "rabbit", "bat", "bee", "tadpole", "frog", "axolotl", "parrot", "allay");
    private static final Set<String> LARGE = Set.of("iron_golem", "camel", "ravager", "ghast", "elder_guardian",
            "horse", "donkey", "mule", "skeleton_horse", "zombie_horse", "sniffer", "happy_ghast");
    private PreyClassifier() { }

    public static PreySize size(MobEntity mob) {
        String id = Registries.ENTITY_TYPE.getId(mob.getType()).getPath();
        if (mob instanceof EnderDragonEntity || id.equals("wither") || id.equals("warden")) return PreySize.BOSS;
        if (mob instanceof SlimeEntity slime) {
            return slime.getSize() <= 1 ? PreySize.SMALL : slime.getSize() <= 2 ? PreySize.MEDIUM : PreySize.LARGE;
        }
        if (LARGE.contains(id)) return PreySize.LARGE;
        if (SMALL.contains(id)) return PreySize.SMALL;
        if (mob.isBaby()) return PreySize.SMALL;
        // Adult cows and sheep are deliberately medium despite a cow's width.
        return mob.getWidth() > 1.7f || mob.getHeight() > 2.8f ? PreySize.LARGE : PreySize.MEDIUM;
    }
}
