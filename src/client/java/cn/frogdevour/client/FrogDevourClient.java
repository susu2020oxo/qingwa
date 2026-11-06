package cn.frogdevour.client;

import cn.frogdevour.FrogData;
import cn.frogdevour.FrogDevour;
import cn.frogdevour.GrowthRules.Stage;
import cn.frogdevour.network.DevourPayload;
import cn.frogdevour.network.TonguePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class FrogDevourClient implements ClientModInitializer {
    public static KeyBinding devourKey;
    private static final Map<UUID, Integer> SIZES = new HashMap<>();
    private static int cooldown;

    @Override public void onInitializeClient() {
        KeyBinding.Category category = KeyBinding.Category.create(FrogDevour.id("controls"));
        devourKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.frogdevour.devour", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, category));
        ClientPlayNetworking.registerGlobalReceiver(TonguePayload.ID, (packet, context) -> {
            TongueEffects.receive(packet);
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            TongueEffects.tick();
            if (cooldown > 0) cooldown--;
            while (devourKey.wasPressed()) {
                if (client.player != null && !client.player.isSpectator() && client.currentScreen == null && cooldown == 0
                        && ClientPlayNetworking.canSend(DevourPayload.ID)) {
                    ClientPlayNetworking.send(DevourPayload.INSTANCE);
                    cooldown = FrogData.stage(client.player).cooldownTicks;
                }
            }
            if (client.world == null) return;
            Set<UUID> present = new HashSet<>();
            for (PlayerEntity player : client.world.getPlayers()) {
                Stage stage = FrogData.stage(player);
                int size = player.isSpectator() ? -1 : stage.index;
                present.add(player.getUuid());
                Integer previous = SIZES.put(player.getUuid(), size);
                if (previous == null || previous != size) player.calculateDimensions();
            }
            SIZES.keySet().retainAll(present);
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            SIZES.clear();
            TongueEffects.clear();
            cooldown = 0;
        });
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, FrogDevour.id("growth"), FrogHud::render);
        WorldRenderEvents.END_EXTRACTION.register(TongueEffects::extract);
        WorldRenderEvents.BEFORE_ENTITIES.register(TongueEffects::render);
    }
}
