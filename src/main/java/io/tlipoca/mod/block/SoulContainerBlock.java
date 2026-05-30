package io.tlipoca.mod.block;

import io.tlipoca.mod.TlipocaMod;
import io.tlipoca.mod.block.entity.SoulContainerBlockEntity;
import io.tlipoca.mod.item.TraineeReaperScytheItem;
import io.tlipoca.mod.yard.YardManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SoulContainerBlock extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 11.0D, 14.0D);

    public SoulContainerBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SoulContainerBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        handleUse(stack, level, pos, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        handleUse(ItemStack.EMPTY, level, pos, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    private static void handleUse(ItemStack stack, Level level, BlockPos pos, Player player) {
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (!(level.getBlockEntity(pos) instanceof SoulContainerBlockEntity container)) {
            serverPlayer.displayClientMessage(Component.literal("收魂容器还没有醒。"), true);
            return;
        }
        if (!YardManager.isInYard(serverPlayer)) {
            serverPlayer.displayClientMessage(Component.literal("容器离庭院锚点太远，无法稳定收容。"), true);
            return;
        }

        if (stack.is(TlipocaMod.TRAINEE_REAPER_SCYTHE.get())) {
            transferFromScythe(serverPlayer, stack, container);
            return;
        }

        extractReceipt(serverPlayer, container);
    }

    private static void transferFromScythe(ServerPlayer player, ItemStack stack, SoulContainerBlockEntity container) {
        int containedSouls = TraineeReaperScytheItem.getContainedSouls(stack);
        if (containedSouls <= 0) {
            player.displayClientMessage(Component.literal("容器里没有听见新的声音。收容：" + container.getStoredSouls() + " / " + SoulContainerBlockEntity.MAX_SOULS), true);
            return;
        }
        if (container.isFull()) {
            player.displayClientMessage(Component.literal("容器已经太满了。收容：" + container.getStoredSouls() + " / " + SoulContainerBlockEntity.MAX_SOULS), true);
            return;
        }

        int transferred = container.addSouls(containedSouls);
        if (transferred <= 0) {
            player.displayClientMessage(Component.literal("容器已经太满了。收容：" + container.getStoredSouls() + " / " + SoulContainerBlockEntity.MAX_SOULS), true);
            return;
        }

        TraineeReaperScytheItem.setContainedSouls(stack, containedSouls - transferred);
        TraineeReaperScytheItem.recordSoulRelease(player, transferred, containedSouls >= TraineeReaperScytheItem.MAX_CONTAINED_SOULS && transferred == containedSouls);
        player.displayClientMessage(Component.literal("容器记下了 " + transferred + " 个名字。收容：" + container.getStoredSouls() + " / " + SoulContainerBlockEntity.MAX_SOULS), true);
    }

    private static void extractReceipt(ServerPlayer player, SoulContainerBlockEntity container) {
        if (!container.tryConsumeForReceipt()) {
            player.displayClientMessage(Component.literal("还不够写成一张收据。收容：" + container.getStoredSouls() + " / " + SoulContainerBlockEntity.MAX_SOULS), true);
            return;
        }

        ItemStack receipt = new ItemStack(TlipocaMod.SOUL_RECEIPT.get());
        if (!player.getInventory().add(receipt)) {
            player.drop(receipt, false);
        }
        player.displayClientMessage(Component.literal("容器吐出一张空白收据。收容：" + container.getStoredSouls() + " / " + SoulContainerBlockEntity.MAX_SOULS), true);
    }
}
