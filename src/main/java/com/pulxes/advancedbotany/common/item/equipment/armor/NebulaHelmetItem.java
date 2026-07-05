package com.pulxes.advancedbotany.common.item.equipment.armor;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import vazkii.botania.api.mana.ManaDiscountArmor;

import java.util.List;

public class NebulaHelmetItem extends NebulaArmorItem implements ManaDiscountArmor {
    private static final String TAG_COSMIC_FACE = "enableCosmicFace";

    public NebulaHelmetItem(boolean revealing, Properties properties) {
        // Thaumcraft's revealing capability has no 1.20.1 target dependency here.
        // Revealing helmets are deliberately retained as a decorative variant only.
        super(ArmorItem.Type.HELMET, revealing, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                setCosmicFace(stack, !isCosmicFaceEnabled(stack));
            }
            return InteractionResultHolder.success(stack);
        }
        return super.use(level, player, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (!isCosmicFaceEnabled(stack)) {
            tooltip.add(Component.translatable("ab.nebulaHelm.mask").withStyle(ChatFormatting.GREEN));
        }
    }

    @Override
    public float getDiscount(ItemStack stack, int slot, Player player, ItemStack tool) {
        return hasFullSet(player) ? 0.3F : 0.0F;
    }

    public static boolean isCosmicFaceEnabled(ItemStack stack) {
        return stack.getTag() == null || !stack.getTag().contains(TAG_COSMIC_FACE) || stack.getTag().getBoolean(TAG_COSMIC_FACE);
    }

    public static void setCosmicFace(ItemStack stack, boolean enabled) {
        stack.getOrCreateTag().putBoolean(TAG_COSMIC_FACE, enabled);
    }
}
