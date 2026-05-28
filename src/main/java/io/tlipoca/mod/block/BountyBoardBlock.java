package io.tlipoca.mod.block;

import io.tlipoca.mod.block.entity.BountyBoardBlockEntity;
import io.tlipoca.mod.network.TlipocaNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class BountyBoardBlock extends Block implements EntityBlock {
    public BountyBoardBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BountyBoardBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        handleUse(level, pos, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        handleUse(level, pos, player);
        return InteractionResult.SUCCESS;
    }

    private static void handleUse(Level level, BlockPos pos, Player player) {
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (!(level.getBlockEntity(pos) instanceof BountyBoardBlockEntity board)) {
            serverPlayer.displayClientMessage(Component.literal("悬赏板正在等待墨迹干透。"), true);
            return;
        }

        board.refreshIfNeeded(level);
        TlipocaNetwork.openBountyBoardScreen(serverPlayer, pos);
    }
}
