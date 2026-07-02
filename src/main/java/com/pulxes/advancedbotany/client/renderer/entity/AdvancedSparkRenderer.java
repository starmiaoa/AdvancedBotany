package com.pulxes.advancedbotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.pulxes.advancedbotany.common.entity.EntityAdvancedSpark;
import com.pulxes.advancedbotany.registry.ModItems;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class AdvancedSparkRenderer extends EntityRenderer<EntityAdvancedSpark> {
    private final ItemRenderer itemRenderer;

    public AdvancedSparkRenderer(EntityRendererProvider.Context context) {
        super(context);
        itemRenderer = context.getItemRenderer();
        shadowRadius = 0.0F;
    }

    @Override
    public void render(EntityAdvancedSpark entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        if (!entity.isInvisible()) {
            poseStack.pushPose();
            poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            poseStack.scale(0.65F, 0.65F, 0.65F);
            itemRenderer.renderStatic(
                    new ItemStack(ModItems.SUPERCONDUCTIVE_SPARK.get()),
                    ItemDisplayContext.GROUND,
                    packedLight,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    buffer,
                    entity.level(),
                    entity.getId());
            poseStack.popPose();
        }
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityAdvancedSpark entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
