package cn.frogdevour;

import cn.frogdevour.GrowthRules.PreySize;
import cn.frogdevour.GrowthRules.Stage;
import cn.frogdevour.network.TonguePayload;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class DevourManager {
    public static final int ANIMATION_TICKS = 12;
    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();
    private static final List<Meal> MEALS = new ArrayList<>();
    private static final Set<UUID> RESERVED = new HashSet<>();
    private static long clock;
    private record Meal(ServerPlayerEntity player, ServerWorld world, List<MobEntity> prey, int due, Stage stage) { }
    private DevourManager() { }

    public static void clear() { COOLDOWNS.clear(); MEALS.clear(); RESERVED.clear(); clock = 0; }
    public static void disconnect(ServerPlayerEntity player) { COOLDOWNS.remove(player.getUuid()); }

    public static void attack(ServerPlayerEntity player) {
        if (!player.isAlive() || player.isSpectator()) return;
        if (clock < COOLDOWNS.getOrDefault(player.getUuid(), 0L)) return;
        Stage stage = FrogData.stage(player);
        COOLDOWNS.put(player.getUuid(), clock + stage.cooldownTicks);
        ServerWorld world = player.getServerWorld();
        Vec3d start = player.getEyePos();
        Vec3d look = player.getRotationVec(1.0f);
        Vec3d end = start.add(look.multiply(stage.reach));
        Vec3d miss = world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE, player)).getPos();
        Box search = new Box(start, end).expand(stage.reach * 0.85 + 2.0);
        List<MobEntity> candidates = world.getEntitiesByClass(MobEntity.class, search,
                mob -> mob.isAlive() && !mob.isRemoved() && !RESERVED.contains(mob.getUuid())
                        && !mob.isInvulnerable() && !mob.hasPassengers() && !mob.hasVehicle()
                        && inAim(player, mob, stage, start, look));
        // Aimed creatures take priority; distance breaks ties within that aim score.
        candidates.sort(Comparator.comparingDouble(mob -> aimScore(start, look, mob)));
        List<MobEntity> selected = new ArrayList<>();
        FeedingBudget budget = new FeedingBudget(stage);
        boolean tooBig = false;
        for (MobEntity mob : candidates) {
            PreySize size = PreyClassifier.size(mob);
            if (!GrowthRules.canEat(stage, size)) { tooBig = true; continue; }
            if (!budget.take(size)) continue;
            selected.add(mob);
            if (budget.remaining() == 0) break;
        }
        List<TonguePayload.Target> visuals = selected.stream()
                .map(mob -> new TonguePayload.Target(mob.getId(), mob.getBoundingBox().getCenter())).toList();
        TonguePayload packet = new TonguePayload(player.getId(), visuals, miss);
        Set<ServerPlayerEntity> viewers = new HashSet<>(PlayerLookup.tracking(player));
        viewers.add(player);
        for (ServerPlayerEntity viewer : viewers) {
            if (ServerPlayNetworking.canSend(viewer, TonguePayload.ID)) ServerPlayNetworking.send(viewer, packet);
        }
        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_FROG_TONGUE,
                SoundCategory.PLAYERS, 1.0f, stage == Stage.GIANT ? 0.7f : 1.0f);
        if (!selected.isEmpty()) {
            selected.forEach(mob -> RESERVED.add(mob.getUuid()));
            MEALS.add(new Meal(player, world, List.copyOf(selected), player.age + ANIMATION_TICKS, stage));
        } else {
            player.sendMessage(Text.translatable(tooBig ? "message.frogdevour.too_big" : "message.frogdevour.no_target"), true);
        }
    }

    private static double aimScore(Vec3d start, Vec3d look, MobEntity mob) {
        Vec3d offset = mob.getBoundingBox().getCenter().subtract(start);
        return (1.0 - look.dotProduct(offset.normalize())) * 100.0 + offset.length();
    }

    private static boolean inAim(ServerPlayerEntity player, MobEntity mob, Stage stage, Vec3d start, Vec3d look) {
        Vec3d point = nearestPoint(mob.getBoundingBox(), start.add(look.multiply(
                Math.max(0, mob.getBoundingBox().getCenter().subtract(start).dotProduct(look)))));
        if (start.squaredDistanceTo(point) > stage.reach * stage.reach) return false;
        if (stage.index < 2) {
            if (mob.getBoundingBox().expand(0.3).raycast(start, start.add(look.multiply(stage.reach))).isEmpty()
                    && !mob.getBoundingBox().contains(start)) return false;
        } else {
            Vec3d offset = point.subtract(start);
            if (offset.lengthSquared() > 0.01 && offset.normalize().dotProduct(look) < Math.cos(Math.toRadians(50))) return false;
        }
        return visible(player, mob, point);
    }

    private static Vec3d nearestPoint(Box box, Vec3d point) {
        return new Vec3d(Math.clamp(point.x, box.minX, box.maxX), Math.clamp(point.y, box.minY, box.maxY),
                Math.clamp(point.z, box.minZ, box.maxZ));
    }

    private static boolean visible(ServerPlayerEntity player, MobEntity mob, Vec3d point) {
        HitResult hit = player.getServerWorld().raycast(new RaycastContext(player.getEyePos(), point,
                RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, player));
        return hit.getType() == HitResult.Type.MISS || hit.getPos().squaredDistanceTo(point) < 0.1;
    }

    public static void tick(MinecraftServer server) {
        clock++;
        Iterator<Meal> iterator = MEALS.iterator();
        while (iterator.hasNext()) {
            Meal meal = iterator.next();
            ServerPlayerEntity player = meal.player();
            if (player.isRemoved() || !player.isAlive() || player.isSpectator() || player.getServerWorld() != meal.world()) {
                release(meal);
                iterator.remove();
                continue;
            }
            if (player.age < meal.due()) {
                // Start the pull only after the tongue has reached its target.
                if (player.age >= meal.due() - 8) {
                    for (MobEntity mob : meal.prey()) {
                        if (!mob.isAlive() || mob.getEntityWorld() != meal.world() || mob instanceof EnderDragonEntity) continue;
                        Vec3d pull = player.getEyePos().subtract(mob.getBoundingBox().getCenter());
                        if (pull.lengthSquared() > 0.2 && visible(player, mob, mob.getBoundingBox().getCenter())) {
                            mob.setVelocity(pull.normalize().multiply(Math.min(1.5, pull.length() * 0.35)));
                            mob.velocityModified = true;
                        }
                    }
                }
                continue;
            }
            int food = FrogData.food(player);
            Stage before = GrowthRules.stage(food);
            int eaten = 0;
            for (MobEntity mob : meal.prey()) {
                if (!mob.isAlive() || mob.isRemoved() || mob.getEntityWorld() != meal.world()) continue;
                PreySize size = PreyClassifier.size(mob);
                Vec3d point = nearestPoint(mob.getBoundingBox(), player.getEyePos());
                if (!GrowthRules.canEat(meal.stage(), size) || !GrowthRules.canEat(FrogData.stage(player), size)
                        || player.getEyePos().squaredDistanceTo(point) > Math.pow(meal.stage().reach + 2, 2)
                        || !visible(player, mob, point) || mob.isInvulnerable() || mob.hasPassengers() || mob.hasVehicle()) continue;
                // Preserve the exit portal, egg and gateway when swallowing the real End fight dragon.
                if (mob instanceof EnderDragonEntity dragon && dragon.getFight() != null) dragon.getFight().dragonKilled(dragon);
                meal.world().spawnParticles(ParticleTypes.HAPPY_VILLAGER, mob.getX(), mob.getBodyY(0.5), mob.getZ(),
                        8, Math.min(mob.getWidth(), 1), 0.3, Math.min(mob.getWidth(), 1), 0.02);
                // Swallow whole: no corpse, slime split, or duplicate death drops.
                mob.remove(Entity.RemovalReason.KILLED);
                food = GrowthRules.add(food, size);
                eaten++;
            }
            if (eaten > 0) {
                FrogData.setFood(player, food);
                player.getHungerManager().add(eaten * 2, 0.6f);
                meal.world().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_BURP,
                        SoundCategory.PLAYERS, 0.8f, 0.8f);
                Stage after = GrowthRules.stage(food);
                player.sendMessage(after != before
                        ? Text.translatable("message.frogdevour.grew", after.chineseName)
                        : Text.translatable("message.frogdevour.ate", eaten), true);
            }
            release(meal);
            iterator.remove();
        }
        if (clock % 1200 == 0) COOLDOWNS.entrySet().removeIf(entry -> entry.getValue() < clock);
    }

    private static void release(Meal meal) { meal.prey().forEach(mob -> RESERVED.remove(mob.getUuid())); }
}
