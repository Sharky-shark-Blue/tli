package io.tlipoca.mod.item;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class YardLoreItem extends Item {
    private final String descriptionKey;

    public YardLoreItem(Properties properties, String descriptionKey) {
        super(properties);
        this.descriptionKey = descriptionKey;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable(descriptionKey).withStyle(ChatFormatting.GRAY));
    }
}
