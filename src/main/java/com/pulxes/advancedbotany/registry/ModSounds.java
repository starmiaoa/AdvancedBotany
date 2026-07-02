package com.pulxes.advancedbotany.registry;

import com.pulxes.advancedbotany.AdvancedBotany;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, AdvancedBotany.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> BOARD_CUBE = register("board_cube");
    public static final DeferredHolder<SoundEvent, SoundEvent> AQUA_SWORD = register("aqua_sword");
    public static final DeferredHolder<SoundEvent, SoundEvent> BLADE_SPACE = register("blade_space");
    public static final DeferredHolder<SoundEvent, SoundEvent> NEBULA_ROD = register("nebula_rod");
    public static final DeferredHolder<SoundEvent, SoundEvent> NEBULA_BLAZE = register("nebula_blaze");
    public static final DeferredHolder<SoundEvent, SoundEvent> FREYR_SLINGSHOT = register("freyr_slingshot");
    public static final DeferredHolder<SoundEvent, SoundEvent> HORN_PLENTY_USING = register("horn_plenty_using");
    public static final DeferredHolder<SoundEvent, SoundEvent> LOKI_CUBE_ARMOR = register("loki_cube_armor");

    private ModSounds() {
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(AdvancedBotany.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }
}
