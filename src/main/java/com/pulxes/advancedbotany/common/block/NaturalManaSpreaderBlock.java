package com.pulxes.advancedbotany.common.block;

import com.mojang.serialization.MapCodec;
import com.pulxes.advancedbotany.common.block.entity.NaturalManaSpreaderBlockEntity;
import com.pulxes.advancedbotany.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.mana.LensEffectItem;
import vazkii.botania.common.item.WandOfTheForestItem;

public class NaturalManaSpreaderBlock extends BaseEntityBlock {
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }

    private static final VoxelShape SHAPE = box(1.0D, 1.0D, 1.0D, 15.0D, 15.0D, 15.0D);

    public NaturalManaSpreaderBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(7.0F)
                .sound(SoundType.WOOD)
                .lightLevel(state -> 12)
                .noOcclusion());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!(level.getBlockEntity(pos) instanceof NaturalManaSpreaderBlockEntity spreader) || placer == null) {
            return;
        }

        Direction direction = Direction.orderedByNearest(placer)[0];
        switch (direction) {
            case DOWN -> spreader.setRotationY(-90.0F);
            case UP -> spreader.setRotationY(90.0F);
            case NORTH -> spreader.setRotationX(270.0F);
            case SOUTH -> spreader.setRotationX(90.0F);
            case EAST -> spreader.setRotationX(180.0F);
            case WEST -> spreader.setRotationX(0.0F);
        }
        spreader.commitRedirection();
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        return toItemResult(use(state, level, pos, player, held));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return use(state, level, pos, player, ItemStack.EMPTY);
    }

    private InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, ItemStack held) {
        if (held.getItem() instanceof WandOfTheForestItem) {
            return InteractionResult.PASS;
        }
        if (!(level.getBlockEntity(pos) instanceof NaturalManaSpreaderBlockEntity spreader)) {
            return InteractionResult.PASS;
        }

        ItemStack lens = spreader.getLens();
        boolean heldLens = !held.isEmpty() && held.getItem() instanceof LensEffectItem;
        if (lens.isEmpty() && heldLens) {
            if (!level.isClientSide()) {
                ItemStack copy = held.copy();
                copy.setCount(1);
                spreader.setLens(copy);
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        if (!lens.isEmpty()) {
            if (!level.isClientSide()) {
                ItemStack removed = spreader.removeLens();
                if (!player.getInventory().add(removed)) {
                    player.drop(removed, false);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return InteractionResult.PASS;
    }

    private static ItemInteractionResult toItemResult(InteractionResult result) {
        return result == InteractionResult.PASS ? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION : ItemInteractionResult.sidedSuccess(false);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof NaturalManaSpreaderBlockEntity spreader) {
            Containers.dropContents(level, pos, spreader);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NaturalManaSpreaderBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.NATURAL_MANA_SPREADER.get(), NaturalManaSpreaderBlockEntity::tick);
    }
}
