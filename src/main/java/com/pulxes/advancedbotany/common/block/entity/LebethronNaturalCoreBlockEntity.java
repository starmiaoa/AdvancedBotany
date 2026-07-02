package com.pulxes.advancedbotany.common.block.entity;

import com.pulxes.advancedbotany.registry.ModBlockEntities;
import com.pulxes.advancedbotany.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class LebethronNaturalCoreBlockEntity extends BlockEntity {
    private static final String TAG_BLOCK = "block";
    private static final String TAG_TICK = "tick";
    private static final String TAG_VALID_TREE = "validTree";

    private int tick;
    private BlockState leafState;
    private boolean validTree;

    public LebethronNaturalCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LEBETHRON_NATURAL_CORE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, LebethronNaturalCoreBlockEntity core) {
        if (core.tick <= 0) {
            core.updateStructure();
            if (core.validTree && core.getLeafState() != null) {
                core.spawnLeaves();
                core.tick = 40;
            }
        } else {
            core.tick--;
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, LebethronNaturalCoreBlockEntity core) {
        // TODO Batch 7/client pass: restore Botania sparkleFX around a valid natural core.
    }

    public boolean getValidTree() {
        return validTree;
    }

    public void updateStructure() {
        boolean oldValidTree = validTree;
        validTree = hasValidTree();
        if (oldValidTree != validTree) {
            sync();
        }
    }

    public boolean setLeafBlock(Player player, BlockState state) {
        if (leafState == null || leafState.isAir()) {
            leafState = state;
            sync();
            return true;
        }
        if (leafState.equals(state)) {
            return false;
        }
        if (level != null && !level.isClientSide()) {
            Vec3 look = player.getLookAngle();
            ItemEntity entity = new ItemEntity(level, player.getX() + look.x, player.getY() + 1.2D, player.getZ() + look.z, new ItemStack(leafState.getBlock()));
            level.addFreshEntity(entity);
        }
        leafState = state;
        sync();
        return true;
    }

    @Nullable
    public Block getLeafBlock() {
        return leafState != null && !leafState.isAir() ? leafState.getBlock() : null;
    }

    @Nullable
    public BlockState getLeafState() {
        return leafState != null && !leafState.isAir() ? leafState : null;
    }

    public boolean hasValidTree() {
        if (level == null) {
            return false;
        }
        if (!checkLebethronWood(worldPosition.below())) {
            return false;
        }
        for (int i = 1; i <= 4; i++) {
            if (!checkLebethronWood(worldPosition.above(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean checkLebethronWood(BlockPos pos) {
        return level != null && level.getBlockState(pos).is(ModBlocks.LEBETHRON_WOOD.get());
    }

    private void spawnLeaves() {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = 0; y < 5; y++) {
                    tryPlaceLeaf(worldPosition.offset(x, y + 2, z));
                }
            }
        }
        tryPlaceLeaf(worldPosition.offset(1, 1, 0));
        tryPlaceLeaf(worldPosition.offset(-1, 1, 0));
        tryPlaceLeaf(worldPosition.offset(0, 1, 1));
        tryPlaceLeaf(worldPosition.offset(0, 1, -1));
        tryPlaceLeaf(worldPosition.offset(1, 7, 0));
        tryPlaceLeaf(worldPosition.offset(-1, 7, 0));
        tryPlaceLeaf(worldPosition.offset(0, 7, 1));
        tryPlaceLeaf(worldPosition.offset(0, 7, -1));
        for (int i = 0; i <= 3; i++) {
            tryPlaceLeaf(worldPosition.offset(0, 6 + i, 0));
        }
        tryPlaceLeaf(worldPosition.offset(0, 2, -2));
        for (int i = -1; i <= 1; i++) {
            tryPlaceLeaf(worldPosition.offset(i, 3, -2));
            tryPlaceLeaf(worldPosition.offset(i, 4, -2));
        }
        tryPlaceLeaf(worldPosition.offset(0, 5, -2));
        tryPlaceLeaf(worldPosition.offset(0, 2, 2));
        for (int i = -1; i <= 1; i++) {
            tryPlaceLeaf(worldPosition.offset(i, 3, 2));
            tryPlaceLeaf(worldPosition.offset(i, 4, 2));
        }
        tryPlaceLeaf(worldPosition.offset(0, 5, 2));
        tryPlaceLeaf(worldPosition.offset(2, 2, 0));
        for (int i = -1; i <= 1; i++) {
            tryPlaceLeaf(worldPosition.offset(2, 3, i));
            tryPlaceLeaf(worldPosition.offset(2, 4, i));
        }
        tryPlaceLeaf(worldPosition.offset(2, 5, 0));
        tryPlaceLeaf(worldPosition.offset(-2, 2, 0));
        for (int i = -1; i <= 1; i++) {
            tryPlaceLeaf(worldPosition.offset(-2, 3, i));
            tryPlaceLeaf(worldPosition.offset(-2, 4, i));
        }
        tryPlaceLeaf(worldPosition.offset(-2, 5, 0));
    }

    private void tryPlaceLeaf(BlockPos pos) {
        if (level == null || leafState == null || level.random.nextInt(10) <= 8) {
            return;
        }
        if (pos.getY() < level.getMaxBuildHeight() && level.getBlockState(pos).isAir()) {
            level.setBlock(pos, leafState, 3);
        }
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (leafState != null) {
            tag.put(TAG_BLOCK, NbtUtils.writeBlockState(leafState));
        }
        tag.putInt(TAG_TICK, tick);
        tag.putBoolean(TAG_VALID_TREE, validTree);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_BLOCK)) {
            leafState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound(TAG_BLOCK));
        }
        tick = tag.getInt(TAG_TICK);
        validTree = tag.getBoolean(TAG_VALID_TREE);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }
}
