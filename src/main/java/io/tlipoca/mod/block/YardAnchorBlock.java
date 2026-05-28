package io.tlipoca.mod.block;

import io.tlipoca.mod.TlipocaMod;
import io.tlipoca.mod.block.entity.YardAnchorBlockEntity;
import io.tlipoca.mod.yard.YardManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class YardAnchorBlock extends Block implements EntityBlock {
    public YardAnchorBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new YardAnchorBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.is(TlipocaMod.GUEST_LEDGER.get())) {
            inspectYard(level, pos, player);
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    private static void inspectYard(Level level, BlockPos pos, Player player) {
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel) || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        YardManager.YardSurvey survey = YardManager.survey(serverLevel, pos);
        serverPlayer.sendSystemMessage(Component.literal("庭院账簿记录：半径 " + survey.radius() + " 格。"));
        serverPlayer.sendSystemMessage(Component.literal("锚点 " + survey.anchors()
            + "，炼金台 " + survey.alchemyTables()
            + "，供物台 " + survey.offeringPedestals() + "。"));
        serverPlayer.displayClientMessage(Component.literal(survey.hasCompleteAlchemySet()
            ? "账簿边缘微微发热：炼金布置已可运转。"
            : "账簿没有合上：至少需要 1 个炼金台和 3 个供物台。"), true);
    }
}
