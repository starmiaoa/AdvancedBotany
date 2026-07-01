package com.pulxes.advancedbotany.common.item.equipment.armor;

import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import vazkii.botania.common.item.equipment.armor.manasteel.ManasteelArmorItem;

import java.util.List;

public class WildHuntArmorItem extends ManasteelArmorItem {
    private static final String TEXTURE = AdvancedBotany.MOD_ID + ":textures/model/wildHuntArmor.png";

    public WildHuntArmorItem(ArmorItem.Type type, Properties properties) {
        super(type, AdvancedBotanyArmorMaterials.WILD_HUNT, properties.stacksTo(1));
    }

    @Override
    public String getArmorTextureAfterInk(ItemStack stack, EquipmentSlot slot) {
        return TEXTURE;
    }

    @Override
    public ItemStack[] getArmorSetStacks() {
        return new ItemStack[]{
                new ItemStack(ModItems.WILD_HUNT_HELMET.get()),
                new ItemStack(ModItems.WILD_HUNT_CHESTPLATE.get()),
                new ItemStack(ModItems.WILD_HUNT_LEGGINGS.get()),
                new ItemStack(ModItems.WILD_HUNT_BOOTS.get())
        };
    }

    @Override
    public boolean hasArmorSetItem(Player player, EquipmentSlot slot) {
        ItemStack stack = player.getItemBySlot(slot);
        if (stack.isEmpty()) {
            return false;
        }
        return switch (slot) {
            case HEAD -> stack.is(ModItems.WILD_HUNT_HELMET.get());
            case CHEST -> stack.is(ModItems.WILD_HUNT_CHESTPLATE.get());
            case LEGS -> stack.is(ModItems.WILD_HUNT_LEGGINGS.get());
            case FEET -> stack.is(ModItems.WILD_HUNT_BOOTS.get());
            default -> false;
        };
    }

    @Override
    public MutableComponent getArmorSetName() {
        return Component.translatable("ab.armorset.wildHunt.name");
    }

    @Override
    public void addArmorSetDescription(ItemStack stack, List<Component> tooltip) {
        tooltip.add(Component.translatable("ab.armorset.wildHunt.desc0"));
        tooltip.add(Component.translatable("ab.armorset.wildHunt.desc1"));
        tooltip.add(Component.translatable("ab.armorset.wildHunt.desc2"));
    }
}
