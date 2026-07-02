package com.pulxes.advancedbotany.common.item.equipment;

import com.pulxes.advancedbotany.common.item.ItemComponentData;
import java.util.Optional;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import vazkii.botania.api.mana.ManaBarTooltip;
import vazkii.botania.api.mana.ManaItem;

public class ManaRingItem extends ItemBauble {
    private static final String TAG_MANA = "mana";

    private final int maxMana;

    public ManaRingItem(Properties properties, int maxMana) {
        super(properties.setNoRepair(), "ring");
        this.maxMana = maxMana;
    }

    public int getMana(ItemStack stack) {
        return ItemComponentData.getInt(stack, TAG_MANA);
    }

    public int getMaxMana(ItemStack stack) {
        return maxMana;
    }

    public void setMana(ItemStack stack, int mana) {
        int clampedMana = Mth.clamp(mana, 0, getMaxMana(stack));
        if (clampedMana > 0) {
            ItemComponentData.putInt(stack, TAG_MANA, clampedMana);
        } else {
            ItemComponentData.remove(stack, TAG_MANA);
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * getManaFraction(stack));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return Mth.hsvToRgb(0.45F + getManaFraction(stack) * 0.08F, 1.0F, 1.0F);
    }

    @Override
    public int getEntityLifespan(ItemStack itemStack, Level level) {
        return Integer.MAX_VALUE;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return Optional.of(ManaBarTooltip.fromManaItem(stack));
    }

    public ManaItem createManaItem(ItemStack stack) {
        return new ManaRingManaItem(stack);
    }

    private float getManaFraction(ItemStack stack) {
        return (float) getMana(stack) / (float) getMaxMana(stack);
    }

    private class ManaRingManaItem implements ManaItem {
        private final ItemStack stack;

        ManaRingManaItem(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public int getMana() {
            return ManaRingItem.this.getMana(stack);
        }

        @Override
        public int getMaxMana() {
            return ManaRingItem.this.getMaxMana(stack);
        }

        @Override
        public void addMana(int mana) {
            ManaRingItem.this.setMana(stack, getMana() + mana);
        }

        @Override
        public boolean canReceiveManaFromPool(BlockEntity pool) {
            return true;
        }

        @Override
        public boolean acceptDispatchedManaFromItem(ItemStack otherStack) {
            return true;
        }

        @Override
        public boolean refuseRequestedManaFromItem(ItemStack otherStack) {
            return false;
        }

        @Override
        public boolean canDrainManaToPool(BlockEntity pool) {
            return true;
        }

        @Override
        public boolean canSendRequestedManaToItem(ItemStack otherStack) {
            return true;
        }

        @Override
        public boolean isNoExport() {
            return false;
        }
    }
}
