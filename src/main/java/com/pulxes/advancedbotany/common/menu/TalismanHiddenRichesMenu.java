package com.pulxes.advancedbotany.common.menu;

import com.pulxes.advancedbotany.common.item.relic.TalismanHiddenRichesItem;
import com.pulxes.advancedbotany.registry.ModItems;
import com.pulxes.advancedbotany.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TalismanHiddenRichesMenu extends AbstractContainerMenu {
    private static final int CHEST_SLOT_COUNT = TalismanHiddenRichesItem.CHEST_SIZE;
    public static final int OFFHAND_SLOT = 40;
    private final int talismanSlot;
    private final int segment;
    private final TalismanContainer container;

    public TalismanHiddenRichesMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        this(containerId, playerInventory,
                buffer.readInt(),
                buffer.readInt());
    }

    public TalismanHiddenRichesMenu(int containerId, Inventory playerInventory, int talismanSlot, int segment) {
        super(ModMenuTypes.TALISMAN_HIDDEN_RICHES.get(), containerId);
        this.talismanSlot = talismanSlot;
        this.segment = segment;
        this.container = new TalismanContainer(playerInventory.player, talismanSlot, segment);
        container.startOpen(playerInventory.player);

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new TalismanSlot(container, column + row * 9, 8 + column * 18, 18 + row * 18));
            }
        }
        addPlayerInventory(playerInventory, 8, 85);
    }

    public static MenuProvider provider(int talismanSlot, int segment, Component title) {
        return new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return title;
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
                return new TalismanHiddenRichesMenu(containerId, playerInventory, talismanSlot, segment);
            }
        };
    }

    private void addPlayerInventory(Inventory playerInventory, int left, int top) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, left + column * 18, top + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            int slot = column;
            if (talismanSlot == slot) {
                addSlot(new LockedSlot(playerInventory, slot, left + column * 18, top + 58));
            } else {
                addSlot(new Slot(playerInventory, slot, left + column * 18, top + 58));
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            copy = stack.copy();
            if (index < CHEST_SLOT_COUNT) {
                if (!moveItemStackTo(stack, CHEST_SLOT_COUNT, slots.size(), true)) {
                    container.save();
                    return ItemStack.EMPTY;
                }
            } else {
                if (isTalisman(stack) || !moveItemStackTo(stack, 0, CHEST_SLOT_COUNT, false)) {
                    container.save();
                    return ItemStack.EMPTY;
                }
            }
            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        container.save();
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
        container.save();
    }

    private static boolean isTalisman(ItemStack stack) {
        return stack.is(ModItems.KEY_TO_HIDDEN_WEALTH.get());
    }

    private static class TalismanContainer extends SimpleContainer {
        private final Player player;
        private final int talismanSlot;
        private final int segment;
        private ItemStack lastKnownTalisman = ItemStack.EMPTY;

        TalismanContainer(Player player, int talismanSlot, int segment) {
            super(TalismanHiddenRichesItem.CHEST_SIZE);
            this.player = player;
            this.talismanSlot = talismanSlot;
            this.segment = segment;
            ItemStack talisman = getTalisman();
            if (isTalisman(talisman)) {
                SimpleContainer savedLoot = TalismanHiddenRichesItem.getChestLoot(talisman, segment);
                for (int i = 0; i < getContainerSize(); i++) {
                    setItem(i, savedLoot.getItem(i));
                }
            }
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return stillValid(player) && !isTalisman(stack);
        }

        @Override
        public boolean stillValid(Player player) {
            return segment >= 0
                    && segment < TalismanHiddenRichesItem.CHEST_COUNT
                    && isTalisman(getTalisman());
        }

        void save() {
            ItemStack target = getTalisman();
            if (!isTalisman(target)) {
                target = lastKnownTalisman;
            }
            if (isTalisman(target)) {
                TalismanHiddenRichesItem.setChestLoot(target, this, segment);
                TalismanHiddenRichesItem.setOpenChest(target, -1);
            }
        }

        private ItemStack getTalisman() {
            ItemStack stack;
            if (talismanSlot == OFFHAND_SLOT) {
                stack = player.getOffhandItem();
            } else if (talismanSlot >= 0 && talismanSlot < player.getInventory().getContainerSize()) {
                stack = player.getInventory().getItem(talismanSlot);
            } else {
                stack = ItemStack.EMPTY;
            }
            if (isTalisman(stack)) {
                lastKnownTalisman = stack;
            }
            return stack;
        }
    }

    private static class TalismanSlot extends Slot {
        TalismanSlot(TalismanContainer container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return container.canPlaceItem(getSlotIndex(), stack);
        }
    }

    private static class LockedSlot extends Slot {
        LockedSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
