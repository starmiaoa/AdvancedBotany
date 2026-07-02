package com.pulxes.advancedbotany.registry;

import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.common.block.entity.BoardFateBlockEntity;
import com.pulxes.advancedbotany.common.block.entity.EngineerHopperBlockEntity;
import com.pulxes.advancedbotany.common.block.entity.GameBoardBlockEntity;
import com.pulxes.advancedbotany.common.block.entity.LebethronNaturalCoreBlockEntity;
import com.pulxes.advancedbotany.common.block.entity.MagicCraftCrateBlockEntity;
import com.pulxes.advancedbotany.common.block.entity.ManaChargerBlockEntity;
import com.pulxes.advancedbotany.common.block.entity.ManaContainerBlockEntity;
import com.pulxes.advancedbotany.common.block.entity.ManaCrystalCubeBlockEntity;
import com.pulxes.advancedbotany.common.block.entity.NaturalManaSpreaderBlockEntity;
import com.pulxes.advancedbotany.common.block.entity.NidavellirForgeBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, AdvancedBotany.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ManaCrystalCubeBlockEntity>> MANA_CRYSTAL_CUBE = BLOCK_ENTITIES.register("mana_crystal_cube",
            () -> BlockEntityType.Builder.of(ManaCrystalCubeBlockEntity::new, ModBlocks.MANA_CRYSTAL_CUBE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NaturalManaSpreaderBlockEntity>> NATURAL_MANA_SPREADER = BLOCK_ENTITIES.register("natural_mana_spreader",
            () -> BlockEntityType.Builder.of(NaturalManaSpreaderBlockEntity::new, ModBlocks.NATURAL_MANA_SPREADER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ManaContainerBlockEntity>> MANA_CONTAINER = BLOCK_ENTITIES.register("mana_container",
            () -> BlockEntityType.Builder.of(
                    ManaContainerBlockEntity::new,
                    ModBlocks.MANA_CONTAINER.get(),
                    ModBlocks.DILUTED_MANA_CONTAINER.get(),
                    ModBlocks.FABULOUS_MANA_CONTAINER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ManaChargerBlockEntity>> MANA_CHARGER = BLOCK_ENTITIES.register("mana_charger",
            () -> BlockEntityType.Builder.of(ManaChargerBlockEntity::new, ModBlocks.MANA_CHARGER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EngineerHopperBlockEntity>> ENGINEER_HOPPER = BLOCK_ENTITIES.register("engineer_hopper",
            () -> BlockEntityType.Builder.of(EngineerHopperBlockEntity::new, ModBlocks.ENGINEER_HOPPER.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NidavellirForgeBlockEntity>> NIDAVELLIR_FORGE = BLOCK_ENTITIES.register("nidavellir_forge",
            () -> BlockEntityType.Builder.of(NidavellirForgeBlockEntity::new, ModBlocks.NIDAVELLIR_FORGE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MagicCraftCrateBlockEntity>> MAGIC_CRAFT_CRATE = BLOCK_ENTITIES.register("magic_craft_crate",
            () -> BlockEntityType.Builder.of(MagicCraftCrateBlockEntity::new, ModBlocks.MAGIC_CRAFT_CRATE.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GameBoardBlockEntity>> PLAYING_BOARD = BLOCK_ENTITIES.register("playing_board",
            () -> BlockEntityType.Builder.of(GameBoardBlockEntity::new, ModBlocks.PLAYING_BOARD.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BoardFateBlockEntity>> FATE_PLAYING_BOARD = BLOCK_ENTITIES.register("fate_playing_board",
            () -> BlockEntityType.Builder.of(BoardFateBlockEntity::new, ModBlocks.FATE_PLAYING_BOARD.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LebethronNaturalCoreBlockEntity>> LEBETHRON_NATURAL_CORE = BLOCK_ENTITIES.register("lebethron_natural_core",
            () -> BlockEntityType.Builder.of(LebethronNaturalCoreBlockEntity::new, ModBlocks.LEBETHRON_NATURAL_CORE.get()).build(null));

    private ModBlockEntities() {
    }

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
