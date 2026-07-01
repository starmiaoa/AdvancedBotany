package com.pulxes.advancedbotany.client.integration.jei;

import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.common.recipe.AdvancedPlateRecipe;
import com.pulxes.advancedbotany.common.recipe.AncientAlphirineRecipe;
import com.pulxes.advancedbotany.registry.ModBlocks;
import com.pulxes.advancedbotany.registry.ModFlowers;
import com.pulxes.advancedbotany.registry.ModRecipes;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;

@JeiPlugin
public class AdvancedBotanyJeiPlugin implements IModPlugin {
    public static final ResourceLocation PLUGIN_ID = new ResourceLocation(AdvancedBotany.MOD_ID, "jei");
    public static final RecipeType<AdvancedPlateRecipe> ADVANCED_PLATE =
            RecipeType.create(AdvancedBotany.MOD_ID, "advanced_plate", AdvancedPlateRecipe.class);
    public static final RecipeType<AncientAlphirineRecipe> ANCIENT_ALPHIRINE =
            RecipeType.create(AdvancedBotany.MOD_ID, "ancient_alphirine", AncientAlphirineRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new AdvancedPlateCategory(guiHelper),
                new AncientAlphirineCategory(guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        RecipeManager recipeManager = level.getRecipeManager();
        List<AdvancedPlateRecipe> plateRecipes = recipeManager.getAllRecipesFor(ModRecipes.ADVANCED_PLATE_TYPE.get());
        registration.addRecipes(ADVANCED_PLATE, plateRecipes);

        List<AncientAlphirineRecipe> alphirineRecipes = recipeManager.getAllRecipesFor(ModRecipes.ANCIENT_ALPHIRINE_TYPE.get());
        registration.addRecipes(ANCIENT_ALPHIRINE, alphirineRecipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.NIDAVELLIR_FORGE.get()), ADVANCED_PLATE);
        registration.addRecipeCatalyst(new ItemStack(ModFlowers.ANCIENT_ALPHIRINE_ITEM.get()), ANCIENT_ALPHIRINE);
    }
}
