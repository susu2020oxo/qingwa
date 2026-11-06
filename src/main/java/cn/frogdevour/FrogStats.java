package cn.frogdevour;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class FrogStats {
    private static final Identifier HEALTH_ID = FrogDevour.id("growth_health");
    private static final double VANILLA_HEALTH = 20.0;
    private FrogStats() { }

    public static void update(ServerPlayerEntity player) {
        EntityAttributeInstance attribute = player.getAttributeInstance(EntityAttributes.MAX_HEALTH);
        if (attribute == null) return;
        double bonus = FrogData.stage(player).maxHealth - VANILLA_HEALTH;
        EntityAttributeModifier current = attribute.getModifier(HEALTH_ID);
        if (current == null && bonus == 0) return;
        if (current != null && current.value() == bonus
                && current.operation() == EntityAttributeModifier.Operation.ADD_VALUE) return;

        float health = player.getHealth();
        float oldMax = player.getMaxHealth();
        attribute.removeModifier(HEALTH_ID);
        if (bonus != 0) {
            // Persist with health so a giant's >20 HP is not truncated when loading a save.
            attribute.addPersistentModifier(new EntityAttributeModifier(HEALTH_ID, bonus, EntityAttributeModifier.Operation.ADD_VALUE));
        }
        player.setHealth(HealthRules.afterMaxChange(health, oldMax, player.getMaxHealth()));
    }
}
