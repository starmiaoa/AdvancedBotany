package com.pulxes.advancedbotany.common.block;

import com.mojang.serialization.MapCodec;
import com.pulxes.advancedbotany.common.block.entity.AdvancedBotanyManaValues;
import com.pulxes.advancedbotany.common.block.entity.ManaContainerBlockEntity;
import com.pulxes.advancedbotany.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class ManaContainerBlock extends BaseEntityBlock {
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }

    private static final VoxelShape SHAPE = box(1.28D, 0.64D, 1.28D, 14.72D, 15.36D, 14.72D);

    private final Variant variant;

    public ManaContainerBlock(Variant variant) {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
                .strength(9.0F)
                .sound(SoundType.STONE)
                .noOcclusion());
        this.variant = variant;
    }

    public Variant getVariant() {
        return variant;
    }

    public int getManaCapacity() {
        return variant.capacity;
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
        return new ManaContainerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return createTickerHelper(type, ModBlockEntities.MANA_CONTAINER.get(), ManaContainerBlockEntity::clientTick);
        }
        return createTickerHelper(type, ModBlockEntities.MANA_CONTAINER.get(), ManaContainerBlockEntity::serverTick);
    }

    public enum Variant {
        NORMAL(AdvancedBotanyManaValues.MANA_CONTAINER_CAPACITY),
        DILUTED(AdvancedBotanyManaValues.DILUTED_MANA_CONTAINER_CAPACITY),
        FABULOUS(AdvancedBotanyManaValues.FABULOUS_MANA_CONTAINER_CAPACITY);

        private final int capacity;

        Variant(int capacity) {
            this.capacity = capacity;
        }
    }
}
