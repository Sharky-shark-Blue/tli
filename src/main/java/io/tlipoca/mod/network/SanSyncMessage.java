package io.tlipoca.mod.network;

import net.minecraft.network.FriendlyByteBuf;

public record SanSyncMessage(int san, boolean forbiddenActive) {
    public static void encode(SanSyncMessage message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.san());
        buffer.writeBoolean(message.forbiddenActive());
    }

    public static SanSyncMessage decode(FriendlyByteBuf buffer) {
        return new SanSyncMessage(buffer.readVarInt(), buffer.readBoolean());
    }
}
