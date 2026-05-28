package io.tlipoca.mod.block.entity;

import io.tlipoca.mod.TlipocaMod;
import io.tlipoca.mod.bounty.BountyManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BountyBoardBlockEntity extends BlockEntity {
    private static final String LAST_REFRESH_KEY = "LastRefreshGameTime";
    private static final String BOUNTY_ID_KEY_PREFIX = "BountyId";
    private static final String CLAIMED_KEY_PREFIX = "Claimed";

    private long lastRefreshGameTime = Long.MIN_VALUE;
    private final String[] bountyIds = new String[BountyManager.SLOT_COUNT];
    private final boolean[] claimed = new boolean[BountyManager.SLOT_COUNT];

    public BountyBoardBlockEntity(BlockPos pos, BlockState state) {
        super(TlipocaMod.BOUNTY_BOARD_BLOCK_ENTITY.get(), pos, state);
        resetSlots();
    }

    public void refreshIfNeeded(Level level) {
        long gameTime = level.getGameTime();
        if (lastRefreshGameTime == Long.MIN_VALUE || gameTime - lastRefreshGameTime >= BountyManager.BOARD_REFRESH_TICKS) {
            refresh(gameTime);
        }
    }

    public long getTicksUntilRefresh(Level level) {
        refreshIfNeeded(level);
        return Math.max(0L, BountyManager.BOARD_REFRESH_TICKS - (level.getGameTime() - lastRefreshGameTime));
    }

    public String getBountyId(int slot) {
        if (slot < 0 || slot >= bountyIds.length) {
            return BountyManager.getIdForSlot(0);
        }
        return bountyIds[slot];
    }

    public boolean isClaimed(int slot) {
        return slot >= 0 && slot < claimed.length && claimed[slot];
    }

    public boolean claim(int slot) {
        if (slot < 0 || slot >= claimed.length || claimed[slot]) {
            return false;
        }
        claimed[slot] = true;
        markChangedAndUpdate();
        return true;
    }

    private void refresh(long gameTime) {
        lastRefreshGameTime = gameTime;
        resetSlots();
        markChangedAndUpdate();
    }

    private void resetSlots() {
        for (int slot = 0; slot < BountyManager.SLOT_COUNT; slot++) {
            bountyIds[slot] = BountyManager.getIdForSlot(slot);
            claimed[slot] = false;
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        lastRefreshGameTime = input.getLongOr(LAST_REFRESH_KEY, Long.MIN_VALUE);
        for (int slot = 0; slot < BountyManager.SLOT_COUNT; slot++) {
            bountyIds[slot] = input.getStringOr(BOUNTY_ID_KEY_PREFIX + slot, BountyManager.getIdForSlot(slot));
            claimed[slot] = input.getBooleanOr(CLAIMED_KEY_PREFIX + slot, false);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong(LAST_REFRESH_KEY, lastRefreshGameTime);
        for (int slot = 0; slot < BountyManager.SLOT_COUNT; slot++) {
            output.putString(BOUNTY_ID_KEY_PREFIX + slot, bountyIds[slot]);
            output.putBoolean(CLAIMED_KEY_PREFIX + slot, claimed[slot]);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    private void markChangedAndUpdate() {
        setChanged();
        if (level != null) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }
}
