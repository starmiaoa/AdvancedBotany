package com.pulxes.advancedbotany.common.block.entity;

import com.pulxes.advancedbotany.registry.ModBlockEntities;
import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.block.Bound;
import vazkii.botania.api.block.WandBindable;
import vazkii.botania.api.block.Wandable;
import vazkii.botania.common.handler.BotaniaSounds;

public class EngineerHopperBlockEntity extends BaseInventoryBlockEntity implements WandBindable, Wandable {
    private static final String TAG_COOLDOWN = "cooldown";
    private static final String TAG_BIND_TYPE = "bindType";
    private static final String TAG_BINDING_X = "bindingX";
    private static final String TAG_BINDING_Y = "bindingY";
    private static final String TAG_BINDING_Z = "bindingZ";
    private static final String TAG_BINDING_SIDE = "bindingSide";

    private int cooldown;
    private boolean bindType;
    private int redstoneSignal;
    private int[] invPosX = new int[] {0, 0};
    private int[] invPosY = new int[] {-1, -1};
    private int[] invPosZ = new int[] {0, 0};
    private int[] invSide = new int[] {-1, -1};

    public EngineerHopperBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENGINEER_HOPPER.get(), pos, state, 1);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EngineerHopperBlockEntity hopper) {
        if (!level.isClientSide()) {
            hopper.serverTick();
        }
    }

    private void serverTick() {
        if (level == null) {
            return;
        }

        redstoneSignal = level.getBestNeighborSignal(worldPosition);
        if (redstoneSignal > 0) {
            return;
        }

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        boolean moved = false;
        ItemStack stack = getItem(0);
        if (!stack.isEmpty()) {
            moved = tryExport();
        }
        if (getItem(0).isEmpty() || getItem(0).getCount() < getItem(0).getMaxStackSize()) {
            moved = tryImport() || moved;
        }

        if (moved) {
            cooldown = 8;
            sync();
        }
    }

    private boolean tryExport() {
        ItemStack stack = getItem(0);
        if (stack.isEmpty()) {
            return false;
        }

        IItemHandler target = getDistantItemHandler(0);
        if (target == null) {
            return false;
        }

        ItemStack remainder = ItemHandlerHelper.insertItem(target, stack.copy(), false);
        int moved = stack.getCount() - remainder.getCount();
        if (moved <= 0) {
            return false;
        }

        ItemStack newStack = stack.copy();
        newStack.shrink(moved);
        items.set(0, newStack.isEmpty() ? ItemStack.EMPTY : newStack);
        return true;
    }

    private boolean tryImport() {
        IItemHandler source = getDistantItemHandler(1);
        if (source == null) {
            return false;
        }

        ItemStack filter = getItem(0);
        int space = filter.isEmpty() ? getMaxStackSize() : Math.min(getMaxStackSize(), filter.getMaxStackSize()) - filter.getCount();
        if (space <= 0) {
            return false;
        }

        for (int slot = 0; slot < source.getSlots(); slot++) {
            ItemStack sourceStack = source.getStackInSlot(slot);
            if (sourceStack.isEmpty()) {
                continue;
            }
            if (!filter.isEmpty() && !ItemStack.isSameItemSameTags(filter, sourceStack)) {
                continue;
            }

            int amount = Math.min(space, sourceStack.getMaxStackSize());
            ItemStack simulated = source.extractItem(slot, amount, true);
            if (simulated.isEmpty()) {
                continue;
            }

            ItemStack extracted = source.extractItem(slot, Math.min(space, simulated.getCount()), false);
            if (extracted.isEmpty()) {
                continue;
            }

            if (filter.isEmpty()) {
                items.set(0, extracted);
            } else {
                filter.grow(extracted.getCount());
            }
            return true;
        }

        return false;
    }

    @Nullable
    private IItemHandler getDistantItemHandler(int index) {
        if (level == null || invPosY.length <= index || invPosY[index] < level.getMinBuildHeight()) {
            return null;
        }

        BlockPos targetPos = new BlockPos(invPosX[index], invPosY[index], invPosZ[index]);
        BlockEntity blockEntity = level.getBlockEntity(targetPos);
        if (blockEntity == null || blockEntity instanceof EngineerHopperBlockEntity) {
            clearBinding(index);
            return null;
        }

        Direction side = sideFromIndex(invSide[index]);
        IItemHandler handler = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, side).orElse(null);
        if (handler != null) {
            return handler;
        }
        if (blockEntity instanceof WorldlyContainer worldly && side != null) {
            return new SidedInvWrapper(worldly, side);
        }
        if (blockEntity instanceof Container container) {
            return new InvWrapper(container);
        }

        clearBinding(index);
        return null;
    }

    private void clearBinding(int index) {
        setDistantInventory(index, 0, -1, 0);
        invSide[index] = -1;
        setChanged();
    }

    @Nullable
    private static Direction sideFromIndex(int side) {
        if (side < 0 || side >= Direction.values().length) {
            return null;
        }
        return Direction.values()[side];
    }

    public int getRedstoneSignal() {
        return redstoneSignal;
    }

    public boolean isBindingOutput() {
        return bindType;
    }

    public BlockPos getDistantInventoryPos(int index) {
        if (index < 0 || index >= invPosX.length || invPosY[index] < 0) {
            return Bound.UNBOUND_POS;
        }
        return new BlockPos(invPosX[index], invPosY[index], invPosZ[index]);
    }

    public void changeBindType() {
        bindType = !bindType;
        sync();
    }

    public void setDistantInventory(int count, int posX, int posY, int posZ) {
        invPosX[count] = posX;
        invPosY[count] = posY;
        invPosZ[count] = posZ;
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
        int invCount = bindType ? 0 : 1;
        if (blockEntity instanceof EngineerHopperBlockEntity) {
            return false;
        }
        if (blockEntity != null && (blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, side).isPresent()
                || blockEntity instanceof Container)) {
            setDistantInventory(invCount, pos.getX(), pos.getY(), pos.getZ());
            invSide[invCount] = side == null ? -1 : side.ordinal();
            sync();
            return true;
        }

        clearBinding(invCount);
        sync();
        return false;
    }

    @Override
    public BlockPos getBinding() {
        return getDistantInventoryPos(bindType ? 0 : 1);
    }

    @Override
    public boolean onUsedByWand(Player player, ItemStack stack, Direction side) {
        if (!player.isShiftKeyDown()) {
            changeBindType();
        }
        if (level != null && !level.isClientSide()) {
            level.playSound(null, worldPosition, BotaniaSounds.ding, SoundSource.BLOCKS, 0.11F, 1.0F);
        }
        return true;
    }

    @Override
    public String toString() {
        return "EngineerHopperBlockEntity{" + "bindType=" + bindType + ", invPosX=" + Arrays.toString(invPosX) + '}';
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(TAG_COOLDOWN, cooldown);
        tag.putBoolean(TAG_BIND_TYPE, bindType);
        tag.putIntArray(TAG_BINDING_X, invPosX);
        tag.putIntArray(TAG_BINDING_Y, invPosY);
        tag.putIntArray(TAG_BINDING_Z, invPosZ);
        tag.putIntArray(TAG_BINDING_SIDE, invSide);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        cooldown = tag.getInt(TAG_COOLDOWN);
        bindType = tag.getBoolean(TAG_BIND_TYPE);
        invPosX = readIntArray(tag, TAG_BINDING_X, new int[] {0, 0});
        invPosY = readIntArray(tag, TAG_BINDING_Y, new int[] {-1, -1});
        invPosZ = readIntArray(tag, TAG_BINDING_Z, new int[] {0, 0});
        invSide = readIntArray(tag, TAG_BINDING_SIDE, new int[] {-1, -1});
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putInt(TAG_COOLDOWN, cooldown);
        tag.putBoolean(TAG_BIND_TYPE, bindType);
        tag.putIntArray(TAG_BINDING_X, invPosX);
        tag.putIntArray(TAG_BINDING_Y, invPosY);
        tag.putIntArray(TAG_BINDING_Z, invPosZ);
        tag.putIntArray(TAG_BINDING_SIDE, invSide);
        return tag;
    }

    private static int[] readIntArray(CompoundTag tag, String key, int[] fallback) {
        int[] values = tag.getIntArray(key);
        return values.length == 2 ? values : fallback;
    }
}
