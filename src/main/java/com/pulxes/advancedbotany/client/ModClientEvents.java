package com.pulxes.advancedbotany.client;

import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.client.gui.MagicCraftCrateScreen;
import com.pulxes.advancedbotany.client.gui.NidavellirForgeScreen;
import com.pulxes.advancedbotany.client.renderer.entity.AdvancedSparkRenderer;
import com.pulxes.advancedbotany.client.renderer.entity.AlphirinePortalRenderer;
import com.pulxes.advancedbotany.client.renderer.entity.AnonymousSteveRenderer;
import com.pulxes.advancedbotany.client.gui.TalismanHiddenRichesScreen;
import com.pulxes.advancedbotany.common.item.SphereOfAttractionItem;
import com.pulxes.advancedbotany.common.item.relic.SphereNavigationItem;
import com.pulxes.advancedbotany.client.renderer.entity.EmptyEntityRenderer;
import com.pulxes.advancedbotany.common.item.equipment.MaterialDestroyerItem;
import com.pulxes.advancedbotany.common.item.equipment.SpaceBladeItem;
import com.pulxes.advancedbotany.registry.ModEntities;
import com.pulxes.advancedbotany.registry.ModItems;
import com.pulxes.advancedbotany.registry.ModMenuTypes;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = AdvancedBotany.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ModClientEvents {
    private ModClientEvents() {
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(ModItems.SPHERE_OF_ATTRACTION.get(), ResourceLocation.fromNamespaceAndPath(AdvancedBotany.MOD_ID, "active"),
                    (stack, level, entity, seed) -> SphereOfAttractionItem.isActive(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.MATERIAL_DESTROYER.get(), ResourceLocation.fromNamespaceAndPath(AdvancedBotany.MOD_ID, "enabled"),
                    (stack, level, entity, seed) -> MaterialDestroyerItem.isEnabled(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.BLADE_OF_SPACE.get(), ResourceLocation.fromNamespaceAndPath(AdvancedBotany.MOD_ID, "enabled_mode"),
                    (stack, level, entity, seed) -> SpaceBladeItem.isEnabledMode(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.SPHERE_OF_NAVIGATION.get(), ResourceLocation.fromNamespaceAndPath(AdvancedBotany.MOD_ID, "active"),
                    (stack, level, entity, seed) -> SphereNavigationItem.isActive(stack) ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.SPHERE_OF_NAVIGATION.get(), ResourceLocation.fromNamespaceAndPath(AdvancedBotany.MOD_ID, "target"),
                    (stack, level, entity, seed) -> SphereNavigationItem.hasTarget(stack) ? 1.0F : 0.0F);
        });
    }

    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.NIDAVELLIR_FORGE.get(), NidavellirForgeScreen::new);
        event.register(ModMenuTypes.MAGIC_CRAFT_CRATE.get(), MagicCraftCrateScreen::new);
        event.register(ModMenuTypes.TALISMAN_HIDDEN_RICHES.get(), TalismanHiddenRichesScreen::new);
    }

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SWORD.get(), EmptyEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.NEBULA_BLAZE.get(), EmptyEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.ADVANCED_SPARK.get(), AdvancedSparkRenderer::new);
        event.registerEntityRenderer(ModEntities.MANA_VINE.get(), EmptyEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.ANONYMOUS_STEVE.get(), AnonymousSteveRenderer::new);
        event.registerEntityRenderer(ModEntities.ALPHIRINE_PORTAL.get(), AlphirinePortalRenderer::new);
        event.registerEntityRenderer(ModEntities.SEED.get(), EmptyEntityRenderer::new);
    }
}
