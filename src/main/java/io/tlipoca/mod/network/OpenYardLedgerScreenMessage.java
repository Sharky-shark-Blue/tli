package io.tlipoca.mod.network;

import net.minecraft.network.FriendlyByteBuf;

public record OpenYardLedgerScreenMessage(boolean inYard, int san, int unlockedOracleCount, boolean firstFullScytheReleaseSeen, int totalSoulsReleased) {
    public static void encode(OpenYardLedgerScreenMessage message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.inYard());
        buffer.writeVarInt(message.san());
        buffer.writeVarInt(message.unlockedOracleCount());
        buffer.writeBoolean(message.firstFullScytheReleaseSeen());
        buffer.writeVarInt(message.totalSoulsReleased());
    }

    public static OpenYardLedgerScreenMessage decode(FriendlyByteBuf buffer) {
        return new OpenYardLedgerScreenMessage(buffer.readBoolean(), buffer.readVarInt(), buffer.readVarInt(), buffer.readBoolean(), buffer.readVarInt());
    }
}
