package com.pulxes.advancedbotany.common.item.equipment;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import vazkii.botania.api.mana.ManaItemHandler;

public class ManaFlowerItem extends ItemBauble {
    private static final int MANA_PER_TICK_BATCH = 54;

    public ManaFlowerItem(Properties properties) {
        super(properties.setNoRepair(), "belt");
    }

    @Override
    protected void onWornTick(ItemStack stack, LivingEntity entity) {
        if (entity instanceof Player player && !entity.level().isClientSide() && entity.tickCount % 5 == 0) {
            ManaItemHandler.instance().dispatchManaExact(stack, player, MANA_PER_TICK_BATCH, true);
        }
    }
}
