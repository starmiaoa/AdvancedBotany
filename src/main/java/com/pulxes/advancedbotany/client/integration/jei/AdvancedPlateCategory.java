package com.pulxes.advancedbotany.client.integration.jei;

import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.common.recipe.AdvancedPlateRecipe;
import com.pulxes.advancedbotany.registry.ModBlocks;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import vazkii.botania.client.gui.HUDHandler;

public class AdvancedPlateCategory implements IRecipeCategory<AdvancedPlateRecipe> {
    private static final ResourceLocation PETAL_OVERLAY = ResourceLocation.fromNamespaceAndPath("botania", "textures/gui/petal_overlay.png");
    private static final int[][] INPUT_POSITIONS = new int[][] {
            {48, 13},
            {20, 61},
            {76, 61}
    };

    private final Component title = Component.translatable("jei." + AdvancedBotany.MOD_ID + ".advanced_plate");
    private final IDrawable icon;
    private final IDrawable overlay;

    public AdvancedPlateCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.NIDAVELLIR_FORGE.get()));
        overlay = guiHelper.createDrawable(PETAL_OVERLAY, 17, 11, 114, 82);
    }

    @Override
    public RecipeType<AdvancedPlateRecipe> getRecipeType() {
        return AdvancedBotanyJeiPlugin.ADVANCED_PLATE;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public int getWidth() {
        return 114;
    }

    @Override
    public int getHeight() {
        return 104;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void draw(AdvancedPlateRecipe recipe, IRecipeSlotsView slotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        overlay.draw(guiGraphics, 0, 4);
        HUDHandler.renderManaBar(guiGraphics, 6, 98, recipe.getColor(), 0.75F, recipe.getManaUsage(), recipe.getManaUsage());
        guiGraphics.drawString(
                net.minecraft.client.Minecraft.getInstance().font,
                NumberFormat.getIntegerInstance(Locale.US).format(recipe.getManaUsage()),
                41,
                96,
                0x404040,
                false);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AdvancedPlateRecipe recipe, IFocusGroup focusGroup) {
        builder.addSlot(RecipeIngredientRole.CATALYST, 48, 45)
                .addItemStack(new ItemStack(ModBlocks.NIDAVELLIR_FORGE.get()))
                .addRichTooltipCallback((slotView, tooltip) ->
                        tooltip.add(Component.translatable("jei." + AdvancedBotany.MOD_ID + ".mana", recipe.getManaUsage())));

        List<Ingredient> ingredients = recipe.getIngredients();
        for (int i = 0; i < ingredients.size(); i++) {
            int[] position = INPUT_POSITIONS[Math.min(i, INPUT_POSITIONS.length - 1)];
            builder.addSlot(RecipeIngredientRole.INPUT, position[0], position[1])
                    .addIngredients(ingredients.get(i));
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, 86, 10)
                .addItemStack(recipe.getOutput());
    }
}
