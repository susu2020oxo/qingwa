package cn.frogdevour.client;

import cn.frogdevour.GrowthRules.Stage;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.model.FrogEntityModel;
import net.minecraft.client.render.entity.state.FrogEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityPose;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

public final class FrogVisuals {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/frog/temperate_frog.png");
    private FrogVisuals() { }

    public static void render(FrogEntityModel model, PlayerEntityRenderState player, FrogPlayerState data,
                              MatrixStack matrices, OrderedRenderCommandQueue queue) {
        Stage stage = data.frogdevour$stage();
        if (player.invisible) return;
        FrogEntityRenderState frog = new FrogEntityRenderState();
        frog.age = player.age;
        frog.bodyYaw = player.bodyYaw;
        frog.relativeHeadYaw = player.relativeHeadYaw;
        frog.pitch = player.pitch;
        frog.limbSwingAnimationProgress = player.limbSwingAnimationProgress;
        frog.limbSwingAmplitude = player.limbSwingAmplitude;
        frog.baseScale = 1;
        frog.ageScale = 1;
        frog.pose = EntityPose.STANDING;
        frog.touchingWater = player.touchingWater;
        frog.insideWaterOrBubbleColumn = player.touchingWater;
        frog.texture = TEXTURE;
        frog.hurt = player.hurt;
        frog.deathTime = player.deathTime;
        float tongueAge = TongueEffects.elapsed(player.id);
        if (tongueAge >= 0) frog.usingTongueAnimationState.start((int) (player.age - tongueAge));
        if (data.frogdevour$jumping()) frog.longJumpingAnimationState.start((int) player.age - 2);
        if (player.touchingWater) frog.idlingInWaterAnimationState.start(0);

        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - player.bodyYaw));
        matrices.scale(-stage.modelScale, -stage.modelScale, stage.modelScale);
        matrices.translate(0, -1.501, 0);
        queue.getBatchingQueue(0).submitModel(model, frog, matrices, RenderLayer.getEntityCutoutNoCull(TEXTURE),
                player.light, OverlayTexture.getUv(0, player.hurt), player.outlineColor, null);
        matrices.pop();
    }
}
