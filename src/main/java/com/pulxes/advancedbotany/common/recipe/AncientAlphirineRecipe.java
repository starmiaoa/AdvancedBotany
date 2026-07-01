package com.pulxes.advancedbotany.common.recipe;

import com.google.gson.JsonObject;
import com.pulxes.advancedbotany.registry.ModRecipes;
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

public class AncientAlphirineRecipe implements Recipe<Container> {
    public static final int DEFAULT_MANA_USAGE = 4500;

    private final ResourceLocation id;
    private final Ingredient input;
    private final ItemStack output;
    private final int chance;
    private final int manaUsage;

    public AncientAlphirineRecipe(ResourceLocation id, Ingredient input, ItemStack output, int chance, int manaUsage) {
        this.id = id;
        this.input = input;
        this.output = output;
        this.chance = Math.max(1, Math.min(100, chance));
        this.manaUsage = Math.max(1, manaUsage);
    }

    @Override
    public boolean matches(Container container, Level level) {
        return !container.isEmpty() && input.test(container.getItem(0));
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
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(input);
        return ingredients;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.ANCIENT_ALPHIRINE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.ANCIENT_ALPHIRINE_TYPE.get();
    }

    public Ingredient getInput() {
        return input;
    }

    public int getChance() {
        return chance;
    }

    public int getManaUsage() {
        return manaUsage;
    }

    public ItemStack getOutput() {
        return output.copy();
    }

    public static class Serializer implements RecipeSerializer<AncientAlphirineRecipe> {
        @Override
        public AncientAlphirineRecipe fromJson(ResourceLocation recipeId, JsonObject json) {
            Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "output"));
            int chance = GsonHelper.getAsInt(json, "chance", 100);
            int mana = GsonHelper.getAsInt(json, "mana", DEFAULT_MANA_USAGE);
            return new AncientAlphirineRecipe(recipeId, input, output, chance, mana);
        }

        @Override
        public AncientAlphirineRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
            Ingredient input = Ingredient.fromNetwork(buffer);
            ItemStack output = buffer.readItem();
            int chance = buffer.readVarInt();
            int mana = buffer.readVarInt();
            return new AncientAlphirineRecipe(recipeId, input, output, chance, mana);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, AncientAlphirineRecipe recipe) {
            recipe.input.toNetwork(buffer);
            buffer.writeItem(recipe.output);
            buffer.writeVarInt(recipe.chance);
            buffer.writeVarInt(recipe.manaUsage);
        }
    }
}
