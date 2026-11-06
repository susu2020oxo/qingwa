package cn.frogdevour.client.mixin;

import cn.frogdevour.KeyConflictRules;
import cn.frogdevour.client.FrogDevourClient;
import cn.frogdevour.network.DevourPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyBinding.class)
public abstract class IrisReloadKeyMixin {
    @Shadow private int timesPressed;

    @Inject(method = "wasPressed", at = @At("HEAD"), cancellable = true)
    private void frogdevour$preventReloadConflict(CallbackInfoReturnable<Boolean> cir) {
        KeyBinding binding = (KeyBinding) (Object) this;
        if (!KeyConflictRules.IRIS_RELOAD.equals(binding.getId())) return;
        KeyBinding devour = FrogDevourClient.devourKey;
        if (devour == null) return;
        MinecraftClient client = MinecraftClient.getInstance();
        boolean playingFrog = client.world != null && client.player != null && client.currentScreen == null
                && !client.player.isSpectator() && ClientPlayNetworking.canSend(DevourPayload.ID);
        if (KeyConflictRules.suppressReload(binding.getId(), binding.getBoundKeyTranslationKey(),
                devour.getBoundKeyTranslationKey(), playingFrog)) {
            // Discard the Iris click, not the devour click; do not save or rewrite user bindings.
            // Draining also prevents a delayed reload when a menu is opened after pressing R.
            timesPressed = 0;
            cir.setReturnValue(false);
        }
    }
}
