package com.pulxes.advancedbotany.common.block.entity;

import com.pulxes.advancedbotany.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.block.WandBindable;
import vazkii.botania.api.block.Wandable;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.spark.SparkAttachable;
import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.botania.xplat.XplatAbstractions;

public class ManaChargerBlockEntity extends BlockEntity implements WorldlyContainer, WandBindable, Wandable {
    private static final String TAG_BINDING_X = "bindingX";
    private static final String TAG_BINDING_Y = "bindingY";
    private static final String TAG_BINDING_Z = "bindingZ";
    private static final String TAG_REQUEST_UPDATE = "requestUpdate";
    private static final int[] AUTOMATION_SLOTS = new int[] {1, 2, 3, 4};
    private static final int SYNC_INTERVAL = 15;

    private final NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
    private final LazyOptional<IItemHandler> unsidedHandler = LazyOptional.of(() -> new InvWrapper(this));
    private final LazyOptional<IItemHandlerModifiable>[] sidedHandlers = SidedInvWrapper.create(this, Direction.values());

    private BlockPos receiverPos;
    private boolean requestUpdate;
    private int ticks;

    public ManaChargerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MANA_CHARGER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ManaChargerBlockEntity charger) {
        charger.tick();
    }

    private void tick() {
        if (level == null) {
            return;
        }

        boolean needsSync = false;
        ManaReceiver receiver = getManaReceiver();
        SparkAttachable sparkReceiver = getSparkReceiver();
        BlockEntity receiverBlockEntity = getReceiverBlockEntity();
        if (receiver != null && sparkReceiver != null && receiverBlockEntity != null) {
            for (int slot = 0; slot < items.size(); slot++) {
                ItemStack stack = items.get(slot);
                ManaItem manaItem = getManaItem(stack);
                if (manaItem == null) {
                    continue;
                }

                if (slot == 0) {
                    if (manaItem.getMana() <= 0 || receiver.isFull() || !manaItem.canExportManaToPool(receiverBlockEntity)) {
                        continue;
                    }
                    int availableMana = sparkReceiver.getAvailableSpaceForMana();
                    int manaValue = Math.min(Math.min(manaItem.getMaxMana() / 256, AdvancedBotanyManaValues.MANA_CHARGER_SPEED) * 3,
                            Math.min(availableMana, manaItem.getMana()));
                    if (manaValue <= 0) {
                        continue;
                    }
                    if (!level.isClientSide()) {
                        manaItem.addMana(-manaValue);
                        setChanged();
                        needsSync = needsSync || level.getGameTime() % SYNC_INTERVAL == 0L || manaItem.getMana() <= 0;
                    }
                    receiver.receiveMana(manaValue);
                    continue;
                }

                if (receiver.getCurrentMana() <= 0 || manaItem.getMana() >= manaItem.getMaxMana() || !manaItem.canReceiveManaFromPool(receiverBlockEntity)) {
                    continue;
                }
                int manaValue = Math.min(Math.min(manaItem.getMaxMana() / 256, AdvancedBotanyManaValues.MANA_CHARGER_SPEED),
                        Math.min(receiver.getCurrentMana(), manaItem.getMaxMana() - manaItem.getMana()));
                if (manaValue <= 0) {
                    continue;
                }
                if (!level.isClientSide()) {
                    manaItem.addMana(manaValue);
                        setChanged();
                    needsSync = needsSync || level.getGameTime() % SYNC_INTERVAL == 0L || manaItem.getMana() >= manaItem.getMaxMana();
                }
                receiver.receiveMana(-manaValue);
            }
        }

        if (!level.isClientSide() && (requestUpdate || needsSync)) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
            requestUpdate = false;
        }
        ticks++;
    }

    @Nullable
    private BlockEntity getReceiverBlockEntity() {
        if (level == null || receiverPos == null) {
            return null;
        }
        BlockEntity blockEntity = level.getBlockEntity(receiverPos);
        if (blockEntity == null) {
            receiverPos = null;
            setChanged();
        }
        return blockEntity;
    }

    @Nullable
    private ManaReceiver getManaReceiver() {
        BlockEntity blockEntity = getReceiverBlockEntity();
        if (blockEntity == null || level == null || receiverPos == null) {
            return null;
        }
        return XplatAbstractions.INSTANCE.findManaReceiver(level, receiverPos, blockEntity.getBlockState(), blockEntity, null);
    }

    @Nullable
    private SparkAttachable getSparkReceiver() {
        BlockEntity blockEntity = getReceiverBlockEntity();
        if (blockEntity == null || level == null || receiverPos == null) {
            return null;
        }
        return XplatAbstractions.INSTANCE.findSparkAttachable(level, receiverPos, blockEntity.getBlockState(), blockEntity, null);
    }

    @Nullable
    private static ManaItem getManaItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        return stack.getCapability(BotaniaForgeCapabilities.MANA_ITEM).orElse(null);
    }

    public void requestUpdate() {
        requestUpdate = true;
        setChanged();
    }

    @Override
    public boolean canSelect(Player player, ItemStack wand, BlockPos pos, Direction side) {
        return true;
    }

    @Override
    public boolean bindTo(Player player, ItemStack wand, BlockPos pos, Direction side) {
        if (level == null) {
            return false;
        }
        boolean far = Math.abs(worldPosition.getX() - pos.getX()) >= 10
                || Math.abs(worldPosition.getY() - pos.getY()) >= 10
                || Math.abs(worldPosition.getZ() - pos.getZ()) >= 10;
        if (far) {
            return false;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) {
            return false;
        }
        ManaReceiver manaReceiver = XplatAbstractions.INSTANCE.findManaReceiver(level, pos, blockEntity.getBlockState(), blockEntity, null);
        SparkAttachable sparkAttachable = XplatAbstractions.INSTANCE.findSparkAttachable(level, pos, blockEntity.getBlockState(), blockEntity, null);
        if (manaReceiver == null || sparkAttachable == null || !manaReceiver.canReceiveManaFromBursts()) {
            return false;
        }

        if (!level.isClientSide()) {
            receiverPos = pos.immutable();
            requestUpdate();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
        return true;
    }

    @Override
    public BlockPos getBinding() {
        return receiverPos;
    }

    @Override
    public boolean onUsedByWand(Player player, ItemStack stack, Direction side) {
        if (level != null && player != null) {
            level.playSound(player, worldPosition, BotaniaSounds.ding, SoundSource.BLOCKS, 0.11F, 1.0F);
        }
        return true;
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack item : items) {
            if (!item.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(items, slot, amount);
        if (!removed.isEmpty()) {
            requestUpdate();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (!stack.isEmpty() && stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        requestUpdate();
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null
                && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return getManaItem(stack) != null;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < items.size(); i++) {
            items.set(i, ItemStack.EMPTY);
        }
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return AUTOMATION_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        if (side != Direction.UP || slot == 0) {
            return false;
        }
        ManaItem manaItem = getManaItem(stack);
        BlockEntity receiverBlockEntity = getReceiverBlockEntity();
        ManaReceiver receiver = getManaReceiver();
        return manaItem != null
                && receiver != null
                && receiverBlockEntity != null
                && manaItem.getMana() < manaItem.getMaxMana()
                && manaItem.canReceiveManaFromPool(receiverBlockEntity);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        if (side != Direction.DOWN || slot == 0) {
            return false;
        }
        ManaItem manaItem = getManaItem(stack);
        return manaItem != null && manaItem.getMana() >= manaItem.getMaxMana();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        if (receiverPos != null) {
            tag.putInt(TAG_BINDING_X, receiverPos.getX());
            tag.putInt(TAG_BINDING_Y, receiverPos.getY());
            tag.putInt(TAG_BINDING_Z, receiverPos.getZ());
        }
        tag.putBoolean(TAG_REQUEST_UPDATE, requestUpdate);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        clearContent();
        ContainerHelper.loadAllItems(tag, items);
        receiverPos = tag.contains(TAG_BINDING_Y)
                ? new BlockPos(tag.getInt(TAG_BINDING_X), tag.getInt(TAG_BINDING_Y), tag.getInt(TAG_BINDING_Z))
                : null;
        requestUpdate = tag.getBoolean(TAG_REQUEST_UPDATE);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        ContainerHelper.saveAllItems(tag, items);
        if (receiverPos != null) {
            tag.putInt(TAG_BINDING_X, receiverPos.getX());
            tag.putInt(TAG_BINDING_Y, receiverPos.getY());
            tag.putInt(TAG_BINDING_Z, receiverPos.getZ());
        }
        tag.putBoolean(TAG_REQUEST_UPDATE, requestUpdate);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        unsidedHandler.invalidate();
        for (LazyOptional<IItemHandlerModifiable> sidedHandler : sidedHandlers) {
            sidedHandler.invalidate();
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            if (side == null) {
                return unsidedHandler.cast();
            }
            return sidedHandlers[side.ordinal()].cast();
        }
        return super.getCapability(capability, side);
    }
}
