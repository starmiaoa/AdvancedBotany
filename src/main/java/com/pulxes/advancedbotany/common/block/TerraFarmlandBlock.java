package com.pulxes.advancedbotany.common.block;

import com.pulxes.advancedbotany.api.AdvancedBotanyAPI;
import com.pulxes.advancedbotany.api.TerraFarmlandList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.IPlantable;

public class TerraFarmlandBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 15.0D, 16.0D);

    public TerraFarmlandBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.DIRT)
                .strength(0.6F)
                .sound(SoundType.GRAVEL)
                .randomTicks()
                .noOcclusion());
    }

    @Override
    public boolean canSustainPlant(BlockState state, BlockGetter level, BlockPos pos, Direction facing, IPlantable plantable) {
        Block plant = plantable.getPlant(level, pos.above()).getBlock();
        return plant != Blocks.NETHER_WART && plant != Blocks.SUGAR_CANE;
    }

    @Override
    public boolean isFertile(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos plantPos = pos.above();
        BlockState plantState = level.getBlockState(plantPos);
        Block plantBlock = plantState.getBlock();

        if (plantBlock instanceof CropBlock cropBlock) {
            if (cropBlock.isMaxAge(plantState)) {
                refreshSeed(level, pos, plantPos, plantState);
            }
            return;
        }

        if (plantBlock instanceof IPlantable) {
            for (TerraFarmlandList seed : AdvancedBotanyAPI.farmlandList) {
                if (plantState.is(seed.getBlock()) && plantState.equals(seed.getBlockState())) {
                    refreshSeed(level, pos, plantPos, plantState);
                    return;
                }
            }
            return;
        }

        level.setBlock(pos, Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
    }

    private void refreshSeed(ServerLevel level, BlockPos farmlandPos, BlockPos plantPos, BlockState plantState) {
        AABB bounds = new AABB(
                farmlandPos.getX() - 4.0D,
                farmlandPos.getY() - 4.0D,
                farmlandPos.getZ() - 4.0D,
                farmlandPos.getX() + 4.0D,
                farmlandPos.getY() + 4.0D,
                farmlandPos.getZ() + 4.0D);
        if (level.getEntitiesOfClass(ItemEntity.class, bounds).size() > 7) {
            return;
        }

        Block plantBlock = plantState.getBlock();
        Item originalSeedItem = plantBlock.asItem();
        List<ItemStack> drops = Block.getDrops(plantState, level, plantPos, null);
        for (ItemStack stack : drops) {
            if (stack.isEmpty()) {
                continue;
            }
            if (isSeedDrop(stack, plantBlock, originalSeedItem)) {
                if (stack.getCount() > 1) {
                    stack.shrink(1);
                }
                continue;
            }
            stack.setCount(Math.min(64, (int) (stack.getCount() * 2.5F)));
        }

        for (ItemStack stack : drops) {
            if (!stack.isEmpty()) {
                ItemEntity item = new ItemEntity(
                        level,
                        farmlandPos.getX() + 0.5D,
                        farmlandPos.getY() + 1.0D,
                        farmlandPos.getZ() + 0.5D,
                        stack);
                level.addFreshEntity(item);
            }
        }

        level.setBlock(plantPos, getResetState(plantBlock), Block.UPDATE_ALL);
    }

    private static BlockState getResetState(Block plantBlock) {
        if (plantBlock instanceof CropBlock cropBlock) {
            return cropBlock.getStateForAge(0);
        }
        return plantBlock.defaultBlockState();
    }

    private static boolean isSeedDrop(ItemStack stack, Block plantBlock, Item originalSeedItem) {
        Item item = stack.getItem();
        if (item == originalSeedItem) {
            return true;
        }
        return item instanceof BlockItem blockItem && blockItem.getBlock() == plantBlock;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.world.level.pathfinder.PathComputationType type) {
        return false;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(Blocks.DIRT);
    }
}
