package com.pulxes.advancedbotany.registry;

import com.pulxes.advancedbotany.common.item.equipment.armor.NebulaArmorItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class ModGameEvents {
    private ModGameEvents() {
    }

    @SubscribeEvent
    public static void playerTick(PlayerTickEvent.Post event) {
        NebulaArmorItem.handlePlayerTick(event);
    }

    @SubscribeEvent
    public static void livingTick(EntityTickEvent.Post event) {
        NebulaArmorItem.handleLivingTick(event);
    }

    @SubscribeEvent
    public static void livingJump(LivingEvent.LivingJumpEvent event) {
        NebulaArmorItem.handleLivingJump(event);
    }
}
