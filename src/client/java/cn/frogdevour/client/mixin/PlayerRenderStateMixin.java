package cn.frogdevour.client.mixin;

import cn.frogdevour.GrowthRules.Stage;
import cn.frogdevour.client.FrogPlayerState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerEntityRenderState.class)
public abstract class PlayerRenderStateMixin implements FrogPlayerState {
    @Unique private Stage frogdevour$stage;
    @Unique private boolean frogdevour$jumping;
    @Override public Stage frogdevour$stage() { return frogdevour$stage; }
    @Override public void frogdevour$stage(Stage stage) { frogdevour$stage = stage; }
    @Override public boolean frogdevour$jumping() { return frogdevour$jumping; }
    @Override public void frogdevour$jumping(boolean jumping) { frogdevour$jumping = jumping; }
}
