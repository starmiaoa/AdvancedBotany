package com.pulxes.advancedbotany.common.item.equipment;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.item.BlockProvider;
import vazkii.botania.common.item.BlackHoleTalismanItem;
import vazkii.botania.common.item.BotaniaItems;

public class BlackHaloItem extends Item {
    public static final int SLOT_COUNT = 12;

    public BlackHaloItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack halo = player.getItemInHand(hand);
        int segment = getSegmentLookedAt(halo, player);
        if (segment < 0) {
            return InteractionResultHolder.pass(halo);
        }
        ItemStack stored = getItemForSlot(halo, segment);
        if (!stored.isEmpty()) {
            if (player.isShiftKeyDown()) {
                ItemStack copy = stored.copy();
                if (!player.getInventory().add(copy)) {
                    player.drop(copy, false);
                }
                setItemSlot(halo, ItemStack.EMPTY, segment);
            } else {
                toggleTalismanActive(stored);
                setItemSlot(halo, stored, segment);
                if (!level.isClientSide()) {
                    level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.3F, 0.1F);
                }
            }
            return InteractionResultHolder.sidedSuccess(halo, level.isClientSide());
        }

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!isFilledTalisman(stack)) {
                continue;
            }
            ItemStack copy = stack.copy();
            copy.setCount(1);
            setItemSlot(halo, copy, segment);
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            return InteractionResultHolder.sidedSuccess(halo, level.isClientSide());
        }
        return InteractionResultHolder.pass(halo);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockEntity blockEntity = level.getBlockEntity(context.getClickedPos());
        if (!(blockEntity instanceof Container container)) {
            return InteractionResult.PASS;
        }

        ItemStack halo = context.getItemInHand();
        Direction side = context.getClickedFace();
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack talisman = getItemForSlot(halo, i);
            if (!(talisman.getItem() instanceof BlackHoleTalismanItem)) {
                continue;
            }
            Block block = BlackHoleTalismanItem.getBlock(talisman);
            if (block == null || block == Blocks.AIR) {
                continue;
            }
            insertFromTalisman(container, talisman, block, side);
            setItemSlot(halo, talisman, i);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static void insertFromTalisman(Container container, ItemStack talisman, Block block, Direction side) {
        int[] slots;
        if (container instanceof WorldlyContainer sided) {
            slots = sided.getSlotsForFace(side);
        } else {
            slots = new int[container.getContainerSize()];
            for (int i = 0; i < slots.length; i++) {
                slots[i] = i;
            }
        }

        Item item = block.asItem();
        for (int slot : slots) {
            ItemStack inSlot = container.getItem(slot);
            if (inSlot.isEmpty()) {
                ItemStack toInsert = new ItemStack(block);
                int moved = BlackHoleTalismanItem.remove(talisman, toInsert.getMaxStackSize());
                if (moved <= 0) {
                    return;
                }
                toInsert.setCount(moved);
                if (canPlace(container, slot, toInsert, side)) {
                    container.setItem(slot, toInsert);
                    container.setChanged();
                } else {
                    BlackHoleTalismanItem.setCount(talisman, BlackHoleTalismanItem.getBlockCount(talisman) + moved);
                }
            } else if (inSlot.is(item) && inSlot.getCount() < inSlot.getMaxStackSize()) {
                int missing = inSlot.getMaxStackSize() - inSlot.getCount();
                int moved = BlackHoleTalismanItem.remove(talisman, missing);
                if (moved <= 0) {
                    return;
                }
                ItemStack simulated = inSlot.copy();
                simulated.grow(moved);
                if (canPlace(container, slot, simulated, side)) {
                    inSlot.grow(moved);
                    container.setChanged();
                } else {
                    BlackHoleTalismanItem.setCount(talisman, BlackHoleTalismanItem.getBlockCount(talisman) + moved);
                }
            }
        }
    }

    private static boolean canPlace(Container container, int slot, ItemStack stack, Direction side) {
        if (!container.canPlaceItem(slot, stack)) {
            return false;
        }
        return !(container instanceof WorldlyContainer sided) || sided.canPlaceItemThroughFace(slot, stack, side);
    }

    @Override
    public void inventoryTick(ItemStack halo, Level level, Entity entity, int slot, boolean selected) {
        boolean equippedLastTick = wasEquipped(halo);
        if (!selected && equippedLastTick) {
            setEquipped(halo, false);
        }
        if (!equippedLastTick && selected && entity instanceof LivingEntity living) {
            setEquipped(halo, true);
            setRotationBase(halo, getCheckingAngle(living) - 15.0F);
        }
        if (level.isClientSide() || level.getGameTime() % 10 != 0) {
            return;
        }
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack stored = getItemForSlot(halo, i);
            if (stored.getItem() instanceof BlackHoleTalismanItem talisman) {
                talisman.inventoryTick(stored, level, entity, slot, selected);
                setItemSlot(halo, stored, i);
            }
        }
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new SimpleCapabilityProvider<>(BotaniaForgeCapabilities.BLOCK_PROVIDER, new HaloBlockProvider(stack));
    }

    private static boolean isFilledTalisman(ItemStack stack) {
        if (!stack.is(BotaniaItems.blackHoleTalisman)) {
            return false;
        }
        Block block = BlackHoleTalismanItem.getBlock(stack);
        return block != null && block != Blocks.AIR;
    }

    private static void toggleTalismanActive(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(BlackHoleTalismanItem.TAG_ACTIVE, !tag.getBoolean(BlackHoleTalismanItem.TAG_ACTIVE));
    }

    private static void setItemSlot(ItemStack halo, ItemStack stack, int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            return;
        }
        CompoundTag tag = halo.getOrCreateTag();
        String key = "itemSlot" + slot;
        if (stack.isEmpty()) {
            tag.remove(key);
            return;
        }
        CompoundTag stored = new CompoundTag();
        stack.save(stored);
        tag.put(key, stored);
    }

    public static ItemStack getItemForSlot(ItemStack halo, int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            return ItemStack.EMPTY;
        }
        CompoundTag tag = halo.getTag();
        if (tag == null || !tag.contains("itemSlot" + slot)) {
            return ItemStack.EMPTY;
        }
        return ItemStack.of(tag.getCompound("itemSlot" + slot));
    }

    public static int getSegmentLookedAt(ItemStack stack, LivingEntity player) {
        float yaw = getCheckingAngle(player, getRotationBase(stack));
        if (!Float.isFinite(yaw)) {
            return -1;
        }
        for (int segment = 0; segment < SLOT_COUNT; segment++) {
            float start = segment * 30.0F;
            if (yaw >= start && yaw < start + 30.0F) {
                return segment;
            }
        }
        return -1;
    }

    public static void setRotationBase(ItemStack stack, float rotation) {
        stack.getOrCreateTag().putFloat("rotationBase", normalizeAngle(rotation));
    }

    public static float getRotationBase(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0.0F : tag.getFloat("rotationBase");
    }

    private static float getCheckingAngle(LivingEntity player, float base) {
        float yaw = Mth.wrapDegrees(player.getYRot()) + 90.0F;
        if (yaw < 0.0F) {
            yaw = 360.0F + yaw;
        }
        yaw -= 360.0F - base;
        float angle = 360.0F - yaw + 15.0F;
        return normalizeAngle(angle);
    }

    private static float getCheckingAngle(LivingEntity player) {
        return getCheckingAngle(player, 0.0F);
    }

    private static float normalizeAngle(float angle) {
        if (!Float.isFinite(angle)) {
            return Float.NaN;
        }
        angle %= 360.0F;
        if (angle < 0.0F) {
            angle += 360.0F;
        }
        return angle;
    }

    public static void setEquipped(ItemStack stack, boolean equipped) {
        stack.getOrCreateTag().putBoolean("equipped", equipped);
    }

    public static boolean wasEquipped(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean("equipped");
    }

    private static class HaloBlockProvider implements BlockProvider {
        private final ItemStack halo;

        private HaloBlockProvider(ItemStack halo) {
            this.halo = halo;
        }

        @Override
        public boolean provideBlock(Player player, ItemStack requestor, Block block, boolean doit) {
            for (int i = 0; i < SLOT_COUNT; i++) {
                ItemStack talisman = getItemForSlot(halo, i);
                if (!(talisman.getItem() instanceof BlackHoleTalismanItem)) {
                    continue;
                }
                if (BlackHoleTalismanItem.getBlock(talisman) == block && BlackHoleTalismanItem.getBlockCount(talisman) > 0) {
                    if (doit) {
                        BlackHoleTalismanItem.remove(talisman, 1);
                        setItemSlot(halo, talisman, i);
                    }
                    return true;
                }
            }
            return false;
        }

        @Override
        public int getBlockCount(Player player, ItemStack requestor, Block block) {
            int count = 0;
            for (int i = 0; i < SLOT_COUNT; i++) {
                ItemStack talisman = getItemForSlot(halo, i);
                if (talisman.getItem() instanceof BlackHoleTalismanItem && BlackHoleTalismanItem.getBlock(talisman) == block) {
                    count += BlackHoleTalismanItem.getBlockCount(talisman);
                }
            }
            return count;
        }
    }
}
