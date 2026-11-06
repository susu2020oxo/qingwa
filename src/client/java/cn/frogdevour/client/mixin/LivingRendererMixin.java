package cn.frogdevour.client.mixin;

import cn.frogdevour.client.FrogPlayerState;
import cn.frogdevour.client.FrogVisuals;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.FrogEntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingRendererMixin extends EntityRenderer<LivingEntity, LivingEntityRenderState> {
    @Unique private FrogEntityModel frogdevour$model;
    protected LivingRendererMixin(EntityRendererFactory.Context context) { super(context); }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void frogdevour$createModel(EntityRendererFactory.Context context, EntityModel<?> model, float shadowRadius, CallbackInfo ci) {
        if ((Object) this instanceof PlayerEntityRenderer) frogdevour$model = new FrogEntityModel(context.getPart(EntityModelLayers.FROG));
    }

    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V",
            at = @At("HEAD"), cancellable = true)
    private void frogdevour$render(LivingEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue,
                                   CameraRenderState camera, CallbackInfo ci) {
        if (frogdevour$model != null && state instanceof PlayerEntityRenderState player
                && state instanceof FrogPlayerState data && data.frogdevour$stage() != null) {
            FrogVisuals.render(frogdevour$model, player, data, matrices, queue);
            super.render(state, matrices, queue, camera);
            ci.cancel();
        }
    }
}
