package com.pulxes.advancedbotany.common.block;

import com.mojang.serialization.MapCodec;
import com.pulxes.advancedbotany.common.block.entity.BoardFateBlockEntity;
import com.pulxes.advancedbotany.common.block.entity.GameBoardBlockEntity;
import com.pulxes.advancedbotany.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.common.handler.BotaniaSounds;

public class PlayingBoardBlock extends BaseEntityBlock {
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }

    private static final VoxelShape SHAPE = box(0.0D, 0.0D, 0.0D, 16.0D, 3.0D, 16.0D);

    private final boolean fate;

    public PlayingBoardBlock(boolean fate) {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(3.0F, 10.0F)
                .sound(SoundType.STONE)
                .noOcclusion());
        this.fate = fate;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack heldItem, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        return toItemResult(use(state, level, pos, player, heldItem));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return use(state, level, pos, player, ItemStack.EMPTY);
    }

    private InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, ItemStack heldItem) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!fate && blockEntity instanceof GameBoardBlockEntity board) {
            if (player.isShiftKeyDown() && !board.hasGame()) {
                board.isSingleGame = !board.isSingleGame;
                if (!level.isClientSide()) {
                    level.playSound(null, pos, BotaniaSounds.ding, SoundSource.BLOCKS, 0.11F, 0.8F);
                    level.sendBlockUpdated(pos, state, state, 3);
                    board.setChanged();
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
            if (!board.hasGame()) {
                if (!level.isClientSide()) {
                    board.setPlayer(player);
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
            if (!board.isSingleGame && board.playersName[1].isEmpty() && !board.playersName[0].equals(player.getName().getString())) {
                if (!level.isClientSide()) {
                    board.setPlayer(player);
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
            return board.dropDice(player) ? InteractionResult.sidedSuccess(level.isClientSide()) : InteractionResult.PASS;
        }

        if (fate && blockEntity instanceof BoardFateBlockEntity board) {
            if (player.isShiftKeyDown()) {
                return board.spawnRelic(player) ? InteractionResult.sidedSuccess(level.isClientSide()) : InteractionResult.PASS;
            }
            return board.insertDice(player, heldItem) ? InteractionResult.sidedSuccess(level.isClientSide()) : InteractionResult.PASS;
        }

        return InteractionResult.PASS;
    }

    private static ItemInteractionResult toItemResult(InteractionResult result) {
        return result == InteractionResult.PASS ? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION : ItemInteractionResult.sidedSuccess(false);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof BoardFateBlockEntity board) {
            Containers.dropContents(level, pos, board);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return fate ? new BoardFateBlockEntity(pos, state) : new GameBoardBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (fate) {
            return level.isClientSide()
                    ? createTickerHelper(type, ModBlockEntities.FATE_PLAYING_BOARD.get(), BoardFateBlockEntity::clientTick)
                    : createTickerHelper(type, ModBlockEntities.FATE_PLAYING_BOARD.get(), BoardFateBlockEntity::serverTick);
        }
        return level.isClientSide()
                ? createTickerHelper(type, ModBlockEntities.PLAYING_BOARD.get(), GameBoardBlockEntity::clientTick)
                : createTickerHelper(type, ModBlockEntities.PLAYING_BOARD.get(), GameBoardBlockEntity::serverTick);
    }
}
