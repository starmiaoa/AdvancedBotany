package com.pulxes.advancedbotany.common.block;

import com.mojang.serialization.MapCodec;
import com.pulxes.advancedbotany.common.block.entity.LebethronNaturalCoreBlockEntity;
import com.pulxes.advancedbotany.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class LebethronNaturalCoreBlock extends BaseEntityBlock {
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }

    public LebethronNaturalCoreBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(6.0F)
                .sound(SoundType.WOOD));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        return toItemResult(use(state, level, pos, player, stack));
    }

    private InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, ItemStack heldItem) {
        if (!(level.getBlockEntity(pos) instanceof LebethronNaturalCoreBlockEntity core)) {
            return InteractionResult.PASS;
        }
        if (!(heldItem.getItem() instanceof BlockItem blockItem)) {
            return InteractionResult.PASS;
        }
        BlockState leafState = blockItem.getBlock().defaultBlockState();
        if (!leafState.is(BlockTags.LEAVES)) {
            return InteractionResult.PASS;
        }
        // The original refuses any data-bearing leaf stack outright instead of consuming it and losing its data.
        if (!heldItem.getComponentsPatch().isEmpty()) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            core.updateStructure();
            if (core.getValidTree() && core.setLeafBlock(player, leafState)) {
                heldItem.shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static ItemInteractionResult toItemResult(InteractionResult result) {
        return result == InteractionResult.PASS ? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION : ItemInteractionResult.sidedSuccess(false);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof LebethronNaturalCoreBlockEntity core && !level.isClientSide()) {
            Block leaf = core.getLeafBlock();
            if (leaf != null) {
                ItemEntity item = new ItemEntity(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, new ItemStack(leaf));
                level.addFreshEntity(item);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LebethronNaturalCoreBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide()
                ? createTickerHelper(type, ModBlockEntities.LEBETHRON_NATURAL_CORE.get(), LebethronNaturalCoreBlockEntity::clientTick)
                : createTickerHelper(type, ModBlockEntities.LEBETHRON_NATURAL_CORE.get(), LebethronNaturalCoreBlockEntity::serverTick);
    }
}
