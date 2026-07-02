package com.pulxes.advancedbotany.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import vazkii.botania.api.BotaniaAPI;

public class LuminousFreyrLianaBlock extends FreyrLianaBlock {
    public static final BooleanProperty HAS_NUGGETS = BooleanProperty.create("has_nuggets");

    public LuminousFreyrLianaBlock() {
        registerDefaultState(defaultBlockState().setValue(HAS_NUGGETS, false));
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return 11;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(state, level, pos, random);
        if (level.getBlockState(pos).is(this) && random.nextInt(11) == 0) {
            level.setBlock(pos, level.getBlockState(pos).setValue(HAS_NUGGETS, true), Block.UPDATE_ALL);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(HAS_NUGGETS) && random.nextInt(3) == 0) {
            BotaniaAPI.instance().sparkleFX(
                    level,
                    pos.getX() + 0.1D + random.nextDouble() * 0.8D,
                    pos.getY() + random.nextDouble() * 0.5D,
                    pos.getZ() + 0.1D + random.nextDouble() * 0.8D,
                    0.9764706F,
                    0.9019608F,
                    0.011764706F,
                    0.14F,
                    20);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        return use(state, level, pos);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return use(state, level, pos).result();
    }

    private ItemInteractionResult use(BlockState state, Level level, BlockPos pos) {
        if (!state.getValue(HAS_NUGGETS)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!level.isClientSide) {
            ItemStack nuggets = new ItemStack(Items.GOLD_NUGGET, 1 + level.random.nextInt(3));
            ItemEntity item = new ItemEntity(level, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, nuggets);
            level.addFreshEntity(item);
            level.setBlock(pos, state.setValue(HAS_NUGGETS, false), Block.UPDATE_ALL);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HAS_NUGGETS);
    }
}
