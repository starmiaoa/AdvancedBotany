package com.pulxes.advancedbotany.api;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import vazkii.botania.api.block.WandHUD;

/**
 * Client compatibility API for old Advanced Botany HUD render hooks.
 */
public interface IRenderHud extends WandHUD {
    void renderHud(Minecraft minecraft, GuiGraphics guiGraphics);

    @Override
    default void renderHUD(GuiGraphics guiGraphics, Window window, Font font, float partialTick) {
        renderHud(Minecraft.getInstance(), guiGraphics);
    }
}
