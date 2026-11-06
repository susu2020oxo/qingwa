package cn.frogdevour.client.mixin;

import cn.frogdevour.FrogData;
import cn.frogdevour.client.FrogPlayerState;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Inject(method = "updateRenderState", at = @At("TAIL"))
    private void frogdevour$capture(Entity entity, EntityRenderState state, float tickProgress, CallbackInfo ci) {
        if (state instanceof FrogPlayerState frog) {
            frog.frogdevour$stage(entity instanceof PlayerEntity player && !player.isSpectator() ? FrogData.stage(player) : null);
            frog.frogdevour$jumping(!entity.isOnGround() && !entity.isTouchingWater());
        }
    }
}
