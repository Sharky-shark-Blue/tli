package io.tlipoca.mod.bounty;

import java.util.List;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public record BountyDefinition(
    String id,
    String name,
    String targetText,
    List<EntityType<?>> targetTypes,
    int required,
    int durationTicks,
    List<ItemStack> rewards,
    String flavorText
) {
    public String rewardText() {
        return rewards.stream()
            .map(stack -> stack.getHoverName().getString() + " x" + stack.getCount())
            .reduce((left, right) -> left + " + " + right)
            .orElse("无");
    }

    public static BountyDefinition wanderingRemnants() {
        return new BountyDefinition(
            "wandering_remnants",
            "游荡残魂",
            "击杀僵尸 / 尸壳 / 溺尸 5只",
            List.of(EntityType.ZOMBIE, EntityType.HUSK, EntityType.DROWNED),
            5,
            24000,
            List.of(new ItemStack(Items.SOUL_SAND, 2)),
            "特莉波卡说：这些魂走得太慢了，帮我推一把。"
        );
    }

    public static BountyDefinition boneLedger() {
        return new BountyDefinition(
            "bone_ledger",
            "白骨账目",
            "击杀骷髅 / 流浪者 5只",
            List.of(EntityType.SKELETON, EntityType.STRAY),
            5,
            24000,
            List.of(new ItemStack(Items.BONE_MEAL, 8), new ItemStack(Items.EXPERIENCE_BOTTLE)),
            "骨头会记账。只是字迹通常很难看。"
        );
    }

    public static BountyDefinition doorwayGaze() {
        return new BountyDefinition(
            "doorway_gaze",
            "门缝凝视",
            "击杀末影人 1只",
            List.of(EntityType.ENDERMAN),
            1,
            36000,
            List.of(new ItemStack(Items.ENDER_PEARL), new ItemStack(Items.AMETHYST_SHARD, 2)),
            "不要盯着它太久。账本说那样不礼貌，也不安全。"
        );
    }

    public static BountyDefinition villageWanderer() {
        return new BountyDefinition(
            "village_wanderer",
            "村外徘徊者",
            "击杀僵尸 1只",
            List.of(EntityType.ZOMBIE),
            1,
            24000,
            List.of(new ItemStack(Items.SOUL_SAND, 2)),
            "记录：它曾登记于附近村落。备注：不影响收割。"
        );
    }
}
