package com.pulxes.advancedbotany.client.integration.jei;

import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.common.recipe.AncientAlphirineRecipe;
import com.pulxes.advancedbotany.registry.ModFlowers;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class AncientAlphirineCategory implements IRecipeCategory<AncientAlphirineRecipe> {
    private static final ResourceLocation PURE_DAISY_OVERLAY = ResourceLocation.fromNamespaceAndPath("botania", "textures/gui/pure_daisy_overlay.png");

    private final Component title = Component.translatable("jei." + AdvancedBotany.MOD_ID + ".ancient_alphirine");
    private final IDrawable icon;
    private final IDrawable overlay;

    public AncientAlphirineCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModFlowers.ANCIENT_ALPHIRINE_ITEM.get()));
        overlay = guiHelper.createDrawable(PURE_DAISY_OVERLAY, 0, 0, 64, 44);
    }

    @Override
    public RecipeType<AncientAlphirineRecipe> getRecipeType() {
        return AdvancedBotanyJeiPlugin.ANCIENT_ALPHIRINE;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public int getWidth() {
        return 96;
    }

    @Override
    public int getHeight() {
        return 58;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void draw(AncientAlphirineRecipe recipe, IRecipeSlotsView slotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        overlay.draw(guiGraphics, 17, 0);
        int barX = 9;
        int barY = 43;
        int barWidth = 78;
        guiGraphics.fill(barX, barY, barX + barWidth, barY + 4, 0xFF2F2F2F);
        guiGraphics.fill(barX, barY, barX + Math.max(1, barWidth * recipe.getChance() / 100), barY + 4, 0xFF48B84A);
        guiGraphics.drawString(Minecraft.getInstance().font,
                Component.translatable("jei." + AdvancedBotany.MOD_ID + ".chance", recipe.getChance()),
                9, 49, 0x3F7F3F, false);
        guiGraphics.drawString(Minecraft.getInstance().font,
                Component.translatable("jei." + AdvancedBotany.MOD_ID + ".mana", recipe.getManaUsage()),
                52, 49, 0x3150C8, false);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AncientAlphirineRecipe recipe, IFocusGroup focusGroup) {
        builder.addSlot(RecipeIngredientRole.INPUT, 9, 12)
                .addIngredients(recipe.getInput());

        builder.addSlot(RecipeIngredientRole.CATALYST, 39, 12)
                .addItemStack(new ItemStack(ModFlowers.ANCIENT_ALPHIRINE_ITEM.get()));

        builder.addSlot(RecipeIngredientRole.OUTPUT, 68, 12)
                .addItemStack(recipe.getOutput());
    }
}
