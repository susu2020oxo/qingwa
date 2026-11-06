package cn.frogdevour.client;

import cn.frogdevour.DevourManager;
import cn.frogdevour.FrogData;
import cn.frogdevour.network.TonguePayload;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

public final class TongueEffects {
    private record Effect(TonguePayload payload, int started) { }
    private static final Map<Integer, Effect> EFFECTS = new HashMap<>();
    private static int ticks;
    private TongueEffects() { }

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

    public static void render(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || EFFECTS.isEmpty()) return;
        Vec3d camera = context.worldState().cameraRenderState.pos;
        float delta = client.getRenderTickCounter().getTickProgress(false);
        MatrixStack matrices = context.matrices();
        matrices.push();
        matrices.translate(-camera.x, -camera.y, -camera.z);
        VertexConsumer vertices = context.consumers().getBuffer(RenderLayer.getDebugFilledBox());
        for (Effect effect : EFFECTS.values()) {
            Entity owner = client.world.getEntityById(effect.payload().owner());
            if (!(owner instanceof PlayerEntity player)) continue;
            float age = ticks - effect.started() + delta;
            double extension = age < 4 ? age / 4.0 : Math.max(0, (12.0 - age) / 8.0);
            Vec3d mouth = player.getLerpedPos(delta).add(0, FrogData.stage(player).eyeHeight() * 0.85, 0)
                    .add(player.getRotationVec(delta).multiply(FrogData.stage(player).width() * 0.45));
            if (player == client.player && client.options.getPerspective().isFirstPerson()) {
                mouth = player.getCameraPosVec(delta).add(0, -0.18, 0).add(player.getRotationVec(delta).multiply(0.3));
            }
            double radius = 0.035 * Math.sqrt(FrogData.stage(player).modelScale);
            if (effect.payload().targets().isEmpty()) {
                tube(matrices.peek().getPositionMatrix(), vertices, mouth, mouth.lerp(effect.payload().miss(), extension), radius);
            } else {
                for (TonguePayload.Target target : effect.payload().targets()) {
                    Entity entity = client.world.getEntityById(target.id());
                    Vec3d end = entity == null ? target.pos() : entity.getLerpedPos(delta).add(0, entity.getHeight() * 0.5, 0);
                    tube(matrices.peek().getPositionMatrix(), vertices, mouth, mouth.lerp(end, extension), radius);
                }
            }
        }
        matrices.pop();
    }

    private static void tube(Matrix4f matrix, VertexConsumer vertices, Vec3d from, Vec3d to, double radius) {
        Vec3d axis = to.subtract(from);
        if (axis.lengthSquared() < 0.001) return;
        Vec3d u = axis.normalize().crossProduct(Math.abs(axis.normalize().y) > 0.95 ? new Vec3d(1, 0, 0) : new Vec3d(0, 1, 0)).normalize().multiply(radius);
        Vec3d v = axis.normalize().crossProduct(u).normalize().multiply(radius);
        Vec3d[] corners = {u.add(v), u.subtract(v), u.multiply(-1).subtract(v), v.subtract(u)};
        for (int i = 0; i < 4; i++) {
            Vec3d a = corners[i], b = corners[(i + 1) % 4];
            vertex(matrix, vertices, from.add(a));
            vertex(matrix, vertices, to.add(a));
            vertex(matrix, vertices, to.add(b));
            vertex(matrix, vertices, from.add(b));
        }
    }
    private static void vertex(Matrix4f matrix, VertexConsumer vertices, Vec3d pos) {
        vertices.vertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z).color(240, 115, 145, 255);
    }
}
