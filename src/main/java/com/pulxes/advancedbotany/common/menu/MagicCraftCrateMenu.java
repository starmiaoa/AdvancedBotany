package com.pulxes.advancedbotany.common.menu;

import com.pulxes.advancedbotany.common.block.entity.MagicCraftCrateBlockEntity;
import com.pulxes.advancedbotany.registry.ModMenuTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class MagicCraftCrateMenu extends AbstractContainerMenu {
    private static final int TILE_SLOT_COUNT = 12;
    private final Container container;

    public MagicCraftCrateMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(containerId, playerInventory, resolveContainer(playerInventory, buffer.readBlockPos()));
    }

    public MagicCraftCrateMenu(int containerId, Inventory playerInventory, Container container) {
        super(ModMenuTypes.MAGIC_CRAFT_CRATE.get(), containerId);
        checkContainerSize(container, TILE_SLOT_COUNT);
        this.container = container;
        container.startOpen(playerInventory.player);

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                int slot = row * 3 + column;
                addSlot(new Slot(container, slot, 30 + column * 18, 18 + row * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return container.canPlaceItem(getSlotIndex(), stack);
                    }
                });
            }
        }
        addSlot(new Slot(container, MagicCraftCrateBlockEntity.OUTPUT_SLOT, 124, 36) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        addSlot(new Slot(container, MagicCraftCrateBlockEntity.WAND_SLOT, 8, 18));
        addSlot(new Slot(container, MagicCraftCrateBlockEntity.BOOK_SLOT, 8, 40));

        addPlayerInventory(playerInventory, 8, 86);
    }

    private static Container resolveContainer(Inventory playerInventory, BlockPos pos) {
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(pos);
        if (blockEntity instanceof MagicCraftCrateBlockEntity crate) {
            return crate;
        }
        return new SimpleContainer(TILE_SLOT_COUNT);
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
            } else if (!moveItemStackTo(stack, MagicCraftCrateBlockEntity.GRID_START, MagicCraftCrateBlockEntity.OUTPUT_SLOT, false)) {
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
