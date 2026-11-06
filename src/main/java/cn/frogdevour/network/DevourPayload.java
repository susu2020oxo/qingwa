package cn.frogdevour.network;

import cn.frogdevour.FrogDevour;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/** Clients request an attack only. The server chooses every target and reward. */
public record DevourPayload() implements CustomPayload {
    public static final DevourPayload INSTANCE = new DevourPayload();
    public static final Id<DevourPayload> ID = new Id<>(FrogDevour.id("devour"));
    public static final PacketCodec<RegistryByteBuf, DevourPayload> CODEC = PacketCodec.unit(INSTANCE);
    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
