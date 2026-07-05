package com.pulxes.advancedbotany.client;

import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.common.block.entity.LebethronNaturalCoreBlockEntity;
import com.pulxes.advancedbotany.common.block.entity.ManaChargerBlockEntity;
import com.pulxes.advancedbotany.common.block.entity.ManaContainerBlockEntity;
import com.pulxes.advancedbotany.common.block.entity.ManaCrystalCubeBlockEntity;
import com.pulxes.advancedbotany.common.block.entity.NaturalManaSpreaderBlockEntity;
import com.pulxes.advancedbotany.registry.ModFlowers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.api.BotaniaForgeClientCapabilities;
import vazkii.botania.api.block.WandHUD;
import vazkii.botania.api.block_entity.BindableSpecialFlowerBlockEntity;
import vazkii.botania.api.block_entity.FunctionalFlowerBlockEntity;
import vazkii.botania.api.block_entity.GeneratingFlowerBlockEntity;
import vazkii.botania.client.core.helper.RenderHelper;
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
        } else if (blockEntity instanceof ManaContainerBlockEntity container) {
            event.addCapability(WAND_HUD, CapabilityUtil.makeProvider(
                    BotaniaForgeClientCapabilities.WAND_HUD,
                    (WandHUD) (guiGraphics, minecraft) -> renderManaHud(guiGraphics, minecraft, container, 0x0095FF,
                            container.getCurrentMana(), container.getMaxMana())));
        } else if (blockEntity instanceof ManaCrystalCubeBlockEntity cube) {
            event.addCapability(WAND_HUD, CapabilityUtil.makeProvider(
                    BotaniaForgeClientCapabilities.WAND_HUD,
                    (WandHUD) (guiGraphics, minecraft) -> renderManaHud(guiGraphics, minecraft, cube, 0x66B2FF,
                            Math.max(0, cube.getKnownMana()), Math.max(0, cube.getKnownMaxMana()))));
        } else if (blockEntity instanceof NaturalManaSpreaderBlockEntity spreader) {
            event.addCapability(WAND_HUD, CapabilityUtil.makeProvider(
                    BotaniaForgeClientCapabilities.WAND_HUD,
                    (WandHUD) (guiGraphics, minecraft) -> renderSpreaderHud(guiGraphics, minecraft, spreader)));
        } else if (blockEntity instanceof ManaChargerBlockEntity charger) {
            event.addCapability(WAND_HUD, CapabilityUtil.makeProvider(
                    BotaniaForgeClientCapabilities.WAND_HUD,
                    (WandHUD) (guiGraphics, minecraft) -> renderBoundBlockHud(guiGraphics, minecraft, charger, charger.getBinding())));
        } else if (blockEntity instanceof LebethronNaturalCoreBlockEntity core) {
            event.addCapability(WAND_HUD, CapabilityUtil.makeProvider(
                    BotaniaForgeClientCapabilities.WAND_HUD,
                    (WandHUD) (guiGraphics, minecraft) -> renderLebethronCoreHud(guiGraphics, minecraft, core)));
        }
    }

    private static void renderManaHud(GuiGraphics guiGraphics, Minecraft minecraft, BlockEntity blockEntity, int color, int mana, int maxMana) {
        String name = new ItemStack(blockEntity.getBlockState().getBlock()).getHoverName().getString();
        int centerX = minecraft.getWindow().getGuiScaledWidth() / 2;
        int centerY = minecraft.getWindow().getGuiScaledHeight() / 2;
        int width = Math.max(102, minecraft.font.width(name)) + 4;
        RenderHelper.renderHUDBox(guiGraphics, centerX - width / 2, centerY + 8, centerX + width / 2, centerY + 48);
        BotaniaAPIClient.instance().drawSimpleManaHUD(guiGraphics, color, mana, maxMana, name);
    }

    private static void renderSpreaderHud(GuiGraphics guiGraphics, Minecraft minecraft, NaturalManaSpreaderBlockEntity spreader) {
        renderManaHud(guiGraphics, minecraft, spreader, 0x007D30, spreader.getCurrentMana(), spreader.getMaxMana());
        int centerY = minecraft.getWindow().getGuiScaledHeight() / 2;
        ItemStack target = getBoundBlockStack(spreader, spreader.getBinding());
        if (!target.isEmpty()) {
            RenderHelper.renderItemWithNameCentered(guiGraphics, minecraft, target, centerY + 50, 0x007D30);
        }
        ItemStack lens = spreader.getLens();
        if (!lens.isEmpty()) {
            RenderHelper.renderItemWithNameCentered(guiGraphics, minecraft, lens, target.isEmpty() ? centerY + 50 : centerY + 68, 0x007D30);
        }
    }

    private static void renderBoundBlockHud(GuiGraphics guiGraphics, Minecraft minecraft, BlockEntity source, BlockPos binding) {
        ItemStack sourceStack = new ItemStack(source.getBlockState().getBlock());
        ItemStack target = getBoundBlockStack(source, binding);
        int centerX = minecraft.getWindow().getGuiScaledWidth() / 2;
        int centerY = minecraft.getWindow().getGuiScaledHeight() / 2;
        int width = Math.max(RenderHelper.itemWithNameWidth(minecraft, sourceStack), RenderHelper.itemWithNameWidth(minecraft, target)) + 8;
        int height = target.isEmpty() ? 32 : 50;
        RenderHelper.renderHUDBox(guiGraphics, centerX - width / 2, centerY + 8, centerX + width / 2, centerY + 8 + height);
        RenderHelper.renderItemWithNameCentered(guiGraphics, minecraft, sourceStack, centerY + 12, 0x66FF66);
        RenderHelper.renderItemWithNameCentered(guiGraphics, minecraft, target, centerY + 30, 0x66FF66);
    }

    private static void renderLebethronCoreHud(GuiGraphics guiGraphics, Minecraft minecraft, LebethronNaturalCoreBlockEntity core) {
        ItemStack coreStack = new ItemStack(core.getBlockState().getBlock());
        ItemStack leafStack = core.getLeafState() == null ? ItemStack.EMPTY : new ItemStack(core.getLeafState().getBlock());
        int centerX = minecraft.getWindow().getGuiScaledWidth() / 2;
        int centerY = minecraft.getWindow().getGuiScaledHeight() / 2;
        int width = Math.max(RenderHelper.itemWithNameWidth(minecraft, coreStack), RenderHelper.itemWithNameWidth(minecraft, leafStack)) + 8;
        RenderHelper.renderHUDBox(guiGraphics, centerX - width / 2, centerY + 8, centerX + width / 2, centerY + (leafStack.isEmpty() ? 40 : 58));
        RenderHelper.renderItemWithNameCentered(guiGraphics, minecraft, coreStack, centerY + 12, core.getValidTree() ? 0x66FF66 : 0xFFAA00);
        RenderHelper.renderItemWithNameCentered(guiGraphics, minecraft, leafStack, centerY + 30, 0x66FF66);
    }

    private static ItemStack getBoundBlockStack(BlockEntity source, BlockPos binding) {
        if (source.getLevel() == null || binding == null) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(source.getLevel().getBlockState(binding).getBlock());
    }
}
