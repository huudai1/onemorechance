package net.com.zeromod.item;

import net.com.zeromod.config.ModConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.com.zeromod.item.SwapTimeItem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.core.BlockPos;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "zeromod", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CursedHeart extends Item {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final UUID HEALTH_MODIFIER_UUID = UUID.fromString("b7a8e3c1-4f2d-4b9a-9c5e-7d8f6a2b3c4d");
    private static final UUID DAMAGE_MODIFIER_UUID = UUID.fromString("c9d7f4a2-5e3c-4c8b-a7d6-8e9f5b3c4d5e");
    private static final String TAG_ACTIVATED = "Activated";
    private static final String TAG_CONFIRM_CLICK = "ConfirmClick";
    private static final String TAG_LEVEL = "Level";
    private static final String TAG_MOB_KILLS = "MobKills";
    private static final String TAG_HEALTH_UPGRADES = "HealthUpgrades";
    private static final String TAG_DAMAGE_UPGRADES = "DamageUpgrades";
    private static final String TAG_TRANSITION_TICKS = "TransitionTicks";
    private static final String PLAYER_USED_TAG = "zeromod:cursed_heart_used";
    private static final String CURSED_HEART_DATA = "zeromod:cursed_heart_data";
    private static final Path DATA_DIR = Paths.get("config/one_more_chance/cursed_heart");
    private static final Map<UUID, CursedHeartData> PLAYER_DATA_CACHE = new ConcurrentHashMap<>();

    private static Component lastMessage = null;
    private static int displayTicks = 0;

    public static class CursedHeartData {
        public boolean activated = false;
        public int level = 1;
        public int mobKills = 0;
        public int healthUpgrades = 0;
        public int damageUpgrades = 0;
        public int transitionTicks = 0;
    }

    public CursedHeart(Properties properties) {
        super(properties.stacksTo(1));
        try {
            Files.createDirectories(DATA_DIR);
        } catch (IOException e) {
            LOGGER.error("Failed to create CursedHeart data directory", e);
        }
    }

    private static Path getPlayerDataPath(Player player) {
        return DATA_DIR.resolve(player.getUUID().toString() + ".json");
    }

    private static void savePlayerData(Player player, CursedHeartData data) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Path path = getPlayerDataPath(player);
        try (Writer writer = new FileWriter(path.toFile())) {
            gson.toJson(data, writer);
            PLAYER_DATA_CACHE.put(player.getUUID(), data); //  cache
            LOGGER.debug("Saved CursedHeart data for {}: {}", player.getName().getString(), data);
        } catch (IOException e) {
            LOGGER.error("Failed to save CursedHeart data for {}", player.getName().getString(), e);
        }
    }

    private static CursedHeartData loadPlayerData(Player player) {
        UUID playerUUID = player.getUUID();
        if (PLAYER_DATA_CACHE.containsKey(playerUUID)) {
            return PLAYER_DATA_CACHE.get(playerUUID);
        }

        Gson gson = new Gson();
        Path path = getPlayerDataPath(player);
        if (Files.exists(path)) {
            try (Reader reader = new FileReader(path.toFile())) {
                CursedHeartData data = gson.fromJson(reader, CursedHeartData.class);
                PLAYER_DATA_CACHE.put(playerUUID, data); //cache
                LOGGER.debug("Loaded CursedHeart data for {}: {}", player.getName().getString(), data);
                return data;
            } catch (IOException e) {
                LOGGER.error("Failed to load CursedHeart data for {}", player.getName().getString(), e);
            }
        }
        CursedHeartData data = new CursedHeartData();
        PLAYER_DATA_CACHE.put(playerUUID, data); //cache
        return data;
    }

    private static CompoundTag dataToTag(CursedHeartData data) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(TAG_ACTIVATED, data.activated);
        tag.putInt(TAG_LEVEL, data.level);
        tag.putInt(TAG_MOB_KILLS, data.mobKills);
        tag.putInt(TAG_HEALTH_UPGRADES, data.healthUpgrades);
        tag.putInt(TAG_DAMAGE_UPGRADES, data.damageUpgrades);
        tag.putInt(TAG_TRANSITION_TICKS, data.transitionTicks);
        return tag;
    }

    private static CursedHeartData tagToData(CompoundTag tag) {
        CursedHeartData data = new CursedHeartData();
        data.activated = tag.getBoolean(TAG_ACTIVATED);
        data.level = tag.getInt(TAG_LEVEL);
        data.mobKills = tag.getInt(TAG_MOB_KILLS);
        data.healthUpgrades = tag.getInt(TAG_HEALTH_UPGRADES);
        data.damageUpgrades = tag.getInt(TAG_DAMAGE_UPGRADES);
        data.transitionTicks = tag.getInt(TAG_TRANSITION_TICKS);
        return data;
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            CompoundTag playerData = player.getPersistentData();

            if (playerData.getBoolean(PLAYER_USED_TAG)) {
                player.sendSystemMessage(Component.literal("You have already used a Cursed Heart!"));
                return InteractionResultHolder.fail(stack);
            }

            CompoundTag tag = stack.getOrCreateTag();

            boolean confirmClick = tag.getBoolean(TAG_CONFIRM_CLICK);
            if (!confirmClick) {
                tag.putBoolean(TAG_CONFIRM_CLICK, true);
                player.sendSystemMessage(Component.literal("Are you sure you want to activate the Cursed Heart? Right-click again to confirm."));
                LOGGER.info("CursedHeart confirmation requested by {}", player.getName().getString());
                return InteractionResultHolder.success(stack);
            } else {
                CursedHeartData data = new CursedHeartData();
                data.activated = true;
                data.level = 1;
                data.mobKills = 0;
                data.healthUpgrades = 0;
                data.damageUpgrades = 0;
                tag = dataToTag(data);
                playerData.put(CURSED_HEART_DATA, tag.copy());
                savePlayerData(player, data);
                applyLevelEffects(player, data.level, tag);
                playerData.putBoolean(PLAYER_USED_TAG, true);
                player.sendSystemMessage(Component.literal("Cursed Heart Level 1 activated!"));
                LOGGER.info("CursedHeart activated by {} at Level 1", player.getName().getString());

                stack.shrink(1);
                return InteractionResultHolder.success(stack);
            }
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack stack, Player player) {
        return true;
    }


    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (!player.getPersistentData().getBoolean(PLAYER_USED_TAG)) return;

        CompoundTag tag = getPlayerCursedHeartData(player);
        if (tag == null || !tag.getBoolean(TAG_ACTIVATED)) return;

        int level = tag.getInt(TAG_LEVEL);
        LivingEntity target = event.getEntity();
        float trueDamage = getTrueDamage(level, tag.getInt(TAG_DAMAGE_UPGRADES));

        target.hurt(target.level().damageSources().magic(), trueDamage);

        if (level == 5) {
            float extraDamage = (float) (target.getMaxHealth() * ModConfig.getLevel5ExtraDamagePercent());
            target.hurt(target.level().damageSources().magic(), extraDamage);
            trueDamage += extraDamage;
        }

        if (level >= 4) {
            float healPercent = (level == 4) ? ModConfig.getLevel4HealPercent() : ModConfig.getLevel5HealPercent();
            float healAmount = trueDamage * healPercent;
            player.heal(healAmount);
            LOGGER.debug("Player {} healed {} from true damage", player.getName().getString(), healAmount);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player) && event.getSource().getEntity() instanceof Player player) {
            if (!player.getPersistentData().getBoolean(PLAYER_USED_TAG)) return;

            CursedHeartData data = loadPlayerData(player);
            if (!data.activated) return;

            data.mobKills++;
            CompoundTag tag = dataToTag(data);
            player.getPersistentData().put(CURSED_HEART_DATA, tag.copy());
            savePlayerData(player, data);
            LOGGER.debug("Mob killed by {}: Total kills {}", player.getName().getString(), data.mobKills);


            displayStats(player, tag);

            switch (data.level) {
                case 1 -> handleLevel1(player, data);
                case 2 -> handleLevel2(player, data);
                case 3 -> handleLevel3(player, data);
                case 4 -> handleLevel4(player, data);
                case 5 -> handleLevelMax(player, data);
            }
        }
    }


    private static void displayStats(Player player, CompoundTag tag) {
        int level = tag.getInt(TAG_LEVEL);
        int mobKills = tag.getInt(TAG_MOB_KILLS);
        int healthUpgrades = tag.getInt(TAG_HEALTH_UPGRADES);
        int damageUpgrades = tag.getInt(TAG_DAMAGE_UPGRADES);
        double healthHearts = healthUpgrades * (level <= 3 ? ModConfig.getHealthUpgradeLow() : ModConfig.getHealthUpgradeHigh()) / 2.0;

        Component message = Component.literal("§cCursed Heart Lv." + level + "   " +
                "§7Kills: §f" + mobKills + "   " +
                "§7HP Bonus: §a+" + String.format("%.1f", healthHearts) + "❤   " +
                "§7DMG Bonus: §c+" + getTrueDamage(level, damageUpgrades));

        player.displayClientMessage(message, true); // true để hiển thị trên action bar
        lastMessage = message;
        displayTicks = 60;
    }


    @SubscribeEvent
    public static void onServerTick(net.minecraftforge.event.TickEvent.ServerTickEvent event) {

    }


    private static boolean shouldPlayerBurn(Player player) {
        CompoundTag tag = getPlayerCursedHeartData(player);
        if (tag == null || !tag.getBoolean(TAG_ACTIVATED)) return false;

        int level = tag.getInt(TAG_LEVEL);
        if (level >= 5) return false;

        Level levelWorld = player.level();
        BlockPos pos = player.blockPosition();

        if (player.isInWater()) return false;
        if (!player.getInventory().armor.get(3).isEmpty()) return false;
        if (!levelWorld.canSeeSky(pos)) return false;
        if (!levelWorld.isDay()) return false;


        player.addEffect(new MobEffectInstance(MobEffects.WITHER, 20, 0, false, false));
        player.hurt(player.level().damageSources().wither(), 1.0f);
        return true;
    }

    private static void handleLevel1(Player player, CursedHeartData data) {
        if (data.mobKills >= (data.healthUpgrades + 1) * ModConfig.getLevel1HealthMobKills()) {
            data.healthUpgrades++;
            updateHealth(player, data.healthUpgrades, data.level);
            savePlayerData(player, data);
            LOGGER.debug("Health Upgrades increased to {} for {}", data.healthUpgrades, player.getName().getString());
        }

        if (data.mobKills >= (data.damageUpgrades + 1) * ModConfig.getLevel1DamageMobKills()) {
            data.damageUpgrades++;
            updateDamage(player, data.damageUpgrades, data.level);
            savePlayerData(player, data);
            LOGGER.debug("Damage Upgrades increased to {} for {}", data.damageUpgrades, player.getName().getString());
        }

        if (data.healthUpgrades >= ModConfig.getLevel1To2HealthUpgrades() && data.damageUpgrades >= ModConfig.getLevel1To2DamageUpgrades()) {
            data.level = 2;
            data.healthUpgrades = 0;
            data.damageUpgrades = 0;
            data.transitionTicks = ModConfig.getTransitionTicks();
            CompoundTag tag = dataToTag(data);
            applyLevelEffects(player, data.level, tag);
            player.sendSystemMessage(Component.literal("Cursed Heart upgraded to Level 2!"));
            LOGGER.info("CursedHeart upgraded to Level 2 for {}", player.getName().getString());
            player.getPersistentData().put(CURSED_HEART_DATA, tag.copy());
            savePlayerData(player, data);
        }
    }

    private static void handleLevel2(Player player, CursedHeartData data) {
        if (data.mobKills >= (data.healthUpgrades + 1) * ModConfig.getLevel2HealthMobKills()) {
            data.healthUpgrades++;
            updateHealth(player, data.healthUpgrades, data.level);
            savePlayerData(player, data);
            LOGGER.debug("Health Upgrades increased to {} for {}", data.healthUpgrades, player.getName().getString());
        }

        if (data.mobKills >= (data.damageUpgrades + 1) * ModConfig.getLevel2DamageMobKills()) {
            data.damageUpgrades++;
            updateDamage(player, data.damageUpgrades, data.level);
            savePlayerData(player, data);
            LOGGER.debug("Damage Upgrades increased to {} for {}", data.damageUpgrades, player.getName().getString());
        }

        if (data.healthUpgrades >= ModConfig.getLevel2To3HealthUpgrades() && data.damageUpgrades >= ModConfig.getLevel2To3DamageUpgrades()) {
            data.level = 3;
            data.healthUpgrades = 0;
            data.damageUpgrades = 0;
            data.transitionTicks = ModConfig.getTransitionTicks();
            CompoundTag tag = dataToTag(data);
            applyLevelEffects(player, data.level, tag);
            player.sendSystemMessage(Component.literal("Cursed Heart upgraded to Level 3!"));
            LOGGER.info("CursedHeart upgraded to Level 3 for {}", player.getName().getString());
            player.getPersistentData().put(CURSED_HEART_DATA, tag.copy());
            savePlayerData(player, data);
        }
    }

    private static void handleLevel3(Player player, CursedHeartData data) {
        if (data.mobKills >= (data.healthUpgrades + 1) * ModConfig.getLevel3HealthMobKills()) {
            data.healthUpgrades++;
            updateHealth(player, data.healthUpgrades, data.level);
            savePlayerData(player, data);
            LOGGER.debug("Health Upgrades increased to {} for {}", data.healthUpgrades, player.getName().getString());
        }

        if (data.mobKills >= (data.damageUpgrades + 1) * ModConfig.getLevel3DamageMobKills()) {
            data.damageUpgrades++;
            updateDamage(player, data.damageUpgrades, data.level);
            savePlayerData(player, data);
            LOGGER.debug("Damage Upgrades increased to {} for {}", data.damageUpgrades, player.getName().getString());
        }

        if (data.healthUpgrades >= ModConfig.getLevel3To4HealthUpgrades() && data.damageUpgrades >= ModConfig.getLevel3To4DamageUpgrades()) {
            data.level = 4;
            data.healthUpgrades = 0;
            data.damageUpgrades = 0;
            data.transitionTicks = ModConfig.getTransitionTicks();
            CompoundTag tag = dataToTag(data);
            applyLevelEffects(player, data.level, tag);
            player.sendSystemMessage(Component.literal("Cursed Heart upgraded to Level 4!"));
            LOGGER.info("CursedHeart upgraded to Level 4 for {}", player.getName().getString());
            player.getPersistentData().put(CURSED_HEART_DATA, tag.copy());
            savePlayerData(player, data);
        }
    }

    private static void handleLevel4(Player player, CursedHeartData data) {
        if (data.mobKills >= (data.healthUpgrades + 1) * ModConfig.getLevel4HealthMobKills()) {
            data.healthUpgrades++;
            updateHealth(player, data.healthUpgrades, data.level);
            savePlayerData(player, data);
            LOGGER.debug("Health Upgrades increased to {} for {}", data.healthUpgrades, player.getName().getString());
        }

        if (data.mobKills >= (data.damageUpgrades + 1) * ModConfig.getLevel4DamageMobKills()) {
            data.damageUpgrades++;
            updateDamage(player, data.damageUpgrades, data.level);
            savePlayerData(player, data);
            LOGGER.debug("Damage Upgrades increased to {} for {}", data.damageUpgrades, player.getName().getString());
        }

        if (data.healthUpgrades >= ModConfig.getLevel4To5HealthUpgrades() && data.damageUpgrades >= ModConfig.getLevel4To5DamageUpgrades()) {
            data.level = 5;
            data.healthUpgrades = 0;
            data.damageUpgrades = 0;
            data.transitionTicks = ModConfig.getTransitionTicks();
            CompoundTag tag = dataToTag(data);
            applyLevelEffects(player, data.level, tag);
            player.sendSystemMessage(Component.literal("Cursed Heart reached Max Level!"));
            LOGGER.info("CursedHeart reached Max Level for {}", player.getName().getString());
            player.getPersistentData().put(CURSED_HEART_DATA, tag.copy());
            savePlayerData(player, data);
        }
    }

    private static void handleLevelMax(Player player, CursedHeartData data) {
        if (data.mobKills >= (data.healthUpgrades + 1) * ModConfig.getLevel5HealthMobKills()) {
            data.healthUpgrades++;
            data.damageUpgrades++;
            updateHealth(player, data.healthUpgrades, data.level);
            updateDamage(player, data.damageUpgrades, data.level);
            savePlayerData(player, data);
            LOGGER.debug("Max Level - Health Upgrades: {}, Damage Upgrades: {} for {}",
                    data.healthUpgrades, data.damageUpgrades, player.getName().getString());
        }
    }

    private static void applyLevelEffects(Player player, int level, ItemStack stack) {
        applyLevelEffects(player, level, stack.getOrCreateTag());
    }

    private static void applyLevelEffects(Player player, int level, CompoundTag tag) {
        player.removeAllEffects();

        AttributeInstance healthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance damageAttribute = player.getAttribute(Attributes.ATTACK_DAMAGE);

        if (!SwapTimeItem.isSwap && healthAttribute != null) {
            healthAttribute.removeModifier(HEALTH_MODIFIER_UUID);
            healthAttribute.addPermanentModifier(new AttributeModifier(
                    HEALTH_MODIFIER_UUID, "CursedHeartHealth", ModConfig.getBaseHealthReduction(), AttributeModifier.Operation.ADDITION
            ));
            player.setHealth(Math.min(player.getHealth(), player.getMaxHealth()));
        }

        if (damageAttribute != null) {
            damageAttribute.removeModifier(DAMAGE_MODIFIER_UUID);
            damageAttribute.addPermanentModifier(new AttributeModifier(
                    DAMAGE_MODIFIER_UUID, "CursedHeartDamage", ModConfig.getBaseTrueDamage(), AttributeModifier.Operation.ADDITION
            ));
        }

        switch (level) {
            case 1:
                break;
            case 2:
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, -1, 0, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, -1, 0, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, -1, 0, false, false));
                break;
            case 3:
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, -1, 0, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, -1, 0, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, -1, 0, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, -1, 0, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, -1, 0, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, -1, 1, false, false));
                break;
            case 4:
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, -1, 0, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, -1, 1, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, -1, 1, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, -1, 0, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, -1, 0, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, -1, 0, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, -1, 2, false, false));
                break;
            case 5:
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, -1, 0, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, -1, 2, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, -1, 2, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, -1, 1, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, -1, 1, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, -1, 3, false, false));
                break;
        }

        int healthUpgrades = tag.getInt(TAG_HEALTH_UPGRADES);
        int damageUpgrades = tag.getInt(TAG_DAMAGE_UPGRADES);
        if (!SwapTimeItem.isSwap) {
            updateHealth(player, healthUpgrades, level);
        }
        updateDamage(player, damageUpgrades, level);

        player.getPersistentData().put(CURSED_HEART_DATA, tag.copy());
    }

    private static void updateHealth(Player player, int healthUpgrades, int level) {
        if (SwapTimeItem.isSwap) {
            return;
        }

        AttributeInstance healthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttribute != null) {
            healthAttribute.removeModifier(HEALTH_MODIFIER_UUID);

            double healthModifier = ModConfig.getBaseHealthReduction()
                    + healthUpgrades * (level <= 3 ? ModConfig.getHealthUpgradeLow() : ModConfig.getHealthUpgradeHigh());

            healthAttribute.addPermanentModifier(new AttributeModifier(
                    HEALTH_MODIFIER_UUID, "CursedHeartHealth", healthModifier, AttributeModifier.Operation.ADDITION
            ));

            player.setHealth(Math.min(player.getHealth(), player.getMaxHealth()));

            LOGGER.debug("Updated health for player {}: Upgrades {}, Modifier {}, Max Health {}",
                    player.getName().getString(), healthUpgrades, healthModifier, player.getMaxHealth());
        }
    }

    private static void updateDamage(Player player, int damageUpgrades, int level) {
        AttributeInstance damageAttribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageAttribute != null) {
            damageAttribute.removeModifier(DAMAGE_MODIFIER_UUID);
            double damageModifier = getTrueDamage(level, damageUpgrades);
            damageAttribute.addPermanentModifier(new AttributeModifier(
                    DAMAGE_MODIFIER_UUID, "CursedHeartDamage", damageModifier, AttributeModifier.Operation.ADDITION
            ));
            LOGGER.debug("Updated damage for player {}: Upgrades {}, Modifier {}",
                    player.getName().getString(), damageUpgrades, damageModifier);
        }
    }

    private static float getTrueDamage(int level, int damageUpgrades) {
        return switch (level) {
            case 1 -> ModConfig.getBaseTrueDamage() + damageUpgrades * ModConfig.getLevel1TrueDamage();
            case 2 -> ModConfig.getLevel2TrueDamage() + damageUpgrades * ModConfig.getLevel1TrueDamage();
            case 3 -> ModConfig.getLevel3TrueDamage() + damageUpgrades * ModConfig.getLevel1TrueDamage();
            case 4 -> ModConfig.getLevel4TrueDamage() + damageUpgrades * ModConfig.getLevel1TrueDamage();
            case 5 -> ModConfig.getLevel5TrueDamage() + damageUpgrades * ModConfig.getLevel5TrueDamageUpgrade();
            default -> ModConfig.getBaseTrueDamage();
        };
    }

    public static CompoundTag getPlayerCursedHeartData(Player player) {
        CursedHeartData data = loadPlayerData(player);
        return dataToTag(data);
    }

    @SubscribeEvent
    public static void onPlayerFallDamage(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player && event.getSource() == player.level().damageSources().fall()) {
            if (!player.getPersistentData().getBoolean(PLAYER_USED_TAG)) return;

            CompoundTag tag = getPlayerCursedHeartData(player);
            if (tag != null && tag.getBoolean(TAG_ACTIVATED)) {
                int level = tag.getInt(TAG_LEVEL);
                if (level >= 4) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (!player.getPersistentData().getBoolean(PLAYER_USED_TAG) || SwapTimeItem.isSwap) return;

        CursedHeartData data = loadPlayerData(player);
        if (data.activated) {
            CompoundTag tag = dataToTag(data);
            player.getPersistentData().put(CURSED_HEART_DATA, tag.copy());
            LOGGER.debug("Applying effects on join for {}: Level {}, Health Upgrades {}, Damage Upgrades {}, Mob Kills {}",
                    player.getName().getString(), data.level, data.healthUpgrades, data.damageUpgrades, data.mobKills);
            applyLevelEffects(player, data.level, tag);
        } else {
            LOGGER.warn("No valid CursedHeart data found for player {} despite PLAYER_USED_TAG being true",
                    player.getName().getString());
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (!player.getPersistentData().getBoolean(PLAYER_USED_TAG) || SwapTimeItem.isSwap) return;

        CursedHeartData data = loadPlayerData(player);
        if (data.activated) {
            CompoundTag tag = dataToTag(data);
            player.getPersistentData().put(CURSED_HEART_DATA, tag.copy());
            LOGGER.debug("Applying effects on respawn for {}: Level {}, Health Upgrades {}, Damage Upgrades {}, Mob Kills {}",
                    player.getName().getString(), data.level, data.healthUpgrades, data.damageUpgrades, data.mobKills);
            applyLevelEffects(player, data.level, tag);
        } else {
            LOGGER.warn("No valid CursedHeart data found for player {} on respawn",
                    player.getName().getString());
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.zeromod.cursed_heart.tooltip")
                .withStyle(style -> style.withColor(0x1E90FF)));
    }
}