package com.pulxes.advancedbotany.common.item;

import com.pulxes.advancedbotany.common.item.equipment.ItemBauble;
import com.pulxes.advancedbotany.registry.ModBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;

public class SphereOfAttractionItem extends ItemBauble {
    private static final String TAG_ACTIVE = "isActive";
    private static final int RANGE = 8;

    public SphereOfAttractionItem(Properties properties) {
        super(properties.setNoRepair(), "belt");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            setActive(stack, !isActive(stack));
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    protected void onWornTick(ItemStack stack, LivingEntity entity) {
        if (!(entity instanceof Player player) || !isActive(stack)) {
            return;
        }

        Level level = player.level();
        AABB bounds = new AABB(player.getX(), player.getY(), player.getZ(), player.getX() + 1.0D, player.getY() + 1.0D, player.getZ() + 1.0D)
                .inflate(RANGE);
        List<FallingBlockEntity> fallingBlocks = level.getEntitiesOfClass(FallingBlockEntity.class, bounds);
        if (level.isClientSide()) {
            return;
        }

        for (FallingBlockEntity fallingBlock : fallingBlocks) {
            BlockState fallingState = fallingBlock.getBlockState();
            if (fallingState.isAir()) {
                continue;
            }

            int x = Mth.floor(fallingBlock.getX());
            int y = Mth.floor(fallingBlock.getY());
            int z = Mth.floor(fallingBlock.getZ());
            BlockPos blockPos = new BlockPos(x, y, z);
            BlockPos supportPos = blockPos.below();
            if (blockPos.getY() >= level.getMaxBuildHeight()
                    || !setBlock(level, supportPos, ModBlocks.ANTIGRAVITATION.get().defaultBlockState(), player, true)) {
                continue;
            }

            if (setBlock(level, blockPos, fallingState, player, true)) {
                fallingBlock.discard();
            }
        }
    }

    public static boolean isActive(ItemStack stack) {
        return ItemComponentData.getBoolean(stack, TAG_ACTIVE, true);
    }

    private static void setActive(ItemStack stack, boolean active) {
        ItemComponentData.putBoolean(stack, TAG_ACTIVE, active);
    }

    private static boolean setBlock(Level level, BlockPos pos, BlockState state, Player player, boolean checkAir) {
        if (checkAir && !level.isEmptyBlock(pos)) {
            return false;
        }
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, pos, level.getBlockState(pos), player);
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
            return false;
        }
        return level.setBlockAndUpdate(pos, state);
    }
}
