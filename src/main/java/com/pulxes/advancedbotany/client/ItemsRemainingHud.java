package com.pulxes.advancedbotany.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.pulxes.advancedbotany.AdvancedBotany;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AdvancedBotany.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ItemsRemainingHud {
    private static final int MAX_TICKS = 30;
    private static final int LEAVE_TICKS = 20;
    private static ItemStack stack = ItemStack.EMPTY;
    private static int ticks;
    private static String text = "";

    private ItemsRemainingHud() {
    }

    @SubscribeEvent
    public static void render(RenderGuiOverlayEvent.Post event) {
        if (ticks <= 0 || stack.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        GuiGraphics guiGraphics = event.getGuiGraphics();
        int pos = MAX_TICKS - ticks;
        int x = minecraft.getWindow().getGuiScaledWidth() / 2 + 10 + Math.max(0, pos - LEAVE_TICKS);
        int y = minecraft.getWindow().getGuiScaledHeight() / 2;
        int start = MAX_TICKS - LEAVE_TICKS;
        float alpha = ticks + event.getPartialTick() > start ? 1.0F : (ticks + event.getPartialTick()) / (float) start;

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
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && ticks > 0) {
            ticks--;
        }
    }

    public static void set(ItemStack itemStack, String displayText) {
        stack = itemStack == null ? ItemStack.EMPTY : itemStack;
        text = displayText == null ? "" : displayText;
        ticks = stack.isEmpty() ? 0 : MAX_TICKS;
    }
}
