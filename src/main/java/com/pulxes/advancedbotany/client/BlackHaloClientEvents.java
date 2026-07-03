package com.pulxes.advancedbotany.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.common.item.equipment.BlackHaloItem;
import com.pulxes.advancedbotany.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.item.BlackHoleTalismanItem;

@Mod.EventBusSubscriber(modid = AdvancedBotany.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class BlackHaloClientEvents {
    private static final ResourceLocation GLOW_TEXTURE =
            new ResourceLocation(AdvancedBotany.MOD_ID, "textures/misc/glow.png");
    private static final int SEGMENT_ANGLE = 360 / BlackHaloItem.SLOT_COUNT;

    private BlackHaloClientEvents() {
    }

    @SubscribeEvent
    public static void renderHud(RenderGuiOverlayEvent.Post event) {
        if (!event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.options.hideGui) {
            return;
        }

        ItemStack halo = player.getMainHandItem();
        if (!halo.is(ModItems.BLACK_HOLE_BOX.get())) {
            return;
        }

        ItemStack talisman = BlackHaloItem.getItemForSlot(halo, BlackHaloItem.getSegmentLookedAt(halo, player));
        if (talisman.isEmpty()) {
            return;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        Font font = minecraft.font;
        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();
        String name = talisman.getHoverName().getString();
        String count = String.valueOf(BlackHoleTalismanItem.getBlockCount(talisman));
        int width = font.width(name);
        int x = screenWidth / 2 - width / 2;
        int y = screenHeight / 2 - 65;

        guiGraphics.fill(x - 6, y - 6, x + width + 6, y + 43, 0x22000000);
        guiGraphics.fill(x - 4, y - 4, x + width + 4, y + 41, 0x22000000);
        guiGraphics.renderItem(talisman, screenWidth / 2 - 8, screenHeight / 2 - 52);
        guiGraphics.drawString(font, name, x, y, 0xFFFFFF, false);
        guiGraphics.drawString(font, count, screenWidth / 2 - font.width(count) / 2, y + 32, 0xFFFFFF, false);
    }

    @SubscribeEvent
    public static void renderWorld(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null) {
            return;
        }

        ItemStack halo = player.getMainHandItem();
        if (!halo.is(ModItems.BLACK_HOLE_BOX.get())) {
            return;
        }

        float partialTick = event.getPartialTick();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();
        RenderType glowType = RenderHelper.getHaloLayer(GLOW_TEXTURE);
        float alpha = ((float) Math.sin((ClientTickHandler.ticksInGame + partialTick) * 0.2F) * 0.5F + 0.5F)
                * 0.4F + 0.3F;

        double cameraX = event.getCamera().getPosition().x;
        double cameraY = event.getCamera().getPosition().y;
        double cameraZ = event.getCamera().getPosition().z;
        double x = player.xo + (player.getX() - player.xo) * partialTick;
        double y = player.yo + (player.getY() - player.yo) * partialTick;
        double z = player.zo + (player.getZ() - player.zo) * partialTick;

        poseStack.pushPose();
        poseStack.translate(x - cameraX, y - cameraY, z - cameraZ);

        float base = BlackHaloItem.getRotationBase(halo);
        float shift = base - SEGMENT_ANGLE / 2.0F;
        float outerRadius = 3.0F;
        float innerRadiusFactor = 0.8F;
        float ringHeight = 0.25F * outerRadius * 2.0F;
        float lowerY = 0.0F;
        int selectedSegment = BlackHaloItem.getSegmentLookedAt(halo, player);

        for (int segment = 0; segment < BlackHaloItem.SLOT_COUNT; segment++) {
            boolean selected = segment == selectedSegment;
            float rotationAngle = (segment + 0.5F) * SEGMENT_ANGLE + shift;

            poseStack.pushPose();
            poseStack.mulPose(Axis.YP.rotationDegrees(rotationAngle));
            double worldTime = ClientTickHandler.ticksInGame + partialTick + segment * 2.75F;
            poseStack.translate(outerRadius * innerRadiusFactor, -0.75D + Math.sin(worldTime / 12.0D) / 30.0D, 0.0D);
            renderStoredBlock(minecraft, poseStack, buffer, halo, segment);
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            float segmentAlpha = selected ? alpha + 0.3F : alpha;
            float red = segment % 2 == 0 ? 0.6F : 1.0F;
            float green = segment % 2 == 0 ? 0.6F : 1.0F;
            float blue = segment % 2 == 0 ? 0.6F : 1.0F;
            if (selected) {
                lowerY = -ringHeight;
            }
            renderGlowSegment(
                    buffer.getBuffer(glowType),
                    poseStack.last().pose(),
                    segment,
                    shift,
                    outerRadius,
                    innerRadiusFactor,
                    ringHeight,
                    lowerY,
                    red,
                    green,
                    blue,
                    segmentAlpha);
            lowerY = 0.0F;
            poseStack.popPose();
        }

        poseStack.popPose();
        buffer.endBatch();
    }

    private static void renderStoredBlock(Minecraft minecraft, PoseStack poseStack, MultiBufferSource buffer,
            ItemStack halo, int segment) {
        ItemStack talisman = BlackHaloItem.getItemForSlot(halo, segment);
        if (!(talisman.getItem() instanceof BlackHoleTalismanItem)) {
            return;
        }

        Block block = BlackHoleTalismanItem.getBlock(talisman);
        if (block == null || block == Blocks.AIR) {
            return;
        }

        poseStack.pushPose();
        poseStack.scale(0.6F, 0.6F, 0.6F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.translate(-0.5F, 0.1F, -0.5F);
        minecraft.getBlockRenderer().renderSingleBlock(
                block.defaultBlockState(),
                poseStack,
                buffer,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    private static void renderGlowSegment(VertexConsumer consumer, Matrix4f matrix, int segment, float shift,
            float outerRadius, float innerRadiusFactor, float ringHeight, float lowerY, float red, float green,
            float blue, float alpha) {
        for (int i = 0; i < SEGMENT_ANGLE; i++) {
            float angle = i + segment * SEGMENT_ANGLE + shift;
            float x0 = (float) Math.cos(angle * Math.PI / 180.0D) * outerRadius;
            float z0 = (float) Math.sin(angle * Math.PI / 180.0D) * outerRadius;
            float x1 = (float) Math.cos((angle + 1.0F) * Math.PI / 180.0D) * outerRadius;
            float z1 = (float) Math.sin((angle + 1.0F) * Math.PI / 180.0D) * outerRadius;

            addGlowVertex(consumer, matrix, x0 * innerRadiusFactor, ringHeight, z0 * innerRadiusFactor,
                    red, green, blue, alpha, 1.0F, 0.25F);
            addGlowVertex(consumer, matrix, x0, lowerY, z0, red, green, blue, alpha, 1.0F, 0.0F);
            addGlowVertex(consumer, matrix, x1, lowerY, z1, red, green, blue, alpha, 0.0F, 0.0F);
            addGlowVertex(consumer, matrix, x1 * innerRadiusFactor, ringHeight, z1 * innerRadiusFactor,
                    red, green, blue, alpha, 0.0F, 0.25F);
        }
    }

    private static void addGlowVertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float z,
            float red, float green, float blue, float alpha, float u, float v) {
        consumer.vertex(matrix, x, y, z)
                .color(red, green, blue, alpha)
                .uv(u, v)
                .endVertex();
    }
}
