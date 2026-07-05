package com.pulxes.advancedbotany.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.pulxes.advancedbotany.common.entity.EntityAdvancedSpark;
import com.pulxes.advancedbotany.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import vazkii.botania.api.mana.spark.SparkUpgradeType;
import vazkii.botania.client.core.helper.RenderHelper;

public class AdvancedSparkRenderer extends EntityRenderer<EntityAdvancedSpark> {
    private final ItemRenderer itemRenderer;
    private final TextureAtlasSprite dispersiveIcon;
    private final TextureAtlasSprite dominantIcon;
    private final TextureAtlasSprite recessiveIcon;
    private final TextureAtlasSprite isolatedIcon;

    public AdvancedSparkRenderer(EntityRendererProvider.Context context) {
        super(context);
        itemRenderer = context.getItemRenderer();
        var atlas = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
        dispersiveIcon = atlas.apply(new ResourceLocation("botania", "item/spark_upgrade_rune_dispersive"));
        dominantIcon = atlas.apply(new ResourceLocation("botania", "item/spark_upgrade_rune_dominant"));
        recessiveIcon = atlas.apply(new ResourceLocation("botania", "item/spark_upgrade_rune_recessive"));
        isolatedIcon = atlas.apply(new ResourceLocation("botania", "item/spark_upgrade_rune_isolated"));
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
            renderUpgrade(entity, partialTick, poseStack, buffer);
            poseStack.popPose();
        }
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    private void renderUpgrade(EntityAdvancedSpark entity, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer) {
        TextureAtlasSprite icon = upgradeIcon(entity.getUpgrade());
        if (icon == null) {
            return;
        }

        double time = entity.level().getGameTime() % 24000L + partialTick + new java.util.Random(entity.getId()).nextInt(200);
        poseStack.pushPose();
        poseStack.translate(
                -0.02D + Math.sin(time / 20.0D) * -0.2D,
                0.24D + Math.cos(time / 20.0D) * -0.2D,
                0.005D);
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) (time * 4.0D)));
        poseStack.scale(0.2F, 0.2F, 0.2F);
        renderIcon(poseStack, buffer.getBuffer(RenderHelper.SPARK), icon, 0xFFFFFFFF);
        poseStack.popPose();
    }

    private TextureAtlasSprite upgradeIcon(SparkUpgradeType upgrade) {
        return switch (upgrade) {
            case DISPERSIVE -> dispersiveIcon;
            case DOMINANT -> dominantIcon;
            case RECESSIVE -> recessiveIcon;
            case ISOLATED -> isolatedIcon;
            default -> null;
        };
    }

    private static void renderIcon(PoseStack poseStack, VertexConsumer consumer, TextureAtlasSprite icon, int color) {
        float u0 = icon.getU0();
        float u1 = icon.getU1();
        float v0 = icon.getV0();
        float v1 = icon.getV1();
        int alpha = color >>> 24 & 255;
        int red = color >>> 16 & 255;
        int green = color >>> 8 & 255;
        int blue = color & 255;
        Matrix4f matrix = poseStack.last().pose();
        vertex(consumer, matrix, -0.5F, -0.25F, u0, v1, red, green, blue, alpha);
        vertex(consumer, matrix, 0.5F, -0.25F, u1, v1, red, green, blue, alpha);
        vertex(consumer, matrix, 0.5F, 0.75F, u1, v0, red, green, blue, alpha);
        vertex(consumer, matrix, -0.5F, 0.75F, u0, v0, red, green, blue, alpha);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, float u, float v,
            int red, int green, int blue, int alpha) {
        consumer.vertex(matrix, x, y, 0.0F)
                .color(red, green, blue, alpha)
                .uv(u, v)
                .uv2(LightTexture.FULL_BRIGHT)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(EntityAdvancedSpark entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
