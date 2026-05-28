package io.tlipoca.mod.yard;

import io.tlipoca.mod.TlipocaMod;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TlipocaMod.MODID)
public final class YardEvents {
    private static final Map<UUID, Boolean> LAST_YARD_STATE = new HashMap<>();

    private YardEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent.Post event) {
        if (event.side() != LogicalSide.SERVER || !(event.player() instanceof ServerPlayer player)) {
            return;
        }

        boolean inYard = YardManager.isInYard(player);
        Boolean wasInYard = LAST_YARD_STATE.put(player.getUUID(), inYard);
        if (wasInYard == null || wasInYard == inYard) {
            return;
        }

        player.displayClientMessage(Component.literal(inYard ? "已进入临时庭院" : "已离开临时庭院"), true);
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_YARD_STATE.remove(event.getEntity().getUUID());
    }
}
