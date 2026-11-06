package cn.frogdevour;

import cn.frogdevour.network.DevourPayload;
import cn.frogdevour.network.TonguePayload;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FrogDevour implements ModInitializer {
    public static final String MOD_ID = "frogdevour";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static boolean initialized;
    public static Identifier id(String path) { return Identifier.of(MOD_ID, path); }

    @Override public void onInitialize() {
        FrogData.initialize();
        PayloadTypeRegistry.playC2S().register(DevourPayload.ID, DevourPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(TonguePayload.ID, TonguePayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(DevourPayload.ID, (payload, context) -> DevourManager.attack(context.player()));
        ServerTickEvents.END_SERVER_TICK.register(DevourManager::tick);
        ServerTickEvents.END_SERVER_TICK.register(server -> server.getPlayerManager().getPlayerList().forEach(FrogStats::update));
        ServerLifecycleEvents.SERVER_STARTED.register(server -> DevourManager.clear());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> DevourManager.clear());
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            player.getAttachedOrCreate(FrogData.FOOD);
            player.calculateDimensions();
            FrogStats.update(player);
            player.sendMessage(Text.translatable("message.frogdevour.welcome"), false);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> DevourManager.disconnect(handler.player));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            newPlayer.calculateDimensions();
            FrogStats.update(newPlayer);
        });
        CommandRegistrationCallback.EVENT.register((dispatcher, access, environment) -> dispatcher.register(
                CommandManager.literal("frog")
                    .executes(context -> {
                        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                        context.getSource().sendFeedback(() -> Text.translatable("message.frogdevour.info",
                                FrogData.stage(player).chineseName, FrogData.food(player)), false);
                        return 1;
                    })
                    .then(CommandManager.literal("stage").requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.argument("level", IntegerArgumentType.integer(1, 4)).executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                            int level = IntegerArgumentType.getInteger(context, "level") - 1;
                            FrogData.setFood(player, GrowthRules.THRESHOLDS[level]);
                            context.getSource().sendFeedback(() -> Text.translatable("message.frogdevour.grew",
                                    FrogData.stage(player).chineseName), false);
                            return 1;
                        })))
                    .then(CommandManager.literal("reset").requires(source -> source.hasPermissionLevel(2)).executes(context -> {
                        FrogData.setFood(context.getSource().getPlayerOrThrow(), 0);
                        return 1;
                    }))));
        initialized = true;
        LOGGER.info("Frog Devour initialized for Minecraft 1.21.10");
    }
}
