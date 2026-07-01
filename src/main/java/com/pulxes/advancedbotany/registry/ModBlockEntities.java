package com.pulxes.advancedbotany.registry;

import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.common.block.entity.ManaChargerBlockEntity;
import com.pulxes.advancedbotany.common.block.entity.ManaContainerBlockEntity;
import com.pulxes.advancedbotany.common.block.entity.ManaCrystalCubeBlockEntity;
import com.pulxes.advancedbotany.common.block.entity.NaturalManaSpreaderBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AdvancedBotany.MOD_ID);

    public static final RegistryObject<BlockEntityType<ManaCrystalCubeBlockEntity>> MANA_CRYSTAL_CUBE = BLOCK_ENTITIES.register("mana_crystal_cube",
            () -> BlockEntityType.Builder.of(ManaCrystalCubeBlockEntity::new, ModBlocks.MANA_CRYSTAL_CUBE.get()).build(null));
    public static final RegistryObject<BlockEntityType<NaturalManaSpreaderBlockEntity>> NATURAL_MANA_SPREADER = BLOCK_ENTITIES.register("natural_mana_spreader",
            () -> BlockEntityType.Builder.of(NaturalManaSpreaderBlockEntity::new, ModBlocks.NATURAL_MANA_SPREADER.get()).build(null));
    public static final RegistryObject<BlockEntityType<ManaContainerBlockEntity>> MANA_CONTAINER = BLOCK_ENTITIES.register("mana_container",
            () -> BlockEntityType.Builder.of(
                    ManaContainerBlockEntity::new,
                    ModBlocks.MANA_CONTAINER.get(),
                    ModBlocks.DILUTED_MANA_CONTAINER.get(),
                    ModBlocks.FABULOUS_MANA_CONTAINER.get()).build(null));
    public static final RegistryObject<BlockEntityType<ManaChargerBlockEntity>> MANA_CHARGER = BLOCK_ENTITIES.register("mana_charger",
            () -> BlockEntityType.Builder.of(ManaChargerBlockEntity::new, ModBlocks.MANA_CHARGER.get()).build(null));

    private ModBlockEntities() {
    }

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
