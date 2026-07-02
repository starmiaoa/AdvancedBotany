package com.pulxes.advancedbotany.client;

import net.neoforged.api.distmarker.Dist;
import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.registry.ModFlowers;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.block.WandHUD;
import vazkii.botania.api.block_entity.BindableSpecialFlowerBlockEntity;
import vazkii.botania.api.block_entity.FunctionalFlowerBlockEntity;
import vazkii.botania.api.block_entity.GeneratingFlowerBlockEntity;
import vazkii.botania.client.render.block_entity.SpecialFlowerBlockEntityRenderer;

@EventBusSubscriber(modid = AdvancedBotany.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ModFlowerClientEvents {
    private ModFlowerClientEvents() {
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ModFlowers.flowerBlocks().forEach(
                block -> ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.cutout())));
    }

    @SubscribeEvent
    public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModFlowers.ANCIENT_ALPHIRINE_BLOCK_ENTITY.get(), SpecialFlowerBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModFlowers.ARDENT_AZARCISSUS_BLOCK_ENTITY.get(), SpecialFlowerBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModFlowers.DICTARIUS_BLOCK_ENTITY.get(), SpecialFlowerBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModFlowers.ASPECOLUS_BLOCK_ENTITY.get(), SpecialFlowerBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModFlowers.PURE_GLADIOLUS_BLOCK_ENTITY.get(), SpecialFlowerBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        var wandHud = BotaniaForgeCapabilities.getBlockApiLookupById(WandHUD.BLOCK_LOOKUP);
        event.registerBlockEntity(wandHud, ModFlowers.ANCIENT_ALPHIRINE_BLOCK_ENTITY.get(),
                (be, context) -> new BindableSpecialFlowerBlockEntity.BindableFlowerWandHud<>((FunctionalFlowerBlockEntity) be));
        event.registerBlockEntity(wandHud, ModFlowers.ARDENT_AZARCISSUS_BLOCK_ENTITY.get(),
                (be, context) -> new BindableSpecialFlowerBlockEntity.BindableFlowerWandHud<>((GeneratingFlowerBlockEntity) be));
        event.registerBlockEntity(wandHud, ModFlowers.DICTARIUS_BLOCK_ENTITY.get(),
                (be, context) -> new BindableSpecialFlowerBlockEntity.BindableFlowerWandHud<>((GeneratingFlowerBlockEntity) be));
        event.registerBlockEntity(wandHud, ModFlowers.ASPECOLUS_BLOCK_ENTITY.get(),
                (be, context) -> new BindableSpecialFlowerBlockEntity.BindableFlowerWandHud<>((FunctionalFlowerBlockEntity) be));
        event.registerBlockEntity(wandHud, ModFlowers.PURE_GLADIOLUS_BLOCK_ENTITY.get(),
                (be, context) -> new BindableSpecialFlowerBlockEntity.BindableFlowerWandHud<>((FunctionalFlowerBlockEntity) be));
    }
}
