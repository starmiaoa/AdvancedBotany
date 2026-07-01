package com.pulxes.advancedbotany.common.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.pulxes.advancedbotany.registry.ModRecipes;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

public class AdvancedPlateRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final NonNullList<Ingredient> inputs;
    private final ItemStack output;
    private final int manaUsage;
    private final int color;

    public AdvancedPlateRecipe(ResourceLocation id, List<Ingredient> inputs, ItemStack output, int manaUsage, int color) {
        this.id = id;
        this.inputs = NonNullList.create();
        this.inputs.addAll(inputs);
        this.output = output;
        this.manaUsage = manaUsage;
        this.color = color;
    }

    @Override
    public boolean matches(Container container, Level level) {
        List<Ingredient> missingInputs = new ArrayList<>(inputs);
        for (int slot = 1; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }

            int matchIndex = -1;
            for (int i = 0; i < missingInputs.size(); i++) {
                if (missingInputs.get(i).test(stack)) {
                    matchIndex = i;
                    break;
                }
            }

            if (matchIndex == -1) {
                return false;
            }
            missingInputs.remove(matchIndex);
        }

        return missingInputs.isEmpty();
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return output.copy();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> copy = NonNullList.create();
        copy.addAll(inputs);
        return copy;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.ADVANCED_PLATE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.ADVANCED_PLATE_TYPE.get();
    }

    public int getManaUsage() {
        return manaUsage;
    }

    public int getColor() {
        return color;
    }

    public ItemStack getOutput() {
        return output.copy();
    }

    public static class Serializer implements RecipeSerializer<AdvancedPlateRecipe> {
        @Override
        public AdvancedPlateRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            JsonArray ingredientArray = GsonHelper.getAsJsonArray(json, "ingredients");
            List<Ingredient> inputs = new ArrayList<>(ingredientArray.size());
            for (int i = 0; i < ingredientArray.size(); i++) {
                inputs.add(Ingredient.fromJson(ingredientArray.get(i)));
            }

            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
            int mana = GsonHelper.getAsInt(json, "mana");
            int color = GsonHelper.getAsInt(json, "color", 0x241E00);
            return new AdvancedPlateRecipe(recipeId, inputs, output, mana, color);
        }

        @Override
        public AdvancedPlateRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            int inputCount = buffer.readVarInt();
            List<Ingredient> inputs = new ArrayList<>(inputCount);
            for (int i = 0; i < inputCount; i++) {
                inputs.add(Ingredient.fromNetwork(buffer));
            }

            ItemStack output = buffer.readItem();
            int mana = buffer.readVarInt();
            int color = buffer.readVarInt();
            return new AdvancedPlateRecipe(recipeId, inputs, output, mana, color);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, AdvancedPlateRecipe recipe) {
            buffer.writeVarInt(recipe.inputs.size());
            for (Ingredient input : recipe.inputs) {
                input.toNetwork(buffer);
            }

            buffer.writeItem(recipe.output);
            buffer.writeVarInt(recipe.manaUsage);
            buffer.writeVarInt(recipe.color);
        }
    }
}
