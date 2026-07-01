package com.pulxes.advancedbotany;

import com.mojang.logging.LogUtils;
import com.pulxes.advancedbotany.registry.ModBlockEntities;
import com.pulxes.advancedbotany.registry.ModBlocks;
import com.pulxes.advancedbotany.registry.ModCreativeTabs;
import com.pulxes.advancedbotany.registry.ModEntities;
import com.pulxes.advancedbotany.registry.ModFlowers;
import com.pulxes.advancedbotany.registry.ModItems;
import com.pulxes.advancedbotany.registry.ModForgeEvents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.eventbus.api.IEventBus;
import org.slf4j.Logger;

@Mod(AdvancedBotany.MOD_ID)
public class AdvancedBotany {
    public static final String MOD_ID = "advancedbotany";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AdvancedBotany() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.register(modBus);
        ModBlocks.register(modBus);
        ModBlockEntities.register(modBus);
        ModEntities.register(modBus);
        ModFlowers.register(modBus);
        ModCreativeTabs.register(modBus);

        modBus.addListener(ModCreativeTabs::addCreativeTabItems);

        MinecraftForge.EVENT_BUS.register(ModForgeEvents.class);
    }
}
