// package net.com.zeromod.network;

package net.com.zeromod.network;

import net.com.zeromod.client.AnimatedOverlay;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundPlayAnimationPacket {

    private final AnimationType type;
    private final boolean start;
    private final int durationTicks;

    public ClientboundPlayAnimationPacket(AnimationType type, boolean start, int durationTicks) {
        this.type = type;
        this.start = start;
        this.durationTicks = durationTicks;
    }

    public static void encode(ClientboundPlayAnimationPacket msg, FriendlyByteBuf buf) {
        buf.writeEnum(msg.type);
        buf.writeBoolean(msg.start);
        buf.writeInt(msg.durationTicks);
    }

    public static ClientboundPlayAnimationPacket decode(FriendlyByteBuf buf) {
        return new ClientboundPlayAnimationPacket(buf.readEnum(AnimationType.class), buf.readBoolean(), buf.readInt());
    }

    public static void handle(ClientboundPlayAnimationPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (msg.start) {
                AnimatedOverlay.play(msg.type.getTexturePrefix(), msg.type.getFrameCount(), msg.durationTicks);
            } else {
                AnimatedOverlay.stop();
            }
        });
        ctx.get().setPacketHandled(true);
    }

    // *** ĐÃ SỬA LẠI ĐỂ CÓ 3 LOẠI HOẠT ẢNH RIÊNG BIỆT ***
    public enum AnimationType {
        REZERO("time", 37),      //  ReZero
        SWAP("overlay", 80),     //  Eye of Chronos
        DODGE("overla", 80);     //  Swap Time

        private final String texturePrefix;
        private final int frameCount;

        AnimationType(String texturePrefix, int frameCount) {
            this.texturePrefix = texturePrefix;
            this.frameCount = frameCount;
        }

        public String getTexturePrefix() {
            return texturePrefix;
        }

        public int getFrameCount() {
            return frameCount;
        }
    }
}