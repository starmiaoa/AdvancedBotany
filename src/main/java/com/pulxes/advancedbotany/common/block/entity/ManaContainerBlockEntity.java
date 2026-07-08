package com.pulxes.advancedbotany.common.block.entity;

import com.pulxes.advancedbotany.common.block.ManaContainerBlock;
import com.pulxes.advancedbotany.registry.ModBlockEntities;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import vazkii.botania.api.block.Wandable;
import vazkii.botania.api.mana.ManaPool;
import vazkii.botania.api.mana.spark.ManaSpark;
import vazkii.botania.api.mana.spark.SparkAttachable;

public class ManaContainerBlockEntity extends BlockEntity implements ManaPool, SparkAttachable, Wandable {
    private static final String TAG_MANA = "mana";
    private static final String TAG_COLOR = "color";
    private static final int SYNC_INTERVAL = 10;

    private int mana;
    private int ticks;
    private boolean syncPending;
    private boolean addedToNetwork;
    private Optional<DyeColor> color = Optional.empty();

    public ManaContainerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MANA_CONTAINER.get(), pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ManaContainerBlockEntity container) {
        // TODO Batch 7/client pass: restore the original floating vessel renderer and wisp particles.
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ManaContainerBlockEntity container) {
        container.ensureInManaNetwork();
        if (container.syncPending && container.ticks % SYNC_INTERVAL == 0) {
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
            container.syncPending = false;
        }
        container.ticks++;
    }

    private void ensureInManaNetwork() {
        // Botania 1.21 removed pool tracking from the mana network (ManaBlockType only keeps
        // COLLECTOR, and Botania's own pools no longer fire the event). Registering this container
        // as a COLLECTOR would make generating flora treat it like a spreader, so register nothing:
        // sparks and bursts find it through the SparkAttachable / ManaReceiver lookups instead.
        addedToNetwork = true;
    }

    @Override
    public void setRemoved() {
        addedToNetwork = false;
        super.setRemoved();
    }

    @Override
    public Level getManaReceiverLevel() {
        return getLevel();
    }

    @Override
    public BlockPos getManaReceiverPos() {
        return getBlockPos();
    }

    @Override
    public int getCurrentMana() {
        return Mth.clamp(mana, 0, getMaxMana());
    }

    @Override
    public boolean isFull() {
        return getCurrentMana() >= getMaxMana();
    }

    @Override
    public void receiveMana(int amount) {
        int oldMana = mana;
        mana = Mth.clamp(getCurrentMana() + amount, 0, getMaxMana());
        if (oldMana != mana) {
            setChanged();
            syncPending = true;
        }
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return true;
    }

    @Override
    public boolean isOutputtingPower() {
        return false;
    }

    @Override
    public int getMaxMana() {
        if (getBlockState().getBlock() instanceof ManaContainerBlock containerBlock) {
            return containerBlock.getManaCapacity();
        }
        return AdvancedBotanyManaValues.MANA_CONTAINER_CAPACITY;
    }

    public Optional<DyeColor> getColor() {
        return color;
    }

    public void setColor(Optional<DyeColor> color) {
        this.color = color;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public boolean canAttachSpark(ItemStack stack) {
        return true;
    }

    @Override
    public int getAvailableSpaceForMana() {
        return Math.max(0, getMaxMana() - getCurrentMana());
    }

    public ManaSpark getAttachedSpark() {
        if (level == null) {
            return null;
        }
        AABB bounds = new AABB(worldPosition.above());
        List<Entity> sparks = level.getEntitiesOfClass(Entity.class, bounds, entity -> entity instanceof ManaSpark);
        return sparks.size() == 1 ? (ManaSpark) sparks.get(0) : null;
    }

    @Override
    public boolean areIncomingTransfersDone() {
        return false;
    }

    @Override
    public boolean onUsedByWand(net.minecraft.world.entity.player.Player player, ItemStack stack, net.minecraft.core.Direction side) {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
            syncPending = false;
        }
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt(TAG_MANA, getCurrentMana());
        color.ifPresent(dyeColor -> tag.putInt(TAG_COLOR, dyeColor.getId()));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        mana = Mth.clamp(tag.getInt(TAG_MANA), 0, getMaxMana());
        color = tag.contains(TAG_COLOR) ? Optional.of(DyeColor.byId(tag.getInt(TAG_COLOR))) : Optional.empty();
        addedToNetwork = false;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putInt(TAG_MANA, getCurrentMana());
        color.ifPresent(dyeColor -> tag.putInt(TAG_COLOR, dyeColor.getId()));
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
