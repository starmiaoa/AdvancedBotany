package com.pulxes.advancedbotany.client.model.armor;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.EquipmentSlot;

public class WildHuntArmorModel extends AdvancedBotanyArmorModel {
    public WildHuntArmorModel(ModelPart root, EquipmentSlot slot) {
        super(root, slot);
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition rightLeg = part(root, "right_leg", 0.0F, 12.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        PartDefinition leftLeg = part(root, "left_leg", 0.0F, 12.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        PartDefinition head = part(root, "head", 0.0F, 0.0F, 0.0F, 0F, 0F, 0F, box(32, 7, -4.0F, -8.0F, -4.0F, 8F, 2F, 8F, 0.25F), box(38, 18, -4.0F, -8.6F, -4.0F, 8F, 1F, 5F, 0.0F), box(21, 20, -3.0F, -8.6F, 1.0F, 6F, 1F, 2F, 0.0F), box(46, 86, -3.5F, -6.0F, 3.0F, 7F, 1F, 1F, 0.1F), box(40, 25, 3.0F, -6.2F, -1.8F, 1F, 4F, 5F, 0.1F), box(52, 25, -4.0F, -6.2F, -1.8F, 1F, 4F, 5F, 0.1F), box(28, 0, -4.0F, -6.3F, -4.0F, 1F, 5F, 2F, 0.1F), box(33, 7, 3.0F, -6.3F, -4.0F, 1F, 5F, 2F, 0.1F));
        PartDefinition cuber1 = part(head, "cube_r1", 0.0F, 3.8F, -2.7F, -0.1745F, 0.0F, 0.1309F, box(31, 25, 1.7F, -6.7F, -3.0F, 2F, 3F, 2F, -0.2F));
        PartDefinition cuber2 = part(head, "cube_r2", 0.0F, 3.8F, -2.7F, -0.1745F, 0.0F, -0.1309F, box(22, 25, -3.7F, -6.7F, -3.0F, 2F, 3F, 2F, -0.2F));
        PartDefinition cuber3 = part(head, "cube_r3", 0.0F, 4.0F, -1.0F, -0.1309F, 0.0F, 0.0F, box(21, 12, -1.0F, -13.1F, -2.8F, 2F, 1F, 6F, 0.0F));
        PartDefinition cuber4 = part(head, "cube_r4", 0.3F, 4.0F, -1.2F, -0.1309F, 0.0F, 0.0F, box(47, 61, 1.0F, -15.2F, -3.4F, 2F, 3F, 2F, -0.3F), box(47, 55, -3.6F, -15.2F, -3.4F, 2F, 3F, 2F, -0.3F));
        PartDefinition cuber5 = part(head, "cube_r5", -0.3F, 4.0F, -1.2F, 0.2182F, 0.0F, 0.0F, box(47, 71, -3.0F, -13.0F, 1.2F, 2F, 1F, 2F, -0.1F), box(47, 67, 1.6F, -13.0F, 1.2F, 2F, 1F, 2F, -0.1F));
        PartDefinition cuber6 = part(head, "cube_r6", 0.0F, 4.0F, -1.0F, 0.1309F, 0.0F, 0.0F, box(24, 8, -1.0F, -11.2F, -2.0F, 2F, 2F, 1F, 0.1F));
        PartDefinition cuber7 = part(head, "cube_r7", -0.8F, 5.5F, -0.1F, -0.1309F, 1.5708F, 0.0F, box(56, 43, -1.0F, -15.7F, -6.4F, 2F, 3F, 2F, -0.35F));
        PartDefinition cuber8 = part(head, "cube_r8", 0.8F, 4.5F, -0.1F, 0.2182F, -1.5708F, 0.0F, box(56, 68, -1.0F, -13.7F, -1.7F, 2F, 3F, 2F, -0.2F));
        PartDefinition cuber9 = part(head, "cube_r9", 0.8F, 5.5F, -0.1F, -0.1309F, -1.5708F, 0.0F, box(56, 49, -1.0F, -15.7F, -6.4F, 2F, 3F, 2F, -0.35F));
        PartDefinition cuber10 = part(head, "cube_r10", -0.8F, 4.5F, -0.1F, 0.2182F, 1.5708F, 0.0F, box(56, 74, -1.0F, -13.7F, -1.7F, 2F, 3F, 2F, -0.2F));
        PartDefinition cuber11 = part(head, "cube_r11", 0.0F, 3.9F, -0.7F, -0.1309F, 0.0F, 0.0F, box(56, 62, -1.0F, -15.7F, -6.6F, 2F, 3F, 2F, -0.35F));
        PartDefinition cuber12 = part(head, "cube_r12", 0.0F, 3.9F, -0.7F, 0.2182F, 0.0F, 0.0F, box(56, 55, -1.0F, -14.7F, -1.7F, 2F, 4F, 2F, -0.2F));
        PartDefinition cuber13 = part(head, "cube_r13", -1.6F, 3.8F, -1.6F, 0.2182F, 0.7854F, 0.0F, box(47, 43, -1.0F, -13.7F, -1.7F, 2F, 3F, 2F, -0.2F));
        PartDefinition cuber14 = part(head, "cube_r14", 1.6F, 3.8F, -1.6F, -0.1309F, -0.7854F, 0.0F, box(0, 0, -1.0F, -14.7F, -6.3F, 2F, 3F, 2F, -0.35F));
        PartDefinition cuber15 = part(head, "cube_r15", 1.6F, 3.8F, -1.6F, 0.2182F, -0.7854F, 0.0F, box(47, 49, -1.0F, -13.7F, -1.7F, 2F, 3F, 2F, -0.2F));
        PartDefinition cuber16 = part(head, "cube_r16", -1.6F, 3.8F, -1.6F, -0.1309F, 0.7854F, 0.0F, box(0, 6, -1.0F, -14.7F, -6.3F, 2F, 3F, 2F, -0.35F));
        PartDefinition body = part(root, "body", 0.0F, 0.0F, 0.0F, 0F, 0F, 0F, box(49, 25, -1.0F, 7.85F, -2.9F, 2F, 2F, 1F, 0.0F), box(0, 84, -4.0F, 0.9F, -3.1F, 8F, 7F, 1F, 0.0F), box(19, 85, -2.5F, 4.7F, 1.9F, 5F, 6F, 1F, 0.0F), box(44, 89, -3.5F, 0.0F, 1.1F, 7F, 7F, 2F, 0.0F), box(0, 93, -4.0F, 0.0F, -2.0F, 8F, 12F, 4F, 0.2F));
        PartDefinition cuber17 = part(body, "cube_r17", 0.0F, -3.0F, 1.2F, 0.0873F, 0.0F, 0.0F, box(36, 1, -4.0F, 10.1F, -5.1F, 8F, 4F, 1F, -0.1F));
        PartDefinition cuber18 = part(body, "cube_r18", 0.0F, 20.5F, 0.8F, 0.0F, 0.0F, 0.0436F, box(51, 35, 0.9F, -14.0F, -3.9F, 2F, 6F, 1F, -0.1F));
        PartDefinition cuber19 = part(body, "cube_r19", 0.0F, 20.5F, 0.8F, 0.0F, 0.0F, -0.0436F, box(58, 35, -2.9F, -14.0F, -3.9F, 2F, 6F, 1F, -0.1F));
        PartDefinition rightarm = part(root, "right_arm", -5.0F, 2.0F, 0.0F, 0F, 0F, 0F, box(31, 95, -3.0F, -2.0F, -2.0F, 4F, 4F, 4F, 0.3F), box(19, 59, -3.0F, 2.0F, -2.0F, 4F, 8F, 4F, 0.1F), box(19, 51, -3.9F, -2.7F, -2.0F, 4F, 3F, 4F, 0.0F));
        PartDefinition cuber21 = part(rightarm, "cube_r21", -1.1F, 10.3F, 0.5F, -0.1309F, 0.7854F, 0.0F, box(0, 29, -1.0F, -13.7F, -6.3F, 2F, 2F, 2F, -0.45F));
        PartDefinition cuber22 = part(rightarm, "cube_r22", 0.8F, 9.9F, 1.0F, -0.1309F, 0.0F, 0.0F, box(9, 51, -3.0F, -14.7F, -3.4F, 2F, 3F, 2F, -0.35F));
        PartDefinition cuber23 = part(rightarm, "cube_r23", 0.8F, 9.9F, 1.0F, 0.2182F, 0.0F, 0.0F, box(9, 63, -3.0F, -13.0F, 1.2F, 2F, 1F, 2F, -0.1F));
        PartDefinition cuber24 = part(rightarm, "cube_r24", -1.1F, 9.7F, -0.8F, 0.2182F, 2.3562F, 0.0F, box(0, 47, -1.0F, -13.7F, -1.7F, 2F, 2F, 2F, -0.3F));
        PartDefinition cuber25 = part(rightarm, "cube_r25", -1.1F, 9.9F, -0.8F, -0.1309F, 2.3562F, 0.0F, box(0, 23, -1.0F, -13.7F, -6.3F, 2F, 2F, 2F, -0.45F));
        PartDefinition cuber26 = part(rightarm, "cube_r26", -1.1F, 10.1F, 0.5F, 0.2182F, 0.7854F, 0.0F, box(0, 53, -1.0F, -13.7F, -1.7F, 2F, 2F, 2F, -0.3F));
        PartDefinition leftarm = part(root, "left_arm", 5.0F, 2.0F, 0.0F, 0F, 0F, 0F, box(19, 72, -1.0F, 2.0F, -2.0F, 4F, 8F, 4F, 0.1F), box(31, 105, -1.0F, -2.0F, -2.0F, 4F, 4F, 4F, 0.3F), box(19, 43, -0.1F, -2.7F, -2.0F, 4F, 3F, 4F, 0.0F));
        PartDefinition cuber27 = part(leftarm, "cube_r27", 1.1F, 10.1F, 0.5F, 0.2182F, -0.7854F, 0.0F, box(0, 59, -1.0F, -13.7F, -1.7F, 2F, 2F, 2F, -0.3F));
        PartDefinition cuber28 = part(leftarm, "cube_r28", 1.1F, 10.3F, 0.5F, -0.1309F, -0.7854F, 0.0F, box(0, 35, -1.0F, -13.7F, -6.3F, 2F, 2F, 2F, -0.45F));
        PartDefinition cuber29 = part(leftarm, "cube_r29", 1.2F, 10.0F, -0.8F, -0.1309F, -2.3562F, 0.0F, box(0, 41, -1.0F, -13.7F, -6.3F, 2F, 2F, 2F, -0.45F));
        PartDefinition cuber30 = part(leftarm, "cube_r30", 1.2F, 9.8F, -0.8F, 0.2182F, -2.3562F, 0.0F, box(0, 65, -1.0F, -13.7F, -1.7F, 2F, 2F, 2F, -0.3F));
        PartDefinition cuber31 = part(leftarm, "cube_r31", 3.3F, 10.0F, 1.0F, -0.1309F, 0.0F, 0.0F, box(9, 57, -3.0F, -14.7F, -3.4F, 2F, 3F, 2F, -0.35F));
        PartDefinition cuber32 = part(leftarm, "cube_r32", 3.3F, 10.0F, 1.0F, 0.2182F, 0.0F, 0.0F, box(9, 67, -3.0F, -13.0F, 1.2F, 2F, 1F, 2F, -0.1F));
        PartDefinition rightlegarmor = part(rightLeg, "right_leg_armor", 0.0F, 0.0F, 0.0F, 0F, 0F, 0F, box(48, 103, -4.0F, 0.4F, -2.0F, 4F, 8F, 4F, 0.15F));
        PartDefinition cuber33 = part(rightlegarmor, "cube_r33", 0.0F, 11.9F, 1.0F, 0.0873F, 0.0F, 0.0F, box(37, 122, -3.5F, -10.1F, -3.1F, 3F, 4F, 2F, -0.3F));
        PartDefinition leftlegarmor = part(leftLeg, "left_leg_armor", 0.0F, 0.0F, 0.0F, 0F, 0F, 0F, box(48, 116, 0.0F, 0.4F, -2.0F, 4F, 8F, 4F, 0.15F));
        PartDefinition cuber34 = part(leftlegarmor, "cube_r34", 0.0F, 11.9F, 1.0F, 0.0873F, 0.0F, 0.0F, box(37, 115, 0.5F, -10.1F, -3.1F, 3F, 4F, 2F, -0.3F));
        PartDefinition rightboot = part(rightLeg, "right_boot", 0.0F, 0.0F, 0.0F, 0F, 0F, 0F, box(0, 120, -4.0F, 9.0F, -2.8F, 4F, 3F, 5F, 0.25F));
        PartDefinition leftboot = part(leftLeg, "left_boot", 0.0F, 0.0F, 0.0F, 0F, 0F, 0F, box(0, 111, 0.0F, 9.0F, -2.8F, 4F, 3F, 5F, 0.25F));
        part(root, "hat", 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        return LayerDefinition.create(mesh, 64, 128);
    }
}
