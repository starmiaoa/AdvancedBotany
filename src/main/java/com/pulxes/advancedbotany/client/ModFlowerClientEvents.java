package com.pulxes.advancedbotany.client;

import net.neoforged.api.distmarker.Dist;
import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.common.block.entity.LebethronNaturalCoreBlockEntity;
import com.pulxes.advancedbotany.common.block.entity.ManaChargerBlockEntity;
import com.pulxes.advancedbotany.common.block.entity.ManaContainerBlockEntity;
import com.pulxes.advancedbotany.common.block.entity.ManaCrystalCubeBlockEntity;
import com.pulxes.advancedbotany.common.block.entity.NaturalManaSpreaderBlockEntity;
import com.pulxes.advancedbotany.registry.ModBlockEntities;
import com.pulxes.advancedbotany.registry.ModFlowers;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.block.WandHUD;
import vazkii.botania.api.block_entity.BindableSpecialFlowerBlockEntity;
import vazkii.botania.api.block_entity.FunctionalFlowerBlockEntity;
import vazkii.botania.api.block_entity.GeneratingFlowerBlockEntity;
import vazkii.botania.client.core.helper.RenderHelper;
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
        event.registerBlockEntity(wandHud, ModBlockEntities.MANA_CONTAINER.get(),
                (be, context) -> (guiGraphics, window, font, partialTick) -> renderManaHud(guiGraphics, window, font, be, 0x0095FF,
                        be.getCurrentMana(), be.getMaxMana()));
        event.registerBlockEntity(wandHud, ModBlockEntities.MANA_CRYSTAL_CUBE.get(),
                (be, context) -> (guiGraphics, window, font, partialTick) -> renderManaHud(guiGraphics, window, font, be, 0x66B2FF,
                        Math.max(0, be.getKnownMana()), Math.max(0, be.getKnownMaxMana())));
        event.registerBlockEntity(wandHud, ModBlockEntities.NATURAL_MANA_SPREADER.get(),
                (be, context) -> (guiGraphics, window, font, partialTick) -> renderSpreaderHud(guiGraphics, window, font, be));
        event.registerBlockEntity(wandHud, ModBlockEntities.MANA_CHARGER.get(),
                (be, context) -> (guiGraphics, window, font, partialTick) -> renderBoundBlockHud(guiGraphics, window, font, be, be.getBinding()));
        event.registerBlockEntity(wandHud, ModBlockEntities.LEBETHRON_NATURAL_CORE.get(),
                (be, context) -> (guiGraphics, window, font, partialTick) -> renderLebethronCoreHud(guiGraphics, window, font, be));
    }

    private static void renderManaHud(GuiGraphics guiGraphics, Window window, Font font, BlockEntity blockEntity, int color, int mana, int maxMana) {
        String name = new ItemStack(blockEntity.getBlockState().getBlock()).getHoverName().getString();
        int centerX = window.getGuiScaledWidth() / 2;
        int centerY = window.getGuiScaledHeight() / 2;
        int width = Math.max(102, font.width(name)) + 4;
        RenderHelper.renderHUDBox(guiGraphics, centerX - width / 2, centerY + 8, centerX + width / 2, centerY + 48);
        BotaniaAPIClient.instance().drawSimpleManaHUD(guiGraphics, window, font, color, mana, maxMana, name);
    }

    private static void renderSpreaderHud(GuiGraphics guiGraphics, Window window, Font font, NaturalManaSpreaderBlockEntity spreader) {
        renderManaHud(guiGraphics, window, font, spreader, 0x007D30, spreader.getCurrentMana(), spreader.getMaxMana());
        int centerY = window.getGuiScaledHeight() / 2;
        ItemStack target = getBoundBlockStack(spreader, spreader.getBinding());
        if (!target.isEmpty()) {
            RenderHelper.renderItemWithNameCentered(guiGraphics, window, font, target, centerY + 50, 0x007D30);
        }
        ItemStack lens = spreader.getLens();
        if (!lens.isEmpty()) {
            RenderHelper.renderItemWithNameCentered(guiGraphics, window, font, lens, target.isEmpty() ? centerY + 50 : centerY + 68, 0x007D30);
        }
    }

    private static void renderBoundBlockHud(GuiGraphics guiGraphics, Window window, Font font, BlockEntity source, BlockPos binding) {
        ItemStack sourceStack = new ItemStack(source.getBlockState().getBlock());
        ItemStack target = getBoundBlockStack(source, binding);
        int centerX = window.getGuiScaledWidth() / 2;
        int centerY = window.getGuiScaledHeight() / 2;
        int width = Math.max(RenderHelper.itemWithNameWidth(sourceStack, font), RenderHelper.itemWithNameWidth(target, font)) + 8;
        int height = target.isEmpty() ? 32 : 50;
        RenderHelper.renderHUDBox(guiGraphics, centerX - width / 2, centerY + 8, centerX + width / 2, centerY + 8 + height);
        RenderHelper.renderItemWithNameCentered(guiGraphics, window, font, sourceStack, centerY + 12, 0x66FF66);
        RenderHelper.renderItemWithNameCentered(guiGraphics, window, font, target, centerY + 30, 0x66FF66);
    }

    private static void renderLebethronCoreHud(GuiGraphics guiGraphics, Window window, Font font, LebethronNaturalCoreBlockEntity core) {
        ItemStack coreStack = new ItemStack(core.getBlockState().getBlock());
        ItemStack leafStack = core.getLeafState() == null ? ItemStack.EMPTY : new ItemStack(core.getLeafState().getBlock());
        int centerX = window.getGuiScaledWidth() / 2;
        int centerY = window.getGuiScaledHeight() / 2;
        int width = Math.max(RenderHelper.itemWithNameWidth(coreStack, font), RenderHelper.itemWithNameWidth(leafStack, font)) + 8;
        RenderHelper.renderHUDBox(guiGraphics, centerX - width / 2, centerY + 8, centerX + width / 2, centerY + (leafStack.isEmpty() ? 40 : 58));
        RenderHelper.renderItemWithNameCentered(guiGraphics, window, font, coreStack, centerY + 12, core.getValidTree() ? 0x66FF66 : 0xFFAA00);
        RenderHelper.renderItemWithNameCentered(guiGraphics, window, font, leafStack, centerY + 30, 0x66FF66);
    }

    private static ItemStack getBoundBlockStack(BlockEntity source, BlockPos binding) {
        if (source.getLevel() == null || binding == null) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(source.getLevel().getBlockState(binding).getBlock());
    }
}
