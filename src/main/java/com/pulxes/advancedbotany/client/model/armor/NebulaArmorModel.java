package com.pulxes.advancedbotany.client.model.armor;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.EquipmentSlot;

public class NebulaArmorModel extends AdvancedBotanyArmorModel {
    public NebulaArmorModel(ModelPart root, EquipmentSlot slot) {
        super(root, slot);
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition rightLeg = part(root, "right_leg", 0.0F, 12.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        PartDefinition leftLeg = part(root, "left_leg", 0.0F, 12.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        PartDefinition head = part(root, "head", 0.0F, 0.0F, 0.0F, 0F, 0F, 0F, box(0, 66, -4.0F, -8.0F, -4.0F, 8F, 2F, 8F, 0.21F), box(36, 90, -3.0F, -8.75F, -3.0F, 6F, 1F, 4F, 0.21F), box(0, 90, -4.0F, -6.0F, -4.0F, 1F, 3F, 7F, 0.2085F), box(19, 86, 3.0F, -6.0F, -4.0F, 1F, 3F, 7F, 0.2085F), box(28, 80, -3.0F, -6.0F, -4.0F, 6F, 1F, 1F, 0.2075F), box(23, 81, -4.0F, -6.0F, 3.0F, 1F, 1F, 1F, 0.2075F), box(23, 78, 3.0F, -6.0F, 3.0F, 1F, 1F, 1F, 0.2075F));
        PartDefinition cuber1 = part(head, "cube_r1", 0.0F, 0.0F, 0.3F, -0.1745F, -0.3491F, 0.0F, box(16, 78, 0.6F, -8.4F, -6.8F, 2F, 3F, 1F, 0.21F));
        PartDefinition cuber2 = part(head, "cube_r2", 0.0F, 0.0F, 0.3F, -0.1745F, 0.3491F, 0.0F, box(9, 78, -2.6F, -8.4F, -6.8F, 2F, 3F, 1F, 0.21F));
        PartDefinition cuber3 = part(head, "cube_r3", 0.0F, 0.0F, 0.0F, -0.2618F, 0.0F, 0.0F, box(5, 83, -1.0F, -9.0F, -2.5F, 2F, 1F, 5F, 0.21F));
        PartDefinition cuber12 = part(head, "cube_r12", 0.0F, 0.0F, 0.0F, 0.7418F, 0.0F, -0.0873F, box(47, 83, -4.6F, -7.0F, 3.0F, 2F, 2F, 4F, 0.21F));
        PartDefinition cuber13 = part(head, "cube_r13", 0.0F, 0.0F, 0.0F, 0.7418F, 0.0F, 0.0873F, box(45, 76, 2.6F, -7.0F, 3.0F, 2F, 2F, 4F, 0.21F));
        PartDefinition cuber14 = part(head, "cube_r14", 0.0F, 0.0F, 0.0F, -0.0873F, 0.0F, 0.0F, box(31, 84, -1.0F, -5.0F, -4.6F, 2F, 1F, 1F, 0.21F));
        PartDefinition cuber15 = part(head, "cube_r15", 0.0F, 0.0F, 0.0F, -0.48F, 0.0F, 0.0F, box(0, 78, -1.0F, -7.9F, -8.0F, 2F, 4F, 2F, 0.21F));
        PartDefinition body = part(root, "body", 0.0F, 0.0F, 0.0F, 0F, 0F, 0F, box(0, 0, -4.5F, -0.2F, -3.0F, 9F, 11F, 6F, 0.0F), box(31, 10, -2.5F, 5.7F, 1.9F, 5F, 4F, 2F, 0.0F), box(31, 0, -3.5F, -0.6F, 2.6F, 7F, 7F, 2F, 0.0F));
        PartDefinition cuber16 = part(body, "cube_r16", 0.0F, 24.0F, -1.0F, 0.1309F, 0.0F, 0.0F, box(57, 16, -1.0F, -20.7F, 7.7F, 2F, 6F, 1F, 0.0F));
        PartDefinition cuber4 = part(body, "cube_r4", -0.1F, 25.2F, 3.35F, 0.2618F, 0.0F, 0.1745F);
        PartDefinition cuber4r1 = part(cuber4, "cube_r4_r1", 0.1F, -1.2F, -3.35F, -0.5236F, 0.0F, 0.0F, box(50, 8, -2.0F, -25.0F, -3.0F, 2F, 5F, 2F, 0.0F));
        PartDefinition cuber5 = part(body, "cube_r5", -0.1F, 25.2F, 3.35F, 0.2618F, 0.0F, -0.1745F);
        PartDefinition cuber5r1 = part(cuber5, "cube_r5_r1", 0.1F, -1.2F, -3.35F, -0.5236F, 0.0F, 0.0F, box(50, 0, 0.0F, -25.0F, -3.0F, 2F, 5F, 2F, 0.0F));
        PartDefinition cuber6 = part(body, "cube_r6", 0.0F, 24.0F, 0.0F, -0.1745F, 0.0F, 0.0F);
        PartDefinition cuber7 = part(body, "cube_r7", 0.0F, 23.75F, 0.0F, -0.0873F, 0.0F, 0.0F);
        PartDefinition cuber7r1 = part(cuber7, "cube_r7_r1", 0.0F, -0.3F, 0.0F, 0.2182F, 0.0F, 0.0F, box(34, 68, -1.5F, -23.1842F, -1.1615F, 3F, 8F, 2F, 0.0F));
        PartDefinition cuber7r2 = part(cuber7, "cube_r7_r2", 0.0F, -0.3F, 0.0F, 0.2618F, 0.0F, 0.0F, box(34, 17, 1.5F, -21.1842F, -0.8615F, 2F, 7F, 2F, 0.0F), box(54, 69, -3.5F, -21.1842F, -0.8615F, 2F, 7F, 2F, 0.0F));
        PartDefinition rightarm = part(root, "right_arm", -5.0F, 2.0F, 0.0F, 0F, 0F, 0F, box(0, 27, -4.0F, -2.4F, -2.5F, 5F, 5F, 5F, 0.05F), box(21, 27, -3.0F, 6.0F, -2.0F, 4F, 4F, 4F, 0.2F));
        PartDefinition cuber17 = part(rightarm, "cube_r17", 0.0F, -3.5F, 0.0F, 0.8727F, 0.0F, 0.0F, box(45, 63, -1.7F, 1.3F, -2.3F, 2F, 1F, 4F, 0.0F));
        PartDefinition cuber8 = part(rightarm, "cube_r8", 5.2F, 22.3F, 0.0F, 0.0F, 0.0F, 0.2182F, box(54, 25, -14.0918F, -24.4119F, -1.0F, 2F, 3F, 2F, 0.0F));
        PartDefinition cuber9 = part(rightarm, "cube_r9", 5.2F, 22.3F, 0.0F, 0.0F, 0.0F, 0.0873F, box(16, 19, -11.8564F, -24.7019F, -2.0F, 4F, 3F, 4F, 0.0F));
        PartDefinition leftarm = part(root, "left_arm", 5.0F, 2.0F, 0.0F, 0F, 0F, 0F, box(38, 26, -1.0F, -2.4F, -2.5F, 5F, 5F, 5F, 0.05F), box(0, 38, -1.0F, 6.0F, -2.0F, 4F, 4F, 4F, 0.2F));
        PartDefinition cuber18 = part(leftarm, "cube_r18", -10.0F, -3.5F, 0.0F, 0.8727F, 0.0F, 0.0F, box(45, 63, 9.7F, 1.3F, -2.3F, 2F, 1F, 4F, 0.0F));
        PartDefinition cuber10 = part(leftarm, "cube_r10", -4.8F, 22.3F, 0.0F, 0.0F, 0.0F, -0.2182F, box(19, 36, 11.6918F, -24.4119F, -1.0F, 2F, 3F, 2F, 0.0F));
        PartDefinition cuber11 = part(leftarm, "cube_r11", -4.8F, 22.3F, 0.0F, 0.0F, 0.0F, -0.0873F, box(24, 38, 7.4564F, -24.7019F, -2.0F, 4F, 3F, 4F, 0.0F));
        PartDefinition rightlegarmor = part(rightLeg, "right_leg_armor", 0.0F, 0.0F, 0.0F, 0F, 0F, 0F, box(0, 50, -4.0F, 0.0F, -2.0F, 4F, 8F, 4F, 0.2F));
        PartDefinition cuber19 = part(rightlegarmor, "cube_r19", 0.0F, 0.0F, 0.0F, 0.2618F, 0.0F, 0.0F, box(46, 17, -3.0F, 0.6F, -3.8F, 2F, 4F, 1F, 0.2F));
        PartDefinition cuber20 = part(rightlegarmor, "cube_r20", 0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, 0.0F, box(7, 20, -3.0F, 2.0F, 0.7F, 2F, 4F, 1F, 0.2F));
        PartDefinition cuber21 = part(rightlegarmor, "cube_r21", 0.0F, 12.0F, 1.0F, 0.1745F, 0.0F, -0.1745F, box(17, 48, -3.0F, -10.7F, -1.9F, 2F, 4F, 2F, 0.2F));
        PartDefinition leftlegarmor = part(leftLeg, "left_leg_armor", 0.0F, 0.0F, 0.0F, 0F, 0F, 0F, box(42, 38, 0.0F, 0.0F, -2.0F, 4F, 8F, 4F, 0.2F));
        PartDefinition cuber22 = part(leftlegarmor, "cube_r22", 0.0F, 0.0F, 0.0F, 0.2618F, 0.0F, 0.0F, box(35, 46, 1.0F, 0.6F, -3.8F, 2F, 4F, 1F, 0.2F));
        PartDefinition cuber23 = part(leftlegarmor, "cube_r23", 0.0F, 0.0F, 0.0F, 0.3054F, 0.0F, 0.0F, box(0, 20, 1.0F, 2.0F, 0.7F, 2F, 4F, 1F, 0.2F));
        PartDefinition cuber24 = part(leftlegarmor, "cube_r24", 0.0F, 12.0F, 1.0F, 0.1745F, 0.0F, 0.1745F, box(26, 48, 1.0F, -10.7F, -1.9F, 2F, 4F, 2F, 0.2F));
        PartDefinition rightboot = part(rightLeg, "right_boot", 0.0F, 0.0F, 0.0F, 0F, 0F, 0F, box(44, 53, -4.0F, 9.0F, -2.8F, 4F, 3F, 5F, 0.2F));
        PartDefinition cuber27 = part(rightboot, "cube_r27", 0.0F, 12.0F, -1.0F, -0.3054F, 0.0F, 0.0F, box(27, 65, -3.0F, -5.7F, 1.9F, 2F, 3F, 1F, 0.2F));
        PartDefinition cuber28 = part(rightboot, "cube_r28", 0.0F, 12.0F, -1.0F, 0.7418F, 0.0F, -0.1745F, box(37, 61, -4.2F, -1.8F, 2.2F, 1F, 2F, 3F, 0.2F));
        PartDefinition leftboot = part(leftLeg, "left_boot", 0.0F, 0.0F, 0.0F, 0F, 0F, 0F, box(18, 56, 0.0F, 9.0F, -2.8F, 4F, 3F, 5F, 0.2F));
        PartDefinition cuber25 = part(leftboot, "cube_r25", 0.0F, 12.0F, -1.0F, -0.3054F, 0.0F, 0.0F, box(40, 52, 1.0F, -5.7F, 1.9F, 2F, 3F, 1F, 0.2F));
        PartDefinition cuber26 = part(leftboot, "cube_r26", 0.0F, 12.0F, -1.0F, 0.7418F, 0.0F, 0.1745F, box(33, 54, 3.2F, -1.8F, 2.2F, 1F, 2F, 3F, 0.2F));
        part(root, "hat", 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        return LayerDefinition.create(mesh, 64, 128);
    }
}
