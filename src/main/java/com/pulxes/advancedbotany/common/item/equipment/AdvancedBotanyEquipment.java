package com.pulxes.advancedbotany.common.item.equipment;

import com.pulxes.advancedbotany.registry.ModItems;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import vazkii.botania.api.BotaniaAPI;

public final class AdvancedBotanyEquipment {
    public static final Tier MITHRIL = new Tier() {
        @Override
        public int getUses() {
            return 0;
        }

        @Override
        public float getSpeed() {
            return 8.0F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 4.0F;
        }

        @Override
        public TagKey<Block> getIncorrectBlocksForDrops() {
            return BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
        }

        @Override
        public int getEnchantmentValue() {
            return 24;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.of(new ItemStack(ModItems.MITHRIL.get()));
        }
    };

    public static final int TERRA_HOE_MANA_PER_REPAIR = 1_760;
    public static final int AQUA_SWORD_SPLASH_MANA = 10;
    public static final int AQUA_SWORD_HOLD_MANA = 15;
    public static final int SPACE_BLADE_PROJECTILE_MANA = 120;
    public static final int SPACE_BLADE_DASH_COOLDOWN = 36;
    public static final int[] SPACE_BLADE_LEVELS = {0, 10_000, 1_000_000, 10_000_000, 100_000_000, 1_000_000_000};
    public static final int[] SPACE_BLADE_CREATIVE_MANA = {9_999, 999_999, 9_999_999, 99_999_999, 999_999_999, Integer.MAX_VALUE - 1};
    public static final int SPRAWL_ROD_MANA_PER_REPAIR = 760;
    public static final int SPRAWL_ROD_MAX_DAMAGE = 100;
    public static final int SPRAWL_ROD_MAX_AREA = 64;
    public static final int NEBULA_ROD_MAX_DAMAGE = 100;
    public static final int NEBULA_ROD_REPAIR_INTERVAL = 18;
    public static final int NEBULA_ROD_MANA_PER_REPAIR = 180;
    public static final int NEBULA_ROD_XZ_LIMIT = 30_000_000;
    public static final int NEBULA_BLAZE_MANA = 125;
    public static final int ADVANCED_SPARK_TRANSFER_SPEED = 48_000;

    private AdvancedBotanyEquipment() {
    }

    public static Tier terrasteelTier() {
        return BotaniaAPI.instance().getTerrasteelItemTier();
    }
}
