package com.pulxes.advancedbotany.registry;

import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.common.menu.MagicCraftCrateMenu;
import com.pulxes.advancedbotany.common.menu.NidavellirForgeMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, AdvancedBotany.MOD_ID);

    public static final RegistryObject<MenuType<NidavellirForgeMenu>> NIDAVELLIR_FORGE =
            MENU_TYPES.register("nidavellir_forge", () -> IForgeMenuType.create(NidavellirForgeMenu::new));
    public static final RegistryObject<MenuType<MagicCraftCrateMenu>> MAGIC_CRAFT_CRATE =
            MENU_TYPES.register("magic_craft_crate", () -> IForgeMenuType.create(MagicCraftCrateMenu::new));

    private ModMenuTypes() {
    }

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
