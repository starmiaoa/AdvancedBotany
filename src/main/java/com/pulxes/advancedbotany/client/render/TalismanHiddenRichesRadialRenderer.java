package com.pulxes.advancedbotany.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.common.item.relic.TalismanHiddenRichesItem;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

public final class TalismanHiddenRichesRadialRenderer {
    private static final int SEGMENT_COUNT = TalismanHiddenRichesItem.CHEST_COUNT;
    private static final int MAX_SEGMENT_COUNT = 16;
    private static final float RADIUS = 3.2F;
    private static final float INNER_SCALE = 0.8F;
    private static final float TEXTURE_V = 0.25F;
    private static final float BAND_HEIGHT = TEXTURE_V * 6.0F;
    private static final ItemStack CHEST_PREVIEW = new ItemStack(Items.CHEST);
    private static final ResourceLocation GLOW = new ResourceLocation(AdvancedBotany.MOD_ID, "textures/misc/glow3.png");
    private static boolean renderFailureLogged;

    private TalismanHiddenRichesRadialRenderer() {
    }

    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        // AFTER_LEVEL matches the original RenderWorldLastEvent hook (same as the Black Halo
        // ring) so weather/particles never draw over the radial ring.
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }

        ItemStack stack = getHeldTalisman(mc.player);
        if (stack.isEmpty()) {
            return;
        }

        try {
            render(mc, event, stack);
            renderFailureLogged = false;
        } catch (Exception exception) {
            if (!renderFailureLogged) {
                AdvancedBotany.LOGGER.warn("Failed to render Key to Hidden Wealth radial preview", exception);
                renderFailureLogged = true;
            }
        }
    }

    private static ItemStack getHeldTalisman(LocalPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof TalismanHiddenRichesItem) {
            return mainHand;
        }
        ItemStack offhand = player.getOffhandItem();
        return offhand.getItem() instanceof TalismanHiddenRichesItem ? offhand : ItemStack.EMPTY;
    }

    private static void render(Minecraft mc, RenderLevelStageEvent event, ItemStack stack) {
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            return;
        }

        float partialTick = event.getPartialTick();
        float ticks = player.tickCount + partialTick;
        float alpha = (Mth.sin(ticks * 0.2F) * 0.5F + 0.5F) * 0.4F + 0.3F;
        float base = TalismanHiddenRichesItem.getRotationBase(stack);
        int segmentAngle = 360 / MAX_SEGMENT_COUNT;
        float shift = base - (float) segmentAngle / 2.0F * SEGMENT_COUNT;
        int selectedSegment = TalismanHiddenRichesItem.getSegmentLookedAt(stack, player);

        Camera camera = event.getCamera();
        Vec3 cameraPosition = camera.getPosition();
        double playerX = Mth.lerp(partialTick, player.xOld, player.getX());
        double playerY = Mth.lerp(partialTick, player.yOld, player.getY());
        double playerZ = Mth.lerp(partialTick, player.zOld, player.getZ());

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
        poseStack.pushPose();
        try {
            poseStack.translate(playerX - cameraPosition.x, playerY - cameraPosition.y, playerZ - cameraPosition.z);

            for (int segment = 0; segment < SEGMENT_COUNT; segment++) {
                renderGlowSegment(poseStack, buffer, segment, segmentAngle, shift, alpha, selectedSegment);
                renderChestPreview(mc, poseStack, buffer, segment, segmentAngle, shift, ticks);
            }
        } finally {
            poseStack.popPose();
            buffer.endBatch();
        }
    }

    private static void renderGlowSegment(PoseStack poseStack, MultiBufferSource buffer, int segment, int segmentAngle,
                                          float shift, float alpha, int selectedSegment) {
        float segmentAlpha = alpha;
        float outerY = 0.0F;
        if (selectedSegment == segment) {
            segmentAlpha += 0.3F;
            outerY = -BAND_HEIGHT;
        }

        float color = segment % 2 == 0 ? 0.6F : 1.0F;
        int colorComponent = Math.round(color * 255.0F);
        int alphaComponent = Math.round(Mth.clamp(segmentAlpha, 0.0F, 1.0F) * 255.0F);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucentEmissive(GLOW));

        poseStack.pushPose();
        try {
            poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            Matrix4f matrix = poseStack.last().pose();
            for (int i = 0; i < segmentAngle; i++) {
                float angle = i + segment * segmentAngle + shift;
                double x0 = Math.cos(angle * Math.PI / 180.0D) * RADIUS;
                double z0 = Math.sin(angle * Math.PI / 180.0D) * RADIUS;
                double x1 = Math.cos((angle + 1.0F) * Math.PI / 180.0D) * RADIUS;
                double z1 = Math.sin((angle + 1.0F) * Math.PI / 180.0D) * RADIUS;

                vertex(consumer, matrix, x0 * INNER_SCALE, BAND_HEIGHT, z0 * INNER_SCALE, 1.0F, TEXTURE_V,
                        colorComponent, alphaComponent);
                vertex(consumer, matrix, x0, outerY, z0, 1.0F, 0.0F, colorComponent, alphaComponent);
                vertex(consumer, matrix, x1, outerY, z1, 0.0F, 0.0F, colorComponent, alphaComponent);
                vertex(consumer, matrix, x1 * INNER_SCALE, BAND_HEIGHT, z1 * INNER_SCALE, 0.0F, TEXTURE_V,
                        colorComponent, alphaComponent);
            }
        } finally {
            poseStack.popPose();
        }
    }

    private static void renderChestPreview(Minecraft mc, PoseStack poseStack, MultiBufferSource buffer, int segment,
                                           int segmentAngle, float shift, float ticks) {
        float rotationAngle = (segment + 0.5F) * segmentAngle + shift;
        double worldTime = ticks + segment * 2.75F;
        poseStack.pushPose();
        try {
            poseStack.mulPose(Axis.YP.rotationDegrees(rotationAngle));
            poseStack.translate(RADIUS * INNER_SCALE + 0.35D, -0.45D + Math.sin(worldTime / 8.0D) / 20.0D, 0.0D);
            poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
            poseStack.scale(0.8F, 0.8F, 0.8F);
            mc.getItemRenderer().renderStatic(CHEST_PREVIEW, ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY, poseStack, buffer, mc.level, segment);
        } finally {
            poseStack.popPose();
        }
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, double x, double y, double z, float u, float v,
                               int colorComponent, int alphaComponent) {
        consumer.vertex(matrix, (float) x, (float) y, (float) z)
                .color(colorComponent, colorComponent, colorComponent, alphaComponent)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
    }
}
