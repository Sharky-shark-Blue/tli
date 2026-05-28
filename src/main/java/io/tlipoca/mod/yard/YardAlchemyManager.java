package io.tlipoca.mod.yard;

import io.tlipoca.mod.TlipocaMod;
import io.tlipoca.mod.block.entity.YardOfferingPedestalBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public final class YardAlchemyManager {
    private static final Direction[] OFFERING_DIRECTIONS = {
        Direction.NORTH,
        Direction.SOUTH,
        Direction.EAST,
        Direction.WEST
    };

    private static final List<Recipe> RECIPES = List.of(
        new Recipe(
            List.of(Items.GLASS_BOTTLE, Items.AMETHYST_SHARD, Items.SWEET_BERRIES),
            new ItemStack(Items.HONEY_BOTTLE),
            "特莉波卡调出了一瓶甜得可疑的安神药。"
        ),
        new Recipe(
            List.of(Items.PAPER, Items.BONE_MEAL, Items.SOUL_SAND),
            new ItemStack(Items.PHANTOM_MEMBRANE),
            "纸页浮起了一瞬，像是被谁写下了名字。"
        ),
        new Recipe(
            List.of(Items.INK_SAC, Items.SOUL_SOIL, Items.GLOWSTONE_DUST),
            new ItemStack(Items.GLOW_INK_SAC),
            "墨水里有微弱的光，像还没熄灭的灵魂。"
        ),
        new Recipe(
            List.of(Items.HONEYCOMB, Items.GLOW_BERRIES, Items.AMETHYST_SHARD),
            new ItemStack(TlipocaMod.STAR_HONEY.get()),
            "蜂蜜映出星点，像旅人睡前没说完的愿望。"
        ),
        new Recipe(
            List.of(Items.VINE, Items.BLUE_ORCHID, Items.GLASS_BOTTLE),
            new ItemStack(TlipocaMod.MOONDEW_LEAF.get()),
            "叶面凝出清亮月露，适合在庭院深处采下。"
        ),
        new Recipe(
            List.of(Items.INK_SAC, Items.AMETHYST_SHARD, TlipocaMod.MOONDEW_LEAF.get()),
            new ItemStack(TlipocaMod.ORACLE_INK.get()),
            "墨色安静下来，像终于愿意回答一个问题。"
        ),
        new Recipe(
            List.of(Items.PAPER, Items.SOUL_SAND, TlipocaMod.ORACLE_INK.get()),
            new ItemStack(TlipocaMod.SOUL_RECEIPT.get()),
            "纸页自行盖上印记：欠下的东西已有凭证。"
        ),
        new Recipe(
            List.of(Items.BOOK, TlipocaMod.SOUL_RECEIPT.get(), TlipocaMod.STAR_HONEY.get()),
            new ItemStack(TlipocaMod.GUEST_LEDGER.get()),
            "账簿合上时轻轻一响，仿佛又多了一位客人。"
        )
    );

    private YardAlchemyManager() {
    }

    public static void tryAlchemy(ServerPlayer player, Level level, BlockPos tablePos) {
        List<OfferingSlot> offerings = findOfferings(level, tablePos);
        for (Recipe recipe : RECIPES) {
            List<OfferingSlot> matchedOfferings = matchRecipe(offerings, recipe);
            if (matchedOfferings.isEmpty()) {
                continue;
            }

            for (OfferingSlot offering : matchedOfferings) {
                offering.pedestal().removeOffering();
            }
            spawnResult(level, tablePos, recipe.result().copy());
            playAlchemyFeedback(level, tablePos);
            player.displayClientMessage(Component.literal(recipe.message()), false);
            return;
        }

        player.displayClientMessage(Component.literal("供物尚未形成稳定配方。"), true);
    }

    private static List<OfferingSlot> findOfferings(Level level, BlockPos tablePos) {
        List<OfferingSlot> offerings = new ArrayList<>();
        for (Direction direction : OFFERING_DIRECTIONS) {
            BlockPos pedestalPos = tablePos.relative(direction);
            if (level.getBlockEntity(pedestalPos) instanceof YardOfferingPedestalBlockEntity pedestal) {
                ItemStack offering = pedestal.getOffering();
                if (!offering.isEmpty()) {
                    offerings.add(new OfferingSlot(pedestal, offering));
                }
            }
        }
        return offerings;
    }

    private static List<OfferingSlot> matchRecipe(List<OfferingSlot> offerings, Recipe recipe) {
        List<OfferingSlot> matchedOfferings = new ArrayList<>();
        for (Item requiredItem : recipe.ingredients()) {
            OfferingSlot match = findUnusedOffering(offerings, matchedOfferings, requiredItem);
            if (match == null) {
                return List.of();
            }
            matchedOfferings.add(match);
        }
        return matchedOfferings;
    }

    private static OfferingSlot findUnusedOffering(List<OfferingSlot> offerings, List<OfferingSlot> usedOfferings, Item item) {
        for (OfferingSlot offering : offerings) {
            if (!usedOfferings.contains(offering) && offering.stack().is(item)) {
                return offering;
            }
        }
        return null;
    }

    private static void spawnResult(Level level, BlockPos tablePos, ItemStack result) {
        ItemEntity itemEntity = new ItemEntity(
            level,
            tablePos.getX() + 0.5D,
            tablePos.getY() + 1.15D,
            tablePos.getZ() + 0.5D,
            result
        );
        itemEntity.setDeltaMovement(0.0D, 0.2D, 0.0D);
        level.addFreshEntity(itemEntity);
    }

    private static void playAlchemyFeedback(Level level, BlockPos tablePos) {
        level.playSound(null, tablePos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 0.8F, 0.85F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, tablePos.getX() + 0.5D, tablePos.getY() + 1.15D, tablePos.getZ() + 0.5D, 10, 0.35D, 0.18D, 0.35D, 0.02D);
            serverLevel.sendParticles(ParticleTypes.ENCHANT, tablePos.getX() + 0.5D, tablePos.getY() + 1.25D, tablePos.getZ() + 0.5D, 18, 0.45D, 0.25D, 0.45D, 0.04D);
        }
    }

    private record OfferingSlot(YardOfferingPedestalBlockEntity pedestal, ItemStack stack) {
    }

    private record Recipe(List<Item> ingredients, ItemStack result, String message) {
    }
}
