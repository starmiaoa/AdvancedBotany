package com.pulxes.advancedbotany.common.menu;

import com.pulxes.advancedbotany.common.block.entity.NidavellirForgeBlockEntity;
import com.pulxes.advancedbotany.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class NidavellirForgeMenu extends AbstractContainerMenu {
    private static final int TILE_SLOT_COUNT = 4;
    private final Container container;
    private final ContainerData data;

    public NidavellirForgeMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(containerId, playerInventory, resolveContainer(playerInventory, buffer.readBlockPos()), new SimpleContainerData(2));
    }

    public NidavellirForgeMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(ModMenuTypes.NIDAVELLIR_FORGE.get(), containerId);
        checkContainerSize(container, TILE_SLOT_COUNT);
        this.container = container;
        this.data = data;
        container.startOpen(playerInventory.player);

        addSlot(new Slot(container, NidavellirForgeBlockEntity.OUTPUT_SLOT, 80, 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        addSlot(new Slot(container, 1, 44, 54));
        addSlot(new Slot(container, 2, 80, 54));
        addSlot(new Slot(container, 3, 116, 54));

        addPlayerInventory(playerInventory, 8, 86);
        addDataSlots(data);
    }

    private static Container resolveContainer(Inventory playerInventory, BlockPos pos) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof NidavellirForgeBlockEntity forge) {
            return forge;
        }
        return new SimpleContainer(TILE_SLOT_COUNT);
    }

    public int getMana() {
        return data.get(0);
    }

    public int getManaToGet() {
        return data.get(1);
    }

    private void addPlayerInventory(Inventory playerInventory, int left, int top) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, left + column * 18, top + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, left + column * 18, top + 58));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            copy = stack.copy();
            if (index < TILE_SLOT_COUNT) {
                if (!moveItemStackTo(stack, TILE_SLOT_COUNT, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 1, TILE_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }
}
