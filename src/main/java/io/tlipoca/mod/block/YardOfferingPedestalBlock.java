package io.tlipoca.mod.block;

import io.tlipoca.mod.block.entity.YardOfferingPedestalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class YardOfferingPedestalBlock extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 12.0D, 15.0D);

    public YardOfferingPedestalBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new YardOfferingPedestalBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, net.minecraft.world.phys.BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof YardOfferingPedestalBlockEntity pedestal) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        if (!pedestal.isEmpty()) {
            serverPlayer.displayClientMessage(Component.literal("供物台上已经有供物。"), true);
            return InteractionResult.SUCCESS;
        }
        if (stack.isEmpty()) {
            return InteractionResult.SUCCESS;
        }

        pedestal.setOffering(stack);
        if (!serverPlayer.isCreative()) {
            stack.shrink(1);
        }
        level.playSound(null, pos, SoundEvents.CHISELED_BOOKSHELF_INSERT, SoundSource.BLOCKS, 0.55F, 1.25F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5D, pos.getY() + 1.05D, pos.getZ() + 0.5D, 6, 0.22D, 0.12D, 0.22D, 0.01D);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, net.minecraft.world.phys.BlockHitResult hit) {
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

    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide() && player.getMainHandItem().isEmpty() && level.getBlockEntity(pos) instanceof YardOfferingPedestalBlockEntity pedestal) {
            dropOffering(level, pos, pedestal);
        }
        super.attack(state, level, pos, player);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof YardOfferingPedestalBlockEntity pedestal && !pedestal.isEmpty()) {
            Block.popResource(level, pos, pedestal.getOfferingForDrop());
            pedestal.removeOffering();
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    private static void dropOffering(Level level, BlockPos pos, YardOfferingPedestalBlockEntity pedestal) {
        if (pedestal.isEmpty()) {
            return;
        }
        ItemStack offering = pedestal.removeOffering();
        ItemEntity itemEntity = new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 1.05D, pos.getZ() + 0.5D, offering);
        itemEntity.setDeltaMovement(0.0D, 0.08D, 0.0D);
        level.addFreshEntity(itemEntity);
        level.playSound(null, pos, SoundEvents.CHISELED_BOOKSHELF_PICKUP, SoundSource.BLOCKS, 0.55F, 1.1F);
    }
}
