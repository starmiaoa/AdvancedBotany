package com.pulxes.advancedbotany.common.block.entity;

import com.pulxes.advancedbotany.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
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
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
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
    private final IItemHandler unsidedHandler = new InvWrapper(this);
    private final IItemHandlerModifiable[] sidedHandlers = createSidedHandlers();

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
                    if (manaItem.getMana() <= 0 || receiver.isFull() || !manaItem.canDrainManaToPool(receiverBlockEntity)) {
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
        return XplatAbstractions.INSTANCE.findBlockApi(ManaReceiver.LOOKUP, level, receiverPos, blockEntity.getBlockState(), blockEntity, null);
    }

    @Nullable
    private SparkAttachable getSparkReceiver() {
        BlockEntity blockEntity = getReceiverBlockEntity();
        if (blockEntity == null || level == null || receiverPos == null) {
            return null;
        }
        return XplatAbstractions.INSTANCE.findBlockApi(SparkAttachable.LOOKUP, level, receiverPos, blockEntity.getBlockState(), blockEntity);
    }

    @Nullable
    private static ManaItem getManaItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        return stack.getCapability(BotaniaForgeCapabilities.getItemApiLookupById(ManaItem.LOOKUP));
    }

    public void requestUpdate() {
        requestUpdate = true;
        setChanged();
    }

    public boolean canSelect(Player player, ItemStack wand, BlockPos pos, Direction side) {
        return true;
    }

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
        ManaReceiver manaReceiver = XplatAbstractions.INSTANCE.findBlockApi(ManaReceiver.LOOKUP, level, pos, blockEntity.getBlockState(), blockEntity, null);
        SparkAttachable sparkAttachable = XplatAbstractions.INSTANCE.findBlockApi(SparkAttachable.LOOKUP, level, pos, blockEntity.getBlockState(), blockEntity);
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
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        if (receiverPos != null) {
            tag.putInt(TAG_BINDING_X, receiverPos.getX());
            tag.putInt(TAG_BINDING_Y, receiverPos.getY());
            tag.putInt(TAG_BINDING_Z, receiverPos.getZ());
        }
        tag.putBoolean(TAG_REQUEST_UPDATE, requestUpdate);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        clearContent();
        ContainerHelper.loadAllItems(tag, items, registries);
        receiverPos = tag.contains(TAG_BINDING_Y)
                ? new BlockPos(tag.getInt(TAG_BINDING_X), tag.getInt(TAG_BINDING_Y), tag.getInt(TAG_BINDING_Z))
                : null;
        requestUpdate = tag.getBoolean(TAG_REQUEST_UPDATE);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        ContainerHelper.saveAllItems(tag, items, registries);
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

    public IItemHandler getItemHandler(@Nullable Direction side) {
        return side == null ? unsidedHandler : sidedHandlers[side.ordinal()];
    }

    private IItemHandlerModifiable[] createSidedHandlers() {
        Direction[] directions = Direction.values();
        IItemHandlerModifiable[] handlers = new IItemHandlerModifiable[directions.length];
        for (Direction direction : directions) {
            handlers[direction.ordinal()] = new SidedInvWrapper(this, direction);
        }
        return handlers;
    }
}
