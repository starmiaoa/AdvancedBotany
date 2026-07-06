package com.pulxes.advancedbotany.client;

import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.client.model.DiceFateModel;
import com.pulxes.advancedbotany.client.renderer.blockentity.BoardFateBlockEntityRenderer;
import com.pulxes.advancedbotany.client.renderer.blockentity.GameBoardBlockEntityRenderer;
import com.pulxes.advancedbotany.registry.ModBlockEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = AdvancedBotany.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class PlayingBoardClientEvents {
    private PlayingBoardClientEvents() {
    }

    @SubscribeEvent
    public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.PLAYING_BOARD.get(), GameBoardBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FATE_PLAYING_BOARD.get(), BoardFateBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(DiceFateModel.LAYER, DiceFateModel::createLayer);
    }
}

