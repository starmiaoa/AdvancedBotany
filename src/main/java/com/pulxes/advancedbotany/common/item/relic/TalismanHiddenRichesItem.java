package com.pulxes.advancedbotany.common.item.relic;

import com.pulxes.advancedbotany.common.menu.TalismanHiddenRichesMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

public class TalismanHiddenRichesItem extends ModRelicItem {
    public static final int CHEST_COUNT = 11;
    public static final int CHEST_SIZE = 27;
    private static final int MAX_SEGMENT_COUNT = 16;

    public TalismanHiddenRichesItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        boolean equippedLastTick = wasEquipped(stack);
        if (!isSelected && equippedLastTick) {
            setEquipped(stack, false);
        }
        if (!equippedLastTick && isSelected) {
            setEquipped(stack, true);
            int segmentAngle = 360 / MAX_SEGMENT_COUNT;
            float shift = (float) segmentAngle / 2.0F * CHEST_COUNT;
            setRotationBase(stack, getCheckingAngle(living, 0.0F) - shift);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!canUseRelic(stack, player)) {
            return InteractionResultHolder.fail(stack);
        }
        int segment = getSegmentLookedAt(stack, player);
        if (segment == -1) {
            return InteractionResultHolder.pass(stack);
        }
        setOpenChest(stack, segment);
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            int slot = hand == InteractionHand.MAIN_HAND ? player.getInventory().selected : TalismanHiddenRichesMenu.OFFHAND_SLOT;
            NetworkHooks.openScreen(serverPlayer,
                    TalismanHiddenRichesMenu.provider(slot, segment, Component.translatable("container.chest")),
                    buffer -> {
                        buffer.writeInt(slot);
                        buffer.writeInt(segment);
                    });
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    protected static int getSegmentLookedAt(ItemStack stack, LivingEntity player) {
        float yaw = getCheckingAngle(player, getRotationBase(stack));
        int segmentAngle = 360 / MAX_SEGMENT_COUNT;
        for (int segment = 0; segment < CHEST_COUNT; segment++) {
            float start = segment * segmentAngle;
            if (yaw >= start && yaw < start + segmentAngle) {
                return segment;
            }
        }
        return -1;
    }

    protected static float getCheckingAngle(LivingEntity player, float base) {
        float yaw = Mth.wrapDegrees(player.getYRot()) + 90.0F;
        int segmentAngle = 360 / MAX_SEGMENT_COUNT;
        float shift = (float) segmentAngle / 2.0F * CHEST_COUNT;
        if (yaw < 0.0F) {
            yaw = 360.0F + yaw;
        }
        float angle = 360.0F - (yaw - (360.0F - base)) + shift;
        if (angle > 360.0F) {
            angle %= 360.0F;
        }
        if (angle < 0.0F) {
            angle = (angle % 360.0F) + 360.0F;
        }
        return angle;
    }

    public static void setRotationBase(ItemStack stack, float rotation) {
        stack.getOrCreateTag().putFloat("rotationBase", rotation);
    }

    public static float getRotationBase(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0.0F : tag.getFloat("rotationBase");
    }

    public static void setEquipped(ItemStack stack, boolean equipped) {
        stack.getOrCreateTag().putBoolean("equipped", equipped);
    }

    public static boolean wasEquipped(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean("equipped");
    }

    public static void setOpenChest(ItemStack stack, int segment) {
        stack.getOrCreateTag().putInt("openChest", segment);
    }

    public static int getOpenChest(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? -1 : tag.getInt("openChest");
    }

    public static void setChestLoot(ItemStack stack, SimpleContainer loot, int segment) {
        ListTag list = new ListTag();
        for (int i = 0; i < loot.getContainerSize(); i++) {
            ItemStack item = loot.getItem(i);
            if (item.isEmpty()) {
                continue;
            }
            CompoundTag itemTag = new CompoundTag();
            itemTag.putByte("slot", (byte) i);
            item.save(itemTag);
            list.add(itemTag);
        }
        stack.getOrCreateTag().put("chestLoot" + segment, list);
    }

    public static SimpleContainer getChestLoot(ItemStack stack, int segment) {
        SimpleContainer loot = new SimpleContainer(CHEST_SIZE);
        if (segment < 0 || segment >= CHEST_COUNT) {
            return loot;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return loot;
        }
        ListTag list = tag.getList("chestLoot" + segment, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag itemTag = list.getCompound(i);
            int slot = itemTag.getByte("slot");
            if (slot >= 0 && slot < CHEST_SIZE) {
                loot.setItem(slot, ItemStack.of(itemTag));
            }
        }
        return loot;
    }
}
