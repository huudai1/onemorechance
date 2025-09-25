package net.com.zeromod.custom;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.com.zeromod.config.ModConfig;

@Mod.EventBusSubscriber(modid = "zeromod")
public class StarterItems {

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!ModConfig.ENABLE_STARTER_ITEMS.get()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;


        CompoundTag persistentData = player.getPersistentData();
        CompoundTag data;
        if (!persistentData.contains(ServerPlayer.PERSISTED_NBT_TAG)) {
            data = new CompoundTag();
            persistentData.put(ServerPlayer.PERSISTED_NBT_TAG, data);
        } else {
            data = persistentData.getCompound(ServerPlayer.PERSISTED_NBT_TAG);
        }

        if (!data.getBoolean("starter_given")) {
            data.putBoolean("starter_given", true);


            player.sendSystemMessage(Component.literal("§eBE CAREFUL, but don't worry too much."));
            player.sendSystemMessage(Component.literal("§a  You always have ONE MORE CHANCE."));


            ItemStack ironHelmet = new ItemStack(Items.IRON_HELMET);
            ItemStack leatherBoot = new ItemStack(Items.LEATHER_BOOTS);
            player.getInventory().armor.set(3, ironHelmet); // Slot 3: mũ
            player.getInventory().armor.set(0, leatherBoot); // Slot 0: giày


            player.getInventory().add(new ItemStack(Items.GOLDEN_APPLE));
            player.getInventory().add(new ItemStack(Items.IRON_AXE));


            var cursedHeartItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation("zeromod", "cursed_heart"));
            var timeDisperserItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation("zeromod", "rezero"));

            if (cursedHeartItem != null) {
                player.getInventory().add(new ItemStack(cursedHeartItem));
            } else {
                System.err.println("[ZeroMod] WARNING: cursed_heart not found in registry.");
            }

            if (timeDisperserItem != null) {
                player.getInventory().add(new ItemStack(timeDisperserItem));
            } else {
                System.err.println("[ZeroMod] WARNING: time_disperser not found in registry.");
            }
        }
    }
}
