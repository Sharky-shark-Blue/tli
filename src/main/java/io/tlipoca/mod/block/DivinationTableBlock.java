package io.tlipoca.mod.block;

import io.tlipoca.mod.network.TlipocaNetwork;
import io.tlipoca.mod.oracle.OracleManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class DivinationTableBlock extends Block {
    public DivinationTableBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        handleUse(level, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        handleUse(level, player);
        return InteractionResult.SUCCESS;
    }

    private static void handleUse(Level level, Player player) {
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (player.isShiftKeyDown()) {
            TlipocaNetwork.openOracleScreen(serverPlayer);
            return;
        }
        OracleManager.tryDivine(serverPlayer);
    }
}
