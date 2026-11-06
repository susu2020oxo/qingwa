package cn.frogdevour.client;

import cn.frogdevour.DevourManager;
import cn.frogdevour.FrogData;
import cn.frogdevour.FrogDevour;
import cn.frogdevour.network.TonguePayload;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TongueEffects {
    private record Effect(TonguePayload payload, int started) { }
    private record Segment(Vec3d from, Vec3d to, double radius, int light) { }
    private static final RenderStateDataKey<List<Segment>> SEGMENTS = RenderStateDataKey.create(() -> "frogdevour:tongues");
    private static final Map<Integer, Effect> EFFECTS = new HashMap<>();
    private static final RenderLayer LAYER = RenderLayer.getEntitySolid(FrogDevour.id("textures/entity/tongue.png"));
    private static final ModelPart TONGUE = createTongue();
    private static int ticks;
    private TongueEffects() { }

    private static ModelPart createTongue() {
        ModelData model = new ModelData();
        model.getRoot().addChild("tongue", ModelPartBuilder.create().uv(0, 0)
                .cuboid(-0.5f, -0.5f, 0, 1, 1, 16), ModelTransform.NONE);
        return TexturedModelData.of(model, 64, 64).createModel().getChild("tongue");
    }

    public static void clear() { EFFECTS.clear(); ticks = 0; }
    public static void receive(TonguePayload packet) { EFFECTS.put(packet.owner(), new Effect(packet, ticks)); }
    public static void tick() {
        ticks++;
        EFFECTS.values().removeIf(effect -> ticks - effect.started() >= DevourManager.ANIMATION_TICKS);
    }
    public static float elapsed(int owner) {
        Effect effect = EFFECTS.get(owner);
        return effect == null ? -1 : ticks - effect.started();
    }

    public static void extract(WorldExtractionContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        List<Segment> segments = new ArrayList<>();
        float delta = context.tickCounter().getTickProgress(false);
        if (context.world() != null) {
            for (Effect effect : EFFECTS.values()) {
                Entity owner = context.world().getEntityById(effect.payload().owner());
                if (!(owner instanceof PlayerEntity player) || player.isSpectator()) continue;
                float age = ticks - effect.started() + delta;
                double extension = age < 4 ? age / 4.0 : Math.max(0, (12.0 - age) / 8.0);
                Vec3d mouth = player.getLerpedPos(delta).add(0, FrogData.stage(player).eyeHeight() * 0.85, 0)
                        .add(player.getRotationVec(delta).multiply(FrogData.stage(player).width() * 0.45));
                if (player == client.player && client.options.getPerspective().isFirstPerson()) {
                    mouth = player.getCameraPosVec(delta).add(0, -0.18, 0).add(player.getRotationVec(delta).multiply(0.3));
                }
                double radius = 0.035 * Math.sqrt(FrogData.stage(player).modelScale);
                int light = WorldRenderer.getLightmapCoordinates(context.world(), player.getBlockPos());
                if (effect.payload().targets().isEmpty()) {
                    segments.add(new Segment(mouth, mouth.lerp(effect.payload().miss(), extension), radius, light));
                } else {
                    for (TonguePayload.Target target : effect.payload().targets()) {
                        Entity entity = context.world().getEntityById(target.id());
                        Vec3d end = entity == null ? target.pos() : entity.getLerpedPos(delta).add(0, entity.getHeight() * 0.5, 0);
                        segments.add(new Segment(mouth, mouth.lerp(end, extension), radius, light));
                    }
                }
            }
        }
        context.worldState().setData(SEGMENTS, List.copyOf(segments));
    }

    public static void render(WorldRenderContext context) {
        List<Segment> segments = context.worldState().getData(SEGMENTS);
        if (segments == null || segments.isEmpty()) return;
        Vec3d camera = context.worldState().cameraRenderState.pos;
        MatrixStack matrices = context.matrices();
        matrices.push();
        try {
            matrices.translate(-camera.x, -camera.y, -camera.z);
            for (Segment segment : segments) {
                tube(matrices, context.commandQueue(), segment.from(), segment.to(), segment.radius(), segment.light());
            }
        } finally {
            matrices.pop();
        }
    }

    private static void tube(MatrixStack matrices, OrderedRenderCommandQueue queue, Vec3d from, Vec3d to, double radius, int light) {
        Vec3d axis = to.subtract(from);
        double length = axis.length();
        if (length < 0.03) return;
        Vec3d direction = axis.multiply(1.0 / length);
        matrices.push();
        try {
            matrices.translate(from.x, from.y, from.z);
            matrices.multiply(new Quaternionf().rotationTo(0, 0, 1, (float) direction.x, (float) direction.y, (float) direction.z));
            matrices.scale((float) (radius * 32), (float) (radius * 32), (float) length);
            // Standard opaque entity geometry supplies UVs, lighting and normals to Iris.
            // Submission never flushes an immediate buffer or changes global render state.
            queue.getBatchingQueue(0).submitModelPart(TONGUE, matrices, LAYER, light, OverlayTexture.DEFAULT_UV, null);
        } finally {
            matrices.pop();
        }
    }
}
