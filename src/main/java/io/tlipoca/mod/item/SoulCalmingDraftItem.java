package io.tlipoca.mod.item;

import io.tlipoca.mod.oracle.OracleManager;
import io.tlipoca.mod.oracle.PlayerOracleData;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public class SoulCalmingDraftItem extends Item {
    private static final int SAN_RESTORE = 15;

    public SoulCalmingDraftItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack, net.minecraft.world.entity.LivingEntity entity) {
        return 32;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, net.minecraft.world.entity.LivingEntity entity) {
        if (level.isClientSide()) {
            return stack;
        }
        if (!(entity instanceof ServerPlayer serverPlayer)) {
            return stack;
        }

        PlayerOracleData data = OracleManager.getData(serverPlayer);
        int before = data.getSan();
        data.setSan(before + SAN_RESTORE);
        OracleManager.saveData(serverPlayer, data);
        OracleManager.syncSan(serverPlayer);
        if (!serverPlayer.getAbilities().instabuild) {
            stack.shrink(1);
            ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
            if (!serverPlayer.addItem(bottle)) {
                serverPlayer.drop(bottle, false);
            }
        }
        serverPlayer.displayClientMessage(Component.literal("SAN +" + (data.getSan() - before)), true);
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.literal("收割的副产品。").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("死神塔说可以用。").withStyle(ChatFormatting.DARK_GRAY));
    }
}
