package cn.frogdevour.mixin;

import cn.frogdevour.FrogDevour;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class FrogBreathingMixin {
    @Inject(method = "canBreatheInWater", at = @At("HEAD"), cancellable = true)
    private void frogdevour$amphibious(CallbackInfoReturnable<Boolean> cir) {
        if (FrogDevour.initialized && (Object) this instanceof PlayerEntity) cir.setReturnValue(true);
    }
}
