package com.pulxes.advancedbotany.client;

import net.neoforged.api.distmarker.Dist;
import com.mojang.blaze3d.systems.RenderSystem;
import com.pulxes.advancedbotany.AdvancedBotany;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = AdvancedBotany.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public final class ItemsRemainingHud {
    private static final int MAX_TICKS = 30;
    private static final int LEAVE_TICKS = 20;
    private static ItemStack stack = ItemStack.EMPTY;
    private static int ticks;
    private static String text = "";

    private ItemsRemainingHud() {
    }

    @SubscribeEvent
    public static void render(RenderGuiLayerEvent.Post event) {
        if (ticks <= 0 || stack.isEmpty()) {
            return;
        }
        if (!event.getName().equals(VanillaGuiLayers.HOTBAR)) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        GuiGraphics guiGraphics = event.getGuiGraphics();
        int pos = MAX_TICKS - ticks;
        int x = minecraft.getWindow().getGuiScaledWidth() / 2 + 10 + Math.max(0, pos - LEAVE_TICKS);
        int y = minecraft.getWindow().getGuiScaledHeight() / 2;
        int start = MAX_TICKS - LEAVE_TICKS;
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        float alpha = ticks + partialTick > start ? 1.0F : (ticks + partialTick) / (float) start;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        guiGraphics.renderItem(stack, x + (int) (16.0F * (1.0F - alpha)), y);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        Font font = minecraft.font;
        int color = 0xFFFFFF | (int) (alpha * 255.0F) << 24;
        guiGraphics.drawString(font, text, x + 20, y + 6, color, false);
        RenderSystem.disableBlend();
    }

    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Post event) {
        if (ticks > 0) {
            ticks--;
        }
    }

    public static void set(ItemStack itemStack, String displayText) {
        stack = itemStack == null ? ItemStack.EMPTY : itemStack;
        text = displayText == null ? "" : displayText;
        ticks = stack.isEmpty() ? 0 : MAX_TICKS;
    }
}
