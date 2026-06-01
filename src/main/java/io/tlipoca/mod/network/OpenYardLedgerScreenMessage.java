package io.tlipoca.mod.network;

import net.minecraft.network.FriendlyByteBuf;

public record OpenYardLedgerScreenMessage(
    boolean inYard,
    int san,
    int unlockedOracleCount,
    boolean firstFullScytheReleaseSeen,
    int totalSoulsReleased,
    int comfort,
    int otherworld,
    int memory,
    String stageName,
    String stageDescription,
    String stageHint,
    boolean hasMistLetter,
    int mistLettersAnswered,
    boolean hasMistVisitor,
    int mistVisitorsHelped,
    int soulContainerCount,
    int storedSouls,
    int maxSoulCapacity
) {
    public static void encode(OpenYardLedgerScreenMessage message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.inYard());
        buffer.writeVarInt(message.san());
        buffer.writeVarInt(message.unlockedOracleCount());
        buffer.writeBoolean(message.firstFullScytheReleaseSeen());
        buffer.writeVarInt(message.totalSoulsReleased());
        buffer.writeVarInt(message.comfort());
        buffer.writeVarInt(message.otherworld());
        buffer.writeVarInt(message.memory());
        buffer.writeUtf(message.stageName());
        buffer.writeUtf(message.stageDescription());
        buffer.writeUtf(message.stageHint());
        buffer.writeBoolean(message.hasMistLetter());
        buffer.writeVarInt(message.mistLettersAnswered());
        buffer.writeBoolean(message.hasMistVisitor());
        buffer.writeVarInt(message.mistVisitorsHelped());
        buffer.writeVarInt(message.soulContainerCount());
        buffer.writeVarInt(message.storedSouls());
        buffer.writeVarInt(message.maxSoulCapacity());
    }

    public static OpenYardLedgerScreenMessage decode(FriendlyByteBuf buffer) {
        return new OpenYardLedgerScreenMessage(
            buffer.readBoolean(),
            buffer.readVarInt(),
            buffer.readVarInt(),
            buffer.readBoolean(),
            buffer.readVarInt(),
            buffer.readVarInt(),
            buffer.readVarInt(),
            buffer.readVarInt(),
            buffer.readUtf(),
            buffer.readUtf(),
            buffer.readUtf(),
            buffer.readBoolean(),
            buffer.readVarInt(),
            buffer.readBoolean(),
            buffer.readVarInt(),
            buffer.readVarInt(),
            buffer.readVarInt(),
            buffer.readVarInt()
        );
    }
}
