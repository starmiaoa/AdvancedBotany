package com.pulxes.advancedbotany.registry;

import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.common.block.AntigravitationBlock;
import com.pulxes.advancedbotany.common.block.EngineerHopperBlock;
import com.pulxes.advancedbotany.common.block.FreyrLianaBlock;
import com.pulxes.advancedbotany.common.block.LebethronWoodBlock;
import com.pulxes.advancedbotany.common.block.LebethronNaturalCoreBlock;
import com.pulxes.advancedbotany.common.block.LuminousFreyrLianaBlock;
import com.pulxes.advancedbotany.common.block.MagicCraftCrateBlock;
import com.pulxes.advancedbotany.common.block.ManaChargerBlock;
import com.pulxes.advancedbotany.common.block.ManaContainerBlock;
import com.pulxes.advancedbotany.common.block.ManaCrystalCubeBlock;
import com.pulxes.advancedbotany.common.block.MithrilStorageBlock;
import com.pulxes.advancedbotany.common.block.NaturalManaSpreaderBlock;
import com.pulxes.advancedbotany.common.block.NidavellirForgeBlock;
import com.pulxes.advancedbotany.common.block.PlayingBoardBlock;
import com.pulxes.advancedbotany.common.block.TerraFarmlandBlock;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, AdvancedBotany.MOD_ID);

    public static final DeferredHolder<Block, Block> MITHRIL_BLOCK = BLOCKS.register("mithril_block", MithrilStorageBlock::new);
    public static final DeferredHolder<Block, Block> LEBETHRON_WOOD = BLOCKS.register("lebethron_wood", () -> new LebethronWoodBlock());
    public static final DeferredHolder<Block, Block> LEBETHRON_PLANKS = BLOCKS.register("lebethron_planks", () -> new LebethronWoodBlock());
    public static final DeferredHolder<Block, Block> MOSSY_LEBETHRON_PLANKS = BLOCKS.register("mossy_lebethron_planks", () -> new LebethronWoodBlock());
    public static final DeferredHolder<Block, Block> GLIMMERING_LEBETHRON_WOOD = BLOCKS.register("glimmering_lebethron_wood", () -> new LebethronWoodBlock(12));
    public static final DeferredHolder<Block, Block> TERRA_FARMLAND = BLOCKS.register("terra_farmland", TerraFarmlandBlock::new);
    public static final DeferredHolder<Block, Block> FREYR_LIANA = BLOCKS.register("freyr_liana", FreyrLianaBlock::new);
    public static final DeferredHolder<Block, Block> LUMINOUS_FREYR_LIANA = BLOCKS.register("luminous_freyr_liana", LuminousFreyrLianaBlock::new);
    public static final DeferredHolder<Block, Block> ANTIGRAVITATION = BLOCKS.register("antigravitation", AntigravitationBlock::new);
    public static final DeferredHolder<Block, Block> MANA_CRYSTAL_CUBE = BLOCKS.register("mana_crystal_cube", ManaCrystalCubeBlock::new);
    public static final DeferredHolder<Block, Block> NATURAL_MANA_SPREADER = BLOCKS.register("natural_mana_spreader", NaturalManaSpreaderBlock::new);
    public static final DeferredHolder<Block, Block> MANA_CONTAINER = BLOCKS.register("mana_container", () -> new ManaContainerBlock(ManaContainerBlock.Variant.NORMAL));
    public static final DeferredHolder<Block, Block> DILUTED_MANA_CONTAINER = BLOCKS.register("diluted_mana_container", () -> new ManaContainerBlock(ManaContainerBlock.Variant.DILUTED));
    public static final DeferredHolder<Block, Block> FABULOUS_MANA_CONTAINER = BLOCKS.register("fabulous_mana_container", () -> new ManaContainerBlock(ManaContainerBlock.Variant.FABULOUS));
    public static final DeferredHolder<Block, Block> MANA_CHARGER = BLOCKS.register("mana_charger", ManaChargerBlock::new);
    public static final DeferredHolder<Block, Block> ENGINEER_HOPPER = BLOCKS.register("engineer_hopper", EngineerHopperBlock::new);
    public static final DeferredHolder<Block, Block> NIDAVELLIR_FORGE = BLOCKS.register("nidavellir_forge", NidavellirForgeBlock::new);
    public static final DeferredHolder<Block, Block> MAGIC_CRAFT_CRATE = BLOCKS.register("magic_craft_crate", MagicCraftCrateBlock::new);
    public static final DeferredHolder<Block, Block> PLAYING_BOARD = BLOCKS.register("playing_board", () -> new PlayingBoardBlock(false));
    public static final DeferredHolder<Block, Block> FATE_PLAYING_BOARD = BLOCKS.register("fate_playing_board", () -> new PlayingBoardBlock(true));
    public static final DeferredHolder<Block, Block> LEBETHRON_NATURAL_CORE = BLOCKS.register("lebethron_natural_core", LebethronNaturalCoreBlock::new);

    private ModBlocks() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
