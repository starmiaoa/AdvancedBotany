package com.pulxes.advancedbotany.common.block.entity.flower;

import com.pulxes.advancedbotany.registry.ModFlowers;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import vazkii.botania.api.block_entity.FunctionalFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;

public class AspecolusBlockEntity extends FunctionalFlowerBlockEntity {
    public static final int MANA_REQUIRED = 1250;
    public static final int MAX_MANA = 15000;
    public static final int COLOR = 9052380;
    public static final int COOLDOWN_PER_PLAYER = 75;
    private static final String TAG_COOLDOWN = "cooldown";
    private static final int RANGE = 1;

    private int cooldown;

    public AspecolusBlockEntity(BlockPos pos, BlockState state) {
        this(ModFlowers.ASPECOLUS_BLOCK_ENTITY.get(), pos, state);
    }

    public AspecolusBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tickFlower() {
        super.tickFlower();
        Level level = getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }
        if (cooldown > 0) {
            cooldown--;
        }
        // Original 1.7.10 aspect research integration was removed with the unsupported dependency.
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

    @Override
    public void writeToPacketNBT(CompoundTag tag) {
        super.writeToPacketNBT(tag);
        tag.putInt(TAG_COOLDOWN, cooldown);
    }

    @Override
    public void readFromPacketNBT(CompoundTag tag) {
        super.readFromPacketNBT(tag);
        cooldown = tag.getInt(TAG_COOLDOWN);
    }
}
