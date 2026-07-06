package com.pulxes.advancedbotany.common.item.equipment.armor;

import com.pulxes.advancedbotany.AdvancedBotany;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

public final class AdvancedBotanyArmorMaterials {
    public static final Holder<ArmorMaterial> NEBULA = Holder.direct(material("nebula", defenses(8, 12, 6, 3), 50));

    private AdvancedBotanyArmorMaterials() {
    }

    private static ArmorMaterial material(String name, Map<ArmorItem.Type, Integer> defenses, int enchantmentValue) {
        ResourceLocation layer = ResourceLocation.fromNamespaceAndPath(AdvancedBotany.MOD_ID, name);
        return new ArmorMaterial(
                defenses,
                enchantmentValue,
                SoundEvents.ARMOR_EQUIP_DIAMOND,
                () -> Ingredient.EMPTY,
                List.of(new ArmorMaterial.Layer(layer)),
                0.0F,
                0.0F);
    }

    private static EnumMap<ArmorItem.Type, Integer> defenses(int helmet, int chestplate, int leggings, int boots) {
        EnumMap<ArmorItem.Type, Integer> values = new EnumMap<>(ArmorItem.Type.class);
        values.put(ArmorItem.Type.HELMET, helmet);
        values.put(ArmorItem.Type.CHESTPLATE, chestplate);
        values.put(ArmorItem.Type.LEGGINGS, leggings);
        values.put(ArmorItem.Type.BOOTS, boots);
        return values;
    }
}
