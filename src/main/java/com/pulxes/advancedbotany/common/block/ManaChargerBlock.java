package com.pulxes.advancedbotany.common.block;

import com.mojang.serialization.MapCodec;
import com.pulxes.advancedbotany.common.block.entity.ManaChargerBlockEntity;
import com.pulxes.advancedbotany.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.ManaItem;

public class ManaChargerBlock extends BaseEntityBlock {
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }

    private static final VoxelShape SHAPE = box(3.0D, 3.0D, 3.0D, 13.0D, 13.0D, 13.0D);

    public ManaChargerBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(6.0F)
                .sound(SoundType.STONE)
                .noOcclusion());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hit) {
        return toItemResult(use(state, level, pos, player, held, hit));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return use(state, level, pos, player, ItemStack.EMPTY, hit);
    }

    private InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, ItemStack held, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof ManaChargerBlockEntity charger)) {
            return InteractionResult.PASS;
        }

        int slot = hit.getDirection().ordinal() - 1;
        if (slot < 0 || slot >= charger.getContainerSize()) {
            return InteractionResult.PASS;
        }

        ItemStack stackInSlot = charger.getItem(slot);
        if (player.isShiftKeyDown()) {
            if (stackInSlot.isEmpty()) {
                return InteractionResult.PASS;
            }
            if (!level.isClientSide()) {
                ItemStack removed = charger.removeItemNoUpdate(slot);
                Vec3 look = player.getLookAngle();
                ItemEntity item = new ItemEntity(level, player.getX() + look.x, player.getY() + 1.2D, player.getZ() + look.z, removed);
                level.addFreshEntity(item);
                charger.requestUpdate();
                level.updateNeighbourForOutputSignal(pos, this);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        if (!held.isEmpty() && held.getCount() == 1 && stackInSlot.isEmpty()
                && held.getCapability(BotaniaForgeCapabilities.getItemApiLookupById(ManaItem.LOOKUP)) != null) {
            if (!level.isClientSide()) {
                ItemStack inserted = held.copy();
                inserted.setCount(1);
                charger.setItem(slot, inserted);
                held.shrink(1);
                charger.requestUpdate();
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
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof ManaChargerBlockEntity charger) {
            Containers.dropContents(level, pos, charger);
            level.updateNeighbourForOutputSignal(pos, this);
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
        return new ManaChargerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.MANA_CHARGER.get(), ManaChargerBlockEntity::tick);
    }
}
