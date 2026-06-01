package io.tlipoca.mod.item;

import io.tlipoca.mod.TlipocaMod;
import io.tlipoca.mod.network.TlipocaNetwork;
import io.tlipoca.mod.oracle.OracleManager;
import io.tlipoca.mod.oracle.PlayerOracleData;
import io.tlipoca.mod.yard.MistVisitorRequest;
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
            tryTurnInMistVisitorRequest(serverPlayer);
            tryTurnInMistLetterRequest(serverPlayer);
            TlipocaNetwork.openYardLedgerScreen(serverPlayer);
        }
        return InteractionResult.SUCCESS;
    }

    private static void tryTurnInMistVisitorRequest(ServerPlayer player) {
        YardManager.YardProfile profile = YardManager.getYardProfile(player);
        if (!YardManager.hasMistNightVisitor(player, profile)) {
            return;
        }

        PlayerOracleData data = OracleManager.getData(player);
        long currentDay = player.level().getGameTime() / 24000L;
        if (data.getLastMistVisitorTurnInDay() == currentDay) {
            player.displayClientMessage(Component.literal("今晚的来客已经离开了。"), true);
            return;
        }

        MistVisitorRequest request = MistVisitorRequest.currentFor(player, profile);
        if (countItem(player, request.requestItem().get()) < request.requestCount()) {
            player.displayClientMessage(Component.literal(request.missingMessage()), true);
            return;
        }

        removeItem(player, request.requestItem().get(), request.requestCount());
        ItemStack reward = request.createRewardStack();
        if (!player.getInventory().add(reward)) {
            player.drop(reward, false);
        }
        data.setLastMistVisitorTurnInDay(currentDay);
        data.addMistVisitorsHelped(1);
        OracleManager.saveData(player, data);
        player.displayClientMessage(Component.literal(request.successMessage()), true);
    }

    private static void tryTurnInMistLetterRequest(ServerPlayer player) {
        YardManager.YardProfile profile = YardManager.getYardProfile(player);
        if (!YardManager.hasMistNightLetter(player, profile)) {
            return;
        }

        PlayerOracleData data = OracleManager.getData(player);
        long currentDay = player.level().getGameTime() / 24000L;
        if (data.getLastMistLetterTurnInDay() == currentDay) {
            player.displayClientMessage(Component.literal("这封信今天已经变轻了。"), true);
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
        data.setLastMistLetterTurnInDay(currentDay);
        data.addMistLettersAnswered(1);
        OracleManager.saveData(player, data);
        player.displayClientMessage(Component.literal("信纸变轻了一点。"), true);
    }

    private static int countHoneyBottles(ServerPlayer player) {
        return countItem(player, Items.HONEY_BOTTLE);
    }

    private static void removeHoneyBottles(ServerPlayer player, int count) {
        removeItem(player, Items.HONEY_BOTTLE, count);
    }

    private static int countItem(ServerPlayer player, net.minecraft.world.item.Item item) {
        int count = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void removeItem(ServerPlayer player, net.minecraft.world.item.Item item, int count) {
        int remaining = count;
        for (int slot = 0; slot < player.getInventory().getContainerSize() && remaining > 0; slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (!stack.is(item)) {
                continue;
            }

            int removed = Math.min(remaining, stack.getCount());
            stack.shrink(removed);
            remaining -= removed;
        }
    }
}
