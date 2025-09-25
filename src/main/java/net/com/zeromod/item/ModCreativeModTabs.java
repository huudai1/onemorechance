package net.com.zeromod.item;

import net.com.zeromod.ZeroMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ZeroMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> HEALER_TAB = CREATIVE_MODE_TABS.register("zero_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.REZERO_ETERNAL.get()))
                    .title(Component.translatable("itemGroup.zeromod.zero_tab")) // Sửa key dịch
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(ModItems.REZERO.get());
                        pOutput.accept(ModItems.REZERO_ETERNAL.get());
                        pOutput.accept(ModItems.TIME_DISPERSER.get());
                        pOutput.accept(ModItems.EYE_OF_CHRONOS.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}