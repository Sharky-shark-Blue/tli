package io.tlipoca.mod.item;

import io.tlipoca.mod.TlipocaMod;
import io.tlipoca.mod.oracle.OracleManager;
import io.tlipoca.mod.oracle.PlayerOracleData;
import io.tlipoca.mod.yard.YardManager;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public class TraineeReaperScytheItem extends Item {
    public static final int MAX_CONTAINED_SOULS = 6;
    private static final String KEY_CONTAINED_SOULS = "contained_souls";

    public TraineeReaperScytheItem(Properties properties) {
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

        int containedSouls = getContainedSouls(stack);
        if (containedSouls <= 0) {
            serverPlayer.displayClientMessage(Component.literal("空的。"), true);
            return InteractionResult.SUCCESS;
        }
        if (!YardManager.isInYard(serverPlayer)) {
            serverPlayer.displayClientMessage(Component.literal("离庭院太远了。"), true);
            return InteractionResult.SUCCESS;
        }

        setContainedSouls(stack, 0);
        giveReleaseResult(serverPlayer, containedSouls);
        serverPlayer.displayClientMessage(Component.literal("今日收割：" + containedSouls + "。"), false);

        PlayerOracleData data = OracleManager.getData(serverPlayer);
        data.addTotalSoulsReleased(containedSouls);
        if (containedSouls >= MAX_CONTAINED_SOULS) {
            if (!data.isFirstFullScytheReleaseSeen()) {
                data.setFirstFullScytheReleaseSeen(true);
                serverPlayer.displayClientMessage(Component.literal("其中一个还没到时间。"), false);
                serverPlayer.displayClientMessage(Component.literal("但我还是收了。"), false);
                serverPlayer.displayClientMessage(Component.literal("……对不起。"), false);
            }
        }
        OracleManager.saveData(serverPlayer, data);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.literal("见习死神使用的旧镰刀。").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("不是为了战斗，而是为了把灵魂暂时隔开。").withStyle(ChatFormatting.DARK_GRAY));
        int containedSouls = getContainedSouls(stack);
        tooltip.accept(Component.literal("收容：" + containedSouls + " / " + MAX_CONTAINED_SOULS).withStyle(containedSouls >= MAX_CONTAINED_SOULS ? ChatFormatting.RED : ChatFormatting.GRAY));
        if (containedSouls >= MAX_CONTAINED_SOULS) {
            tooltip.accept(Component.literal("太满了。会漏出来。").withStyle(ChatFormatting.RED));
        }
    }

    public static boolean tryContainSoul(ServerPlayer player, ItemStack stack) {
        if (!stack.is(TlipocaMod.TRAINEE_REAPER_SCYTHE.get())) {
            return false;
        }

        int containedSouls = getContainedSouls(stack);
        if (containedSouls >= MAX_CONTAINED_SOULS) {
            player.displayClientMessage(Component.literal("太满了。"), true);
            player.displayClientMessage(Component.literal("会漏出来。"), true);
            return false;
        }

        int next = Math.min(MAX_CONTAINED_SOULS, containedSouls + 1);
        setContainedSouls(stack, next);
        player.displayClientMessage(Component.literal(next >= MAX_CONTAINED_SOULS ? "太满了。会漏出来。" : "收割记录更新。"), true);
        return true;
    }

    public static int getContainedSouls(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return 0;
        }
        return Math.max(0, Math.min(MAX_CONTAINED_SOULS, data.copyTag().getIntOr(KEY_CONTAINED_SOULS, 0)));
    }

    private static void setContainedSouls(ItemStack stack, int containedSouls) {
        CompoundTag tag = stack.get(DataComponents.CUSTOM_DATA) == null
            ? new CompoundTag()
            : stack.get(DataComponents.CUSTOM_DATA).copyTag();
        tag.putInt(KEY_CONTAINED_SOULS, Math.max(0, Math.min(MAX_CONTAINED_SOULS, containedSouls)));
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private static void giveReleaseResult(ServerPlayer player, int containedSouls) {
        int count = Math.max(1, Math.min(3, (containedSouls + 1) / 2));
        ItemStack reward = new ItemStack(TlipocaMod.SOUL_RECEIPT.get(), count);
        if (!player.getInventory().add(reward)) {
            player.drop(reward, false);
        }
    }
}
