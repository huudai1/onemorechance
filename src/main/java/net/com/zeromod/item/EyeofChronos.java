package net.com.zeromod.item;

import net.com.zeromod.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.Blocks;
import net.com.zeromod.network.ClientboundPlayAnimationPacket;
import net.com.zeromod.network.ModNetwork;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;

@Mod.EventBusSubscriber(modid = "zeromod", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EyeofChronos extends Item {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private static final Set<UUID> processedProjectiles = new HashSet<>();
    private static final int DEFAULT_DURABILITY = 100;

    public EyeofChronos(Properties properties) {
        super(properties.durability(DEFAULT_DURABILITY));
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return ModConfig.getMaxDurability();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            UUID playerUUID = player.getUUID();
            PlayerData data = playerDataMap.computeIfAbsent(playerUUID, k -> new PlayerData());

            if (data.isOnCooldown(level.getGameTime())) {
                long ticksRemaining = data.cooldownEnd - level.getGameTime();
                int secondsRemaining = (int) (ticksRemaining / 20);
                player.displayClientMessage(Component.literal("Cooldown: " + secondsRemaining + " seconds!"), true);
                return InteractionResultHolder.fail(stack);
            }

            if (data.isActive) {
                long ticksRemaining = data.effectEndTime - level.getGameTime();
                int secondsRemaining = (int) (ticksRemaining / 20);
                player.displayClientMessage(Component.literal("Dodge effect: " + secondsRemaining + " seconds!"), true);
                return InteractionResultHolder.fail(stack);
            }

            int durationTicks = (int) ModConfig.getEffectDuration();
            ModNetwork.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                    new ClientboundPlayAnimationPacket(ClientboundPlayAnimationPacket.AnimationType.SWAP, true, durationTicks)
            );
            data.activate(level.getGameTime());
            player.displayClientMessage(Component.literal("Activate dodge for 60 seconds!"), true);
            LOGGER.info("EyeofChronos được kích hoạt bởi {}", player.getName().getString());
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) return;

        UUID playerUUID = player.getUUID();
        PlayerData data = playerDataMap.get(playerUUID);
        if (data == null || !data.isActive) return;

        ItemStack stack = findSiuuDodgeItem(player);
        if (stack.isEmpty()) return;

        LOGGER.info("LivingAttackEvent được kích hoạt cho {} từ {}", player.getName().getString(), event.getSource().getEntity());

        if (event.getSource().getEntity() instanceof Player attacker) {
            data.attackers.add(attacker.getUUID());
            LOGGER.info("Người chơi {} được đánh dấu là kẻ tấn công của {}", attacker.getName().getString(), player.getName().getString());
        }

        if (event.getSource().getDirectEntity() instanceof Projectile) {
            event.setCanceled(true);
            LOGGER.info("Hủy bỏ tấn công bằng đạn cho {} từ {}", player.getName().getString(), event.getSource().getEntity());
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player) || player.level().isClientSide()) return;

        UUID playerUUID = player.getUUID();
        PlayerData data = playerDataMap.get(playerUUID);
        if (data == null || !data.isActive) return;

        ItemStack stack = findSiuuDodgeItem(player);
        if (stack.isEmpty()) return;

        if (tryTeleport(player, stack, (ServerLevel) player.level(), event.getEntity())) {
            LOGGER.info("Dịch chuyển {} sau khi gây sát thương cho {}", player.getName().getString(), event.getEntity().getName().getString());
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        ServerLevel level = event.getServer().overworld();
        long currentTime = level.getGameTime();

        if (currentTime % ModConfig.getScanFrequencyTicks() != 0) return;

        playerDataMap.entrySet().removeIf(entry -> {
            UUID playerUUID = entry.getKey();
            PlayerData data = entry.getValue();

            Player player = level.getPlayerByUUID(playerUUID);
            if (player == null) return true;

            if (data.isActive && currentTime >= data.effectEndTime) {
                data.deactivate();
                player.displayClientMessage(Component.literal("Dodge effect expired !"), true);
                ModNetwork.INSTANCE.send(
                        PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                        new ClientboundPlayAnimationPacket(ClientboundPlayAnimationPacket.AnimationType.SWAP, false, 0)
                );
                LOGGER.info("Hiệu ứng né tránh hết hạn cho {}", player.getName().getString());
                return false;
            }

            if (data.isActive && currentTime % 20 == 0) {
                long ticksRemaining = data.effectEndTime - currentTime;
                if (ticksRemaining > 0) {
                    int secondsRemaining = (int) (ticksRemaining / 20);
                    player.displayClientMessage(Component.literal("Dodge effect: " + secondsRemaining + " Seconds"), true);
                }
            }

            if (data.isActive) {
                ItemStack stack = findSiuuDodgeItem(player);
                if (stack.isEmpty()) return false;

                AABB projectileHitbox = new AABB(
                        player.position().x - ModConfig.getProjectileHitboxSize(),
                        player.position().y - ModConfig.getProjectileHitboxSize(),
                        player.position().z - ModConfig.getProjectileHitboxSize(),
                        player.position().x + ModConfig.getProjectileHitboxSize(),
                        player.position().y + ModConfig.getProjectileHitboxSize(),
                        player.position().z + ModConfig.getProjectileHitboxSize()
                );

                AABB mobHitbox = new AABB(
                        player.position().x - ModConfig.getMobHitboxSize(),
                        player.position().y - ModConfig.getMobHitboxSize(),
                        player.position().z - ModConfig.getMobHitboxSize(),
                        player.position().x + ModConfig.getMobHitboxSize(),
                        player.position().y + ModConfig.getMobHitboxSize(),
                        player.position().z + ModConfig.getMobHitboxSize()
                );

                AABB endermanHitbox = new AABB(
                        player.position().x - ModConfig.getEndermanHitboxSize(),
                        player.position().y - ModConfig.getEndermanHitboxSize(),
                        player.position().z - ModConfig.getEndermanHitboxSize(),
                        player.position().x + ModConfig.getEndermanHitboxSize(),
                        player.position().y + ModConfig.getEndermanHitboxSize(),
                        player.position().z + ModConfig.getEndermanHitboxSize()
                );

                AABB detectionBox = new AABB(
                        player.position().x - ModConfig.getDetectionRange(),
                        player.position().y - ModConfig.getDetectionRange(),
                        player.position().z - ModConfig.getDetectionRange(),
                        player.position().x + ModConfig.getDetectionRange(),
                        player.position().y + ModConfig.getDetectionRange(),
                        player.position().z + ModConfig.getDetectionRange()
                );

                for (Creeper creeper : level.getEntitiesOfClass(Creeper.class, detectionBox)) {
                    if (creeper.isIgnited() || creeper.getSwellDir() > 0) {
                        LOGGER.debug("Phát hiện Creeper gần {}: Ignited={}, SwellDir={}", player.getName().getString(), creeper.isIgnited(), creeper.getSwellDir());
                        if (tryTeleport(player, stack, level, creeper)) {
                            LOGGER.info("Dịch chuyển {} do vụ nổ Creeper", player.getName().getString());
                            return false;
                        }
                    }
                }

                for (Projectile projectile : level.getEntitiesOfClass(Projectile.class, projectileHitbox)) {
                    if (projectile.getOwner() != player && projectile.isAlive() && projectile.getDeltaMovement().lengthSqr() > 0.1) {
                        UUID projectileUUID = projectile.getUUID();
                        if (!processedProjectiles.contains(projectileUUID)) {
                            processedProjectiles.add(projectileUUID);
                            if (tryTeleport(player, stack, level, projectile)) {
                                LOGGER.info("Dịch chuyển {} do Đạn {}", player.getName().getString(), projectile.getClass().getSimpleName());
                                return false;
                            }
                        }
                    }
                }

                for (Mob mob : level.getEntitiesOfClass(Mob.class, mobHitbox)) {
                    if (!(mob instanceof Creeper) && mob.getTarget() == player) {
                        if (tryTeleport(player, stack, level, mob)) {
                            LOGGER.info("Dịch chuyển {} do Mob {}", player.getName().getString(), mob.getName().getString());
                            return false;
                        }
                    }
                }

                for (EnderMan enderman : level.getEntitiesOfClass(EnderMan.class, endermanHitbox)) {
                    if (enderman.getTarget() == player) {
                        if (tryTeleport(player, stack, level, enderman)) {
                            LOGGER.info("Dịch chuyển {} do Enderman", player.getName().getString());
                            return false;
                        }
                    }
                }

                for (Player otherPlayer : level.getEntitiesOfClass(Player.class, mobHitbox)) {
                    if (otherPlayer != player && data.attackers.contains(otherPlayer.getUUID())) {
                        if (tryTeleport(player, stack, level, otherPlayer)) {
                            LOGGER.info("Dịch chuyển {} do người chơi tấn công {}", player.getName().getString(), otherPlayer.getName().getString());
                            return false;
                        }
                    }
                }
            }

            return false;
        });
    }

    private static ItemStack findSiuuDodgeItem(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof EyeofChronos) {
                return stack;
            }
        }
        if (player.getOffhandItem().getItem() instanceof EyeofChronos) {
            return player.getOffhandItem();
        }
        return ItemStack.EMPTY;
    }

    private static boolean tryTeleport(Player player, ItemStack stack, ServerLevel level, Entity dangerSource) {
        long currentTime = level.getGameTime();
        PlayerData data = playerDataMap.get(player.getUUID());
        if (data == null || !data.isActive) return false;

        Vec3 startPos = player.position();
        Vec3 dangerPos = dangerSource != null ? dangerSource.position() : startPos;
        double bestDistance = -1;
        Vec3 bestPos = null;
        boolean isProjectile = dangerSource instanceof Projectile;

        for (double xOffset = -ModConfig.getTeleportBoxXz(); xOffset <= ModConfig.getTeleportBoxXz(); xOffset += 1.0) {
            for (double zOffset = -ModConfig.getTeleportBoxXz(); zOffset <= ModConfig.getTeleportBoxXz(); zOffset += 1.0) {
                for (double yOffset = -ModConfig.getTeleportBoxYDown(); yOffset <= ModConfig.getTeleportBoxYUp(); yOffset += 1.0) {
                    double x = Math.floor(startPos.x + xOffset);
                    double z = Math.floor(startPos.z + zOffset);
                    double y = Math.floor(startPos.y + yOffset);

                    BlockPos pos = new BlockPos((int) x, (int) y, (int) z);
                    BlockState belowState = level.getBlockState(pos.below());
                    FluidState fluidState = level.getFluidState(pos);
                    BlockState headState = level.getBlockState(pos.above());
                    BlockState headState2 = level.getBlockState(pos.above(2));
                    BlockState currentState = level.getBlockState(pos);

                    boolean isSolidOrIce = belowState.isSolidRender(level, pos.below()) ||
                            belowState.getBlock() == Blocks.ICE ||
                            belowState.getBlock() == Blocks.PACKED_ICE ||
                            belowState.getBlock() == Blocks.BLUE_ICE ||
                            belowState.getBlock() == Blocks.FROSTED_ICE;

                    boolean isSafePosition = isSolidOrIce &&
                            !fluidState.isSource() &&
                            !level.getFluidState(pos.above()).isSource() &&
                            !headState.isSolidRender(level, pos.above()) &&
                            !headState2.isSolidRender(level, pos.above(2)) &&
                            level.noCollision(player, player.getBoundingBox().move(x - player.getX(), y - player.getY(), z - player.getZ())) &&
                            fluidState.getType() != Fluids.LAVA &&
                            fluidState.getType() != Fluids.FLOWING_LAVA &&
                            !currentState.isSuffocating(level, pos) &&
                            !belowState.is(Blocks.MAGMA_BLOCK) &&
                            !belowState.is(Blocks.CAMPFIRE) &&
                            !belowState.is(Blocks.SOUL_CAMPFIRE);

                    if (isSafePosition) {
                        double safeY = Math.floor(y) + 1.0;
                        Vec3 candidatePos = new Vec3(x, safeY, z);
                        double distance = candidatePos.distanceTo(dangerPos);
                        if (distance > bestDistance) {
                            bestDistance = distance;
                            bestPos = candidatePos;
                        }
                    }
                }
            }
        }

        if (bestPos == null) {
            data.failedTeleportCount++;
            player.displayClientMessage(Component.literal("TP failed!"), true);
            LOGGER.warn("Không thể dịch chuyển {}: Không tìm thấy vị trí an toàn (Lần thất bại: {})", player.getName().getString(), data.failedTeleportCount);

            if (data.failedTeleportCount >= 2) {
                Vec3 fallbackPos = calculateFallbackPosition(startPos, dangerPos, isProjectile, level, player);
                if (fallbackPos != null) {
                    bestPos = fallbackPos;
                    data.failedTeleportCount = 0;
                    LOGGER.info("Thử dịch chuyển dự phòng cho {} đến ({}, {}, {})", player.getName().getString(), bestPos.x, bestPos.y, bestPos.z);
                }
            }
        } else {
            data.failedTeleportCount = 0;
        }

        if (bestPos != null) {
            for (int i = 0; i < 16; i++) {
                level.addParticle(ParticleTypes.PORTAL,
                        startPos.x + (level.random.nextDouble() - 0.5) * 0.8,
                        startPos.y + level.random.nextDouble() * 2.0,
                        startPos.z + (level.random.nextDouble() - 0.5) * 0.8,
                        (level.random.nextDouble() - 0.5) * 0.1,
                        (level.random.nextDouble() - 0.5) * 0.1,
                        (level.random.nextDouble() - 0.5) * 0.1);
            }

            player.teleportTo(bestPos.x, bestPos.y, bestPos.z);

            for (int i = 0; i < 16; i++) {
                level.addParticle(ParticleTypes.PORTAL,
                        bestPos.x + (level.random.nextDouble() - 0.5) * 0.8,
                        bestPos.y + level.random.nextDouble() * 2.0,
                        bestPos.z + (level.random.nextDouble() - 0.5) * 0.8,
                        (level.random.nextDouble() - 0.5) * 0.1,
                        (level.random.nextDouble() - 0.5) * 0.1,
                        (level.random.nextDouble() - 0.5) * 0.1);
            }

            level.playSound(null, startPos.x, startPos.y, startPos.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
            stack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(p.getUsedItemHand()));
            player.displayClientMessage(Component.literal("Dogde !"), true);
            LOGGER.info("Đã dịch chuyển {} đến ({}, {}, {})", player.getName().getString(), bestPos.x, bestPos.y, bestPos.z);
            return true;
        }

        return false;
    }

    private static Vec3 calculateFallbackPosition(Vec3 startPos, Vec3 dangerPos, boolean isProjectile, ServerLevel level, Player player) {
        double x, y, z;

        if (isProjectile) {
            x = Math.floor(startPos.x + ModConfig.getFallbackDistance()) + 0.5;
            y = Math.floor(startPos.y);
            z = Math.floor(startPos.z) + 0.5;
        } else {
            Vec3 direction = startPos.subtract(dangerPos).normalize();
            x = Math.floor(startPos.x + direction.x * ModConfig.getFallbackDistance()) + 0.5;
            y = Math.floor(startPos.y);
            z = Math.floor(startPos.z + direction.z * ModConfig.getFallbackDistance()) + 0.5;
        }

        BlockPos pos = new BlockPos((int) x, (int) y, (int) z);
        BlockState belowState = level.getBlockState(pos.below());
        FluidState fluidState = level.getFluidState(pos);
        BlockState headState = level.getBlockState(pos.above());
        BlockState headState2 = level.getBlockState(pos.above(2));
        BlockState currentState = level.getBlockState(pos);

        boolean isSolidOrIce = belowState.isSolidRender(level, pos.below()) ||
                belowState.getBlock() == Blocks.ICE ||
                belowState.getBlock() == Blocks.PACKED_ICE ||
                belowState.getBlock() == Blocks.BLUE_ICE ||
                belowState.getBlock() == Blocks.FROSTED_ICE;

        boolean isSafePosition = isSolidOrIce &&
                !fluidState.isSource() &&
                !level.getFluidState(pos.above()).isSource() &&
                !headState.isSolidRender(level, pos.above()) &&
                !headState2.isSolidRender(level, pos.above(2)) &&
                level.noCollision(player, player.getBoundingBox().move(x - player.getX(), y - player.getY(), z - player.getZ())) &&
                fluidState.getType() != Fluids.LAVA &&
                fluidState.getType() != Fluids.FLOWING_LAVA &&
                !currentState.isSuffocating(level, pos) &&
                !belowState.is(Blocks.MAGMA_BLOCK) &&
                !belowState.is(Blocks.CAMPFIRE) &&
                !belowState.is(Blocks.SOUL_CAMPFIRE);

        if (isSafePosition) {
            double safeY = Math.floor(y) + 1.0;
            return new Vec3(x, safeY, z);
        }

        for (double yOffset = -ModConfig.getTeleportBoxYDown(); yOffset <= ModConfig.getTeleportBoxYUp(); yOffset += 1.0) {
            double tryY = Math.floor(startPos.y + yOffset);
            pos = new BlockPos((int) x, (int) tryY, (int) z);
            belowState = level.getBlockState(pos.below());
            fluidState = level.getFluidState(pos);
            headState = level.getBlockState(pos.above());
            headState2 = level.getBlockState(pos.above(2));
            currentState = level.getBlockState(pos);

            isSolidOrIce = belowState.isSolidRender(level, pos.below()) ||
                    belowState.getBlock() == Blocks.ICE ||
                    belowState.getBlock() == Blocks.PACKED_ICE ||
                    belowState.getBlock() == Blocks.BLUE_ICE ||
                    belowState.getBlock() == Blocks.FROSTED_ICE;

            isSafePosition = isSolidOrIce &&
                    !fluidState.isSource() &&
                    !level.getFluidState(pos.above()).isSource() &&
                    !headState.isSolidRender(level, pos.above()) &&
                    !headState2.isSolidRender(level, pos.above(2)) &&
                    level.noCollision(player, player.getBoundingBox().move(x - player.getX(), tryY - player.getY(), z - player.getZ())) &&
                    fluidState.getType() != Fluids.LAVA &&
                    fluidState.getType() != Fluids.FLOWING_LAVA &&
                    !currentState.isSuffocating(level, pos) &&
                    !belowState.is(Blocks.MAGMA_BLOCK) &&
                    !belowState.is(Blocks.CAMPFIRE) &&
                    !belowState.is(Blocks.SOUL_CAMPFIRE);

            if (isSafePosition) {
                double safeY = Math.floor(tryY) + 1.0;
                return new Vec3(x, safeY, z);
            }
        }

        return null;
    }

    private static class PlayerData {
        boolean isActive;
        long cooldownEnd;
        long effectEndTime;
        int failedTeleportCount;
        Set<UUID> attackers;

        PlayerData() {
            this.isActive = false;
            this.cooldownEnd = 0;
            this.effectEndTime = 0;
            this.failedTeleportCount = 0;
            this.attackers = new HashSet<>();
        }

        void activate(long currentTime) {
            this.isActive = true;
            this.effectEndTime = currentTime + ModConfig.getEffectDuration();
            this.cooldownEnd = currentTime + ModConfig.getCooldownTicks();
            this.failedTeleportCount = 0;
            this.attackers.clear();
        }

        void deactivate() {
            this.isActive = false;
            this.effectEndTime = 0;
            this.failedTeleportCount = 0;
            this.attackers.clear();
        }

        boolean isOnCooldown(long currentTime) {
            return currentTime < cooldownEnd;
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.zeromod.eye_of_chronos.tooltip")
                .withStyle(style -> style.withColor(0x1E90FF)));
    }
}