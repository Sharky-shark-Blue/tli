package io.tlipoca.mod.block.entity;

import io.tlipoca.mod.TlipocaMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class SoulContainerBlockEntity extends BlockEntity {
    public static final int MAX_SOULS = 24;
    private static final String STORED_SOULS_KEY = "StoredSouls";

    private int storedSouls;

    public SoulContainerBlockEntity(BlockPos pos, BlockState state) {
        super(TlipocaMod.SOUL_CONTAINER_BLOCK_ENTITY.get(), pos, state);
    }

    public int getStoredSouls() {
        return storedSouls;
    }

    public boolean isFull() {
        return storedSouls >= MAX_SOULS;
    }

    public int addSouls(int amount) {
        if (amount <= 0 || isFull()) {
            return 0;
        }

        int transferred = Math.min(amount, MAX_SOULS - storedSouls);
        storedSouls += transferred;
        markChangedAndUpdate();
        return transferred;
    }

    public boolean tryConsumeForReceipt() {
        if (storedSouls < 3) {
            return false;
        }

        storedSouls -= 3;
        markChangedAndUpdate();
        return true;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        storedSouls = Math.max(0, Math.min(MAX_SOULS, input.getIntOr(STORED_SOULS_KEY, 0)));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt(STORED_SOULS_KEY, storedSouls);
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
