//Sprite, logic in event
package net.com.zeromod.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

 class TimeDisperserItem extends Item {
     TimeDisperserItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.zeromod.time_disperser.tooltip")
                .withStyle(style -> style.withColor(0x1E90FF)));

    }
}
