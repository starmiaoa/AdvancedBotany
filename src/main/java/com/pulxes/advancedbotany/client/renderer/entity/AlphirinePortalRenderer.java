package com.pulxes.advancedbotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.common.entity.EntityAlphirinePortal;
import java.util.Random;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class AlphirinePortalRenderer extends EntityRenderer<EntityAlphirinePortal> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AdvancedBotany.MOD_ID, "textures/entity/alphirine_portal.png");
    private static final int FRAME_COUNT = 16;
    private static final int FRAME_TIME = 3;

    public AlphirinePortalRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(EntityAlphirinePortal entity, float yaw, float partialTicks, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        double time = entity.tickCount + partialTicks;
        time += new Random((long) ((int) entity.getX() ^ (int) entity.getY() ^ (int) entity.getZ())).nextInt(360);

        float burn = Math.min(1.0F, (entity.tickCount + partialTicks) * 0.0561F);
        burn = Math.max(0.0F, (float) (burn + Math.sin(time / 3.2D) / 9.0D));

        poseStack.pushPose();
        float scale = burn / 3.15F;
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) time * 2.0F));

        renderQuad(poseStack, buffer, Mth.clamp(burn, 0.0F, 1.0F), time);
        poseStack.popPose();

        super.render(entity, yaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static void renderQuad(PoseStack poseStack, MultiBufferSource buffer, float alpha, double time) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        int frame = (int) ((time / FRAME_TIME) % FRAME_COUNT);
        float frameHeight = 1.0F / FRAME_COUNT;
        float minV = frame * frameHeight;
        float maxV = minV + frameHeight;

        addVertex(consumer, poseStack, -0.5F, -0.5F, 0.0F, 0.0F, minV, alpha);
        addVertex(consumer, poseStack, 0.5F, -0.5F, 0.0F, 1.0F, minV, alpha);
        addVertex(consumer, poseStack, 0.5F, 0.5F, 0.0F, 1.0F, maxV, alpha);
        addVertex(consumer, poseStack, -0.5F, 0.5F, 0.0F, 0.0F, maxV, alpha);

        addVertex(consumer, poseStack, -0.5F, 0.5F, 0.0F, 0.0F, maxV, alpha);
        addVertex(consumer, poseStack, 0.5F, 0.5F, 0.0F, 1.0F, maxV, alpha);
        addVertex(consumer, poseStack, 0.5F, -0.5F, 0.0F, 1.0F, minV, alpha);
        addVertex(consumer, poseStack, -0.5F, -0.5F, 0.0F, 0.0F, minV, alpha);
    }

    private static void addVertex(VertexConsumer consumer, PoseStack poseStack, float x, float y, float z, float u,
            float v, float alpha) {
        consumer.addVertex(poseStack.last().pose(), x, y, z)
                .setColor(1.0F, 1.0F, 1.0F, alpha)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT)
                .setNormal(poseStack.last(), 0.0F, 0.0F, 1.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityAlphirinePortal entity) {
        return TEXTURE;
    }
}
