package com.pulxes.advancedbotany.registry;

import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.api.AdvancedBotanyAPI;
import com.pulxes.advancedbotany.common.item.AirOfForgottenLandsItem;
import com.pulxes.advancedbotany.common.item.ApothecaryResourceItem;
import com.pulxes.advancedbotany.common.item.FoilResourceItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
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

    private ModItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }

    private static Item.Properties defaultProperties() {
        return new Item.Properties().rarity(Rarity.COMMON);
    }
}
