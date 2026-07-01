package com.pulxes.advancedbotany.common.item.equipment;

import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaForgeCapabilities;
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
        return stack.getOrCreateTag().getInt(TAG_MANA);
    }

    public int getMaxMana(ItemStack stack) {
        return maxMana;
    }

    public void setMana(ItemStack stack, int mana) {
        int clampedMana = Mth.clamp(mana, 0, getMaxMana(stack));
        if (clampedMana > 0) {
            stack.getOrCreateTag().putInt(TAG_MANA, clampedMana);
        } else if (stack.hasTag()) {
            stack.getTag().remove(TAG_MANA);
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

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ManaRingCapabilityProvider(stack);
    }

    private float getManaFraction(ItemStack stack) {
        return (float) getMana(stack) / (float) getMaxMana(stack);
    }

    private class ManaRingCapabilityProvider implements ICapabilityProvider {
        private final LazyOptional<ManaItem> manaItem;

        ManaRingCapabilityProvider(ItemStack stack) {
            this.manaItem = LazyOptional.of(() -> new ManaRingManaItem(stack));
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction side) {
            if (capability == BotaniaForgeCapabilities.MANA_ITEM) {
                return manaItem.cast();
            }
            return LazyOptional.empty();
        }
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
        public boolean canReceiveManaFromItem(ItemStack otherStack) {
            return true;
        }

        @Override
        public boolean canExportManaToPool(BlockEntity pool) {
            return true;
        }

        @Override
        public boolean canExportManaToItem(ItemStack otherStack) {
            return true;
        }

        @Override
        public boolean isNoExport() {
            return false;
        }
    }
}
