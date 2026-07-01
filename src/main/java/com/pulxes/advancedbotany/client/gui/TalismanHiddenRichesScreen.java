package com.pulxes.advancedbotany.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.pulxes.advancedbotany.common.menu.TalismanHiddenRichesMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class TalismanHiddenRichesScreen extends AbstractContainerScreen<TalismanHiddenRichesMenu> {
    private static final ResourceLocation BACKGROUND = new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");

    public TalismanHiddenRichesScreen(TalismanHiddenRichesMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageHeight = 168;
        inventoryLabelY = imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }
}
