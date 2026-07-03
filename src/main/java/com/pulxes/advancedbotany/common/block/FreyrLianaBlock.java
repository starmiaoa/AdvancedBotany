package com.pulxes.advancedbotany.common.block;

import com.pulxes.advancedbotany.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FreyrLianaBlock extends BushBlock {
    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    public FreyrLianaBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .noCollission()
                .instabreak()
                .sound(SoundType.GRASS)
                .randomTicks()
                .noOcclusion());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos abovePos = pos.above();
        BlockState above = level.getBlockState(abovePos);
        return isOriginalSupportBlock(above);
    }

    private boolean isOriginalSupportBlock(BlockState above) {
        return above.getBlock() instanceof FreyrLianaBlock
                || above.is(BlockTags.LEAVES)
                || above.is(BlockTags.LOGS)
                || above.is(BlockTags.PLANKS)
                || above.is(BlockTags.DIRT)
                || above.is(BlockTags.SAND)
                || above.is(BlockTags.BASE_STONE_OVERWORLD)
                || above.is(BlockTags.BASE_STONE_NETHER)
                || above.is(Blocks.FARMLAND)
                || above.is(Blocks.DIRT_PATH)
                || above.is(Blocks.GRAVEL)
                || above.is(Blocks.CLAY)
                || above.is(ModBlocks.GLIMMERING_LEBETHRON_WOOD.get());
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
            return;
        }

        level.updateNeighborsAt(pos, this);
        for (int offset = 1; offset < 5; offset++) {
            BlockPos targetPos = pos.below(offset);
            BlockState targetState = level.getBlockState(targetPos);
            Block targetBlock = targetState.getBlock();
            if (!(targetBlock instanceof BonemealableBlock bonemealable)) {
                continue;
            }

            applyFertilizer(level, targetPos, targetState, bonemealable, random);
            BlockPos farmlandPos = targetPos.below();
            BlockState farmlandState = level.getBlockState(farmlandPos);
            if (farmlandState.is(ModBlocks.TERRA_FARMLAND.get())) {
                farmlandState.randomTick(level, farmlandPos, random);
            }
            break;
        }
    }

    private static void applyFertilizer(ServerLevel level, BlockPos pos, BlockState state, BonemealableBlock bonemealable, RandomSource random) {
        if (state.getBlock() instanceof SaplingBlock || level.getBlockEntity(pos) != null) {
            return;
        }
        if (!bonemealable.isValidBonemealTarget(level, pos, state, false) || !bonemealable.isBonemealSuccess(level, random, pos, state)) {
            return;
        }
        for (int i = 0; i < 18; i++) {
            if (!level.getBlockState(pos).equals(state)) {
                return;
            }
            bonemealable.performBonemeal(level, random, pos, state);
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return 100;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return 60;
    }

    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return true;
    }
}
