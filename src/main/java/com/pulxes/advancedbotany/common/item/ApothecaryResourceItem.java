package com.pulxes.advancedbotany.common.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import vazkii.botania.api.recipe.CustomApothecaryColor;

public class ApothecaryResourceItem extends Item implements CustomApothecaryColor {
    public static final int APOTHECARY_PARTICLE_COLOR = 0x9B0000;

    public ApothecaryResourceItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getParticleColor(ItemStack stack) {
        return APOTHECARY_PARTICLE_COLOR;
    }
}
