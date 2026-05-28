package io.tlipoca.mod.block.entity;

import io.tlipoca.mod.TlipocaMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class YardOfferingPedestalBlockEntity extends BlockEntity {
    private static final String OFFERING_KEY = "Offering";
    private ItemStack offering = ItemStack.EMPTY;

    public YardOfferingPedestalBlockEntity(BlockPos pos, BlockState state) {
        super(TlipocaMod.YARD_OFFERING_PEDESTAL_BLOCK_ENTITY.get(), pos, state);
    }

    public ItemStack getOffering() {
        return offering;
    }

    public boolean isEmpty() {
        return offering.isEmpty();
    }

    public void setOffering(ItemStack stack) {
        offering = stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
        markChangedAndUpdate();
    }

    public ItemStack removeOffering() {
        ItemStack removed = offering;
        offering = ItemStack.EMPTY;
        markChangedAndUpdate();
        return removed;
    }

    public ItemStack getOfferingForDrop() {
        return offering.copy();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        offering = input.read(OFFERING_KEY, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store(OFFERING_KEY, ItemStack.OPTIONAL_CODEC, offering);
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
