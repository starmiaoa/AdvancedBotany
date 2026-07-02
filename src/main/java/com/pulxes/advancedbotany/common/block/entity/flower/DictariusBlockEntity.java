package com.pulxes.advancedbotany.common.block.entity.flower;

import com.pulxes.advancedbotany.registry.ModFlowers;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import vazkii.botania.api.block_entity.GeneratingFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;

public class DictariusBlockEntity extends GeneratingFlowerBlockEntity {
    public static final int WORK_MANA_PLAYER = 480;
    public static final int WORK_MANA_VILLAGER = 80;
    public static final int COOLDOWN_TIME = 200;
    public static final int MAX_MANA = 8000;
    public static final int COLOR = 13815218;
    public static final int MAX_DICTARIUS_COUNT = 64;
    private static final String TAG_COOLDOWN = "cooldown";
    private static final int RANGE = 2;
    private static final int NEAR_FLOWER_CHECK_RANGE = 4;

    private int cooldown;

    public DictariusBlockEntity(BlockPos pos, BlockState state) {
        this(ModFlowers.DICTARIUS_BLOCK_ENTITY.get(), pos, state);
    }

    public DictariusBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tickFlower() {
        super.tickFlower();
        Level level = getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }

        if (level.getGameTime() % 1200L == 0L) {
            checkNearDictarius();
        }

        if (getMana() < getMaxMana() && cooldown == 0) {
            AABB bounds = new AABB(getEffectivePos()).inflate(2.0D, 1.0D, 2.0D);
            List<LivingEntity> livingEntities = level.getEntitiesOfClass(LivingEntity.class, bounds);
            int workMana = 0;
            int villagers = 0;

            for (int i = 0; i < Math.min(livingEntities.size(), 16); i++) {
                LivingEntity living = livingEntities.get(i);
                if (living instanceof Player) {
                    workMana += WORK_MANA_PLAYER;
                } else if (living instanceof Villager) {
                    workMana += WORK_MANA_VILLAGER;
                    if (villagers > 15 && level.random.nextInt(100) <= 4) {
                        living.discard();
                    }
                    villagers++;
                }
            }

            if (workMana > 0) {
                cooldown = COOLDOWN_TIME;
                int manaGain = (int) (workMana * level.random.nextDouble());
                if (manaGain > 0) {
                    addMana(Math.min(manaGain, getMaxMana() - getMana()));
                    syncFlower();
                }
            }
        }

        if (cooldown > 0) {
            cooldown--;
        }
    }

    public void checkNearDictarius() {
        Level level = getLevel();
        if (level == null) {
            return;
        }

        int foundFlowers = 0;
        BlockPos center = getEffectivePos();
        for (int z = -NEAR_FLOWER_CHECK_RANGE; z < NEAR_FLOWER_CHECK_RANGE; z++) {
            for (int x = -NEAR_FLOWER_CHECK_RANGE; x < NEAR_FLOWER_CHECK_RANGE; x++) {
                for (int y = -NEAR_FLOWER_CHECK_RANGE; y < NEAR_FLOWER_CHECK_RANGE; y++) {
                    BlockEntity blockEntity = level.getBlockEntity(center.offset(x, y, z));
                    if (!(blockEntity instanceof DictariusBlockEntity)) {
                        continue;
                    }
                    if (foundFlowers >= MAX_DICTARIUS_COUNT) {
                        level.levelEvent(2001, getBlockPos(), Block.getId(getBlockState()));
                        BlockPos below = getBlockPos().below();
                        if (level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
                            level.setBlock(getBlockPos(), Blocks.DEAD_BUSH.defaultBlockState(), Block.UPDATE_ALL);
                        } else {
                            level.removeBlock(getBlockPos(), false);
                        }
                        return;
                    }
                    foundFlowers++;
                }
            }
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

    public void writeToPacketNBT(CompoundTag tag) {
        tag.putInt(TAG_COOLDOWN, cooldown);
    }

    public void readFromPacketNBT(CompoundTag tag) {
        cooldown = tag.getInt(TAG_COOLDOWN);
    }
    private void syncFlower() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), net.minecraft.world.level.block.Block.UPDATE_ALL);
        }
    }

}
