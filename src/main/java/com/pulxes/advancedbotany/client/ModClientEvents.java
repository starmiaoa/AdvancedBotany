package com.pulxes.advancedbotany.client;

import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.client.gui.MagicCraftCrateScreen;
import com.pulxes.advancedbotany.client.gui.NidavellirForgeScreen;
import com.pulxes.advancedbotany.client.renderer.entity.EmptyEntityRenderer;
import com.pulxes.advancedbotany.common.item.equipment.MaterialDestroyerItem;
import com.pulxes.advancedbotany.common.item.equipment.SpaceBladeItem;
import com.pulxes.advancedbotany.registry.ModEntities;
import com.pulxes.advancedbotany.registry.ModItems;
import com.pulxes.advancedbotany.registry.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
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
            ItemProperties.register(ModItems.MATERIAL_DESTROYER.get(), new ResourceLocation(AdvancedBotany.MOD_ID, "enabled"),
                    (stack, level, entity, seed) -> MaterialDestroyerItem.isEnabled(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.BLADE_OF_SPACE.get(), new ResourceLocation(AdvancedBotany.MOD_ID, "enabled_mode"),
                    (stack, level, entity, seed) -> SpaceBladeItem.isEnabledMode(stack) ? 1.0F : 0.0F);
        });
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SWORD.get(), EmptyEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.NEBULA_BLAZE.get(), EmptyEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.ADVANCED_SPARK.get(), EmptyEntityRenderer::new);
    }
}
