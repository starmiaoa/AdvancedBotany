package com.pulxes.advancedbotany.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.common.entity.EntityAnonymousSteve;
import com.pulxes.advancedbotany.common.item.relic.PocketWardrobeItem;
import com.pulxes.advancedbotany.registry.ModEntities;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid = AdvancedBotany.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class PocketWardrobeRadialRenderer {
    private static final int SEGMENT_COUNT = 5;
    private static final int MAX_SEGMENT_COUNT = 12;
    private static final float RADIUS = 3.6F;
    private static final float INNER_SCALE = 0.8F;
    private static final float TEXTURE_V = 0.25F;
    private static final float BAND_HEIGHT = TEXTURE_V * 6.0F;
    private static final ResourceLocation GLOW = new ResourceLocation(AdvancedBotany.MOD_ID, "textures/misc/glow1.png");
    private static final ResourceLocation PRIORITY_GLOW = new ResourceLocation(AdvancedBotany.MOD_ID, "textures/misc/glow2.png");
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.FEET,
            EquipmentSlot.LEGS,
            EquipmentSlot.CHEST,
            EquipmentSlot.HEAD
    };

    private static ClientLevel previewLevel;
    private static EntityAnonymousSteve previewSteve;
    private static boolean renderFailureLogged;

    private PocketWardrobeRadialRenderer() {
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        // AFTER_LEVEL matches the original RenderWorldLastEvent hook (same as the Black Halo
        // ring) so weather/particles never draw over the radial ring.
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            clearPreview();
            return;
        }

        ItemStack stack = mc.player.getMainHandItem();
        if (!(stack.getItem() instanceof PocketWardrobeItem)) {
            return;
        }

        try {
            render(mc, event, stack);
            renderFailureLogged = false;
        } catch (Exception exception) {
            if (!renderFailureLogged) {
                AdvancedBotany.LOGGER.warn("Failed to render Pocket Wardrobe radial preview", exception);
                renderFailureLogged = true;
            }
        }
    }

    @SubscribeEvent
    public static void onClientLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        clearPreview();
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() == previewLevel) {
            clearPreview();
        }
    }

    private static void render(Minecraft mc, RenderLevelStageEvent event, ItemStack stack) {
        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || level == null) {
            clearPreview();
            return;
        }

        float partialTick = event.getPartialTick();
        float ticks = player.tickCount + partialTick;
        float alpha = (Mth.sin(ticks * 0.2F) * 0.5F + 0.5F) * 0.4F + 0.3F;
        float base = PocketWardrobeItem.getRotationBase(stack);
        int segmentAngle = 360 / MAX_SEGMENT_COUNT;
        float shift = base - (float) segmentAngle / 2.0F * SEGMENT_COUNT;
        int selectedSegment = PocketWardrobeItem.getSegmentLookedAt(stack, player);

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
                renderGlowSegment(poseStack, buffer, stack, segment, segmentAngle, shift, alpha, selectedSegment);
                renderPreviewSteve(mc, level, poseStack, buffer, stack, segment, segmentAngle, shift, ticks);
            }
        } finally {
            poseStack.popPose();
            buffer.endBatch();
        }
    }

    private static void renderGlowSegment(PoseStack poseStack, MultiBufferSource buffer, ItemStack stack, int segment,
                                          int segmentAngle, float shift, float alpha, int selectedSegment) {
        float segmentAlpha = alpha;
        float outerY = 0.0F;
        if (selectedSegment == segment) {
            segmentAlpha += 0.3F;
            outerY = -BAND_HEIGHT;
        }

        float color = segment % 2 == 0 ? 0.6F : 1.0F;
        int colorComponent = Math.round(color * 255.0F);
        int alphaComponent = Math.round(Mth.clamp(segmentAlpha, 0.0F, 1.0F) * 255.0F);
        RenderType renderType = RenderType.entityTranslucentEmissive(getGlowTexture(stack, segment));
        VertexConsumer consumer = buffer.getBuffer(renderType);

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

    private static void renderPreviewSteve(Minecraft mc, ClientLevel level, PoseStack poseStack,
                                           MultiBufferSource buffer, ItemStack stack, int segment,
                                           int segmentAngle, float shift, float ticks) {
        EntityAnonymousSteve steve = getPreviewSteve(level);
        ItemStack[] armorSet = PocketWardrobeItem.getArmorSet(stack, segment);
        boolean hasArmor = false;
        for (int i = 0; i < ARMOR_SLOTS.length; i++) {
            ItemStack armor = armorSet[i] == null ? ItemStack.EMPTY : armorSet[i];
            steve.setItemSlot(ARMOR_SLOTS[i], armor.copy());
            hasArmor |= !armor.isEmpty();
        }
        if (!hasArmor) {
            return;
        }

        LocalPlayer player = mc.player;
        if (player != null) {
            steve.moveTo(player.getX(), player.getY(), player.getZ(), 0.0F, 10.0F);
            steve.yBodyRot = 0.0F;
            steve.yBodyRotO = 0.0F;
            steve.setYHeadRot(0.0F);
            steve.yHeadRotO = 0.0F;
        }

        float rotationAngle = (segment + 0.5F) * segmentAngle + shift;
        double worldTime = ticks + segment * 2.75F;
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(rotationAngle));
        poseStack.translate(RADIUS * INNER_SCALE, -0.75D + Math.sin(worldTime / 12.0D) / 26.0D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        poseStack.translate(0.0D, 0.8125D, 0.0D);
        poseStack.scale(0.6F, 0.6F, 0.6F);

        mc.getEntityRenderDispatcher().setRenderShadow(false);
        try {
            mc.getEntityRenderDispatcher().render(steve, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F,
                    poseStack, buffer, LightTexture.FULL_BRIGHT);
        } finally {
            mc.getEntityRenderDispatcher().setRenderShadow(true);
            poseStack.popPose();
        }
    }

    private static EntityAnonymousSteve getPreviewSteve(ClientLevel level) {
        if (previewSteve == null || previewLevel != level) {
            previewLevel = level;
            previewSteve = new EntityAnonymousSteve(ModEntities.ANONYMOUS_STEVE.get(), level);
        }
        return previewSteve;
    }

    private static ResourceLocation getGlowTexture(ItemStack stack, int segment) {
        return PocketWardrobeItem.getPrioritySet(stack) == segment ? PRIORITY_GLOW : GLOW;
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

    private static void clearPreview() {
        previewSteve = null;
        previewLevel = null;
    }
}
