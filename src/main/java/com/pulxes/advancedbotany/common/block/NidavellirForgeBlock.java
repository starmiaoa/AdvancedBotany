package com.pulxes.advancedbotany.common.block;

import com.mojang.serialization.MapCodec;
import com.pulxes.advancedbotany.common.block.entity.NidavellirForgeBlockEntity;
import com.pulxes.advancedbotany.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class NidavellirForgeBlock extends BaseEntityBlock {
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape NORTH_SHAPE = box(3.0D, 0.0D, 0.0D, 13.0D, 12.0D, 15.0D);
    private static final VoxelShape SOUTH_SHAPE = box(3.0D, 0.0D, 1.0D, 13.0D, 12.0D, 16.0D);
    private static final VoxelShape WEST_SHAPE = box(0.0D, 0.0D, 3.0D, 15.0D, 12.0D, 13.0D);
    private static final VoxelShape EAST_SHAPE = box(1.0D, 0.0D, 3.0D, 16.0D, 12.0D, 13.0D);

    public NidavellirForgeBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.0F, 10.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SOUTH_SHAPE;
            case WEST -> WEST_SHAPE;
            case EAST -> EAST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        return toItemResult(use(state, level, pos, player));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return use(state, level, pos, player);
    }

    private InteractionResult use(BlockState state, Level level, BlockPos pos, Player player) {
        if (!(level.getBlockEntity(pos) instanceof NidavellirForgeBlockEntity forge)) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                if (!forge.getItem(NidavellirForgeBlockEntity.OUTPUT_SLOT).isEmpty()) {
                    dropItemWithDirection(level, player, forge.getItem(NidavellirForgeBlockEntity.OUTPUT_SLOT).copy());
                    forge.setItem(NidavellirForgeBlockEntity.OUTPUT_SLOT, ItemStack.EMPTY);
                    forge.requestUpdate = true;
                    return InteractionResult.SUCCESS;
                }
                for (int i = forge.getContainerSize() - 1; i >= NidavellirForgeBlockEntity.FIRST_INPUT_SLOT; i--) {
                    ItemStack stack = forge.getItem(i);
                    if (!stack.isEmpty()) {
                        dropItemWithDirection(level, player, stack.copy());
                        forge.setItem(i, ItemStack.EMPTY);
                        forge.requestUpdate = true;
                        return InteractionResult.SUCCESS;
                    }
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu( forge, buffer -> buffer.writeBlockPos(pos));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static ItemInteractionResult toItemResult(InteractionResult result) {
        return result == InteractionResult.PASS ? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION : ItemInteractionResult.sidedSuccess(false);
    }

    private static void dropItemWithDirection(Level level, Player player, ItemStack stack) {
        Vec3 look = player.getLookAngle();
        ItemEntity item = new ItemEntity(level, player.getX() + look.x, player.getY() + 1.2D, player.getZ() + look.z, stack);
        level.addFreshEntity(item);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof NidavellirForgeBlockEntity forge) {
            Containers.dropContents(level, pos, forge);
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
        return new NidavellirForgeBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide()
                ? createTickerHelper(type, ModBlockEntities.NIDAVELLIR_FORGE.get(), NidavellirForgeBlockEntity::clientTick)
                : createTickerHelper(type, ModBlockEntities.NIDAVELLIR_FORGE.get(), NidavellirForgeBlockEntity::serverTick);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
