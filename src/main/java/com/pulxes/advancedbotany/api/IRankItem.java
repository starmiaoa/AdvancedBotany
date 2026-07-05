package com.pulxes.advancedbotany.api;

import net.minecraft.world.item.ItemStack;
import vazkii.botania.api.mana.ManaItem;

/**
 * 1.7.10 compatibility API for mana items with ranked capacity tiers.
 * In 1.20, actual mana storage is exposed through Botania's ManaItem capability.
 */
public interface IRankItem extends ManaItem {
    int getLevel(ItemStack stack);

    int[] getLevels();
}
