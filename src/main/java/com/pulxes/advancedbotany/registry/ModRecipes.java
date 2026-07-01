package com.pulxes.advancedbotany.registry;

import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.common.recipe.AdvancedPlateRecipe;
import com.pulxes.advancedbotany.common.recipe.AncientAlphirineRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, AdvancedBotany.MOD_ID);
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, AdvancedBotany.MOD_ID);

    public static final RegistryObject<RecipeSerializer<AdvancedPlateRecipe>> ADVANCED_PLATE_SERIALIZER =
            SERIALIZERS.register("advanced_plate", AdvancedPlateRecipe.Serializer::new);
    public static final RegistryObject<RecipeType<AdvancedPlateRecipe>> ADVANCED_PLATE_TYPE =
            TYPES.register("advanced_plate", () -> new ModRecipeType<>("advanced_plate"));

    public static final RegistryObject<RecipeSerializer<AncientAlphirineRecipe>> ANCIENT_ALPHIRINE_SERIALIZER =
            SERIALIZERS.register("ancient_alphirine", AncientAlphirineRecipe.Serializer::new);
    public static final RegistryObject<RecipeType<AncientAlphirineRecipe>> ANCIENT_ALPHIRINE_TYPE =
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
            this.id = new ResourceLocation(AdvancedBotany.MOD_ID, name);
        }

        @Override
        public String toString() {
            return id.toString();
        }
    }
}
