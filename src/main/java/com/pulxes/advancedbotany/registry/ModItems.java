package com.pulxes.advancedbotany.registry;

import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.api.AdvancedBotanyAPI;
import com.pulxes.advancedbotany.common.item.AdvancedBotanyGuideItem;
import com.pulxes.advancedbotany.common.item.AirOfForgottenLandsItem;
import com.pulxes.advancedbotany.common.item.ApothecaryResourceItem;
import com.pulxes.advancedbotany.common.item.FoilResourceItem;
import com.pulxes.advancedbotany.common.item.SphereOfAttractionItem;
import com.pulxes.advancedbotany.common.item.equipment.ManaFlowerItem;
import com.pulxes.advancedbotany.common.item.equipment.ManaRingItem;
import com.pulxes.advancedbotany.common.item.equipment.AdvancedSparkItem;
import com.pulxes.advancedbotany.common.item.equipment.AquaSwordItem;
import com.pulxes.advancedbotany.common.item.equipment.BlackHaloItem;
import com.pulxes.advancedbotany.common.item.equipment.MaterialDestroyerItem;
import com.pulxes.advancedbotany.common.item.equipment.NebulaBlazeItem;
import com.pulxes.advancedbotany.common.item.equipment.NebulaRodItem;
import com.pulxes.advancedbotany.common.item.equipment.SpaceBladeItem;
import com.pulxes.advancedbotany.common.item.equipment.SprawlRodItem;
import com.pulxes.advancedbotany.common.item.equipment.TerraHoeItem;
import com.pulxes.advancedbotany.common.item.equipment.armor.NebulaArmorItem;
import com.pulxes.advancedbotany.common.item.equipment.armor.NebulaHelmetItem;
import com.pulxes.advancedbotany.common.item.equipment.armor.WildHuntArmorItem;
import net.minecraft.world.item.ArmorItem;
import com.pulxes.advancedbotany.common.item.relic.FreyrSlingshotItem;
import com.pulxes.advancedbotany.common.item.relic.HornOfPlentyItem;
import com.pulxes.advancedbotany.common.item.relic.PocketWardrobeItem;
import com.pulxes.advancedbotany.common.item.relic.SphereNavigationItem;
import com.pulxes.advancedbotany.common.item.relic.TalismanHiddenRichesItem;
import com.pulxes.advancedbotany.common.item.relic.WildHuntWhipItem;
import java.util.function.Supplier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, AdvancedBotany.MOD_ID);

    public static final DeferredHolder<Item, Item> MITHRIL = ITEMS.register("mithril", () -> new Item(defaultProperties()));
    public static final DeferredHolder<Item, Item> MITHRIL_NUGGET = ITEMS.register("mithril_nugget", () -> new Item(defaultProperties()));
    public static final DeferredHolder<Item, Item> MANA_NETHER_STAR = ITEMS.register("mana_nether_star", () -> new FoilResourceItem(defaultProperties()));
    public static final DeferredHolder<Item, Item> AIR_OF_FORGOTTEN_LANDS = ITEMS.register("air_of_forgotten_lands", () -> new AirOfForgottenLandsItem(defaultProperties()));
    public static final DeferredHolder<Item, Item> NATURES_GIFT = ITEMS.register("natures_gift", () -> new ApothecaryResourceItem(defaultProperties()));
    public static final DeferredHolder<Item, Item> NEBULA_FRAGMENT = ITEMS.register("nebula_fragment", () -> new Item(defaultProperties().rarity(AdvancedBotanyAPI.RARITY_NEBULA)));
    public static final DeferredHolder<Item, Item> PIECE_OF_NEBULA = ITEMS.register("piece_of_nebula", () -> new Item(defaultProperties().rarity(AdvancedBotanyAPI.RARITY_NEBULA)));
    public static final DeferredHolder<Item, Item> MITHRIL_MANA_RING = ITEMS.register("mithril_mana_ring", () -> new ManaRingItem(defaultProperties().fireResistant(), 8_000_000));
    public static final DeferredHolder<Item, Item> NEBULA_MANA_RING = ITEMS.register("nebula_mana_ring", () -> new ManaRingItem(defaultProperties().rarity(AdvancedBotanyAPI.RARITY_NEBULA).fireResistant(), 48_000_000));
    public static final DeferredHolder<Item, Item> SPHERE_OF_ATTRACTION = ITEMS.register("sphere_of_attraction", () -> new SphereOfAttractionItem(defaultProperties()));
    public static final DeferredHolder<Item, Item> MANA_FLOWER = ITEMS.register("mana_flower", () -> new ManaFlowerItem(defaultProperties()));
    public static final DeferredHolder<Item, Item> LEXICON = ITEMS.register("lexicon", () -> new AdvancedBotanyGuideItem(defaultProperties().stacksTo(1)));
    public static final DeferredHolder<Item, Item> MITHRIL_BLOCK = blockItem("mithril_block", ModBlocks.MITHRIL_BLOCK);
    public static final DeferredHolder<Item, Item> LEBETHRON_WOOD = blockItem("lebethron_wood", ModBlocks.LEBETHRON_WOOD);
    public static final DeferredHolder<Item, Item> LEBETHRON_PLANKS = blockItem("lebethron_planks", ModBlocks.LEBETHRON_PLANKS);
    public static final DeferredHolder<Item, Item> MOSSY_LEBETHRON_PLANKS = blockItem("mossy_lebethron_planks", ModBlocks.MOSSY_LEBETHRON_PLANKS);
    public static final DeferredHolder<Item, Item> GLIMMERING_LEBETHRON_WOOD = blockItem("glimmering_lebethron_wood", ModBlocks.GLIMMERING_LEBETHRON_WOOD);
    public static final DeferredHolder<Item, Item> TERRA_FARMLAND = blockItem("terra_farmland", ModBlocks.TERRA_FARMLAND);
    public static final DeferredHolder<Item, Item> FREYR_LIANA = blockItem("freyr_liana", ModBlocks.FREYR_LIANA);
    public static final DeferredHolder<Item, Item> LUMINOUS_FREYR_LIANA = blockItem("luminous_freyr_liana", ModBlocks.LUMINOUS_FREYR_LIANA);
    public static final DeferredHolder<Item, Item> ANTIGRAVITATION = blockItem("antigravitation", ModBlocks.ANTIGRAVITATION);
    public static final DeferredHolder<Item, Item> MANA_CRYSTAL_CUBE = blockItem("mana_crystal_cube", ModBlocks.MANA_CRYSTAL_CUBE);
    public static final DeferredHolder<Item, Item> NATURAL_MANA_SPREADER = blockItem("natural_mana_spreader", ModBlocks.NATURAL_MANA_SPREADER);
    public static final DeferredHolder<Item, Item> MANA_CONTAINER = blockItem("mana_container", ModBlocks.MANA_CONTAINER);
    public static final DeferredHolder<Item, Item> DILUTED_MANA_CONTAINER = blockItem("diluted_mana_container", ModBlocks.DILUTED_MANA_CONTAINER);
    public static final DeferredHolder<Item, Item> FABULOUS_MANA_CONTAINER = blockItem("fabulous_mana_container", ModBlocks.FABULOUS_MANA_CONTAINER);
    public static final DeferredHolder<Item, Item> MANA_CHARGER = blockItem("mana_charger", ModBlocks.MANA_CHARGER);
    public static final DeferredHolder<Item, Item> ENGINEER_HOPPER = blockItem("engineer_hopper", ModBlocks.ENGINEER_HOPPER);
    public static final DeferredHolder<Item, Item> NIDAVELLIR_FORGE = blockItem("nidavellir_forge", ModBlocks.NIDAVELLIR_FORGE);
    public static final DeferredHolder<Item, Item> MAGIC_CRAFT_CRATE = blockItem("magic_craft_crate", ModBlocks.MAGIC_CRAFT_CRATE);
    public static final DeferredHolder<Item, Item> PLAYING_BOARD = blockItem("playing_board", ModBlocks.PLAYING_BOARD);
    public static final DeferredHolder<Item, Item> FATE_PLAYING_BOARD = blockItem("fate_playing_board", ModBlocks.FATE_PLAYING_BOARD);
    public static final DeferredHolder<Item, Item> LEBETHRON_NATURAL_CORE = blockItem("lebethron_natural_core", ModBlocks.LEBETHRON_NATURAL_CORE);
    public static final DeferredHolder<Item, Item> MATERIAL_DESTROYER = ITEMS.register("material_destroyer", () -> new MaterialDestroyerItem(defaultProperties()));
    public static final DeferredHolder<Item, Item> TERRA_HOE = ITEMS.register("terra_hoe", () -> new TerraHoeItem(defaultProperties()));
    public static final DeferredHolder<Item, Item> AQUA_SWORD = ITEMS.register("aqua_sword", () -> new AquaSwordItem(defaultProperties()));
    public static final DeferredHolder<Item, Item> BLACK_HOLE_BOX = ITEMS.register("black_hole_box", () -> new BlackHaloItem(defaultProperties()));
    public static final DeferredHolder<Item, Item> ROD_OF_SPRAWL = ITEMS.register("rod_of_sprawl", () -> new SprawlRodItem(defaultProperties()));
    public static final DeferredHolder<Item, Item> BLADE_OF_SPACE = ITEMS.register("blade_of_space", () -> new SpaceBladeItem(defaultProperties()));
    public static final DeferredHolder<Item, Item> ROD_OF_NEBULA = ITEMS.register("rod_of_nebula", () -> new NebulaRodItem(defaultProperties()));
    public static final DeferredHolder<Item, Item> NEBULA_BLAZE = ITEMS.register("nebula_blaze", () -> new NebulaBlazeItem(defaultProperties()));
    public static final DeferredHolder<Item, Item> SUPERCONDUCTIVE_SPARK = ITEMS.register("superconductive_spark", () -> new AdvancedSparkItem(defaultProperties()));
    public static final DeferredHolder<Item, Item> NEBULA_HELMET = ITEMS.register("nebula_helmet",
            () -> new NebulaHelmetItem(false, defaultProperties().rarity(AdvancedBotanyAPI.RARITY_NEBULA)));
    public static final DeferredHolder<Item, Item> NEBULA_HELMET_OF_REVEALING = ITEMS.register("nebula_helmet_of_revealing",
            () -> new NebulaHelmetItem(true, defaultProperties().rarity(AdvancedBotanyAPI.RARITY_NEBULA)));
    public static final DeferredHolder<Item, Item> NEBULA_CHESTPLATE = ITEMS.register("nebula_chestplate",
            () -> new NebulaArmorItem(ArmorItem.Type.CHESTPLATE, false, defaultProperties().rarity(AdvancedBotanyAPI.RARITY_NEBULA)));
    public static final DeferredHolder<Item, Item> NEBULA_LEGGINGS = ITEMS.register("nebula_leggings",
            () -> new NebulaArmorItem(ArmorItem.Type.LEGGINGS, false, defaultProperties().rarity(AdvancedBotanyAPI.RARITY_NEBULA)));
    public static final DeferredHolder<Item, Item> NEBULA_BOOTS = ITEMS.register("nebula_boots",
            () -> new NebulaArmorItem(ArmorItem.Type.BOOTS, false, defaultProperties().rarity(AdvancedBotanyAPI.RARITY_NEBULA)));
    public static final DeferredHolder<Item, Item> WILD_HUNT_HELMET = ITEMS.register("wild_hunt_helmet",
            () -> new WildHuntArmorItem(ArmorItem.Type.HELMET, defaultProperties().rarity(AdvancedBotanyAPI.RARITY_WILD_HUNT)));
    public static final DeferredHolder<Item, Item> WILD_HUNT_CHESTPLATE = ITEMS.register("wild_hunt_chestplate",
            () -> new WildHuntArmorItem(ArmorItem.Type.CHESTPLATE, defaultProperties().rarity(AdvancedBotanyAPI.RARITY_WILD_HUNT)));
    public static final DeferredHolder<Item, Item> WILD_HUNT_LEGGINGS = ITEMS.register("wild_hunt_leggings",
            () -> new WildHuntArmorItem(ArmorItem.Type.LEGGINGS, defaultProperties().rarity(AdvancedBotanyAPI.RARITY_WILD_HUNT)));
    public static final DeferredHolder<Item, Item> WILD_HUNT_BOOTS = ITEMS.register("wild_hunt_boots",
            () -> new WildHuntArmorItem(ArmorItem.Type.BOOTS, defaultProperties().rarity(AdvancedBotanyAPI.RARITY_WILD_HUNT)));
    public static final DeferredHolder<Item, Item> FREYR_SLINGSHOT = ITEMS.register("freyr_slingshot", () -> new FreyrSlingshotItem(defaultProperties().rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> HORN_OF_PLENTY = ITEMS.register("horn_of_plenty", () -> new HornOfPlentyItem(defaultProperties().rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> NIMBLE_CUBE = ITEMS.register("nimble_cube", () -> new PocketWardrobeItem(defaultProperties().rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> SPHERE_OF_NAVIGATION = ITEMS.register("sphere_of_navigation", () -> new SphereNavigationItem(defaultProperties().rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> KEY_TO_HIDDEN_WEALTH = ITEMS.register("key_to_hidden_wealth", () -> new TalismanHiddenRichesItem(defaultProperties().rarity(Rarity.EPIC)));
    public static final DeferredHolder<Item, Item> WILD_HUNT_WHIP = ITEMS.register("wild_hunt_whip", () -> new WildHuntWhipItem(defaultProperties().rarity(Rarity.EPIC)));

    private ModItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    public static void registerFateBoardRelics() {
        AdvancedBotanyAPI.registerFateBoardRelic(FREYR_SLINGSHOT.get());
        AdvancedBotanyAPI.registerFateBoardRelic(HORN_OF_PLENTY.get());
        AdvancedBotanyAPI.registerFateBoardRelic(NIMBLE_CUBE.get());
        AdvancedBotanyAPI.registerFateBoardRelic(SPHERE_OF_NAVIGATION.get());
        AdvancedBotanyAPI.registerFateBoardRelic(KEY_TO_HIDDEN_WEALTH.get());
        AdvancedBotanyAPI.registerFateBoardRelic(WILD_HUNT_WHIP.get());
    }

    private static Item.Properties defaultProperties() {
        return new Item.Properties().rarity(Rarity.COMMON);
    }

    private static DeferredHolder<Item, Item> blockItem(String name, Supplier<? extends Block> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), defaultProperties()));
    }
}
