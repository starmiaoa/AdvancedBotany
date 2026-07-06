package com.pulxes.advancedbotany;

import com.mojang.logging.LogUtils;
import com.pulxes.advancedbotany.api.AdvancedBotanyAPI;
import com.pulxes.advancedbotany.common.network.ModNetwork;
import com.pulxes.advancedbotany.registry.ModBlockEntities;
import com.pulxes.advancedbotany.registry.ModBlocks;
import com.pulxes.advancedbotany.registry.ModCreativeTabs;
import com.pulxes.advancedbotany.registry.ModCurios;
import com.pulxes.advancedbotany.registry.ModEntities;
import com.pulxes.advancedbotany.registry.ModFlowers;
import com.pulxes.advancedbotany.registry.ModItems;
import com.pulxes.advancedbotany.registry.ModForgeEvents;
import com.pulxes.advancedbotany.registry.ModGameEvents;
import com.pulxes.advancedbotany.registry.ModMenuTypes;
import com.pulxes.advancedbotany.registry.ModRecipes;
import com.pulxes.advancedbotany.registry.ModSounds;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherWartBlock;
import org.slf4j.Logger;

@Mod(AdvancedBotany.MOD_ID)
public class AdvancedBotany {
    public static final String MOD_ID = "advancedbotany";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AdvancedBotany(IEventBus modBus) {
        ModItems.register(modBus);
        ModBlocks.register(modBus);
        ModBlockEntities.register(modBus);
        ModMenuTypes.register(modBus);
        ModRecipes.register(modBus);
        ModEntities.register(modBus);
        ModSounds.register(modBus);
        ModFlowers.register(modBus);
        ModCreativeTabs.register(modBus);

        modBus.register(ModForgeEvents.class);
        modBus.addListener(ModCreativeTabs::addCreativeTabItems);
        modBus.addListener(ModCurios::enqueueIMC);
        modBus.addListener(ModEntities::registerAttributes);
        modBus.addListener(this::commonSetup);

        modBus.addListener(ModNetwork::registerPayloadHandlers);
        NeoForge.EVENT_BUS.register(ModGameEvents.class);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            AdvancedBotanyAPI.registerDefaultBoardEntries();
            AdvancedBotanyAPI.registerFarmlandSeed(
                    Blocks.NETHER_WART,
                    Blocks.NETHER_WART.defaultBlockState().setValue(NetherWartBlock.AGE, 3));
            ModItems.registerFateBoardRelics();
        });
    }
}
