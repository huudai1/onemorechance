// package net.com.zeromod.event;

package net.com.zeromod.event;

import net.com.zeromod.config.ModConfig;
import net.com.zeromod.item.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "zeromod", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientTooltipHandler {

    // Hàm onItemTooltip đã được chuyển từ PlayerEventHandler.java sang đây
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        if (player == null || stack.isEmpty()) return;

        if (stack.getItem() == ModItems.REZERO.get()) {
            CompoundTag tag = stack.getOrCreateTag();
            int usesRemaining = tag.contains("UsesRemaining") ? tag.getInt("UsesRemaining") : ModConfig.getMaxUsesRezero();
            event.getToolTip().add(Component.literal("Uses Remaining: " + usesRemaining));
        }
    }
}