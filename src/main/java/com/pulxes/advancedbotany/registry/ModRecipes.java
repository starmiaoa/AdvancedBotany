package com.pulxes.advancedbotany.registry;

import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.common.recipe.AdvancedPlateRecipe;
import com.pulxes.advancedbotany.common.recipe.AncientAlphirineRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, AdvancedBotany.MOD_ID);
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, AdvancedBotany.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AdvancedPlateRecipe>> ADVANCED_PLATE_SERIALIZER =
            SERIALIZERS.register("advanced_plate", AdvancedPlateRecipe.Serializer::new);
    public static final DeferredHolder<RecipeType<?>, RecipeType<AdvancedPlateRecipe>> ADVANCED_PLATE_TYPE =
            TYPES.register("advanced_plate", () -> new ModRecipeType<>("advanced_plate"));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AncientAlphirineRecipe>> ANCIENT_ALPHIRINE_SERIALIZER =
            SERIALIZERS.register("ancient_alphirine", AncientAlphirineRecipe.Serializer::new);
    public static final DeferredHolder<RecipeType<?>, RecipeType<AncientAlphirineRecipe>> ANCIENT_ALPHIRINE_TYPE =
            TYPES.register("ancient_alphirine", () -> new ModRecipeType<>("ancient_alphirine"));

    private ModRecipes() {
    }

    public static void register(IEventBus eventBus) {
        TYPES.register(eventBus);
        SERIALIZERS.register(eventBus);
    }

    private static final class ModRecipeType<T extends Recipe<?>> implements RecipeType<T> {
        private final ResourceLocation id;

        private ModRecipeType(String name) {
            this.id = ResourceLocation.fromNamespaceAndPath(AdvancedBotany.MOD_ID, name);
        }

        @Override
        public String toString() {
            return id.toString();
        }
    }
}
