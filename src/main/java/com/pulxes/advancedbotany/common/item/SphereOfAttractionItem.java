package com.pulxes.advancedbotany.common.item;

import com.pulxes.advancedbotany.registry.ModBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;

public class SphereOfAttractionItem extends Item {
    private static final String TAG_ACTIVE = "isActive";
    private static final int RANGE = 8;

    public SphereOfAttractionItem(Properties properties) {
        super(properties.stacksTo(1).setNoRepair());
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
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!(entity instanceof Player player) || !isActive(stack)) {
            return;
        }

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
        CompoundTag tag = stack.getTag();
        return tag == null || !tag.contains(TAG_ACTIVE) || tag.getBoolean(TAG_ACTIVE);
    }

    private static void setActive(ItemStack stack, boolean active) {
        stack.getOrCreateTag().putBoolean(TAG_ACTIVE, active);
    }

    private static boolean setBlock(Level level, BlockPos pos, BlockState state, Player player, boolean checkAir) {
        if (checkAir && !level.isEmptyBlock(pos)) {
            return false;
        }
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, pos, level.getBlockState(pos), player);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            return false;
        }
        return level.setBlockAndUpdate(pos, state);
    }
}
