package io.tlipoca.mod.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public record ClaimBountyMessage(BlockPos boardPos, int bountyIndex) {
    public static void encode(ClaimBountyMessage message, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(message.boardPos());
        buffer.writeVarInt(message.bountyIndex());
    }

    public static ClaimBountyMessage decode(FriendlyByteBuf buffer) {
        return new ClaimBountyMessage(buffer.readBlockPos(), buffer.readVarInt());
    }
}
