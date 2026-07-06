package com.pulxes.advancedbotany.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.client.model.DiceFateModel;
import com.pulxes.advancedbotany.common.block.entity.BoardFateBlockEntity;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class BoardFateBlockEntityRenderer implements BlockEntityRenderer<BoardFateBlockEntity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(AdvancedBotany.MOD_ID, "textures/block/model/dicefate.png");

    private final DiceFateModel model;

    public BoardFateBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new DiceFateModel(context.bakeLayer(DiceFateModel.LAYER));
    }

    @Override
    public void render(BoardFateBlockEntity board, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, int packedOverlay) {
        double time = board.getLevel() == null ? 0.0D : board.getLevel().getGameTime() + partialTick;
        time += new Random(board.getBlockPos().getX() ^ board.getBlockPos().getY() ^ board.getBlockPos().getZ()).nextInt(360);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F - Minecraft.getInstance().gameRenderer.getMainCamera().getYRot()));

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        for (int i = 0; i < board.getContainerSize(); i++) {
            if (board.getItem(i).isEmpty()) {
                continue;
            }
            renderDice(board.slotChance[i], board.clientTick[i], i, time + i * 83.256F, partialTick, poseStack,
                    consumer, packedLight, OverlayTexture.NO_OVERLAY);
        }

        poseStack.popPose();
    }

    private void renderDice(byte chance, int clientTick, int index, double time, float partialTick, PoseStack poseStack,
            VertexConsumer consumer, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        float dropAnim = 1.0F - Math.min(150.0F, clientTick * clientTick * 1.42F + partialTick) / 150.0F;
        dropAnim = Mth.clamp(dropAnim, 0.0F, 1.0F);
        float alpha = (float) Math.cos(dropAnim);
        float pos = index == 0 ? 0.16F + 0.08F * dropAnim : -0.16F - 0.08F * dropAnim;

        poseStack.translate(pos, 0.02F + Math.sin(time / 12.0D) / 48.0D + 0.28F * dropAnim, pos);
        poseStack.scale(0.25F, 0.25F, 0.25F);
        renderChance(chance, dropAnim, alpha, poseStack, consumer, packedLight, packedOverlay);

        poseStack.popPose();
    }

    private void renderChance(byte chance, float dropAnim, float alpha, PoseStack poseStack, VertexConsumer consumer,
            int packedLight, int packedOverlay) {
        float dropAngle = 70.0F * dropAnim;
        switch (chance) {
            case 1 -> model.render(poseStack, consumer, packedLight, packedOverlay, 180.0F + dropAngle, dropAngle, 0.0F, alpha);
            case 2 -> model.render(poseStack, consumer, packedLight, packedOverlay, dropAngle, dropAngle, 0.0F, alpha);
            case 3 -> model.render(poseStack, consumer, packedLight, packedOverlay, 90.0F + dropAngle, dropAngle, 0.0F, alpha);
            case 4 -> model.render(poseStack, consumer, packedLight, packedOverlay, 270.0F + dropAngle, dropAngle, 0.0F, alpha);
            case 5 -> model.render(poseStack, consumer, packedLight, packedOverlay, dropAngle, dropAngle, 270.0F, alpha);
            case 6 -> model.render(poseStack, consumer, packedLight, packedOverlay, dropAngle, dropAngle, 90.0F, alpha);
            default -> model.render(poseStack, consumer, packedLight, packedOverlay, dropAngle, dropAngle, 0.0F, alpha);
        }
    }
}
