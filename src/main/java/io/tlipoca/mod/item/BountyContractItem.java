package io.tlipoca.mod.item;

import io.tlipoca.mod.bounty.BountyDefinition;
import io.tlipoca.mod.bounty.BountyManager;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class BountyContractItem extends Item {
    public BountyContractItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        CompoundTag tag = BountyManager.getContractTag(stack);
        if (!tag.contains("id")) {
            player.displayClientMessage(Component.literal("这是一张空白悬赏单。"), true);
            return InteractionResult.SUCCESS;
        }

        long gameTime = level.getGameTime();
        if (BountyManager.isExpired(tag, gameTime)) {
            player.displayClientMessage(Component.literal("这张悬赏单已经失效。"), true);
            stack.shrink(1);
            return InteractionResult.SUCCESS;
        }
        if (!BountyManager.isComplete(tag)) {
            player.displayClientMessage(Component.literal("悬赏进度："
                + tag.getIntOr("progress", 0) + "/" + tag.getIntOr("required", 1)), true);
            return InteractionResult.SUCCESS;
        }

        BountyDefinition definition = BountyManager.getDefinition(tag.getStringOr("id", ""));
        BountyManager.giveRewards(serverPlayer, definition);
        player.displayClientMessage(Component.literal("悬赏报酬已结清：" + definition.name()), true);
        stack.shrink(1);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = BountyManager.getContractTag(stack);
        if (!tag.contains("id")) {
            tooltip.accept(Component.literal("空白悬赏单").withStyle(ChatFormatting.GRAY));
            return;
        }

        long remaining = Math.max(0L, tag.getLongOr("deadlineGameTime", 0L) - context.level().getGameTime());
        boolean expired = remaining <= 0L;
        boolean complete = BountyManager.isComplete(tag);
        tooltip.accept(Component.literal(tag.getStringOr("name", "未知悬赏")).withStyle(ChatFormatting.GOLD));
        tooltip.accept(Component.literal("目标：" + tag.getStringOr("target", "未知")).withStyle(ChatFormatting.GRAY));
        String flavor = tag.getStringOr("flavor", "");
        if (!flavor.isBlank()) {
            tooltip.accept(Component.literal(flavor).withStyle(ChatFormatting.DARK_GRAY));
        }
        tooltip.accept(Component.literal("进度：" + tag.getIntOr("progress", 0) + "/" + tag.getIntOr("required", 1)).withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("剩余：" + BountyManager.formatRemainingTime(remaining)).withStyle(expired ? ChatFormatting.RED : ChatFormatting.GRAY));
        tooltip.accept(Component.literal("奖励：" + tag.getStringOr("reward", "未知")).withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("状态：" + (expired ? "已过期" : (complete ? "已完成" : "进行中")))
            .withStyle(expired ? ChatFormatting.RED : (complete ? ChatFormatting.GREEN : ChatFormatting.YELLOW)));
    }
}
