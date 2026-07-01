package com.pulxes.advancedbotany.common.item.equipment.armor;

import com.pulxes.advancedbotany.AdvancedBotany;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;

public final class AdvancedBotanyArmorMaterials {
    public static final ArmorMaterial NEBULA = new Material("nebula", 0, defenses(3, 8, 6, 3), 26);
    public static final ArmorMaterial WILD_HUNT = new Material("wild_hunt", 34, defenses(7, 8, 3, 2), 26);

    private static final EnumMap<ArmorItem.Type, Integer> BASE_DURABILITY = new EnumMap<>(ArmorItem.Type.class);

    static {
        BASE_DURABILITY.put(ArmorItem.Type.BOOTS, 13);
        BASE_DURABILITY.put(ArmorItem.Type.LEGGINGS, 15);
        BASE_DURABILITY.put(ArmorItem.Type.CHESTPLATE, 16);
        BASE_DURABILITY.put(ArmorItem.Type.HELMET, 11);
    }

    private AdvancedBotanyArmorMaterials() {
    }

    private static EnumMap<ArmorItem.Type, Integer> defenses(int helmet, int chestplate, int leggings, int boots) {
        EnumMap<ArmorItem.Type, Integer> values = new EnumMap<>(ArmorItem.Type.class);
        values.put(ArmorItem.Type.HELMET, helmet);
        values.put(ArmorItem.Type.CHESTPLATE, chestplate);
        values.put(ArmorItem.Type.LEGGINGS, leggings);
        values.put(ArmorItem.Type.BOOTS, boots);
        return values;
    }

    private record Material(String name, int durabilityMultiplier, EnumMap<ArmorItem.Type, Integer> defenses,
                            int enchantmentValue) implements ArmorMaterial {
        @Override
        public int getDurabilityForType(ArmorItem.Type type) {
            return BASE_DURABILITY.get(type) * durabilityMultiplier;
        }

        @Override
        public int getDefenseForType(ArmorItem.Type type) {
            return defenses.get(type);
        }

        @Override
        public int getEnchantmentValue() {
            return enchantmentValue;
        }

        @Override
        public SoundEvent getEquipSound() {
            return SoundEvents.ARMOR_EQUIP_DIAMOND;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.EMPTY;
        }

        @Override
        public String getName() {
            return AdvancedBotany.MOD_ID + ":" + name;
        }

        @Override
        public float getToughness() {
            return 0.0F;
        }

        @Override
        public float getKnockbackResistance() {
            return 0.0F;
        }
    }
}
