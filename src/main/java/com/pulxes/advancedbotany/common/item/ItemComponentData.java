package com.pulxes.advancedbotany.common.item;

import java.util.function.Consumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class ItemComponentData {
    private ItemComponentData() {
    }

    public static CompoundTag copy(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    public static void update(ItemStack stack, Consumer<CompoundTag> updater) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, updater);
    }

    public static boolean contains(ItemStack stack, String key) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).contains(key);
    }

    public static boolean getBoolean(ItemStack stack, String key) {
        return copy(stack).getBoolean(key);
    }

    public static boolean getBoolean(ItemStack stack, String key, boolean defaultValue) {
        CompoundTag tag = copy(stack);
        return tag.contains(key) ? tag.getBoolean(key) : defaultValue;
    }

    public static void putBoolean(ItemStack stack, String key, boolean value) {
        update(stack, tag -> tag.putBoolean(key, value));
    }

    public static int getInt(ItemStack stack, String key) {
        return copy(stack).getInt(key);
    }

    public static void putInt(ItemStack stack, String key, int value) {
        update(stack, tag -> tag.putInt(key, value));
    }

    public static short getShort(ItemStack stack, String key) {
        return copy(stack).getShort(key);
    }

    public static void putShort(ItemStack stack, String key, short value) {
        update(stack, tag -> tag.putShort(key, value));
    }

    public static float getFloat(ItemStack stack, String key) {
        return copy(stack).getFloat(key);
    }

    public static void putFloat(ItemStack stack, String key, float value) {
        update(stack, tag -> tag.putFloat(key, value));
    }

    public static String getString(ItemStack stack, String key) {
        return copy(stack).getString(key);
    }

    public static void putString(ItemStack stack, String key, String value) {
        update(stack, tag -> tag.putString(key, value));
    }

    public static ListTag getList(ItemStack stack, String key, int elementType) {
        return copy(stack).getList(key, elementType);
    }

    public static CompoundTag getCompound(ItemStack stack, String key) {
        return copy(stack).getCompound(key);
    }

    public static void putTag(ItemStack stack, String key, Tag value) {
        update(stack, tag -> tag.put(key, value));
    }

    public static void remove(ItemStack stack, String key) {
        update(stack, tag -> tag.remove(key));
    }

    public static CompoundTag saveItem(ItemStack stack, HolderLookup.Provider registries) {
        return (CompoundTag) stack.save(registries, new CompoundTag());
    }

    public static ItemStack parseItem(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag itemTag = tag.copy();
        itemTag.remove("slot");
        return ItemStack.parseOptional(registries, itemTag);
    }
}
