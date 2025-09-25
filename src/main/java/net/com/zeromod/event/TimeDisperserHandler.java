package net.com.zeromod.event;

import net.com.zeromod.config.ModConfig;
import net.com.zeromod.item.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import java.util.List;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
 class TimeDisperserHandler {

    private static final Map<UUID, Integer> deathCountdownMap = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) return;
        ItemStack disperserStack = findTimeDisperserStack(player);
        if (disperserStack.isEmpty()) return;

        DamageSource source = event.getSource();


        if (source.getEntity() instanceof Mob) {
            float originalDamage = event.getAmount();
            int hungerDamage = (int) Math.ceil(originalDamage / 2.0);

            FoodData foodData = player.getFoodData();
            int currentFood = foodData.getFoodLevel();

            if (currentFood > 0) {

                if (currentFood >= hungerDamage) {
                    foodData.setFoodLevel(currentFood - hungerDamage);
                    player.causeFoodExhaustion(0.5F * hungerDamage);


                    disperserStack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));


                    event.setCanceled(true);

                    return;
                }

                else {
                    int absorbedByFood = currentFood;
                    int damageLeftToDoToHunger = hungerDamage - absorbedByFood;

                    foodData.setFoodLevel(0);
                    player.causeFoodExhaustion(0.5F * absorbedByFood);


                    disperserStack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));


                    float remainingDamage = damageLeftToDoToHunger * 2.0f;
                    event.setAmount(remainingDamage);
                }
            }
        }

        boolean hasTimeDisperser = false;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() == ModItems.TIME_DISPERSER.get()) {
                hasTimeDisperser = true;
                break;
            }
        }

        if (!hasTimeDisperser) return;

        float finalDamage = event.getAmount();

        if (finalDamage >= player.getHealth()) {
            UUID id = player.getUUID();
            if (!deathCountdownMap.containsKey(id)) {
                event.setAmount(0);
                player.setHealth(1.0f);
                player.getFoodData().setFoodLevel(0);
                int knockdownTicks = ModConfig.getTimeDisperserKnockdownSeconds() * 20;
                deathCountdownMap.put(id, knockdownTicks);

                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, knockdownTicks, 0, false, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, knockdownTicks, 4, false, false, false));
                player.displayClientMessage(Component.literal("Death is coming to you slowly"), true);
                deaggroNearbyMobs(player);
            }
        }
    }

    private static ItemStack findTimeDisperserStack(Player player) {

        if (player.getMainHandItem().is(ModItems.TIME_DISPERSER.get())) {
            return player.getMainHandItem();
        }
        if (player.getOffhandItem().is(ModItems.TIME_DISPERSER.get())) {
            return player.getOffhandItem();
        }

        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ModItems.TIME_DISPERSER.get())) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static void deaggroNearbyMobs(Player player) {
        double radius = ModConfig.getTimeDisperserDeaggroRadius();
        AABB area = new AABB(player.blockPosition()).inflate(radius);
        List<Mob> nearbyMobs = player.level().getEntitiesOfClass(Mob.class, area);

        for (Mob mob : nearbyMobs) {
            // Check if the mob is targeting the player
            if (mob.getTarget() != null && mob.getTarget().is(player)) {
                mob.setTarget(null); // Stop attacking
                mob.setLastHurtByMob(null); // Forget who hurt them
            }
        }
    }

    public static void onPlayerTick(Player player) {
        if (player.level().isClientSide || !player.isAlive()) return;

        UUID id = player.getUUID();
        if (!deathCountdownMap.containsKey(id)) return;

        int remainingTicks = deathCountdownMap.get(id);
        int secondsLeft = (remainingTicks + 19) / 20;

        player.displayClientMessage(Component.literal("The Reaper swings in " + secondsLeft +" and you're the target" + "----(Eat to survive!)"), true);

        FoodData foodData = player.getFoodData();
        if (foodData.getFoodLevel() >= 20) {
            deathCountdownMap.remove(id);
            player.displayClientMessage(Component.literal("You escaped death!"), true);
            System.out.println("You survived!");
            player.removeEffect(MobEffects.DARKNESS);
            player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            player.removeEffect(MobEffects.DAMAGE_RESISTANCE);
            return;
        }

        if (remainingTicks == ModConfig.getCountdownTicks()) {
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, ModConfig.getCountdownTicks() + 40, 3, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ModConfig.getCountdownTicks() + 20, 5, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, ModConfig.getCountdownTicks() + 20, 2, false, false));
        }

        // Sửa cách gửi âm thanh: Sử dụng Holder cho SoundEvent
        if (remainingTicks % 20 == 0 && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSoundPacket(
                    net.minecraft.core.Holder.direct(SoundEvents.WARDEN_HEARTBEAT), // Sử dụng Holder.direct
                    SoundSource.PLAYERS,
                    player.getX(), player.getY(), player.getZ(),
                    1.0f, 1.0f, player.level().random.nextLong()
            ));
        }

        remainingTicks--;
        if (remainingTicks <= 0) {
            player.hurt(player.damageSources().generic(), Float.MAX_VALUE);
            deathCountdownMap.remove(id);
            System.out.println("Death is always with you!");
            player.removeEffect(MobEffects.DARKNESS);
            player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            player.removeEffect(MobEffects.DAMAGE_RESISTANCE);
        } else {
            deathCountdownMap.put(id, remainingTicks);
        }
    }
}