package com.pulxes.advancedbotany.registry;

import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.api.AdvancedBotanyAPI;
import com.pulxes.advancedbotany.common.item.AirOfForgottenLandsItem;
import com.pulxes.advancedbotany.common.item.ApothecaryResourceItem;
import com.pulxes.advancedbotany.common.item.FoilResourceItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AdvancedBotany.MOD_ID);

    public static final RegistryObject<Item> MITHRIL = ITEMS.register("mithril", () -> new Item(defaultProperties()));
    public static final RegistryObject<Item> MITHRIL_NUGGET = ITEMS.register("mithril_nugget", () -> new Item(defaultProperties()));
    public static final RegistryObject<Item> MANA_NETHER_STAR = ITEMS.register("mana_nether_star", () -> new FoilResourceItem(defaultProperties()));
    public static final RegistryObject<Item> AIR_OF_FORGOTTEN_LANDS = ITEMS.register("air_of_forgotten_lands", () -> new AirOfForgottenLandsItem(defaultProperties()));
    public static final RegistryObject<Item> NATURES_GIFT = ITEMS.register("natures_gift", () -> new ApothecaryResourceItem(defaultProperties()));
    public static final RegistryObject<Item> NEBULA_FRAGMENT = ITEMS.register("nebula_fragment", () -> new Item(defaultProperties().rarity(AdvancedBotanyAPI.RARITY_NEBULA)));
    public static final RegistryObject<Item> PIECE_OF_NEBULA = ITEMS.register("piece_of_nebula", () -> new Item(defaultProperties().rarity(AdvancedBotanyAPI.RARITY_NEBULA)));
    public static final RegistryObject<Item> MITHRIL_BLOCK = blockItem("mithril_block", ModBlocks.MITHRIL_BLOCK);
    public static final RegistryObject<Item> LEBETHRON_WOOD = blockItem("lebethron_wood", ModBlocks.LEBETHRON_WOOD);
    public static final RegistryObject<Item> LEBETHRON_PLANKS = blockItem("lebethron_planks", ModBlocks.LEBETHRON_PLANKS);
    public static final RegistryObject<Item> MOSSY_LEBETHRON_PLANKS = blockItem("mossy_lebethron_planks", ModBlocks.MOSSY_LEBETHRON_PLANKS);
    public static final RegistryObject<Item> GLIMMERING_LEBETHRON_WOOD = blockItem("glimmering_lebethron_wood", ModBlocks.GLIMMERING_LEBETHRON_WOOD);
    public static final RegistryObject<Item> TERRA_FARMLAND = blockItem("terra_farmland", ModBlocks.TERRA_FARMLAND);
    public static final RegistryObject<Item> FREYR_LIANA = blockItem("freyr_liana", ModBlocks.FREYR_LIANA);
    public static final RegistryObject<Item> LUMINOUS_FREYR_LIANA = blockItem("luminous_freyr_liana", ModBlocks.LUMINOUS_FREYR_LIANA);
    public static final RegistryObject<Item> ANTIGRAVITATION = blockItem("antigravitation", ModBlocks.ANTIGRAVITATION);
    public static final RegistryObject<Item> MANA_CRYSTAL_CUBE = blockItem("mana_crystal_cube", ModBlocks.MANA_CRYSTAL_CUBE);
    public static final RegistryObject<Item> NATURAL_MANA_SPREADER = blockItem("natural_mana_spreader", ModBlocks.NATURAL_MANA_SPREADER);
    public static final RegistryObject<Item> MANA_CONTAINER = blockItem("mana_container", ModBlocks.MANA_CONTAINER);
    public static final RegistryObject<Item> DILUTED_MANA_CONTAINER = blockItem("diluted_mana_container", ModBlocks.DILUTED_MANA_CONTAINER);
    public static final RegistryObject<Item> FABULOUS_MANA_CONTAINER = blockItem("fabulous_mana_container", ModBlocks.FABULOUS_MANA_CONTAINER);
    public static final RegistryObject<Item> MANA_CHARGER = blockItem("mana_charger", ModBlocks.MANA_CHARGER);

    private ModItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    private static Item.Properties defaultProperties() {
        return new Item.Properties().rarity(Rarity.COMMON);
    }

    private static RegistryObject<Item> blockItem(String name, RegistryObject<? extends Block> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), defaultProperties()));
    }
}
