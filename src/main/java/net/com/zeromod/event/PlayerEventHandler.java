package net.com.zeromod.event;

import net.com.zeromod.config.ModConfig;
import net.com.zeromod.data.PlayerLocation;
import net.com.zeromod.data.ReZeroData;
import net.com.zeromod.item.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber
public class PlayerEventHandler {
    private static final Logger LOGGER = LogManager.getLogger("zeromod/PlayerEventHandler");

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) return;

        ServerPlayer player = (ServerPlayer) event.player;
        UUID uuid = player.getUUID();

        if (ReZeroData.isOverlayTriggered(player)) {
            ReZeroData.tickOverlay(player);

            int overlayDurationTicks = ModConfig.getOverlayDurationSeconds() * 20;
            int teleportCompletionTicks = (int) (ModConfig.getRezeroTeleportDurationSeconds() * 20);
            teleportCompletionTicks = Math.min(teleportCompletionTicks, overlayDurationTicks);
            int numberOfSteps = ModConfig.getRezeroTeleportSteps();
            int remainingTicks = ReZeroData.getOverlayTimers().getOrDefault(uuid, 0);
            int elapsedTicks = overlayDurationTicks - remainingTicks;

            if (elapsedTicks <= teleportCompletionTicks && numberOfSteps > 0) {
                int ticksPerStep = teleportCompletionTicks / numberOfSteps;
                if (ticksPerStep <= 0) ticksPerStep = 1;
                int currentStep = elapsedTicks / ticksPerStep;
                int lastStep = ReZeroData.getRewindStepTracker().getOrDefault(uuid, -1);

                if (currentStep > lastStep) {
                    List<PlayerLocation> history = ReZeroData.getPositionHistory(uuid);
                    if (!history.isEmpty()) {
                        PlayerLocation finalLocation = history.get(0);
                        if (player.position().equals(finalLocation.getPosition()) && player.level().dimension().equals(finalLocation.getDimension())) {
                            ReZeroData.getRewindStepTracker().put(uuid, currentStep);
                            return;
                        }

                        float progress = (float) elapsedTicks / (float) teleportCompletionTicks;
                        progress = Math.min(progress, 1.0f);
                        int targetIndex = (int) ((1.0 - progress) * (history.size() - 1));
                        targetIndex = Math.max(0, targetIndex);

                        PlayerLocation targetLocation = history.get(targetIndex);
                        Vec3 targetPos = targetLocation.getPosition();
                        ResourceKey<Level> targetDim = targetLocation.getDimension();
                        float targetYaw = targetLocation.getYaw();
                        float targetPitch = targetLocation.getPitch();

                        ServerLevel targetLevel = player.server.getLevel(targetDim);

                        if (targetLevel != null) {

                            player.teleportTo(targetLevel, targetPos.x, targetPos.y, targetPos.z, targetYaw, targetPitch);
                        }

                        ReZeroData.getRewindStepTracker().put(uuid, currentStep);
                        LOGGER.info("ReZero: Executing step {}/{} to index {} at elapsed tick {}", currentStep, numberOfSteps, targetIndex, elapsedTicks);
                    }
                }
            }
        } else {
            ItemStack stack = findReZeroStack(player);
            if (!stack.isEmpty() && !player.getCooldowns().isOnCooldown(stack.getItem())) {
                ReZeroData.savePosition(player);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack stack = findReZeroStack(player);
        if (stack.isEmpty()) {
            return;
        }

        if (player.getHealth() - event.getAmount() > 0) {
            return;
        }

        Item item = stack.getItem();
        if (player.getCooldowns().isOnCooldown(item)) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        int usesRemaining = tag.contains("UsesRemaining") ? tag.getInt("UsesRemaining") : ModConfig.getMaxUsesRezero();
        if (item == ModItems.REZERO.get() && usesRemaining <= 0) {
            return;
        }

        PlayerLocation saved = ReZeroData.getSavedLocation(player.getUUID());
        if (saved == null) {
            LOGGER.warn("[PlayerEventHandler] No saved position found for player {}", player.getName().getString());
            return;
        }

        event.setCanceled(true);
        ReZeroData.triggerOverlay(player);

        float maxHealthAfterRezero = ModConfig.getMaxHealthAfterRezero();
        if (player.getHealth() > maxHealthAfterRezero) {
            player.setHealth(maxHealthAfterRezero);
        }

        if (item == ModItems.REZERO.get()) {
            usesRemaining--;
            tag.putInt("UsesRemaining", usesRemaining);
            player.getCooldowns().addCooldown(item, ModConfig.getCooldownRezeroSeconds() * 20);
            if (usesRemaining <= 0) {
                stack.shrink(1);
                player.getInventory().setChanged();
            }
        } else if (item == ModItems.REZERO_ETERNAL.get()) {
            player.getCooldowns().addCooldown(item, ModConfig.getCooldownEternalSeconds() * 20);
        }
    }

    private static ItemStack findReZeroStack(ServerPlayer player) {
        for (ItemStack stack : player.getInventory().items) {
            Item item = stack.getItem();
            if (item == ModItems.REZERO.get() || item == ModItems.REZERO_ETERNAL.get()) {
                CompoundTag tag = stack.getOrCreateTag();
                if (!tag.contains("UsesRemaining") && item == ModItems.REZERO.get()) {
                    tag.putInt("UsesRemaining", ModConfig.getMaxUsesRezero());
                }
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
}