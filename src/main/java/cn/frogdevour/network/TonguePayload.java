package cn.frogdevour.network;

import cn.frogdevour.FrogDevour;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public record TonguePayload(int owner, List<Target> targets, Vec3d miss) implements CustomPayload {
    public record Target(int id, Vec3d pos) { }
    public TonguePayload { targets = List.copyOf(targets); }
    public static final Id<TonguePayload> ID = new Id<>(FrogDevour.id("tongues"));
    public static final PacketCodec<RegistryByteBuf, TonguePayload> CODEC = new PacketCodec<>() {
        @Override public TonguePayload decode(RegistryByteBuf buf) {
            int owner = buf.readVarInt();
            int count = buf.readVarInt();
            if (count < 0 || count > 27) throw new IllegalArgumentException("Invalid tongue count");
            List<Target> targets = new ArrayList<>(count);
            for (int i = 0; i < count; i++) targets.add(new Target(buf.readVarInt(), readVec(buf)));
            return new TonguePayload(owner, targets, readVec(buf));
        }
        @Override public void encode(RegistryByteBuf buf, TonguePayload value) {
            buf.writeVarInt(value.owner());
            buf.writeVarInt(value.targets().size());
            for (Target target : value.targets()) {
                buf.writeVarInt(target.id());
                writeVec(buf, target.pos());
            }
            writeVec(buf, value.miss());
        }
    };
    private static Vec3d readVec(RegistryByteBuf buf) { return new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble()); }
    private static void writeVec(RegistryByteBuf buf, Vec3d pos) { buf.writeDouble(pos.x); buf.writeDouble(pos.y); buf.writeDouble(pos.z); }
    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
