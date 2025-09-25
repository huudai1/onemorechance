// package net.com.zeromod.client;

package net.com.zeromod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod.EventBusSubscriber(modid = "zeromod", value = Dist.CLIENT)
public class AnimatedOverlay {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnimatedOverlay.class);

    private static boolean isActive = false;
    private static ResourceLocation[] currentTextures;
    private static int totalFrames;
    private static int totalDurationTicks;
    private static long startTime = -1;

    public static void play(String texturePrefix, int frameCount, int durationTicks) {
        if (isActive) {
            LOGGER.warn("Tried to play an animation while another is already active.");
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            LOGGER.warn("Cannot activate overlay: No level loaded");
            return;
        }

        // Initialize textures
        currentTextures = new ResourceLocation[frameCount];
        for (int i = 0; i < frameCount; i++) {
            currentTextures[i] = new ResourceLocation("zeromod", "textures/gui/" + texturePrefix + (i + 1) + ".png");
        }

        totalFrames = frameCount;
        totalDurationTicks = durationTicks > 0 ? durationTicks : 1; // Avoid division by zero
        isActive = true;
        startTime = minecraft.level.getGameTime();
        LOGGER.info("Animation '{}' activated for {} ticks.", texturePrefix, totalDurationTicks);
    }

    public static void stop() {
        if (isActive) {
            isActive = false;
            startTime = -1;
            currentTextures = null;
            LOGGER.info("Animation stopped.");
        }
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Pre event) {
        if (!isActive || currentTextures == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !minecraft.player.isAlive() || minecraft.level == null) {
            stop();
            return;
        }

        long gameTime = minecraft.level.getGameTime();
        long elapsedTicks = gameTime - startTime;

        if (elapsedTicks >= totalDurationTicks) {
            stop();
            return;
        }

        // Calculate current frame based on duration
        // This ensures the animation stretches to fit the exact duration
        float progress = (float) elapsedTicks / totalDurationTicks;
        int frameIndex = (int) (progress * totalFrames);
        frameIndex = Math.min(frameIndex, totalFrames - 1); // Clamp to last frame

        ResourceLocation currentTexture = currentTextures[frameIndex];

        var guiGraphics = event.getGuiGraphics();
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.8F); // Set slight transparency

        guiGraphics.blit(currentTexture, 0, 0, 0, 0, width, height, width, height);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (isActive) {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft.level == null || minecraft.player == null) {
                    stop();
                    return;
                }
                long elapsedTicks = minecraft.level.getGameTime() - startTime;
                if (elapsedTicks >= totalDurationTicks) {
                    stop();
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        if (isActive) {
            stop();
            LOGGER.info("Animation stopped due to player logout.");
        }
    }
}