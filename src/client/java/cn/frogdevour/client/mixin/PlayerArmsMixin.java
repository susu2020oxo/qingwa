package cn.frogdevour.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerArmsMixin {
    @Inject(method = {"renderRightArm", "renderLeftArm"}, at = @At("HEAD"), cancellable = true)
    private void frogdevour$hideHumanArms(MatrixStack matrices, OrderedRenderCommandQueue queue, int light,
                                         Identifier texture, boolean sleeve, CallbackInfo ci) {
        var player = MinecraftClient.getInstance().player;
        if (player != null && !player.isSpectator()) ci.cancel();
    }
}
