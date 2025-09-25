package net.com.zeromod.event;

import net.com.zeromod.item.CursedHeart;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "zeromod", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CursedHeartClientHandler {


    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || !player.getPersistentData().getBoolean("zeromod:cursed_heart_used")) return;

        CompoundTag tag = CursedHeart.getPlayerCursedHeartData(player); // Gọi hàm static từ class CursedHeart
        if (tag == null || !tag.getBoolean("Activated")) return;

        int level = tag.getInt("Level");
        int mobKills = tag.getInt("MobKills");
        int healthUpgrades = tag.getInt("HealthUpgrades");
        int damageUpgrades = tag.getInt("DamageUpgrades");


        GuiGraphics guiGraphics = event.getGuiGraphics();
        Font font = minecraft.font;
        int y = 10;

        guiGraphics.drawString(font, "Cursed Heart Level: " + level, 10, y, 0xFFFFFF);
        y += 10;
        guiGraphics.drawString(font, "Mob Kills: " + mobKills, 10, y, 0xFFFFFF);
        y += 10;
        guiGraphics.drawString(font, "Health Upgrades: " + healthUpgrades, 10, y, 0xFFFFFF);
        y += 10;
        guiGraphics.drawString(font, "Damage Upgrades: " + damageUpgrades, 10, y, 0xFFFFFF);
    }
}