package net.com.zeromod;

import net.com.zeromod.config.ModConfig;
import com.mojang.logging.LogUtils;
import net.com.zeromod.custom.StarterItems;
import net.com.zeromod.event.ShearWoolHandler;
import net.com.zeromod.item.ModCreativeModTabs;
import net.com.zeromod.item.ModItems;
import net.com.zeromod.network.ModNetwork;
import net.com.zeromod.sound.ModSounds;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;


@Mod(ZeroMod.MOD_ID)
public class ZeroMod {

    public static final String MOD_ID = "zeromod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ZeroMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register(modEventBus);
        ModCreativeModTabs.register(modEventBus);
        ModSounds.register(modEventBus);
        ModNetwork.register();
        ModConfig.register();
        MinecraftForge.EVENT_BUS.register( StarterItems.class);



        MinecraftForge.EVENT_BUS.register(new ShearWoolHandler());



        modEventBus.addListener(this::commonSetup);

        
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Common setup complete.");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Server is starting...");
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("Client setup complete.");
        }
    }
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModSetup {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("Client setup complete.");
        }

    }

}