package com.pulxes.advancedbotany.common.item.relic;

import com.pulxes.advancedbotany.common.item.ItemComponentData;
import com.pulxes.advancedbotany.registry.ModSounds;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.bus.api.SubscribeEvent;

public class PocketWardrobeItem extends ModRelicItem {
    private static final int SEGMENT_COUNT = 5;
    private static final int MAX_SEGMENT_COUNT = 12;

    public PocketWardrobeItem(Properties properties) {
        super(properties);
        NeoForge.EVENT_BUS.register(this);
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
        if (!level.isClientSide()) {
            if (player.isShiftKeyDown()) {
                setPrioritySet(stack, segment);
            } else {
                swapArmorSet(stack, player, segment);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
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
            float shift = (float) segmentAngle / 2.0F * SEGMENT_COUNT;
            setRotationBase(stack, getCheckingAngle(living) - shift);
        }

        int fightingTick = getFightingTick(stack);
        if (fightingTick > 0) {
            setFightingTick(stack, fightingTick - 1);
        } else if (!level.isClientSide() && fightingTick == 0 && getFightingMode(stack) && entity instanceof Player player) {
            setFightingMode(stack, false);
            swapArmorSet(stack, player, getPrioritySet(stack));
        }
    }

    @SubscribeEvent
    public void onLivingAttack(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.getAbilities().instabuild) {
            return;
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof PocketWardrobeItem wardrobe) || !wardrobe.canUseRelic(stack, player)) {
                continue;
            }
            if (!getFightingMode(stack)) {
                int priority = getPrioritySet(stack);
                if (!hasArmor(getArmorSet(stack, priority, player.registryAccess()))) {
                    continue;
                }
                setFightingTick(stack, 32);
                setFightingMode(stack, true);
                wardrobe.swapArmorSet(stack, player, priority);
                return;
            }
            setFightingTick(stack, 32);
            return;
        }
    }

    public void swapArmorSet(ItemStack wardrobe, Player player, int segment) {
        ItemStack[] currentArmor = new ItemStack[4];
        ItemStack[] storedArmor = getArmorSet(wardrobe, segment, player.registryAccess());
        for (int i = 0; i < 4; i++) {
            currentArmor[i] = player.getInventory().armor.get(i).copy();
            player.getInventory().armor.set(i, storedArmor[i]);
        }
        setArmorSet(wardrobe, currentArmor, segment, player.registryAccess());
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        player.level().playSound(null, player.blockPosition(), ModSounds.LOKI_CUBE_ARMOR.get(), SoundSource.PLAYERS, 0.3F, 0.86F);
    }

    public void setArmorSet(ItemStack wardrobe, ItemStack[] armorSet, int segment, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (int i = 0; i < armorSet.length; i++) {
            ItemStack armor = armorSet[i];
            if (armor == null || armor.isEmpty()) {
                continue;
            }
            CompoundTag itemTag = ItemComponentData.saveItem(armor, registries);
            itemTag.putByte("slot", (byte) i);
            list.add(itemTag);
        }
        ItemComponentData.putTag(wardrobe, "armorSet" + segment, list);
    }

    public static ItemStack[] getArmorSet(ItemStack wardrobe, int segment, HolderLookup.Provider registries) {
        ItemStack[] armorSet = new ItemStack[] {ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};
        if (segment < 0 || segment >= SEGMENT_COUNT) {
            return armorSet;
        }
        ListTag list = ItemComponentData.getList(wardrobe, "armorSet" + segment, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag itemTag = list.getCompound(i);
            int slot = itemTag.getByte("slot");
            if (slot >= 0 && slot < armorSet.length) {
                armorSet[slot] = ItemComponentData.parseItem(itemTag, registries);
            }
        }
        return armorSet;
    }

    private static boolean hasArmor(ItemStack[] armorSet) {
        for (ItemStack armor : armorSet) {
            if (armor != null && !armor.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static int getSegmentLookedAt(ItemStack stack, LivingEntity player) {
        float yaw = getCheckingAngle(player, getRotationBase(stack));
        int segmentAngle = 360 / MAX_SEGMENT_COUNT;
        for (int segment = 0; segment < SEGMENT_COUNT; segment++) {
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
        float shift = (float) segmentAngle / 2.0F * SEGMENT_COUNT;
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

    protected static float getCheckingAngle(LivingEntity player) {
        return getCheckingAngle(player, 0.0F);
    }

    public static void setEquipped(ItemStack stack, boolean equipped) {
        ItemComponentData.putBoolean(stack, "equipped", equipped);
    }

    public static boolean wasEquipped(ItemStack stack) {
        return ItemComponentData.getBoolean(stack, "equipped");
    }

    public static void setRotationBase(ItemStack stack, float rotation) {
        ItemComponentData.putFloat(stack, "rotationBase", rotation);
    }

    public static float getRotationBase(ItemStack stack) {
        return ItemComponentData.getFloat(stack, "rotationBase");
    }

    public static void setFightingMode(ItemStack stack, boolean mode) {
        ItemComponentData.putBoolean(stack, "fightingMode", mode);
    }

    public static boolean getFightingMode(ItemStack stack) {
        return ItemComponentData.getBoolean(stack, "fightingMode");
    }

    public static void setFightingTick(ItemStack stack, int tick) {
        ItemComponentData.putInt(stack, "fightingTick", tick);
    }

    public static int getFightingTick(ItemStack stack) {
        return ItemComponentData.getInt(stack, "fightingTick");
    }

    public static void setPrioritySet(ItemStack stack, int segment) {
        ItemComponentData.putInt(stack, "prioritySet", Mth.clamp(segment, 0, SEGMENT_COUNT - 1));
    }

    public static int getPrioritySet(ItemStack stack) {
        CompoundTag tag = ItemComponentData.copy(stack);
        return tag.isEmpty() ? 2 : Mth.clamp(tag.getInt("prioritySet"), 0, SEGMENT_COUNT - 1);
    }
}
