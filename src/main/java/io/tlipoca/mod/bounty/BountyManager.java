package io.tlipoca.mod.bounty;

import io.tlipoca.mod.TlipocaMod;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class BountyManager {
    public static final int SLOT_COUNT = 3;
    public static final long BOARD_REFRESH_TICKS = 24000L;
    private static final List<BountyDefinition> DEFINITIONS = List.of(
        BountyDefinition.wanderingRemnants(),
        BountyDefinition.boneLedger(),
        BountyDefinition.doorwayGaze()
    );

    private BountyManager() {
    }

    public static BountyDefinition getDefinition(String id) {
        for (BountyDefinition definition : DEFINITIONS) {
            if (definition.id().equals(id)) {
                return definition;
            }
        }
        return DEFINITIONS.get(0);
    }

    public static BountyDefinition getDefinitionForSlot(int slot) {
        return DEFINITIONS.get(Math.floorMod(slot, DEFINITIONS.size()));
    }

    public static String getIdForSlot(int slot) {
        return getDefinitionForSlot(slot).id();
    }

    public static ItemStack createContract(String bountyId, long currentGameTime) {
        BountyDefinition definition = getDefinition(bountyId);
        ItemStack stack = new ItemStack(TlipocaMod.BOUNTY_CONTRACT.get());
        CompoundTag tag = new CompoundTag();
        tag.putString("id", definition.id());
        tag.putString("name", definition.name());
        tag.putString("target", definition.targetText());
        tag.putInt("progress", 0);
        tag.putInt("required", definition.required());
        tag.putLong("deadlineGameTime", currentGameTime + definition.durationTicks());
        tag.putString("reward", definition.rewardText());
        tag.putString("status", "active");
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }

    public static CompoundTag getContractTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? new CompoundTag() : data.copyTag();
    }

    public static void setContractTag(ItemStack stack, CompoundTag tag) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static boolean isExpired(CompoundTag tag, long gameTime) {
        return gameTime > tag.getLongOr("deadlineGameTime", 0L);
    }

    public static boolean isComplete(CompoundTag tag) {
        return tag.getIntOr("progress", 0) >= tag.getIntOr("required", 1);
    }

    public static String formatRemainingTime(long remainingTicks) {
        if (remainingTicks <= 0L) {
            return "已过期";
        }
        if (remainingTicks < 1200L) {
            return "不足1分钟";
        }

        long minutes = remainingTicks / 1200L;
        long hours = remainingTicks / 72000L;
        if (hours > 0L) {
            long remainderMinutes = minutes - hours * 60L;
            return hours + "小时" + remainderMinutes + "分钟";
        }
        return minutes + "分钟";
    }

    public static void giveRewards(ServerPlayer player, BountyDefinition definition) {
        for (ItemStack reward : definition.rewards()) {
            ItemStack copy = reward.copy();
            if (!player.getInventory().add(copy)) {
                player.drop(copy, false);
            }
        }
    }
}
