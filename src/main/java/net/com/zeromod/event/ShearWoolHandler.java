package net.com.zeromod.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ShearWoolHandler {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        BlockPos pos = event.getPos();
        Level level = event.getLevel();
        BlockState state = level.getBlockState(pos);


        if ((state.is(Blocks.WHITE_WOOL) || state.is(Blocks.BLACK_WOOL) || state.is(Blocks.BLUE_WOOL) || state.is(Blocks.LIGHT_GRAY_WOOL) || state.is(Blocks.GRAY_WOOL)
                || state.is(Blocks.BROWN_WOOL) || state.is(Blocks.RED_WOOL) || state.is(Blocks.ORANGE_WOOL) || state.is(Blocks.YELLOW_WOOL)
                || state.is(Blocks.LIME_WOOL) || state.is(Blocks.GREEN_WOOL) || state.is(Blocks.CYAN_WOOL) || state.is(Blocks.LIGHT_BLUE_WOOL)
                || state.is(Blocks.PURPLE_WOOL) || state.is(Blocks.MAGENTA_WOOL) || state.is(Blocks.PINK_WOOL))
                && player.getItemInHand(InteractionHand.MAIN_HAND).getItem() == Items.SHEARS) {

            // server
            if (!level.isClientSide) {
                ServerLevel serverLevel = (ServerLevel) level;

                // Xóa block và spawn 6 sợi chỉ
                serverLevel.removeBlock(pos, false);
                serverLevel.addFreshEntity(new ItemEntity(serverLevel, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(Items.STRING, 4)));

                // sound
                level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, player.getSoundSource(), 1.0F, 1.0F);
            }


            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }
}
