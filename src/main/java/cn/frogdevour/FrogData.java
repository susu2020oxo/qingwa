package cn.frogdevour;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.codec.PacketCodecs;

public final class FrogData {
    public static final AttachmentType<Integer> FOOD = AttachmentRegistry.create(FrogDevour.id("food"), builder -> builder
            .initializer(() -> 0)
            .persistent(Codec.intRange(0, GrowthRules.MAX_UNITS))
            .copyOnDeath()
            .syncWith(PacketCodecs.VAR_INT, AttachmentSyncPredicate.all()));
    private FrogData() { }
    public static void initialize() { }
    public static int food(PlayerEntity player) {
        Integer value = player.getAttached(FOOD);
        return value == null ? 0 : value;
    }
    public static GrowthRules.Stage stage(PlayerEntity player) { return GrowthRules.stage(food(player)); }
    public static void setFood(PlayerEntity player, int units) {
        player.setAttached(FOOD, Math.clamp(units, 0, GrowthRules.MAX_UNITS));
        player.calculateDimensions();
    }
}
