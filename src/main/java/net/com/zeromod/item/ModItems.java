package net.com.zeromod.item;

import net.com.zeromod.ZeroMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ZeroMod.MOD_ID);
    public static final RegistryObject<Item> REZERO = ITEMS.register("rezero",
            () -> new ReZero(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> REZERO_ETERNAL = ITEMS.register("rezero_eternal",
            () -> new ReZero_Eternal(new Item.Properties().stacksTo(1))); // hoặc gì đó đặc biệt hơn
    public static final RegistryObject<Item> TIME_DISPERSER = ITEMS.register("time_disperser",
            () -> new SwapTimeItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> EYE_OF_CHRONOS = ITEMS.register("eye_of_chronos",
            () -> new EyeofChronos(new Item.Properties().stacksTo(1))
    );



    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

