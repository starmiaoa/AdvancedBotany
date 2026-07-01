package com.pulxes.advancedbotany.registry;

import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.common.item.equipment.AdvancedBotanyEquipment;
import com.pulxes.advancedbotany.common.item.equipment.SpaceBladeItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AdvancedBotany.MOD_ID);

    public static final RegistryObject<CreativeModeTab> ADVANCED_BOTANY = CREATIVE_TABS.register("advancedbotany", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.advancedbotany"))
            .icon(() -> new ItemStack(ModItems.MITHRIL.get()))
            .displayItems((parameters, output) -> {
                output.accept(ModItems.MITHRIL.get());
                output.accept(ModItems.MITHRIL_NUGGET.get());
                output.accept(ModItems.MANA_NETHER_STAR.get());
                output.accept(ModItems.AIR_OF_FORGOTTEN_LANDS.get());
                output.accept(ModItems.NATURES_GIFT.get());
                output.accept(ModItems.NEBULA_FRAGMENT.get());
                output.accept(ModItems.PIECE_OF_NEBULA.get());
                output.accept(ModItems.MITHRIL_MANA_RING.get());
                output.accept(ModItems.NEBULA_MANA_RING.get());
                output.accept(ModItems.SPHERE_OF_ATTRACTION.get());
                output.accept(ModItems.MANA_FLOWER.get());
                output.accept(ModItems.MITHRIL_BLOCK.get());
                output.accept(ModItems.LEBETHRON_WOOD.get());
                output.accept(ModItems.LEBETHRON_PLANKS.get());
                output.accept(ModItems.MOSSY_LEBETHRON_PLANKS.get());
                output.accept(ModItems.GLIMMERING_LEBETHRON_WOOD.get());
                output.accept(ModItems.TERRA_FARMLAND.get());
                output.accept(ModItems.FREYR_LIANA.get());
                output.accept(ModItems.LUMINOUS_FREYR_LIANA.get());
                output.accept(ModItems.ANTIGRAVITATION.get());
                output.accept(ModItems.MANA_CRYSTAL_CUBE.get());
                output.accept(ModItems.NATURAL_MANA_SPREADER.get());
                output.accept(ModItems.MANA_CONTAINER.get());
                output.accept(ModItems.DILUTED_MANA_CONTAINER.get());
                output.accept(ModItems.FABULOUS_MANA_CONTAINER.get());
                output.accept(ModItems.MANA_CHARGER.get());
                output.accept(ModItems.ENGINEER_HOPPER.get());
                output.accept(ModItems.NIDAVELLIR_FORGE.get());
                output.accept(ModItems.MAGIC_CRAFT_CRATE.get());
                output.accept(ModItems.PLAYING_BOARD.get());
                output.accept(ModItems.FATE_PLAYING_BOARD.get());
                output.accept(ModItems.LEBETHRON_NATURAL_CORE.get());
                output.accept(ModItems.MATERIAL_DESTROYER.get());
                output.accept(ModItems.TERRA_HOE.get());
                output.accept(ModItems.AQUA_SWORD.get());
                output.accept(ModItems.BLACK_HOLE_BOX.get());
                output.accept(ModItems.ROD_OF_SPRAWL.get());
                output.accept(ModItems.BLADE_OF_SPACE.get());
                for (int mana : AdvancedBotanyEquipment.SPACE_BLADE_CREATIVE_MANA) {
                    ItemStack blade = new ItemStack(ModItems.BLADE_OF_SPACE.get());
                    SpaceBladeItem.setMana(blade, mana);
                    output.accept(blade);
                }
                output.accept(ModItems.ROD_OF_NEBULA.get());
                output.accept(ModItems.NEBULA_BLAZE.get());
                output.accept(ModItems.SUPERCONDUCTIVE_SPARK.get());
                ModFlowers.addToCreativeTab(output);
            })
            .build());

    private ModCreativeTabs() {
    }

    public static void register(IEventBus eventBus) {
        CREATIVE_TABS.register(eventBus);
    }

    public static void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
    }
}
