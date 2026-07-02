package com.pulxes.advancedbotany.common.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.pulxes.advancedbotany.registry.ModRecipes;
import java.util.ArrayList;
import java.util.List;
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

public class AdvancedPlateRecipe implements Recipe<RecipeInput> {
    private final NonNullList<Ingredient> inputs;
    private final ItemStack output;
    private final int manaUsage;
    private final int color;

    public AdvancedPlateRecipe(List<Ingredient> inputs, ItemStack output, int manaUsage, int color) {
        this.inputs = NonNullList.create();
        this.inputs.addAll(inputs);
        this.output = output;
        this.manaUsage = manaUsage;
        this.color = color;
    }

    @Override
    public boolean matches(RecipeInput container, Level level) {
        List<Ingredient> missingInputs = new ArrayList<>(inputs);
        for (int slot = 1; slot < container.size(); slot++) {
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
        NonNullList<Ingredient> copy = NonNullList.create();
        copy.addAll(inputs);
        return copy;
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
        private static final MapCodec<AdvancedPlateRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.LIST_CODEC_NONEMPTY.fieldOf("ingredients").forGetter(recipe -> recipe.inputs),
                ItemStack.STRICT_CODEC.fieldOf("output").forGetter(recipe -> recipe.output),
                Codec.INT.fieldOf("mana").forGetter(recipe -> recipe.manaUsage),
                Codec.INT.optionalFieldOf("color", 0x241E00).forGetter(recipe -> recipe.color)
        ).apply(instance, AdvancedPlateRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, AdvancedPlateRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()),
                recipe -> recipe.inputs,
                ItemStack.STREAM_CODEC,
                recipe -> recipe.output,
                ByteBufCodecs.VAR_INT,
                recipe -> recipe.manaUsage,
                ByteBufCodecs.VAR_INT,
                recipe -> recipe.color,
                AdvancedPlateRecipe::new
        );

        @Override
        public MapCodec<AdvancedPlateRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AdvancedPlateRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
