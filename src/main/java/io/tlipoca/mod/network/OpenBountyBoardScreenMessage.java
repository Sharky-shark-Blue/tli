package io.tlipoca.mod.network;

import io.tlipoca.mod.bounty.BountyManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public record OpenBountyBoardScreenMessage(BlockPos boardPos, String[] bountyIds, boolean[] claimed, long ticksUntilRefresh) {
    public static void encode(OpenBountyBoardScreenMessage message, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(message.boardPos());
        for (int slot = 0; slot < BountyManager.SLOT_COUNT; slot++) {
            buffer.writeUtf(message.bountyIds()[slot]);
            buffer.writeBoolean(message.claimed()[slot]);
        }
        buffer.writeVarLong(message.ticksUntilRefresh());
    }

    public static OpenBountyBoardScreenMessage decode(FriendlyByteBuf buffer) {
        BlockPos boardPos = buffer.readBlockPos();
        String[] bountyIds = new String[BountyManager.SLOT_COUNT];
        boolean[] claimed = new boolean[BountyManager.SLOT_COUNT];
        for (int slot = 0; slot < BountyManager.SLOT_COUNT; slot++) {
            bountyIds[slot] = buffer.readUtf();
            claimed[slot] = buffer.readBoolean();
        }
        return new OpenBountyBoardScreenMessage(boardPos, bountyIds, claimed, buffer.readVarLong());
    }
}
