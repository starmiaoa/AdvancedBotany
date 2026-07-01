package com.pulxes.advancedbotany.client;

import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.client.gui.MagicCraftCrateScreen;
import com.pulxes.advancedbotany.client.gui.NidavellirForgeScreen;
import com.pulxes.advancedbotany.registry.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = AdvancedBotany.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ModClientEvents {
    private ModClientEvents() {
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.NIDAVELLIR_FORGE.get(), NidavellirForgeScreen::new);
            MenuScreens.register(ModMenuTypes.MAGIC_CRAFT_CRATE.get(), MagicCraftCrateScreen::new);
        });
    }
}
