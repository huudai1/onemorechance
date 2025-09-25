package net.com.zeromod.item;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.com.zeromod.config.ModConfig;
import net.com.zeromod.network.ModNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.com.zeromod.network.ClientboundPlayAnimationPacket;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Mod.EventBusSubscriber(modid = "zeromod")
public class SwapTimeItem extends Item {
    private static final Logger LOGGER = LogManager.getLogger("zeromod/SwapTimeItem");
    private static final Map<UUID, SwapData> pendingSwaps = new HashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String SWAP_DATA_DIR = "config/one_more_chance";
    private static final String SWAP_DATA_FILE = "swapdata.json";
    public static boolean isSwap = false;

    public SwapTimeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!player.level().isClientSide() && target instanceof Mob) {
            if (player.getPersistentData().getBoolean("zeromod:cursed_heart_used")) {
                player.sendSystemMessage(Component.literal("You cannot swap bodies while your heart is cursed."));
                return InteractionResult.FAIL;
            }

            isSwap = true;
            Mob mobTarget = (Mob) target;
            ServerLevel level = (ServerLevel) player.level();

            // Gửi packet tới client
            int durationTicks = (int) ModConfig.getSwapDuration();
            ModNetwork.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                    new ClientboundPlayAnimationPacket(ClientboundPlayAnimationPacket.AnimationType.DODGE, true, durationTicks)
            );

            // NoAI = true
            mobTarget.setNoAi(true);

            // Lưu giá trị MAX_HEALTH ban đầu
            AttributeInstance pAttr = player.getAttribute(Attributes.MAX_HEALTH);
            AttributeInstance mAttr = mobTarget.getAttribute(Attributes.MAX_HEALTH);
            double playerOriginalHealth = pAttr != null ? pAttr.getBaseValue() : 20.0;
            double mobOriginalHealth = mAttr != null ? mAttr.getBaseValue() : 20.0;

            //  pendingSwaps
            pendingSwaps.put(target.getUUID(), new SwapData(
                    player.getUUID(),
                    mobTarget.getUUID(),
                    level.getGameTime() + ModConfig.getSwapDuration(),
                    level.getGameTime() + ModConfig.getSwapTimeOffset(),
                    level.getGameTime() + ModConfig.getAiDuration(),
                    playerOriginalHealth,
                    mobOriginalHealth
            ));

            //  file JSON
            saveSwapData(level);

            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ModConfig.getSlowDuration(), ModConfig.getSlowLevel()));
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, ModConfig.getSlowDuration(), ModConfig.getWeaknessLevel()));
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, ModConfig.getGenerationDuration(), ModConfig.getGenerationLevel()));

            stack.shrink(1);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        ServerLevel level = event.getServer().overworld();
        long currentTime = level.getGameTime();

        pendingSwaps.entrySet().removeIf(entry -> {
            UUID mobUUID = entry.getKey();
            SwapData data = entry.getValue();

            Player player = level.getPlayerByUUID(data.playerUUID);
            Mob mob = (Mob) level.getEntity(data.mobUUID);


            if (mob == null || player == null || !mob.isAlive() || !player.isAlive()) {
                saveSwapData(level); // file JSON
                return true;
            }

            // AI_DURATION

            if (currentTime >= data.aiDisableTime && mob.isNoAi()) {

                mob.setNoAi(false);
                isSwap = false;
                LOGGER.debug("[SwapTimeItem] Re-enabled AI for mob {} and set isSwap to false", mob.getName().getString());
            }


            long ticksRemaining = data.aiEnableTime - currentTime;
            if (ticksRemaining > 0 && currentTime % 20 == 0) {
                int secondsRemaining = (int) (ticksRemaining / 20);
                player.displayClientMessage(Component.literal("Swap effect expires in " + secondsRemaining + " seconds"), true);
            }

            // MAX_HEALTH
            if (currentTime >= data.swapTime && !data.swapped) {
                AttributeInstance pAttr = player.getAttribute(Attributes.MAX_HEALTH);
                AttributeInstance mAttr = mob.getAttribute(Attributes.MAX_HEALTH);

                if (pAttr != null && mAttr != null) {
                    double pMax = pAttr.getBaseValue();
                    double mMax = mAttr.getBaseValue();

                    // MAX_HEALTH
                    pAttr.setBaseValue(mMax);
                    mAttr.setBaseValue(pMax);

                    player.setHealth(Math.min(player.getHealth(), (float) mMax));
                    mob.setHealth(Math.min(mob.getHealth(), (float) pMax));

                    player.displayClientMessage(Component.literal("Health attributes swapped with " + mob.getName().getString() + "!"), true);
                } else {
                    LOGGER.error("[SwapTimeItem] Failed to swap health: Player or mob attribute is null");
                }

                data.swapped = true;
                saveSwapData(level); // file JSON swapped
            }

            if (currentTime >= data.aiEnableTime) {
                isSwap = false;
                AttributeInstance pAttr = player.getAttribute(Attributes.MAX_HEALTH);
                AttributeInstance mAttr = mob.getAttribute(Attributes.MAX_HEALTH);

                if (pAttr != null && mAttr != null) {
                    pAttr.setBaseValue(data.playerOriginalHealth);
                    mAttr.setBaseValue(data.mobOriginalHealth);

                    player.setHealth(Math.min(player.getHealth(), (float) data.playerOriginalHealth));
                    mob.setHealth(Math.min(mob.getHealth(), (float) data.mobOriginalHealth));
                }

                ModNetwork.INSTANCE.send(
                        PacketDistributor.PLAYER.with(() -> (ServerPlayer) player),
                        new ClientboundPlayAnimationPacket(ClientboundPlayAnimationPacket.AnimationType.DODGE, false, 0)
                );
                saveSwapData(level);
                LOGGER.debug("[SwapTimeItem] Swap effect expired for player {}, isSwap set to false", player.getName().getString());
                return true;
            }

            return false;
        });
    }

    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level.isClientSide()) return;
        if (level.dimension() != Level.OVERWORLD) return; // overworld

        loadSwapData(level);
    }

    @SubscribeEvent
    public static void onWorldSave(LevelEvent.Save event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level.isClientSide()) return;
        if (level.dimension() != Level.OVERWORLD) return; // overworld

        saveSwapData(level);
    }

    private static void saveSwapData(ServerLevel level) {
        try {
            Path worldDir = level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT);
            Path modDir = worldDir.resolve(SWAP_DATA_DIR); // config/one_more_chance
            Path filePath = modDir.resolve(SWAP_DATA_FILE); // config/one_more_chance/swapdata.json

            Files.createDirectories(modDir);

            Map<String, SwapData> jsonFriendlyMap = new HashMap<>();
            pendingSwaps.forEach((uuid, data) -> jsonFriendlyMap.put(uuid.toString(), data));

            try (Writer writer = Files.newBufferedWriter(filePath)) {
                GSON.toJson(jsonFriendlyMap, writer);
            }
            LOGGER.debug("[SwapTimeItem] Saved swap data to {}", filePath);
        } catch (IOException e) {
            LOGGER.error("[SwapTimeItem] Failed to save swap data", e);
        }
    }

    private static void loadSwapData(ServerLevel level) {
        try {
            Path worldDir = level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT);
            Path modDir = worldDir.resolve(SWAP_DATA_DIR); // config/one_more_chance
            Path filePath = modDir.resolve(SWAP_DATA_FILE); // config/one_more_chance/swapdata.json

            if (Files.exists(filePath)) {
                try (Reader reader = Files.newBufferedReader(filePath)) {
                    Type mapType = new TypeToken<Map<String, SwapData>>() {}.getType();
                    Map<String, SwapData> jsonFriendlyMap = GSON.fromJson(reader, mapType);
                    pendingSwaps.clear();
                    if (jsonFriendlyMap != null) {
                        jsonFriendlyMap.forEach((uuidStr, data) -> pendingSwaps.put(UUID.fromString(uuidStr), data));
                    }
                    LOGGER.debug("[SwapTimeItem] Loaded swap data from {}", filePath);
                }
            } else {
                LOGGER.debug("[SwapTimeItem] No swap data file found at {}", filePath);
            }
        } catch (IOException e) {
            LOGGER.error("[SwapTimeItem] Failed to load swap data", e);
        }
    }

    private static class SwapData {
        UUID playerUUID;
        UUID mobUUID;
        long aiEnableTime;
        long swapTime;
        long aiDisableTime;
        boolean swapped;
        double playerOriginalHealth;
        double mobOriginalHealth;

        SwapData(UUID playerUUID, UUID mobUUID, long aiEnableTime, long swapTime, long aiDisableTime,
                 double playerOriginalHealth, double mobOriginalHealth) {
            this.playerUUID = playerUUID;
            this.mobUUID = mobUUID;
            this.aiEnableTime = aiEnableTime;
            this.swapTime = swapTime;
            this.aiDisableTime = aiDisableTime;
            this.swapped = false;
            this.playerOriginalHealth = playerOriginalHealth;
            this.mobOriginalHealth = mobOriginalHealth;
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.zeromod.swap_time.tooltip")
                .withStyle(style -> style.withColor(0x1E90FF)));
    }
}