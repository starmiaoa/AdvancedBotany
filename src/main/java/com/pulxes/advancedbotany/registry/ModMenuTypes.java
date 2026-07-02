package com.pulxes.advancedbotany.registry;

import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.common.menu.MagicCraftCrateMenu;
import com.pulxes.advancedbotany.common.menu.NidavellirForgeMenu;
import com.pulxes.advancedbotany.common.menu.TalismanHiddenRichesMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, AdvancedBotany.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<NidavellirForgeMenu>> NIDAVELLIR_FORGE =
            MENU_TYPES.register("nidavellir_forge", () -> IMenuTypeExtension.create(NidavellirForgeMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<MagicCraftCrateMenu>> MAGIC_CRAFT_CRATE =
            MENU_TYPES.register("magic_craft_crate", () -> IMenuTypeExtension.create(MagicCraftCrateMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<TalismanHiddenRichesMenu>> TALISMAN_HIDDEN_RICHES =
            MENU_TYPES.register("talisman_hidden_riches", () -> IMenuTypeExtension.create(TalismanHiddenRichesMenu::new));

    private ModMenuTypes() {
    }

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
