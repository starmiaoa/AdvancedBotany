package com.pulxes.advancedbotany.common.block.entity;

import com.pulxes.advancedbotany.common.menu.MagicCraftCrateMenu;
import com.pulxes.advancedbotany.registry.ModBlockEntities;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.block.Wandable;

public class MagicCraftCrateBlockEntity extends BaseInventoryBlockEntity implements WorldlyContainer, Wandable, MenuProvider {
    public static final int GRID_START = 0;
    public static final int GRID_END = 8;
    public static final int OUTPUT_SLOT = 9;
    public static final int WAND_SLOT = 10;
    public static final int BOOK_SLOT = 11;
    private static final int INVENTORY_SIZE = 12;
    private static final String TAG_WAITING_STACK = "waitingStack";
    private static final String TAG_PATTERN = "pattern";
    private static final String TAG_SIGNAL = "signal";
    private static final int[] ACCESSIBLE_SLOTS = new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    private static final AbstractContainerMenu DUMMY_MENU = new AbstractContainerMenu(null, -1) {
        @Override
        public ItemStack quickMoveStack(Player player, int index) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(Player player) {
            return false;
        }
    };

    private final IItemHandlerModifiable[] sidedHandlers = createSidedHandlers();
    private final boolean[] pattern = new boolean[] {true, true, true, true, true, true, true, true, true};
    private int signal;
    private ItemStack waitingStack = ItemStack.EMPTY;

    public MagicCraftCrateBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MAGIC_CRAFT_CRATE.get(), pos, state, INVENTORY_SIZE);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MagicCraftCrateBlockEntity crate) {
        if (!level.isClientSide()) {
            crate.serverTick();
        }
    }

    private void serverTick() {
        if (level == null) {
            return;
        }

        boolean crafted = craft(true);
        if (crafted && canEject() || isFull() && waitingStack.isEmpty()) {
            ejectAll();
        }

        int newSignal = 0;
        while (newSignal < 9 && (isLocked(newSignal) || !getItem(newSignal).isEmpty())) {
            newSignal++;
        }
        if (!waitingStack.isEmpty()) {
            newSignal = 14;
        }
        if (newSignal != signal) {
            signal = newSignal;
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
            sync();
        }
    }

    private boolean craft(boolean fullCheck) {
        if (level == null || fullCheck && !isFull()) {
            return false;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }

        NonNullList<ItemStack> craftingStacks = NonNullList.withSize(9, ItemStack.EMPTY);
        for (int i = 0; i < 9; i++) {
            if (!isLocked(i)) {
                craftingStacks.set(i, getItem(i).copy());
            }
        }
        CraftingInput crafting = CraftingInput.of(3, 3, craftingStacks);
        Optional<CraftingRecipe> recipe = serverLevel.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, crafting, serverLevel)
                .map(holder -> holder.value());
        if (recipe.isEmpty()) {
            waitingStack = ItemStack.EMPTY;
            return false;
        }

        ItemStack result = recipe.get().assemble(crafting, serverLevel.registryAccess());
        if (result.isEmpty()) {
            waitingStack = ItemStack.EMPTY;
            return false;
        }

        ItemStack output = getItem(OUTPUT_SLOT);
        if (!output.isEmpty() && (!ItemStack.isSameItemSameComponents(output, result) || output.getCount() + result.getCount() > output.getMaxStackSize())) {
            waitingStack = result.copy();
            sync();
            return false;
        }

        NonNullList<ItemStack> remaining = recipe.get().getRemainingItems(crafting);
        for (int i = 0; i < 9; i++) {
            if (isLocked(i)) {
                continue;
            }
            items.set(i, remaining.get(i).copy());
        }
        if (output.isEmpty()) {
            items.set(OUTPUT_SLOT, result.copy());
        } else {
            output.grow(result.getCount());
        }
        waitingStack = ItemStack.EMPTY;
        sync();
        return true;
    }

    public boolean isFull() {
        for (int i = 0; i < 9; i++) {
            if (!isLocked(i) && getItem(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public boolean[] getPattern() {
        return pattern;
    }

    public void setPattern(boolean[] newPattern) {
        if (newPattern.length != 9) {
            return;
        }
        System.arraycopy(newPattern, 0, pattern, 0, pattern.length);
        sync();
    }

    public boolean isLocked(int slot) {
        return slot >= 0 && slot < 9 && !pattern[slot];
    }

    public int getSignal() {
        return signal;
    }

    public ItemStack getWaitingStack() {
        return waitingStack;
    }

    public boolean canEject() {
        if (level == null) {
            return false;
        }
        BlockPos below = worldPosition.below();
        BlockState state = level.getBlockState(below);
        return state.isAir() || state.getCollisionShape(level, below, CollisionContext.empty()).isEmpty();
    }

    public void ejectAll() {
        for (int i = 0; i <= OUTPUT_SLOT; i++) {
            ItemStack stack = getItem(i);
            if (!stack.isEmpty()) {
                eject(stack, false);
                items.set(i, ItemStack.EMPTY);
            }
        }
        waitingStack = ItemStack.EMPTY;
        sync();
    }

    public void eject(ItemStack stack, boolean redstone) {
        if (level == null || level.isClientSide() || stack.isEmpty()) {
            return;
        }
        ItemEntity item = new ItemEntity(level, worldPosition.getX() + 0.5D, worldPosition.getY() - 0.5D, worldPosition.getZ() + 0.5D, stack.copy());
        item.setDeltaMovement(0.0D, 0.0D, 0.0D);
        if (redstone) {
            item.setPickUpDelay(0);
        }
        level.addFreshEntity(item);
        level.playSound(null, worldPosition, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.2F, 1.5F);
    }

    @Override
    public boolean onUsedByWand(Player player, ItemStack stack, Direction side) {
        if (level != null && !level.isClientSide()) {
            ejectAll();
        }
        return true;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.advancedbotany.magic_craft_crate");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new MagicCraftCrateMenu(containerId, inventory, this);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot < 9 && !isLocked(slot) || slot == WAND_SLOT || slot == BOOK_SLOT;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return ACCESSIBLE_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return slot < 9 && !isLocked(slot);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return slot == OUTPUT_SLOT;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt(TAG_SIGNAL, signal);
        tag.putByteArray(TAG_PATTERN, patternToBytes());
        if (!waitingStack.isEmpty()) {
            CompoundTag stackTag = new CompoundTag();
            waitingStack.save(registries, stackTag);
            tag.put(TAG_WAITING_STACK, stackTag);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        signal = tag.getInt(TAG_SIGNAL);
        byte[] bytes = tag.getByteArray(TAG_PATTERN);
        if (bytes.length == 9) {
            for (int i = 0; i < pattern.length; i++) {
                pattern[i] = bytes[i] != 0;
            }
        }
        waitingStack = tag.contains(TAG_WAITING_STACK) ? ItemStack.parseOptional(registries, tag.getCompound(TAG_WAITING_STACK)) : ItemStack.EMPTY;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        tag.putInt(TAG_SIGNAL, signal);
        tag.putByteArray(TAG_PATTERN, patternToBytes());
        if (!waitingStack.isEmpty()) {
            CompoundTag stackTag = new CompoundTag();
            waitingStack.save(registries, stackTag);
            tag.put(TAG_WAITING_STACK, stackTag);
        }
        return tag;
    }

    private byte[] patternToBytes() {
        byte[] bytes = new byte[pattern.length];
        for (int i = 0; i < pattern.length; i++) {
            bytes[i] = (byte) (pattern[i] ? 1 : 0);
        }
        return bytes;
    }

    @Override
    public IItemHandler getItemHandler(@Nullable Direction side) {
        return side == null ? super.getItemHandler(null) : sidedHandlers[side.ordinal()];
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
