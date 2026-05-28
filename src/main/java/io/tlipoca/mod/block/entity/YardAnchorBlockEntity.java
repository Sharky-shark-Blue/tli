package io.tlipoca.mod.block.entity;

import io.tlipoca.mod.TlipocaMod;
import io.tlipoca.mod.yard.YardManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class YardAnchorBlockEntity extends BlockEntity {
    public YardAnchorBlockEntity(BlockPos pos, BlockState state) {
        super(TlipocaMod.YARD_ANCHOR_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel serverLevel) {
            YardManager.register(serverLevel, worldPosition);
        }
    }

    @Override
    public void setRemoved() {
        if (level instanceof ServerLevel serverLevel) {
            YardManager.unregister(serverLevel, worldPosition);
        }
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        if (level instanceof ServerLevel serverLevel) {
            YardManager.unregister(serverLevel, worldPosition);
        }
        super.onChunkUnloaded();
    }
}
