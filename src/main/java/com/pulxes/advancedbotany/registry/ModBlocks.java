package com.pulxes.advancedbotany.registry;

import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.common.block.AntigravitationBlock;
import com.pulxes.advancedbotany.common.block.FreyrLianaBlock;
import com.pulxes.advancedbotany.common.block.LebethronWoodBlock;
import com.pulxes.advancedbotany.common.block.LuminousFreyrLianaBlock;
import com.pulxes.advancedbotany.common.block.ManaChargerBlock;
import com.pulxes.advancedbotany.common.block.ManaContainerBlock;
import com.pulxes.advancedbotany.common.block.ManaCrystalCubeBlock;
import com.pulxes.advancedbotany.common.block.MithrilStorageBlock;
import com.pulxes.advancedbotany.common.block.NaturalManaSpreaderBlock;
import com.pulxes.advancedbotany.common.block.TerraFarmlandBlock;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, AdvancedBotany.MOD_ID);

    public static final RegistryObject<Block> MITHRIL_BLOCK = BLOCKS.register("mithril_block", MithrilStorageBlock::new);
    public static final RegistryObject<Block> LEBETHRON_WOOD = BLOCKS.register("lebethron_wood", LebethronWoodBlock::new);
    public static final RegistryObject<Block> LEBETHRON_PLANKS = BLOCKS.register("lebethron_planks", LebethronWoodBlock::new);
    public static final RegistryObject<Block> MOSSY_LEBETHRON_PLANKS = BLOCKS.register("mossy_lebethron_planks", LebethronWoodBlock::new);
    public static final RegistryObject<Block> GLIMMERING_LEBETHRON_WOOD = BLOCKS.register("glimmering_lebethron_wood", () -> new LebethronWoodBlock(12));
    public static final RegistryObject<Block> TERRA_FARMLAND = BLOCKS.register("terra_farmland", TerraFarmlandBlock::new);
    public static final RegistryObject<Block> FREYR_LIANA = BLOCKS.register("freyr_liana", FreyrLianaBlock::new);
    public static final RegistryObject<Block> LUMINOUS_FREYR_LIANA = BLOCKS.register("luminous_freyr_liana", LuminousFreyrLianaBlock::new);
    public static final RegistryObject<Block> ANTIGRAVITATION = BLOCKS.register("antigravitation", AntigravitationBlock::new);
    public static final RegistryObject<Block> MANA_CRYSTAL_CUBE = BLOCKS.register("mana_crystal_cube", ManaCrystalCubeBlock::new);
    public static final RegistryObject<Block> NATURAL_MANA_SPREADER = BLOCKS.register("natural_mana_spreader", NaturalManaSpreaderBlock::new);
    public static final RegistryObject<Block> MANA_CONTAINER = BLOCKS.register("mana_container", () -> new ManaContainerBlock(ManaContainerBlock.Variant.NORMAL));
    public static final RegistryObject<Block> DILUTED_MANA_CONTAINER = BLOCKS.register("diluted_mana_container", () -> new ManaContainerBlock(ManaContainerBlock.Variant.DILUTED));
    public static final RegistryObject<Block> FABULOUS_MANA_CONTAINER = BLOCKS.register("fabulous_mana_container", () -> new ManaContainerBlock(ManaContainerBlock.Variant.FABULOUS));
    public static final RegistryObject<Block> MANA_CHARGER = BLOCKS.register("mana_charger", ManaChargerBlock::new);

    private ModBlocks() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
