// package net.com.zeromod.network;

package net.com.zeromod.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModNetwork {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String PROTOCOL_VERSION = "2"; // Changed version for new packet structure
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("zeromod", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        LOGGER.info("Registering network channel zeromod:main");
        int id = 0;
        INSTANCE.registerMessage(id++, ClientboundPlayAnimationPacket.class, ClientboundPlayAnimationPacket::encode, ClientboundPlayAnimationPacket::decode, ClientboundPlayAnimationPacket::handle);
    }
}