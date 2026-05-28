package io.tlipoca.mod.item;

import io.tlipoca.mod.network.TlipocaNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class YardLedgerItem extends YardLoreItem {
    public YardLedgerItem(Properties properties, String descriptionKey) {
        super(properties, descriptionKey);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            TlipocaNetwork.openYardLedgerScreen(serverPlayer);
        }
        return InteractionResult.SUCCESS;
    }
}
