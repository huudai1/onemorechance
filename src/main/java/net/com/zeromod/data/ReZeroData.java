package net.com.zeromod.data;

import net.com.zeromod.config.ModConfig;
import net.com.zeromod.network.ClientboundPlayAnimationPacket;
import net.com.zeromod.network.ModNetwork;
import net.com.zeromod.sound.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.PacketDistributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingDeque;

public class ReZeroData {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReZeroData.class);

    private static final Map<UUID, LinkedBlockingDeque<PlayerLocation>> positionHistory = new HashMap<>();
    public static final Map<UUID, Boolean> overlayStates = new HashMap<>();
    private static final Map<UUID, Integer> overlayTimers = new HashMap<>();
    private static final Map<UUID, Float> savedHealthMap = new HashMap<>();
    private static final Map<UUID, Integer> positionTickCounter = new HashMap<>();
    private static final Map<UUID, Integer> rewindStepTracker = new HashMap<>();

    public static void savePosition(ServerPlayer player) {
        UUID uuid = player.getUUID();
        int tickCount = positionTickCounter.getOrDefault(uuid, 0);
        tickCount++;

        if (tickCount >= ModConfig.getPositionSaveIntervalTicks()) {
            positionHistory.computeIfAbsent(uuid, k -> new LinkedBlockingDeque<>(ModConfig.getPositionHistorySize()));
            LinkedBlockingDeque<PlayerLocation> history = positionHistory.get(uuid);
            if (history.size() >= ModConfig.getPositionHistorySize()) {
                history.pollFirst();
            }
            //  (yaw, pitch)
            history.addLast(new PlayerLocation(player.position(), player.level().dimension(), player.getYRot(), player.getXRot()));
            tickCount = 0;
        }

        positionTickCounter.put(uuid, tickCount);
    }

    public static PlayerLocation getSavedLocation(UUID uuid) {
        LinkedBlockingDeque<PlayerLocation> history = positionHistory.get(uuid);
        return (history != null && !history.isEmpty()) ? history.peekFirst() : null;
    }

    public static void triggerOverlay(ServerPlayer player) {
        UUID uuid = player.getUUID();
        overlayStates.put(uuid, true);
        overlayTimers.put(uuid, ModConfig.getOverlayDurationSeconds() * 20);
        savedHealthMap.put(uuid, player.getHealth());
        rewindStepTracker.put(uuid, -1);
        player.level().playSound(null, player.blockPosition(), ModSounds.REWIND.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, ModConfig.getOverlayDurationSeconds() * 20, 0, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ModConfig.getOverlayDurationSeconds() * 20, 255, false, false));
        //player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, ModConfig.getOverlayDurationSeconds() * 20 + 20, 0, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, ModConfig.getOverlayDurationSeconds() * 20, 4, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, ModConfig.getOverlayDurationSeconds() * 20, 0, false, false));
        player.setSwimming(true);
        int durationTicks = ModConfig.getOverlayDurationSeconds() * 20;
        ModNetwork.INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> player),
                new ClientboundPlayAnimationPacket(ClientboundPlayAnimationPacket.AnimationType.REZERO, true, durationTicks)
        );
    }

    public static void stopOverlay(ServerPlayer player) {
        UUID uuid = player.getUUID();
        overlayStates.put(uuid, false);
        overlayTimers.remove(uuid);
        player.setSwimming(false);
        rewindStepTracker.remove(uuid);

        PlayerLocation finalLocation = getSavedLocation(uuid);
        if (finalLocation != null) {
            ServerLevel targetLevel = player.server.getLevel(finalLocation.getDimension());
            if (targetLevel != null) {
                Vec3 pos = finalLocation.getPosition();
                float yaw = finalLocation.getYaw();
                float pitch = finalLocation.getPitch();
                // Dịch chuyển cuối cùng, có xử lý đúng chiều không gian và góc nhìn
                player.teleportTo(targetLevel, pos.x, pos.y, pos.z, yaw, pitch);
            }
        }
        Float oldHealth = savedHealthMap.remove(uuid);
        if (oldHealth != null) {
            float healAmount = Math.max(oldHealth, player.getHealth() + 8.0f);
            player.setHealth(Math.min(healAmount, player.getMaxHealth()));
        }
        ModNetwork.INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> player),
                new ClientboundPlayAnimationPacket(ClientboundPlayAnimationPacket.AnimationType.REZERO, false, 0)
        );
    }

    public static void tickOverlay(ServerPlayer player) {
        UUID uuid = player.getUUID();
        if (overlayStates.getOrDefault(uuid, false)) {
            int time = overlayTimers.getOrDefault(uuid, 0);
            if (time > 0) {
                overlayTimers.put(uuid, time - 1);
            } else {
                stopOverlay(player);
            }
        }
    }

    public static boolean isOverlayTriggered(Player player) {
        return overlayStates.getOrDefault(player.getUUID(), false);
    }

    public static Map<UUID, Integer> getOverlayTimers() {
        return overlayTimers;
    }

    public static Map<UUID, Integer> getRewindStepTracker() {
        return rewindStepTracker;
    }

    public static List<PlayerLocation> getPositionHistory(UUID uuid) {
        LinkedBlockingDeque<PlayerLocation> history = positionHistory.get(uuid);
        if (history == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(history);
    }
}