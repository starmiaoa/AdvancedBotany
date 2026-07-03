package com.pulxes.advancedbotany.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.pulxes.advancedbotany.AdvancedBotany;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

public class DiceFateModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(new ResourceLocation(AdvancedBotany.MOD_ID, "dice_fate"), "main");

    private final ModelPart main;

    public DiceFateModel(ModelPart root) {
        this.main = root.getChild("main");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("main",
                CubeListBuilder.create()
                        .texOffs(0, 24).addBox(-6.0F, -6.0F, -6.0F, 12.0F, 12.0F, 12.0F)
                        .texOffs(0, 0).addBox(-6.0F, -6.0F, -6.0F, 12.0F, 12.0F, 12.0F),
                PartPose.offset(0.0F, 18.0F, 0.0F));
        return LayerDefinition.create(mesh, 48, 48);
    }

    public void render(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay,
            float rotX, float rotY, float rotZ, float alpha) {
        main.xRot = radians(rotX);
        main.yRot = radians(rotY);
        main.zRot = radians(rotZ);
        main.render(poseStack, consumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, alpha);
    }

    private static float radians(float degrees) {
        return degrees / 180.0F * (float) Math.PI;
    }
}
