package net.com.zeromod.event;

import com.mojang.blaze3d.systems.RenderSystem;
import net.com.zeromod.config.ModConfig;
import net.com.zeromod.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientOverlayHandler {
    private static final String HOTBAR_OVERLAY_PATH = "hotbar";
    private static final int HOTBAR_X_OFFSET = -90;
    private static final int HOTBAR_SLOT_SPACING = 20;
    private static final int HOTBAR_SLOT_X_OFFSET = 1;
    private static final int HOTBAR_SLOT_Y_OFFSET = -20;
    private static final int HOTBAR_SLOT_SIZE = 18;
    private static final int COOLDOWN_TEXT_Y_OFFSET = 4;
    private static final int COOLDOWN_OVERLAY_COLOR = 0x88000000;
    private static final int COOLDOWN_TEXT_COLOR = 0xFFFFFF;
    private static final float COOLDOWN_Z_LEVEL = 500.0f;
    private static final int PROGRESS_BAR_X_OFFSET = 2;
    private static final int PROGRESS_BAR_Y_OFFSET = 15;
    private static final int PROGRESS_BAR_MAX_WIDTH = 13;
    private static final int PROGRESS_BAR_HEIGHT = 1;
    private static final int PROGRESS_BAR_REZERO_COLOR = 0xFF00FF00;
    private static final int PROGRESS_BAR_ETERNAL_COLOR = 0xFF800080;
    private static final float PROGRESS_BAR_Z_LEVEL = 1000.0f;
    private static final int TICKS_PER_SECOND = 20;
    private static final int MINIMUM_SECONDS_DISPLAY = 1;

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (!event.getOverlay().id().getPath().equals(HOTBAR_OVERLAY_PATH)) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        GuiGraphics guiGraphics = event.getGuiGraphics();
        var player = mc.player;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            boolean isRezero = stack.getItem() == ModItems.REZERO.get();
            boolean isEternal = stack.getItem() == ModItems.REZERO_ETERNAL.get();
            if (!(isRezero || isEternal)) continue;

            int x = event.getWindow().getGuiScaledWidth() / 2 + HOTBAR_X_OFFSET + i * HOTBAR_SLOT_SPACING + HOTBAR_SLOT_X_OFFSET;
            int y = event.getWindow().getGuiScaledHeight() + HOTBAR_SLOT_Y_OFFSET;

            boolean onCooldown = player.getCooldowns().isOnCooldown(stack.getItem());
            float cooldownPercent = player.getCooldowns().getCooldownPercent(stack.getItem(), mc.getFrameTime());
            int cooldownTicks = onCooldown ? (int) (cooldownPercent * (isRezero ? ModConfig.getCooldownRezeroSeconds() : ModConfig.getCooldownEternalSeconds()) * TICKS_PER_SECOND) : 0;

            if (onCooldown) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, COOLDOWN_Z_LEVEL);

                RenderSystem.enableBlend();
                guiGraphics.fill(x, y, x + HOTBAR_SLOT_SIZE, y + HOTBAR_SLOT_SIZE, COOLDOWN_OVERLAY_COLOR);
                RenderSystem.disableBlend();

                int seconds = Math.max(MINIMUM_SECONDS_DISPLAY, cooldownTicks / TICKS_PER_SECOND);
                String text = String.valueOf(seconds);
                int textX = x + HOTBAR_SLOT_SIZE / 2 - mc.font.width(text) / 2;
                int textY = y + COOLDOWN_TEXT_Y_OFFSET;
                guiGraphics.drawString(mc.font, text, textX, textY, COOLDOWN_TEXT_COLOR, true);

                guiGraphics.pose().popPose();
            }

            if (isRezero && cooldownTicks > 0) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, PROGRESS_BAR_Z_LEVEL);

                float progress = 1.0f - cooldownPercent;
                int barWidth = Math.max(1, Math.round(PROGRESS_BAR_MAX_WIDTH * progress));
                guiGraphics.fill(x + PROGRESS_BAR_X_OFFSET, y + PROGRESS_BAR_Y_OFFSET,
                        x + PROGRESS_BAR_X_OFFSET + barWidth, y + PROGRESS_BAR_Y_OFFSET + PROGRESS_BAR_HEIGHT,
                        PROGRESS_BAR_REZERO_COLOR);

                guiGraphics.pose().popPose();
            }

            if (isEternal && cooldownTicks > 0) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, PROGRESS_BAR_Z_LEVEL);

                float progress = 1.0f - cooldownPercent;
                int barWidth = Math.max(1, Math.round(PROGRESS_BAR_MAX_WIDTH * progress));
                guiGraphics.fill(x + PROGRESS_BAR_X_OFFSET, y + PROGRESS_BAR_Y_OFFSET,
                        x + PROGRESS_BAR_X_OFFSET + barWidth, y + PROGRESS_BAR_Y_OFFSET + PROGRESS_BAR_HEIGHT,
                        PROGRESS_BAR_ETERNAL_COLOR);

                guiGraphics.pose().popPose();
            }
        }
    }
}