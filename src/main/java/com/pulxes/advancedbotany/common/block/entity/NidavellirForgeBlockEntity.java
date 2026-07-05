package com.pulxes.advancedbotany.common.block.entity;

import com.pulxes.advancedbotany.common.menu.NidavellirForgeMenu;
import com.pulxes.advancedbotany.common.recipe.AdvancedPlateRecipe;
import com.pulxes.advancedbotany.registry.ModBlockEntities;
import com.pulxes.advancedbotany.registry.ModRecipes;
import java.awt.Color;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.mana.ManaPool;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.spark.ManaSpark;
import vazkii.botania.api.mana.spark.SparkAttachable;
import vazkii.botania.api.mana.spark.SparkHelper;
import vazkii.botania.common.handler.BotaniaSounds;

public class NidavellirForgeBlockEntity extends BaseInventoryBlockEntity implements WorldlyContainer, ManaReceiver, SparkAttachable, MenuProvider {
    public static final int OUTPUT_SLOT = 0;
    public static final int FIRST_INPUT_SLOT = 1;
    private static final int INVENTORY_SIZE = 4;
    private static final String TAG_MANA = "mana";
    private static final String TAG_MANA_TO_GET = "manaToGet";
    private static final String TAG_REQUEST_UPDATE = "requestUpdate";
    private static final String TAG_RECIPE_COLOR = "recipeColor";
    private static final int[] ALL_SLOTS = new int[] {0, 1, 2, 3};
    private static final int DATA_MANA_LOW = 0;
    private static final int DATA_MANA_HIGH = 1;
    private static final int DATA_MANA_TO_GET_LOW = 2;
    private static final int DATA_MANA_TO_GET_HIGH = 3;
    private static final int DATA_COUNT = 4;

    private final LazyOptional<IItemHandlerModifiable>[] sidedHandlers = SidedInvWrapper.create(this, Direction.values());
    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_MANA_LOW -> low(mana);
                case DATA_MANA_HIGH -> high(mana);
                case DATA_MANA_TO_GET_LOW -> low(manaToGet);
                case DATA_MANA_TO_GET_HIGH -> high(manaToGet);
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case DATA_MANA_LOW -> mana = combine(value, high(mana));
                case DATA_MANA_HIGH -> mana = combine(low(mana), value);
                case DATA_MANA_TO_GET_LOW -> manaToGet = combine(value, high(manaToGet));
                case DATA_MANA_TO_GET_HIGH -> manaToGet = combine(low(manaToGet), value);
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    private int mana;
    private int manaToGet;
    private int recipeColor = 0x241E00;
    public boolean requestUpdate;

    public NidavellirForgeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NIDAVELLIR_FORGE.get(), pos, state, INVENTORY_SIZE);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, NidavellirForgeBlockEntity forge) {
        forge.updateServer();
        ManaSpark spark = forge.getAttachedSpark();
        if (spark != null) {
            List<ManaSpark> sparks = SparkHelper.getSparksAround(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, spark.getNetwork());
            for (ManaSpark otherSpark : sparks) {
                if (spark != otherSpark && otherSpark.getAttachedManaReceiver() instanceof ManaPool) {
                    otherSpark.registerTransfer(spark);
                }
            }
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, NidavellirForgeBlockEntity forge) {
        if (forge.mana <= 0 || forge.manaToGet <= 0) {
            return;
        }

        double worldTime = level.getGameTime();
        float bob = (float) (Math.sin((worldTime + new Random(pos.asLong()).nextInt(360)) / 18.0D) / 24.0D);
        float progress = Math.min(1.0F, forge.mana / (float) forge.manaToGet);
        float ticks = progress * 100.0F;
        int totalSpiritCount = 3;
        double tickIncrement = 360.0D / totalSpiritCount;
        double wticks = ticks * 5.0D - tickIncrement;
        double radius = Math.sin((ticks - 100.0F) / 10.0D) * 0.5D;
        float size = 0.4F;
        float[] colors = progressColor(forge.recipeColor, progress);

        for (int i = 0; i < totalSpiritCount; i++) {
            double x = pos.getX() + Math.sin(wticks * Math.PI / 180.0D) * radius + 0.5D;
            double y = pos.getY() - bob + 0.85D + Math.abs(radius) * 0.7D;
            double z = pos.getZ() + Math.cos(wticks * Math.PI / 180.0D) * radius + 0.5D;
            wticks += tickIncrement;
            BotaniaAPI.instance().sparkleFX(level, x, y, z, colors[0], colors[1], colors[2], 0.85F * size, 2);
            if (progress >= 1.0F) {
                for (int j = 0; j < 4; j++) {
                    BotaniaAPI.instance().sparkleFX(level,
                            pos.getX() + 0.5D,
                            pos.getY() + 1.1D - bob,
                            pos.getZ() + 0.5D,
                            colors[0], colors[1], colors[2],
                            level.random.nextFloat() * 0.06F + 0.06F,
                            2);
                }
            }
        }
    }

    private static int low(int value) {
        return value & 0xFFFF;
    }

    private static int high(int value) {
        return value >>> 16 & 0xFFFF;
    }

    private static int combine(int low, int high) {
        return (low & 0xFFFF) | (high & 0xFFFF) << 16;
    }

    private static float[] progressColor(int color, float progress) {
        float[] hsb = Color.RGBtoHSB(color & 0xFF, color >> 8 & 0xFF, color >> 16 & 0xFF, null);
        int rgb = Color.HSBtoRGB(hsb[0], hsb[1], progress);
        return new float[] {
                (rgb & 0xFF) / 255.0F,
                (rgb >> 8 & 0xFF) / 255.0F,
                (rgb >> 16 & 0xFF) / 255.0F
        };
    }

    private void updateServer() {
        if (level == null) {
            return;
        }

        boolean hasUpdate = false;
        List<ItemEntity> entities = level.getEntitiesOfClass(ItemEntity.class, new AABB(worldPosition, worldPosition.offset(1, 1, 1)));
        for (ItemEntity item : entities) {
            if (item.isRemoved() || item.getItem().isEmpty()) {
                continue;
            }
            ItemStack stack = item.getItem();
            int inserted = addItemStack(stack);
            if (inserted > 0) {
                // Mutating the live stack does not mark the entity's synched data dirty;
                // partial pickups desync the client count unless we setItem a fresh copy.
                ItemStack remaining = stack.copy();
                remaining.shrink(inserted);
                if (remaining.isEmpty()) {
                    item.discard();
                } else {
                    item.setItem(remaining);
                }
                hasUpdate = true;
                break;
            }
        }

        int oldManaToGet = manaToGet;
        Optional<AdvancedPlateRecipe> recipe = findMatchingAdvancedPlateRecipe();
        if (recipe.isPresent() && canAcceptOutput(recipe.get().getOutput())) {
            AdvancedPlateRecipe view = recipe.get();
            manaToGet = view.getManaUsage();
            recipeColor = view.getColor();
            if (mana >= manaToGet && manaToGet > 0) {
                craft(view);
                hasUpdate = true;
            }
        } else {
            mana = 0;
            manaToGet = 0;
            recipeColor = 0x241E00;
        }

        if (oldManaToGet != manaToGet || requestUpdate || hasUpdate) {
            requestUpdate = false;
            sync();
        }
    }

    private Optional<AdvancedPlateRecipe> findMatchingAdvancedPlateRecipe() {
        if (level == null) {
            return Optional.empty();
        }
        return level.getRecipeManager().getAllRecipesFor(ModRecipes.ADVANCED_PLATE_TYPE.get())
                .stream()
                .filter(recipe -> recipe.matches(this, level))
                .findFirst();
    }

    private boolean canAcceptOutput(ItemStack output) {
        ItemStack currentOutput = getItem(OUTPUT_SLOT);
        return currentOutput.isEmpty()
                || ItemStack.isSameItemSameTags(currentOutput, output)
                && currentOutput.getCount() + output.getCount() <= currentOutput.getMaxStackSize();
    }

    private void craft(AdvancedPlateRecipe recipe) {
        receiveMana(-recipe.getManaUsage());
        manaToGet = 0;
        for (int i = FIRST_INPUT_SLOT; i < getContainerSize(); i++) {
            ItemStack stack = getItem(i);
            if (stack.getCount() > 1) {
                stack.shrink(1);
            } else {
                items.set(i, ItemStack.EMPTY);
            }
        }
        ItemStack output = recipe.getOutput();
        ItemStack currentOutput = getItem(OUTPUT_SLOT);
        if (currentOutput.isEmpty()) {
            items.set(OUTPUT_SLOT, output);
        } else {
            currentOutput.grow(output.getCount());
        }
        if (level != null) {
            level.playSound(null, worldPosition, BotaniaSounds.terrasteelCraft, SoundSource.BLOCKS, 1.0F, 2.0F);
        }
    }

    private int addItemStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        for (int i = FIRST_INPUT_SLOT; i < getContainerSize(); i++) {
            ItemStack slotStack = getItem(i);
            if (slotStack.isEmpty()) {
                ItemStack stackToAdd = stack.copy();
                items.set(i, stackToAdd);
                return stack.getCount();
            }
            if (ItemStack.isSameItemSameTags(stack, slotStack) && slotStack.getCount() < stack.getMaxStackSize()) {
                int count = Math.min(stack.getCount(), stack.getMaxStackSize() - slotStack.getCount());
                slotStack.grow(count);
                return count;
            }
        }
        return 0;
    }

    public int getRecipeColor() {
        return recipeColor;
    }

    public ContainerData getContainerData() {
        return dataAccess;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.advancedbotany.nidavellir_forge");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new NidavellirForgeMenu(containerId, inventory, this, dataAccess);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return ALL_SLOTS;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot != OUTPUT_SLOT;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        if (side != Direction.UP || slot == OUTPUT_SLOT) {
            return false;
        }
        for (int i = FIRST_INPUT_SLOT; i < getContainerSize(); i++) {
            ItemStack slotStack = getItem(i);
            if (!slotStack.isEmpty() && slotStack.getCount() >= slotStack.getMaxStackSize() && ItemStack.isSameItemSameTags(stack, slotStack)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return side == Direction.DOWN && slot == OUTPUT_SLOT || side != Direction.DOWN && side != Direction.UP && slot != OUTPUT_SLOT;
    }

    @Override
    public Level getManaReceiverLevel() {
        return level;
    }

    @Override
    public BlockPos getManaReceiverPos() {
        return worldPosition;
    }

    @Override
    public int getCurrentMana() {
        return mana;
    }

    @Override
    public boolean isFull() {
        return mana >= manaToGet;
    }

    @Override
    public void receiveMana(int amount) {
        int oldMana = mana;
        mana = Math.max(0, Math.min(mana + amount, manaToGet));
        if (mana != oldMana) {
            requestUpdate = true;
            setChanged();
        }
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return !isFull();
    }

    @Override
    public boolean canAttachSpark(ItemStack stack) {
        return true;
    }

    @Override
    public int getAvailableSpaceForMana() {
        return Math.max(0, manaToGet - getCurrentMana());
    }

    @Override
    public ManaSpark getAttachedSpark() {
        if (level == null) {
            return null;
        }
        AABB bounds = new AABB(worldPosition.above(), worldPosition.above().offset(1, 1, 1));
        List<Entity> sparks = level.getEntitiesOfClass(Entity.class, bounds, entity -> entity instanceof ManaSpark);
        return sparks.size() == 1 ? (ManaSpark) sparks.get(0) : null;
    }

    @Override
    public boolean areIncomingTranfersDone() {
        return isFull();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(TAG_MANA, mana);
        tag.putInt(TAG_MANA_TO_GET, manaToGet);
        tag.putBoolean(TAG_REQUEST_UPDATE, requestUpdate);
        tag.putInt(TAG_RECIPE_COLOR, recipeColor);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        mana = tag.getInt(TAG_MANA);
        manaToGet = tag.getInt(TAG_MANA_TO_GET);
        requestUpdate = tag.getBoolean(TAG_REQUEST_UPDATE);
        recipeColor = tag.contains(TAG_RECIPE_COLOR) ? tag.getInt(TAG_RECIPE_COLOR) : 0x241E00;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        ContainerHelper.saveAllItems(tag, items);
        tag.putInt(TAG_MANA, mana);
        tag.putInt(TAG_MANA_TO_GET, manaToGet);
        tag.putBoolean(TAG_REQUEST_UPDATE, requestUpdate);
        tag.putInt(TAG_RECIPE_COLOR, recipeColor);
        return tag;
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        for (LazyOptional<IItemHandlerModifiable> sidedHandler : sidedHandlers) {
            sidedHandler.invalidate();
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER && side != null) {
            return sidedHandlers[side.ordinal()].cast();
        }
        return super.getCapability(capability, side);
    }
}
