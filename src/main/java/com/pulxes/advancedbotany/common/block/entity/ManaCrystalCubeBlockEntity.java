package com.pulxes.advancedbotany.common.block.entity;

import com.pulxes.advancedbotany.registry.ModBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import vazkii.botania.api.block.Wandable;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.spark.ManaSpark;
import vazkii.botania.api.mana.spark.SparkAttachable;
import vazkii.botania.api.mana.spark.SparkHelper;

public class ManaCrystalCubeBlockEntity extends BlockEntity implements Wandable {
    private static final String TAG_KNOWN_MANA = "knownMana";
    private static final String TAG_KNOWN_MAX_MANA = "knownMaxMana";

    private int knownMana = -1;
    private int knownMaxMana = -1;

    public ManaCrystalCubeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MANA_CRYSTAL_CUBE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ManaCrystalCubeBlockEntity cube) {
        // The original cube is passive; values are refreshed on right click/wand interaction.
    }

    public void updateKnownMana() {
        int[] mana = getManaAround();
        knownMana = mana[0];
        knownMaxMana = mana[1];
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public int[] getManaAround() {
        int current = 0;
        int max = 0;
        if (level == null) {
            return new int[] {current, max};
        }

        double x = worldPosition.getX();
        double y = worldPosition.getY();
        double z = worldPosition.getZ();
        int range = SparkHelper.SPARK_SCAN_RANGE;
        AABB bounds = new AABB(x - range, y - range, z - range, x + range, y + range, z + range);
        List<Entity> sparks = level.getEntitiesOfClass(Entity.class, bounds, entity -> entity instanceof ManaSpark);
        for (Entity entity : sparks) {
            ManaSpark spark = (ManaSpark) entity;
            SparkAttachable attachable = spark.getAttachedTile();
            ManaReceiver receiver = spark.getAttachedManaReceiver();
            if (attachable == null || receiver == null) {
                continue;
            }
            int receiverMana = receiver.getCurrentMana();
            current += receiverMana;
            max += receiverMana + attachable.getAvailableSpaceForMana();
        }
        return new int[] {current, max};
    }

    @Override
    public boolean onUsedByWand(net.minecraft.world.entity.player.Player player, net.minecraft.world.item.ItemStack stack, net.minecraft.core.Direction side) {
        if (level != null && !level.isClientSide()) {
            updateKnownMana();
        }
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(TAG_KNOWN_MANA, knownMana);
        tag.putInt(TAG_KNOWN_MAX_MANA, knownMaxMana);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        knownMana = tag.contains(TAG_KNOWN_MANA) ? tag.getInt(TAG_KNOWN_MANA) : -1;
        knownMaxMana = tag.contains(TAG_KNOWN_MAX_MANA) ? tag.getInt(TAG_KNOWN_MAX_MANA) : -1;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putInt(TAG_KNOWN_MANA, knownMana);
        tag.putInt(TAG_KNOWN_MAX_MANA, knownMaxMana);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
