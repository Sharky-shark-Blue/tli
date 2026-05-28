package io.tlipoca.mod.block;

import io.tlipoca.mod.yard.YardManager;
import io.tlipoca.mod.yard.YardAlchemyManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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

public class YardAlchemyTableBlock extends Block {
    public YardAlchemyTableBlock(BlockBehaviour.Properties properties) {
        super(properties);
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
        if (!YardManager.isInYard(serverPlayer)) {
            serverPlayer.displayClientMessage(Component.literal("庭院锚点过远，炼金无法稳定。"), true);
            return;
        }
        YardAlchemyManager.tryAlchemy(serverPlayer, level, pos);
    }
}
