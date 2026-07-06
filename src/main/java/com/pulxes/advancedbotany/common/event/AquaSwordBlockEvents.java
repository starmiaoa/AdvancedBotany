package com.pulxes.advancedbotany.common.event;

import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.common.item.equipment.AquaSwordItem;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = AdvancedBotany.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class AquaSwordBlockEvents {
    private AquaSwordBlockEvents() {
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || event.getAmount() <= 0.0F) {
            return;
        }
        if (!player.isUsingItem()) {
            return;
        }

        ItemStack useStack = player.getUseItem();
        if (!(useStack.getItem() instanceof AquaSwordItem) || !canBlock(event.getSource())) {
            return;
        }

        event.setAmount(event.getAmount() * 0.5F);
    }

    private static boolean canBlock(DamageSource source) {
        if (source.is(DamageTypeTags.BYPASSES_SHIELD)
                || source.is(DamageTypes.FALL)
                || source.is(DamageTypes.MAGIC)
                || source.is(DamageTypes.INDIRECT_MAGIC)
                || source.is(DamageTypes.FELL_OUT_OF_WORLD)
                || source.is(DamageTypes.OUTSIDE_BORDER)) {
            return false;
        }
        return source.getDirectEntity() != null || source.getEntity() != null;
    }
}

