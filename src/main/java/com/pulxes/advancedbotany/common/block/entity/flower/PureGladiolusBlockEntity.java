package com.pulxes.advancedbotany.common.block.entity.flower;

import com.pulxes.advancedbotany.registry.ModFlowers;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import vazkii.botania.api.block_entity.FunctionalFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;
import vazkii.botania.common.block.BotaniaBlocks;

public class PureGladiolusBlockEntity extends FunctionalFlowerBlockEntity {
    public static final int MANA_REQUIRED = 1000;
    public static final int MAX_MANA = 10000;
    public static final int COLOR = 0xFF00FF;
    public static final int COOLDOWN_TIME = 180;
    private static final String TAG_COOLDOWN = "Cooldown";
    private static final int RANGE = 1;

    private int cooldown = COOLDOWN_TIME;

    public PureGladiolusBlockEntity(BlockPos pos, BlockState state) {
        this(ModFlowers.PURE_GLADIOLUS_BLOCK_ENTITY.get(), pos, state);
    }

    public PureGladiolusBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tickFlower() {
        super.tickFlower();
        Level level = getLevel();
        if (level == null || !level.getBlockState(getBlockPos().above()).isAir()) {
            return;
        }

        List<Player> players = level.getEntitiesOfClass(
                Player.class,
                new AABB(getEffectivePos()).inflate(1.9D, 1.2D, 1.9D));
        if (players.isEmpty()) {
            return;
        }

        if (cooldown > 0) {
            int decrement = level.getBlockState(getBlockPos().below()).is(BotaniaBlocks.enchantedSoil) ? 2 : 1;
            cooldown = Math.max(0, cooldown - decrement);
        }
        // Original 1.7.10 aspect orb integration was removed with the unsupported dependency.
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
