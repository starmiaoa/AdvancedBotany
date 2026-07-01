package com.pulxes.advancedbotany.common.block.entity.flower;

import com.pulxes.advancedbotany.registry.ModFlowers;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import vazkii.botania.api.block_entity.FunctionalFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;

public class AncientAlphirineBlockEntity extends FunctionalFlowerBlockEntity {
    public static final int MANA_REQUIRED = 4500;
    public static final int MAX_MANA = 180000;
    public static final int COLOR = 13680472;
    private static final int RANGE = 1;

    public AncientAlphirineBlockEntity(BlockPos pos, BlockState state) {
        this(ModFlowers.ANCIENT_ALPHIRINE_BLOCK_ENTITY.get(), pos, state);
    }

    public AncientAlphirineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tickFlower() {
        super.tickFlower();
        Level level = getLevel();
        if (level == null || ticksExisted % 10 != 0 || getMana() < MANA_REQUIRED) {
            return;
        }

        List<ItemEntity> nearbyItems = level.getEntitiesOfClass(
                ItemEntity.class,
                new AABB(getEffectivePos()).inflate(1.0D, 0.0D, 1.0D),
                item -> !item.isRemoved() && !item.getItem().isEmpty());

        if (!nearbyItems.isEmpty()) {
            // TODO Batch 8: consume matching RecipeAncientAlphirine inputs and spawn EntityAlphirinePortal.
        }
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
