package io.tlipoca.mod.bounty;

import io.tlipoca.mod.TlipocaMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TlipocaMod.MODID)
public final class BountyEvents {
    private BountyEvents() {
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        ServerPlayer player = resolveKillerPlayer(event.getSource().getEntity(), event.getSource().getDirectEntity());
        if (player == null) {
            return;
        }

        EntityType<?> killedType = event.getEntity().getType();
        long gameTime = player.level().getGameTime();
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.is(TlipocaMod.BOUNTY_CONTRACT.get())) {
                continue;
            }
            updateContract(player, stack, killedType, gameTime);
        }
    }

    private static void updateContract(ServerPlayer player, ItemStack stack, EntityType<?> killedType, long gameTime) {
        CompoundTag tag = BountyManager.getContractTag(stack);
        if (!tag.contains("id") || BountyManager.isExpired(tag, gameTime) || BountyManager.isComplete(tag)) {
            return;
        }

        BountyDefinition definition = BountyManager.getDefinition(tag.getStringOr("id", ""));
        if (!definition.targetTypes().contains(killedType)) {
            return;
        }

        int nextProgress = Math.min(definition.required(), tag.getIntOr("progress", 0) + 1);
        tag.putInt("progress", nextProgress);
        BountyManager.setContractTag(stack, tag);
        if (nextProgress >= definition.required()) {
            player.displayClientMessage(Component.literal("悬赏完成：" + definition.name() + "。右键悬赏单领取报酬。"), false);
        }
    }

    private static ServerPlayer resolveKillerPlayer(Entity sourceEntity, Entity directEntity) {
        if (sourceEntity instanceof ServerPlayer player) {
            return player;
        }
        if (directEntity instanceof Projectile projectile && projectile.getOwner() instanceof ServerPlayer player) {
            return player;
        }
        return null;
    }
}
