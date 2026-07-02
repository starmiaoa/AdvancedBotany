package com.pulxes.advancedbotany.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

public class AdvancedBotanyArmorModel extends HumanoidModel<LivingEntity> {
    private final EquipmentSlot slot;
    private final ModelPart rightLegArmor;
    private final ModelPart leftLegArmor;
    private final ModelPart rightBoot;
    private final ModelPart leftBoot;

    protected AdvancedBotanyArmorModel(ModelPart root, EquipmentSlot slot) {
        super(root);
        this.slot = slot;
        this.rightLegArmor = rightLeg.getChild("right_leg_armor");
        this.leftLegArmor = leftLeg.getChild("left_leg_armor");
        this.rightBoot = rightLeg.getChild("right_boot");
        this.leftBoot = leftLeg.getChild("left_boot");
    }

    @Override
    public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        rightLeg.setPos(0.0F, 12.0F, 0.0F);
        leftLeg.setPos(0.0F, 12.0F, 0.0F);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                               int color) {
        setArmorPartVisibility();
        super.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, color);
    }

    private void setArmorPartVisibility() {
        setAllVisible(false);
        rightLegArmor.visible = false;
        leftLegArmor.visible = false;
        rightBoot.visible = false;
        leftBoot.visible = false;

        switch (slot) {
            case HEAD -> head.visible = true;
            case CHEST -> {
                body.visible = true;
                rightArm.visible = true;
                leftArm.visible = true;
            }
            case LEGS -> {
                rightLeg.visible = true;
                leftLeg.visible = true;
                rightLegArmor.visible = true;
                leftLegArmor.visible = true;
            }
            case FEET -> {
                rightLeg.visible = true;
                leftLeg.visible = true;
                rightBoot.visible = true;
                leftBoot.visible = true;
            }
            default -> {
            }
        }
    }

    protected static PartDefinition part(PartDefinition parent, String name, float x, float y, float z,
                                         float xRot, float yRot, float zRot, Box... boxes) {
        CubeListBuilder builder = CubeListBuilder.create();
        for (Box box : boxes) {
            builder.texOffs(box.u, box.v)
                    .addBox(box.x, box.y, box.z, box.dx, box.dy, box.dz, new CubeDeformation(box.inflate));
        }
        return parent.addOrReplaceChild(name, builder, PartPose.offsetAndRotation(x, y, z, xRot, yRot, zRot));
    }

    protected static Box box(int u, int v, float x, float y, float z, float dx, float dy, float dz, float inflate) {
        return new Box(u, v, x, y, z, dx, dy, dz, inflate);
    }

    protected record Box(int u, int v, float x, float y, float z, float dx, float dy, float dz, float inflate) {
    }
}
