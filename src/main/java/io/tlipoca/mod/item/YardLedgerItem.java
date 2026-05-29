package io.tlipoca.mod.item;

import io.tlipoca.mod.TlipocaMod;
import io.tlipoca.mod.network.TlipocaNetwork;
import io.tlipoca.mod.yard.YardManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class YardLedgerItem extends YardLoreItem {
    private static final int MIST_LETTER_HONEY_COST = 2;

    public YardLedgerItem(Properties properties, String descriptionKey) {
        super(properties, descriptionKey);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            tryTurnInMistLetterRequest(serverPlayer);
            TlipocaNetwork.openYardLedgerScreen(serverPlayer);
        }
        return InteractionResult.SUCCESS;
    }

    private static void tryTurnInMistLetterRequest(ServerPlayer player) {
        YardManager.YardProfile profile = YardManager.getYardProfile(player);
        if (!YardManager.hasMistNightLetter(player, profile)) {
            return;
        }

        if (countHoneyBottles(player) < MIST_LETTER_HONEY_COST) {
            player.displayClientMessage(Component.literal("来信还在等蜂蜜的甜味。"), true);
            return;
        }

        removeHoneyBottles(player, MIST_LETTER_HONEY_COST);
        ItemStack reward = new ItemStack(TlipocaMod.MEMORY_FRAGMENT.get());
        if (!player.getInventory().add(reward)) {
            player.drop(reward, false);
        }
        player.displayClientMessage(Component.literal("信纸变轻了一点。"), true);
    }

    private static int countHoneyBottles(ServerPlayer player) {
        int count = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(Items.HONEY_BOTTLE)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void removeHoneyBottles(ServerPlayer player, int count) {
        int remaining = count;
        for (int slot = 0; slot < player.getInventory().getContainerSize() && remaining > 0; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.is(Items.HONEY_BOTTLE)) {
                continue;
            }

            int removed = Math.min(remaining, stack.getCount());
            stack.shrink(removed);
            remaining -= removed;
        }
    }
}
