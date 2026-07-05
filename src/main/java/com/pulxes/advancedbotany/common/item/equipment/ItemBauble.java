package com.pulxes.advancedbotany.common.item.equipment;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import vazkii.botania.common.handler.BotaniaSounds;

public abstract class ItemBauble extends Item implements ICurioItem {
    private final String slotType;

    protected ItemBauble(Properties properties, String slotType) {
        super(properties.stacksTo(1));
        this.slotType = slotType;
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        // Curios 5 handles right-click equipping through this hook; explicit use()
        // handling would duplicate Curios' slot selection and stack movement logic.
        return true;
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return slotType.equals(slotContext.identifier());
    }

    @Override
    public void curioTick(SlotContext slotContext, ItemStack stack) {
        onWornTick(stack, slotContext.entity());
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if (!entity.level().isClientSide()) {
            entity.level().playSound(null, entity.blockPosition(), BotaniaSounds.equipBauble, SoundSource.PLAYERS, 0.1F, 1.3F);
        }
        onEquipped(stack, entity);
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        onUnequipped(stack, slotContext.entity());
    }

    protected void onWornTick(ItemStack stack, LivingEntity entity) {
    }

    protected void onEquipped(ItemStack stack, LivingEntity entity) {
    }

    protected void onUnequipped(ItemStack stack, LivingEntity entity) {
    }
}
