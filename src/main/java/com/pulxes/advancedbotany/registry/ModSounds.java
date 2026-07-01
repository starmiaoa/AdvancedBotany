package com.pulxes.advancedbotany.registry;

import com.pulxes.advancedbotany.AdvancedBotany;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, AdvancedBotany.MOD_ID);

    public static final RegistryObject<SoundEvent> BOARD_CUBE = register("board_cube");
    public static final RegistryObject<SoundEvent> AQUA_SWORD = register("aqua_sword");
    public static final RegistryObject<SoundEvent> BLADE_SPACE = register("blade_space");
    public static final RegistryObject<SoundEvent> NEBULA_ROD = register("nebula_rod");
    public static final RegistryObject<SoundEvent> NEBULA_BLAZE = register("nebula_blaze");
    public static final RegistryObject<SoundEvent> FREYR_SLINGSHOT = register("freyr_slingshot");
    public static final RegistryObject<SoundEvent> HORN_PLENTY_USING = register("horn_plenty_using");
    public static final RegistryObject<SoundEvent> LOKI_CUBE_ARMOR = register("loki_cube_armor");

    private ModSounds() {
    }

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }

    private static RegistryObject<SoundEvent> register(String name) {
        ResourceLocation id = new ResourceLocation(AdvancedBotany.MOD_ID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }
}
