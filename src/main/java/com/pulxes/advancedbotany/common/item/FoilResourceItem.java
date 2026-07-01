package com.pulxes.advancedbotany.common.item;

import net.minecraft.world.item.ItemStack;

public class FoilResourceItem extends ApothecaryResourceItem {
    public FoilResourceItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
