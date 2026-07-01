package com.pulxes.advancedbotany.client;

import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.client.gui.MagicCraftCrateScreen;
import com.pulxes.advancedbotany.client.gui.NidavellirForgeScreen;
import com.pulxes.advancedbotany.common.item.SphereOfAttractionItem;
import com.pulxes.advancedbotany.registry.ModItems;
import com.pulxes.advancedbotany.registry.ModMenuTypes;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = AdvancedBotany.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ModClientEvents {
    private ModClientEvents() {
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.NIDAVELLIR_FORGE.get(), NidavellirForgeScreen::new);
            MenuScreens.register(ModMenuTypes.MAGIC_CRAFT_CRATE.get(), MagicCraftCrateScreen::new);
            ItemProperties.register(ModItems.SPHERE_OF_ATTRACTION.get(), new ResourceLocation(AdvancedBotany.MOD_ID, "active"),
                    (stack, level, entity, seed) -> SphereOfAttractionItem.isActive(stack) ? 1.0F : 0.0F);
        });
    }
}
