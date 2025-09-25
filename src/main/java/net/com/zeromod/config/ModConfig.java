package net.com.zeromod.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig.Type;

public class ModConfig {
    public static final ForgeConfigSpec SPEC;

    // ReZero Config
    public static final ForgeConfigSpec.IntValue OVERLAY_DURATION_SECONDS;
    public static final ForgeConfigSpec.IntValue COOLDOWN_REZERO_SECONDS;
    public static final ForgeConfigSpec.IntValue COOLDOWN_ETERNAL_SECONDS;
    public static final ForgeConfigSpec.IntValue MAX_USES_REZERO;
    public static final ForgeConfigSpec.DoubleValue MAX_HEALTH_AFTER_REZERO;
    public static final ForgeConfigSpec.IntValue POSITION_HISTORY_SIZE;
    public static final ForgeConfigSpec.IntValue POSITION_SAVE_INTERVAL_TICKS;
    public static final ForgeConfigSpec.IntValue REZERO_TELEPORT_STEPS;
    public static final ForgeConfigSpec.DoubleValue REZERO_TELEPORT_DURATION_SECONDS;


    //StarterPack Config
    public  static  final ForgeConfigSpec.BooleanValue ENABLE_STARTER_ITEMS ;

    // SwapTime Config
    public static final ForgeConfigSpec.IntValue COUNTDOWN_TICKS;
    public static final ForgeConfigSpec.LongValue SWAP_DURATION;
    public static final ForgeConfigSpec.IntValue AI_DURATION;
    public static final ForgeConfigSpec.LongValue SWAP_TIME_OFFSET;
    public static final ForgeConfigSpec.IntValue SLOW_DURATION;
    public static final ForgeConfigSpec.IntValue SLOW_LEVEL;
    public static final ForgeConfigSpec.IntValue WEAKNESS_LEVEL;
    public static final ForgeConfigSpec.IntValue GENERATION_LEVEL;
    public static final ForgeConfigSpec.IntValue GENERATION_DURATION;
    public static final ForgeConfigSpec.IntValue MAX_DURABILITY;

    // TimeDisperser Config
    public static final ForgeConfigSpec.LongValue COOLDOWN_TICKS;
    // NEW CONFIGS for TimeDisperser
    public static final ForgeConfigSpec.IntValue TIME_DISPERSER_KNOCKDOWN_SECONDS;
    public static final ForgeConfigSpec.DoubleValue TIME_DISPERSER_DEAGGRO_RADIUS;

    // Eye Of Chronos Config
    public static final ForgeConfigSpec.LongValue EFFECT_DURATION;
    public static final ForgeConfigSpec.DoubleValue DETECTION_RANGE;
    public static final ForgeConfigSpec.DoubleValue PROJECTILE_HITBOX_SIZE;
    public static final ForgeConfigSpec.DoubleValue MOB_HITBOX_SIZE;
    public static final ForgeConfigSpec.DoubleValue ENDERMAN_HITBOX_SIZE;
    public static final ForgeConfigSpec.IntValue SCAN_FREQUENCY_TICKS;
    public static final ForgeConfigSpec.DoubleValue TELEPORT_BOX_XZ;
    public static final ForgeConfigSpec.DoubleValue TELEPORT_BOX_Y_UP;
    public static final ForgeConfigSpec.DoubleValue TELEPORT_BOX_Y_DOWN;
    public static final ForgeConfigSpec.DoubleValue FALLBACK_DISTANCE;

    // Cursed Heart Config
    public static final ForgeConfigSpec.IntValue LEVEL_1_HEALTH_MOB_KILLS;
    public static final ForgeConfigSpec.IntValue LEVEL_2_HEALTH_MOB_KILLS;
    public static final ForgeConfigSpec.IntValue LEVEL_3_HEALTH_MOB_KILLS;
    public static final ForgeConfigSpec.IntValue LEVEL_4_HEALTH_MOB_KILLS;
    public static final ForgeConfigSpec.IntValue LEVEL_5_HEALTH_MOB_KILLS;
    public static final ForgeConfigSpec.IntValue LEVEL_1_DAMAGE_MOB_KILLS;
    public static final ForgeConfigSpec.IntValue LEVEL_2_DAMAGE_MOB_KILLS;
    public static final ForgeConfigSpec.IntValue LEVEL_3_DAMAGE_MOB_KILLS;
    public static final ForgeConfigSpec.IntValue LEVEL_4_DAMAGE_MOB_KILLS;
    public static final ForgeConfigSpec.IntValue LEVEL_1_TO_2_HEALTH_UPGRADES;
    public static final ForgeConfigSpec.IntValue LEVEL_1_TO_2_DAMAGE_UPGRADES;
    public static final ForgeConfigSpec.IntValue LEVEL_2_TO_3_HEALTH_UPGRADES;
    public static final ForgeConfigSpec.IntValue LEVEL_2_TO_3_DAMAGE_UPGRADES;
    public static final ForgeConfigSpec.IntValue LEVEL_3_TO_4_HEALTH_UPGRADES;
    public static final ForgeConfigSpec.IntValue LEVEL_3_TO_4_DAMAGE_UPGRADES;
    public static final ForgeConfigSpec.IntValue LEVEL_4_TO_5_HEALTH_UPGRADES;
    public static final ForgeConfigSpec.IntValue LEVEL_4_TO_5_DAMAGE_UPGRADES;
    public static final ForgeConfigSpec.DoubleValue BASE_TRUE_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue LEVEL_1_TRUE_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue LEVEL_2_TRUE_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue LEVEL_3_TRUE_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue LEVEL_4_TRUE_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue LEVEL_5_TRUE_DAMAGE;
    public static final ForgeConfigSpec.DoubleValue LEVEL_5_TRUE_DAMAGE_UPGRADE;
    public static final ForgeConfigSpec.DoubleValue LEVEL_5_EXTRA_DAMAGE_PERCENT;
    public static final ForgeConfigSpec.DoubleValue LEVEL_4_HEAL_PERCENT;
    public static final ForgeConfigSpec.DoubleValue LEVEL_5_HEAL_PERCENT;
    public static final ForgeConfigSpec.DoubleValue BASE_HEALTH_REDUCTION;
    public static final ForgeConfigSpec.DoubleValue HEALTH_UPGRADE_LOW;
    public static final ForgeConfigSpec.DoubleValue HEALTH_UPGRADE_HIGH;
    public static final ForgeConfigSpec.IntValue TRANSITION_TICKS;


    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        // ReZero Config
        builder.push("ReZero");
        OVERLAY_DURATION_SECONDS = builder
                .comment("Duration of the ReZero overlay effect in seconds")
                .defineInRange("overlay_duration_seconds", 5, 1, 60);
        COOLDOWN_REZERO_SECONDS = builder
                .comment("Cooldown duration for ReZero item in seconds")
                .defineInRange("cooldown_rezero_seconds", 10 * 60, 60, 3600);
        COOLDOWN_ETERNAL_SECONDS = builder
                .comment("Cooldown duration for ReZero Eternal item in seconds")
                .defineInRange("cooldown_eternal_seconds", 4 * 60, 60, 3600);
        MAX_USES_REZERO = builder
                .comment("Maximum number of uses for ReZero item")
                .defineInRange("max_uses_rezero", 4, 1, 100);
        MAX_HEALTH_AFTER_REZERO = builder
                .comment("Maximum health after ReZero activation (in half-hearts)")
                .defineInRange("max_health_after_rezero", 1.0, 0.5, 20.0);
        POSITION_HISTORY_SIZE = builder
                .comment("Number of position history entries to store (4 seconds at 20 ticks/s)")
                .defineInRange("position_history_size", 80, 20, 200);
        POSITION_SAVE_INTERVAL_TICKS = builder
                .comment("Interval for saving player position in ticks")
                .defineInRange("position_save_interval_ticks", 10, 1, 100);
        REZERO_TELEPORT_STEPS = builder
                .comment("small step = performance, high step = quality. Set to 1 for a single teleport.")
                .defineInRange("rezero_teleport_steps", 5, 1, 40);
        REZERO_TELEPORT_DURATION_SECONDS = builder
                .comment("The duration in seconds for the multi-step teleportation to complete.", "This should be less than or equal to overlay_duration_seconds for the best effect.")
                .defineInRange("rezero_teleport_duration_seconds", 2.0, 0.5, 60.0);
        builder.pop();

        // SwapTime Config
        builder.push("SwapTime");
        COUNTDOWN_TICKS = builder
                .comment("Countdown duration for SwapTime in ticks")
                .defineInRange("countdown_ticks", 200, 20, 1000);
        SWAP_DURATION = builder
                .comment("Duration of the swap effect in ticks")
                .defineInRange("swap_duration", 1200L, 200L, 7200L);
        AI_DURATION = builder
                .comment("Duration of AI effect in seconds")
                .defineInRange("ai_duration", 60, 1, 300);
        SWAP_TIME_OFFSET = builder
                .comment("Time offset for swap in ticks")
                .defineInRange("swap_time_offset", 40L, 20L, 200L);
        SLOW_DURATION = builder
                .comment("Duration of slow effect in seconds")
                .defineInRange("slow_duration", 60, 1, 300);
        SLOW_LEVEL = builder
                .comment("Level of slow effect")
                .defineInRange("slow_level", 1, 1, 5);
        WEAKNESS_LEVEL = builder
                .comment("Level of weakness effect")
                .defineInRange("weakness_level", 1, 1, 5);
        GENERATION_LEVEL = builder
                .comment("Level of generation effect")
                .defineInRange("generation_level", 10, 1, 20);
        GENERATION_DURATION = builder
                .comment("Duration of generation effect in seconds")
                .defineInRange("generation_duration", 100, 10, 600);
        MAX_DURABILITY = builder
                .comment("Maximum durability for SwapTime item")
                .defineInRange("max_durability", 100, 10, 1000);
        builder.pop();

        // TimeDisperser Config
        builder.push("TimeDisperser");
        COOLDOWN_TICKS = builder
                .comment("Cooldown duration for TimeDisperser in ticks")
                .defineInRange("cooldown_ticks", 12000L, 600L, 72000L);
        // NEW
        TIME_DISPERSER_KNOCKDOWN_SECONDS = builder
                .comment("Duration in seconds the player is knocked down before dying from Time Disperser's effect.")
                .defineInRange("knockdown_seconds", 10, 1, 120);
        // NEW
        TIME_DISPERSER_DEAGGRO_RADIUS = builder
                .comment("Radius in blocks around the player in which mobs will stop targeting them when knocked down.")
                .defineInRange("deaggro_radius", 32.0, 0.0, 128.0);
        builder.pop();

        //StarterPack
        builder.push("Starter pack");
        ENABLE_STARTER_ITEMS = builder
                .comment("Starter Pack")
                .define("On OFF", true);
        builder.pop();


        // Eye Of Chronos Config
        builder.push("EyeOfChronos");
        EFFECT_DURATION = builder
                .comment("Duration of Eye of Chronos effect in ticks")
                .defineInRange("effect_duration", 1200L, 200L, 7200L);
        DETECTION_RANGE = builder
                .comment("Detection range for Eye of Chronos in blocks")
                .defineInRange("detection_range", 4.0, 1.0, 20.0);
        PROJECTILE_HITBOX_SIZE = builder
                .comment("Hitbox size for projectiles in blocks")
                .defineInRange("projectile_hitbox_size", 4.0, 0.5, 10.0);
        MOB_HITBOX_SIZE = builder
                .comment("Hitbox size for mobs in blocks")
                .defineInRange("mob_hitbox_size", 1.75, 0.5, 5.0);
        ENDERMAN_HITBOX_SIZE = builder
                .comment("Hitbox size for Enderman in blocks")
                .defineInRange("enderman_hitbox_size", 1.75, 0.5, 5.0);
        SCAN_FREQUENCY_TICKS = builder
                .comment("Frequency of scanning in ticks")
                .defineInRange("scan_frequency_ticks", 2, 1, 20);
        TELEPORT_BOX_XZ = builder
                .comment("Teleport box size in X/Z directions in blocks")
                .defineInRange("teleport_box_xz", 5.0, 1.0, 20.0);
        TELEPORT_BOX_Y_UP = builder
                .comment("Teleport box size upward in Y direction in blocks")
                .defineInRange("teleport_box_y_up", 5.0, 1.0, 20.0);
        TELEPORT_BOX_Y_DOWN = builder
                .comment("Teleport box size downward in Y direction in blocks")
                .defineInRange("teleport_box_y_down", 2.0, 1.0, 20.0);
        FALLBACK_DISTANCE = builder
                .comment("Fallback distance for teleport in blocks")
                .defineInRange("fallback_distance", 5.0, 1.0, 20.0);
        builder.pop();

        // Cursed Heart Config
        builder.push("CursedHeart");
        LEVEL_1_HEALTH_MOB_KILLS = builder
                .comment("Mob kills required for level 1 health upgrade")
                .defineInRange("level_1_health_mob_kills", 10, 1, 100);
        LEVEL_2_HEALTH_MOB_KILLS = builder
                .comment("Mob kills required for level 2 health upgrade")
                .defineInRange("level_2_health_mob_kills", 15, 1, 100);
        LEVEL_3_HEALTH_MOB_KILLS = builder
                .comment("Mob kills required for level 3 health upgrade")
                .defineInRange("level_3_health_mob_kills", 20, 1, 100);
        LEVEL_4_HEALTH_MOB_KILLS = builder
                .comment("Mob kills required for level 4 health upgrade")
                .defineInRange("level_4_health_mob_kills", 25, 1, 100);
        LEVEL_5_HEALTH_MOB_KILLS = builder
                .comment("Mob kills required for level 5 health upgrade")
                .defineInRange("level_5_health_mob_kills", 30, 1, 100);
        LEVEL_1_DAMAGE_MOB_KILLS = builder
                .comment("Mob kills required for level 1 damage upgrade")
                .defineInRange("level_1_damage_mob_kills", 10, 1, 100);
        LEVEL_2_DAMAGE_MOB_KILLS = builder
                .comment("Mob kills required for level 2 damage upgrade")
                .defineInRange("level_2_damage_mob_kills", 20, 1, 100);
        LEVEL_3_DAMAGE_MOB_KILLS = builder
                .comment("Mob kills required for level 3 damage upgrade")
                .defineInRange("level_3_damage_mob_kills", 30, 1, 100);
        LEVEL_4_DAMAGE_MOB_KILLS = builder
                .comment("Mob kills required for level 4 damage upgrade")
                .defineInRange("level_4_damage_mob_kills", 40, 1, 100);
        LEVEL_1_TO_2_HEALTH_UPGRADES = builder
                .comment("Health upgrades required from level 1 to 2")
                .defineInRange("level_1_to_2_health_upgrades", 10, 1, 50);
        LEVEL_1_TO_2_DAMAGE_UPGRADES = builder
                .comment("Damage upgrades required from level 1 to 2")
                .defineInRange("level_1_to_2_damage_upgrades", 3, 1, 50);
        LEVEL_2_TO_3_HEALTH_UPGRADES = builder
                .comment("Health upgrades required from level 2 to 3")
                .defineInRange("level_2_to_3_health_upgrades", 15, 1, 50);
        LEVEL_2_TO_3_DAMAGE_UPGRADES = builder
                .comment("Damage upgrades required from level 2 to 3")
                .defineInRange("level_2_to_3_damage_upgrades", 5, 1, 50);
        LEVEL_3_TO_4_HEALTH_UPGRADES = builder
                .comment("Health upgrades required from level 3 to 4")
                .defineInRange("level_3_to_4_health_upgrades", 20, 1, 50);
        LEVEL_3_TO_4_DAMAGE_UPGRADES = builder
                .comment("Damage upgrades required from level 3 to 4")
                .defineInRange("level_3_to_4_damage_upgrades", 7, 1, 50);
        LEVEL_4_TO_5_HEALTH_UPGRADES = builder
                .comment("Health upgrades required from level 4 to 5")
                .defineInRange("level_4_to_5_health_upgrades", 25, 1, 50);
        LEVEL_4_TO_5_DAMAGE_UPGRADES = builder
                .comment("Damage upgrades required from level 4 to 5")
                .defineInRange("level_4_to_5_damage_upgrades", 10, 1, 50);
        BASE_TRUE_DAMAGE = builder
                .comment("Base true damage for Cursed Heart")
                .defineInRange("base_true_damage", 0.5, 0.0, 5.0);
        LEVEL_1_TRUE_DAMAGE = builder
                .comment("True damage for level 1")
                .defineInRange("level_1_true_damage", 0.25, 0.0, 5.0);
        LEVEL_2_TRUE_DAMAGE = builder
                .comment("True damage for level 2")
                .defineInRange("level_2_true_damage", 0.25, 0.0, 5.0);
        LEVEL_3_TRUE_DAMAGE = builder
                .comment("True damage for level 3")
                .defineInRange("level_3_true_damage", 0.5, 0.0, 5.0);
        LEVEL_4_TRUE_DAMAGE = builder
                .comment("True damage for level 4")
                .defineInRange("level_4_true_damage", 0.5, 0.0, 5.0);
        LEVEL_5_TRUE_DAMAGE = builder
                .comment("True damage for level 5")
                .defineInRange("level_5_true_damage", 0.75, 0.0, 5.0);
        LEVEL_5_TRUE_DAMAGE_UPGRADE = builder
                .comment("True damage upgrade for level 5")
                .defineInRange("level_5_true_damage_upgrade", 1.0, 0.0, 5.0);
        LEVEL_5_EXTRA_DAMAGE_PERCENT = builder
                .comment("Extra damage percent for level 5")
                .defineInRange("level_5_extra_damage_percent", 0.05, 0.0, 1.0);
        LEVEL_4_HEAL_PERCENT = builder
                .comment("Heal percent for level 4 (lifesteal)")
                .defineInRange("level_4_heal_percent", 0.1, 0.0, 1.0);
        LEVEL_5_HEAL_PERCENT = builder
                .comment("Heal percent for level 5 (lifesteal)")
                .defineInRange("level_5_heal_percent", 0.15, 0.0, 1.0);
        BASE_HEALTH_REDUCTION = builder
                .comment("Base health reduction for Cursed Heart")
                .defineInRange("base_health_reduction", -16.0, -20.0, 0.0);
        HEALTH_UPGRADE_LOW = builder
                .comment("Low-end health upgrade amount")
                .defineInRange("health_upgrade_low", 1.0, 0.0, 5.0);
        HEALTH_UPGRADE_HIGH = builder
                .comment("High-end health upgrade amount")
                .defineInRange("health_upgrade_high", 2.0, 0.0, 5.0);
        TRANSITION_TICKS = builder
                .comment("Transition duration in ticks")
                .defineInRange("transition_ticks", 20, 1, 100);
        builder.pop();

        SPEC = builder.build();
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(Type.COMMON, SPEC, "zeromod.toml");
    }

    // Getter methods to access config values
    public static double getRezeroTeleportDurationSeconds() { return REZERO_TELEPORT_DURATION_SECONDS.get(); }
    public static int getRezeroTeleportSteps() { return REZERO_TELEPORT_STEPS.get(); }
    public static int getTimeDisperserKnockdownSeconds() { return TIME_DISPERSER_KNOCKDOWN_SECONDS.get(); }
    public static double getTimeDisperserDeaggroRadius() { return TIME_DISPERSER_DEAGGRO_RADIUS.get(); }
    public static int getOverlayDurationSeconds() { return OVERLAY_DURATION_SECONDS.get(); }
    public static int getCooldownRezeroSeconds() { return COOLDOWN_REZERO_SECONDS.get(); }
    public static int getCooldownEternalSeconds() { return COOLDOWN_ETERNAL_SECONDS.get(); }
    public static int getMaxUsesRezero() { return MAX_USES_REZERO.get(); }
    public static float getMaxHealthAfterRezero() { return MAX_HEALTH_AFTER_REZERO.get().floatValue(); }
    public static int getPositionHistorySize() { return POSITION_HISTORY_SIZE.get(); }
    public static int getPositionSaveIntervalTicks() { return POSITION_SAVE_INTERVAL_TICKS.get(); }
    public static int getCountdownTicks() { return COUNTDOWN_TICKS.get(); }
    public static long getSwapDuration() { return SWAP_DURATION.get(); }
    public static int getAiDuration() { return AI_DURATION.get(); }
    public static long getSwapTimeOffset() { return SWAP_TIME_OFFSET.get(); }
    public static int getSlowDuration() { return SLOW_DURATION.get(); }
    public static int getSlowLevel() { return SLOW_LEVEL.get(); }
    public static int getWeaknessLevel() { return WEAKNESS_LEVEL.get(); }
    public static int getGenerationLevel() { return GENERATION_LEVEL.get(); }
    public static int getGenerationDuration() { return GENERATION_DURATION.get(); }
    public static int getMaxDurability() { return MAX_DURABILITY.get(); }
    public static long getCooldownTicks() { return COOLDOWN_TICKS.get(); }
    public static long getEffectDuration() { return EFFECT_DURATION.get(); }
    public static double getDetectionRange() { return DETECTION_RANGE.get(); }
    public static double getProjectileHitboxSize() { return PROJECTILE_HITBOX_SIZE.get(); }
    public static double getMobHitboxSize() { return MOB_HITBOX_SIZE.get(); }
    public static double getEndermanHitboxSize() { return ENDERMAN_HITBOX_SIZE.get(); }
    public static int getScanFrequencyTicks() { return SCAN_FREQUENCY_TICKS.get(); }
    public static double getTeleportBoxXz() { return TELEPORT_BOX_XZ.get(); }
    public static double getTeleportBoxYUp() { return TELEPORT_BOX_Y_UP.get(); }
    public static double getTeleportBoxYDown() { return TELEPORT_BOX_Y_DOWN.get(); }
    public static double getFallbackDistance() { return FALLBACK_DISTANCE.get(); }
    public static int getLevel1HealthMobKills() { return LEVEL_1_HEALTH_MOB_KILLS.get(); }
    public static int getLevel2HealthMobKills() { return LEVEL_2_HEALTH_MOB_KILLS.get(); }
    public static int getLevel3HealthMobKills() { return LEVEL_3_HEALTH_MOB_KILLS.get(); }
    public static int getLevel4HealthMobKills() { return LEVEL_4_HEALTH_MOB_KILLS.get(); }
    public static int getLevel5HealthMobKills() { return LEVEL_5_HEALTH_MOB_KILLS.get(); }
    public static int getLevel1DamageMobKills() { return LEVEL_1_DAMAGE_MOB_KILLS.get(); }
    public static int getLevel2DamageMobKills() { return LEVEL_2_DAMAGE_MOB_KILLS.get(); }
    public static int getLevel3DamageMobKills() { return LEVEL_3_DAMAGE_MOB_KILLS.get(); }
    public static int getLevel4DamageMobKills() { return LEVEL_4_DAMAGE_MOB_KILLS.get(); }
    public static int getLevel1To2HealthUpgrades() { return LEVEL_1_TO_2_HEALTH_UPGRADES.get(); }
    public static int getLevel1To2DamageUpgrades() { return LEVEL_1_TO_2_DAMAGE_UPGRADES.get(); }
    public static int getLevel2To3HealthUpgrades() { return LEVEL_2_TO_3_HEALTH_UPGRADES.get(); }
    public static int getLevel2To3DamageUpgrades() { return LEVEL_2_TO_3_DAMAGE_UPGRADES.get(); }
    public static int getLevel3To4HealthUpgrades() { return LEVEL_3_TO_4_HEALTH_UPGRADES.get(); }
    public static int getLevel3To4DamageUpgrades() { return LEVEL_3_TO_4_DAMAGE_UPGRADES.get(); }
    public static int getLevel4To5HealthUpgrades() { return LEVEL_4_TO_5_HEALTH_UPGRADES.get(); }
    public static int getLevel4To5DamageUpgrades() { return LEVEL_4_TO_5_DAMAGE_UPGRADES.get(); }
    public static float getBaseTrueDamage() { return BASE_TRUE_DAMAGE.get().floatValue(); }
    public static float getLevel1TrueDamage() { return LEVEL_1_TRUE_DAMAGE.get().floatValue(); }
    public static float getLevel2TrueDamage() { return LEVEL_2_TRUE_DAMAGE.get().floatValue(); }
    public static float getLevel3TrueDamage() { return LEVEL_3_TRUE_DAMAGE.get().floatValue(); }
    public static float getLevel4TrueDamage() { return LEVEL_4_TRUE_DAMAGE.get().floatValue(); }
    public static float getLevel5TrueDamage() { return LEVEL_5_TRUE_DAMAGE.get().floatValue(); }
    public static float getLevel5TrueDamageUpgrade() { return LEVEL_5_TRUE_DAMAGE_UPGRADE.get().floatValue(); }
    public static float getLevel5ExtraDamagePercent() { return LEVEL_5_EXTRA_DAMAGE_PERCENT.get().floatValue(); }
    public static float getLevel4HealPercent() { return LEVEL_4_HEAL_PERCENT.get().floatValue(); }
    public static float getLevel5HealPercent() { return LEVEL_5_HEAL_PERCENT.get().floatValue(); }
    public static double getBaseHealthReduction() { return BASE_HEALTH_REDUCTION.get(); }
    public static double getHealthUpgradeLow() { return HEALTH_UPGRADE_LOW.get(); }
    public static double getHealthUpgradeHigh() { return HEALTH_UPGRADE_HIGH.get(); }
    public static int getTransitionTicks() { return TRANSITION_TICKS.get(); }

}