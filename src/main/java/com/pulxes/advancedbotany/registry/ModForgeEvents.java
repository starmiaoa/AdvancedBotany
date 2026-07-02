package com.pulxes.advancedbotany.registry;

import com.pulxes.advancedbotany.common.block.entity.BaseInventoryBlockEntity;
import com.pulxes.advancedbotany.common.block.entity.ManaChargerBlockEntity;
import com.pulxes.advancedbotany.common.item.equipment.BlackHaloItem;
import com.pulxes.advancedbotany.common.item.equipment.ManaRingItem;
import com.pulxes.advancedbotany.common.item.equipment.SpaceBladeItem;
import com.pulxes.advancedbotany.common.item.equipment.armor.NebulaArmorItem;
import com.pulxes.advancedbotany.common.item.relic.ModRelicItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.block.Wandable;
import vazkii.botania.api.item.BlockProvider;
import vazkii.botania.api.item.Relic;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.spark.SparkAttachable;

public final class ModForgeEvents {
    private ModForgeEvents() {
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        registerBlockCapabilities(event);
        registerItemCapabilities(event);
    }

    private static void registerBlockCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(BotaniaForgeCapabilities.getBlockApiLookupById(ManaReceiver.LOOKUP),
                ModBlockEntities.MANA_CONTAINER.get(), (be, side) -> be);
        event.registerBlockEntity(BotaniaForgeCapabilities.getBlockApiLookupById(SparkAttachable.LOOKUP),
                ModBlockEntities.MANA_CONTAINER.get(), (be, ignored) -> be);
        event.registerBlockEntity(BotaniaForgeCapabilities.getBlockApiLookupById(Wandable.LOOKUP),
                ModBlockEntities.MANA_CONTAINER.get(), (be, side) -> be);

        event.registerBlockEntity(BotaniaForgeCapabilities.getBlockApiLookupById(ManaReceiver.LOOKUP),
                ModBlockEntities.NATURAL_MANA_SPREADER.get(), (be, side) -> be);
        event.registerBlockEntity(BotaniaForgeCapabilities.getBlockApiLookupById(Wandable.LOOKUP),
                ModBlockEntities.NATURAL_MANA_SPREADER.get(), (be, side) -> be);

        event.registerBlockEntity(BotaniaForgeCapabilities.getBlockApiLookupById(Wandable.LOOKUP),
                ModBlockEntities.MANA_CRYSTAL_CUBE.get(), (be, side) -> be);
        event.registerBlockEntity(BotaniaForgeCapabilities.getBlockApiLookupById(Wandable.LOOKUP),
                ModBlockEntities.MANA_CHARGER.get(), (be, side) -> be);
        event.registerBlockEntity(BotaniaForgeCapabilities.getBlockApiLookupById(Wandable.LOOKUP),
                ModBlockEntities.ENGINEER_HOPPER.get(), (be, side) -> be);
        event.registerBlockEntity(BotaniaForgeCapabilities.getBlockApiLookupById(Wandable.LOOKUP),
                ModBlockEntities.MAGIC_CRAFT_CRATE.get(), (be, side) -> be);

        event.registerBlockEntity(BotaniaForgeCapabilities.getBlockApiLookupById(ManaReceiver.LOOKUP),
                ModBlockEntities.NIDAVELLIR_FORGE.get(), (be, side) -> be);
        event.registerBlockEntity(BotaniaForgeCapabilities.getBlockApiLookupById(SparkAttachable.LOOKUP),
                ModBlockEntities.NIDAVELLIR_FORGE.get(), (be, ignored) -> be);

        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.ENGINEER_HOPPER.get(), BaseInventoryBlockEntity::getItemHandler);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.NIDAVELLIR_FORGE.get(), BaseInventoryBlockEntity::getItemHandler);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.MAGIC_CRAFT_CRATE.get(), BaseInventoryBlockEntity::getItemHandler);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.MANA_CHARGER.get(), ManaChargerBlockEntity::getItemHandler);
    }

    private static void registerItemCapabilities(RegisterCapabilitiesEvent event) {
        ItemCapability<ManaItem, Void> manaItemCapability = BotaniaForgeCapabilities.getItemApiLookupById(ManaItem.LOOKUP);
        event.registerItem(manaItemCapability, (stack, context) -> ((ManaRingItem) stack.getItem()).createManaItem(stack),
                ModItems.MITHRIL_MANA_RING.get(), ModItems.NEBULA_MANA_RING.get());
        event.registerItem(manaItemCapability, (stack, context) -> SpaceBladeItem.createManaItem(stack),
                ModItems.BLADE_OF_SPACE.get());
        registerNebulaManaItem(event, manaItemCapability,
                ModItems.NEBULA_HELMET.get(), ModItems.NEBULA_HELMET_OF_REVEALING.get(),
                ModItems.NEBULA_CHESTPLATE.get(), ModItems.NEBULA_LEGGINGS.get(), ModItems.NEBULA_BOOTS.get());

        event.registerItem(BotaniaForgeCapabilities.getItemApiLookupById(BlockProvider.LOOKUP),
                (stack, context) -> BlackHaloItem.createBlockProvider(stack), ModItems.BLACK_HOLE_BOX.get());

        event.registerItem(BotaniaForgeCapabilities.getItemApiLookupById(Relic.LOOKUP),
                (stack, context) -> stack.getItem() instanceof ModRelicItem relicItem ? relicItem.createRelic(stack) : null,
                ModItems.FREYR_SLINGSHOT.get(), ModItems.HORN_OF_PLENTY.get(), ModItems.NIMBLE_CUBE.get(),
                ModItems.SPHERE_OF_NAVIGATION.get(), ModItems.KEY_TO_HIDDEN_WEALTH.get(), ModItems.WILD_HUNT_WHIP.get());
    }

    private static void registerNebulaManaItem(RegisterCapabilitiesEvent event,
            ItemCapability<ManaItem, Void> manaItemCapability, Item... items) {
        event.registerItem(manaItemCapability, (stack, context) -> createNebulaManaItem(stack), items);
    }

    private static ManaItem createNebulaManaItem(ItemStack stack) {
        return stack.getItem() instanceof NebulaArmorItem armor ? armor.createManaItem(stack) : null;
    }

}
