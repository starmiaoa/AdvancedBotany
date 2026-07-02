package com.pulxes.advancedbotany.common.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pulxes.advancedbotany.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class AncientAlphirineRecipe implements Recipe<RecipeInput> {
    public static final int DEFAULT_MANA_USAGE = 4500;

    private final Ingredient input;
    private final ItemStack output;
    private final int chance;
    private final int manaUsage;

    public AncientAlphirineRecipe(Ingredient input, ItemStack output, int chance, int manaUsage) {
        this.input = input;
        this.output = output;
        this.chance = Math.max(1, Math.min(100, chance));
        this.manaUsage = Math.max(1, manaUsage);
    }

    @Override
    public boolean matches(RecipeInput container, Level level) {
        return !container.isEmpty() && input.test(container.getItem(0));
    }

    @Override
    public ItemStack assemble(RecipeInput container, HolderLookup.Provider registries) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return output.copy();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(input);
        return ingredients;
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
        private static final MapCodec<AncientAlphirineRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("input").forGetter(recipe -> recipe.input),
                ItemStack.STRICT_CODEC.fieldOf("output").forGetter(recipe -> recipe.output),
                Codec.INT.optionalFieldOf("chance", 100).forGetter(recipe -> recipe.chance),
                Codec.INT.optionalFieldOf("mana", DEFAULT_MANA_USAGE).forGetter(recipe -> recipe.manaUsage)
        ).apply(instance, AncientAlphirineRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, AncientAlphirineRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC,
                recipe -> recipe.input,
                ItemStack.STREAM_CODEC,
                recipe -> recipe.output,
                ByteBufCodecs.VAR_INT,
                recipe -> recipe.chance,
                ByteBufCodecs.VAR_INT,
                recipe -> recipe.manaUsage,
                AncientAlphirineRecipe::new
        );

        @Override
        public MapCodec<AncientAlphirineRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AncientAlphirineRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
