package com.pulxes.advancedbotany.common.block.entity.flower;

import com.pulxes.advancedbotany.registry.ModFlowers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import vazkii.botania.api.block_entity.FunctionalFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;

public class PureGladiolusBlockEntity extends FunctionalFlowerBlockEntity {
    public static final int MANA_REQUIRED = 1000;
    public static final int MAX_MANA = 10000;
    public static final int COLOR = 0xFF00FF;
    public static final int COOLDOWN_TIME = 180;
    private static final int RANGE = 1;

    public PureGladiolusBlockEntity(BlockPos pos, BlockState state) {
        this(ModFlowers.PURE_GLADIOLUS_BLOCK_ENTITY.get(), pos, state);
    }

    public PureGladiolusBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tickFlower() {
        super.tickFlower();
        // 原版仅 Thaumcraft 存在,本版停用。
    }

    @Override
    public RadiusDescriptor getRadius() {
        return RadiusDescriptor.Rectangle.square(getEffectivePos(), RANGE);
    }

    @Override
    public int getMaxMana() {
        return MAX_MANA;
    }

    @Override
    public int getColor() {
        return COLOR;
    }
}
