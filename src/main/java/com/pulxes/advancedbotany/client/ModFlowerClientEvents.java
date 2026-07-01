package com.pulxes.advancedbotany.client;

import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.registry.ModFlowers;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.block.WandHUD;
import vazkii.botania.api.block_entity.BindableSpecialFlowerBlockEntity;
import vazkii.botania.api.block_entity.FunctionalFlowerBlockEntity;
import vazkii.botania.api.block_entity.GeneratingFlowerBlockEntity;
import vazkii.botania.client.render.block_entity.SpecialFlowerBlockEntityRenderer;
import vazkii.botania.forge.CapabilityUtil;

@Mod.EventBusSubscriber(modid = AdvancedBotany.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ModFlowerClientEvents {
    private static final ResourceLocation WAND_HUD = new ResourceLocation(AdvancedBotany.MOD_ID, "flower_wand_hud");

    private ModFlowerClientEvents() {
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ModFlowers.flowerBlocks().forEach(
                block -> ItemBlockRenderTypes.setRenderLayer(block.get(), RenderType.cutout())));
        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, ModFlowerClientEvents::attachBlockEntityCapabilities);
    }

    @SubscribeEvent
    public static void registerBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModFlowers.ANCIENT_ALPHIRINE_BLOCK_ENTITY.get(), SpecialFlowerBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModFlowers.ARDENT_AZARCISSUS_BLOCK_ENTITY.get(), SpecialFlowerBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModFlowers.DICTARIUS_BLOCK_ENTITY.get(), SpecialFlowerBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModFlowers.ASPECOLUS_BLOCK_ENTITY.get(), SpecialFlowerBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModFlowers.PURE_GLADIOLUS_BLOCK_ENTITY.get(), SpecialFlowerBlockEntityRenderer::new);
    }

    private static void attachBlockEntityCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
        BlockEntity blockEntity = event.getObject();
        ResourceLocation id = ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(blockEntity.getType());
        if (id == null || !AdvancedBotany.MOD_ID.equals(id.getNamespace())) {
            return;
        }

        if (blockEntity instanceof FunctionalFlowerBlockEntity flower) {
            WandHUD hud = new BindableSpecialFlowerBlockEntity.BindableFlowerWandHud<>(flower);
            event.addCapability(WAND_HUD, CapabilityUtil.makeProvider(BotaniaForgeClientCapabilities.WAND_HUD, hud));
        } else if (blockEntity instanceof GeneratingFlowerBlockEntity flower) {
            WandHUD hud = new BindableSpecialFlowerBlockEntity.BindableFlowerWandHud<>(flower);
            event.addCapability(WAND_HUD, CapabilityUtil.makeProvider(BotaniaForgeClientCapabilities.WAND_HUD, hud));
        }
    }
}
