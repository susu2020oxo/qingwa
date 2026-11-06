package cn.frogdevour.mixin;

import cn.frogdevour.FrogData;
import cn.frogdevour.FrogDevour;
import cn.frogdevour.GrowthRules;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingDimensionsMixin {
    @Inject(method = "getDimensions", at = @At("HEAD"), cancellable = true)
    private void frogdevour$dimensions(EntityPose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        if (FrogDevour.initialized && (Object) this instanceof PlayerEntity player && !player.isSpectator()) {
            GrowthRules.Stage stage = FrogData.stage(player);
            cir.setReturnValue(EntityDimensions.changing(stage.width(), stage.height()).withEyeHeight(stage.eyeHeight()));
        }
    }
}
