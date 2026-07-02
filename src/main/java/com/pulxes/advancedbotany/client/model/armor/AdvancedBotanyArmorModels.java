package com.pulxes.advancedbotany.client.model.armor;

import com.pulxes.advancedbotany.AdvancedBotany;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.EnumMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public final class AdvancedBotanyArmorModels {
    public static final ModelLayerLocation NEBULA_ARMOR = layer("nebula_armor");
    public static final ModelLayerLocation WILD_HUNT_ARMOR = layer("wild_hunt_armor");

    private static Map<EquipmentSlot, HumanoidModel<LivingEntity>> nebula = Map.of();
    private static Map<EquipmentSlot, HumanoidModel<LivingEntity>> wildHunt = Map.of();

    private AdvancedBotanyArmorModels() {
    }

    public static void init(EntityRendererProvider.Context context) {
        nebula = make(context, NEBULA_ARMOR, NebulaArmorModel::new);
        wildHunt = make(context, WILD_HUNT_ARMOR, WildHuntArmorModel::new);
    }

    public static HumanoidModel<LivingEntity> nebula(EquipmentSlot slot) {
        return nebula.get(slot);
    }

    public static HumanoidModel<LivingEntity> wildHunt(EquipmentSlot slot) {
        return wildHunt.get(slot);
    }

    private static Map<EquipmentSlot, HumanoidModel<LivingEntity>> make(EntityRendererProvider.Context context,
                                                                       ModelLayerLocation layer,
                                                                       ModelFactory factory) {
        EnumMap<EquipmentSlot, HumanoidModel<LivingEntity>> models = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                models.put(slot, factory.create(context.bakeLayer(layer), slot));
            }
        }
        return models;
    }

    private static ModelLayerLocation layer(String path) {
        return new ModelLayerLocation(new ResourceLocation(AdvancedBotany.MOD_ID, path), "main");
    }

    @FunctionalInterface
    private interface ModelFactory {
        HumanoidModel<LivingEntity> create(net.minecraft.client.model.geom.ModelPart root, EquipmentSlot slot);
    }
}
