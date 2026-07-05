package com.pulxes.advancedbotany.api;

import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.common.recipe.AdvancedPlateRecipe;
import com.pulxes.advancedbotany.common.recipe.AncientAlphirineRecipe;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public final class AdvancedBotanyAPI {
    public static final Rarity RARITY_NEBULA = Rarity.create("NEBULA", ChatFormatting.LIGHT_PURPLE);
    public static final List<AdvancedPlateRecipe> advancedPlateRecipes = new ArrayList<>();
    public static final List<AncientAlphirineRecipe> alphirineRecipes = new ArrayList<>();
    public static final List<TerraFarmlandList> farmlandList = new ArrayList<>();
    public static final List<ItemStack> relicList = new ArrayList<>();
    public static final List<ItemStack> diceList = new ArrayList<>();

    private AdvancedBotanyAPI() {
    }

    public static TerraFarmlandList registerFarmlandSeed(Block block, BlockState blockState) {
        TerraFarmlandList seed = new TerraFarmlandList(block, blockState);
        farmlandList.add(seed);
        return seed;
    }

    public static TerraFarmlandList registerFarmlandSeed(Block block) {
        return registerFarmlandSeed(block, block.defaultBlockState());
    }

    /**
     * Compatibility shim for the 1.7.10 API. Runtime recipes are data-driven in 1.20;
     * this list is retained for addons that still register or inspect AB recipes in code.
     */
    public static AdvancedPlateRecipe registerAdvancedPlateRecipe(ItemStack output, ItemStack input1, ItemStack input2,
                                                                  ItemStack input3, int mana, int color) {
        ResourceLocation id = new ResourceLocation(AdvancedBotany.MOD_ID, "api/advanced_plate_" + advancedPlateRecipes.size());
        AdvancedPlateRecipe recipe = new AdvancedPlateRecipe(id,
                Arrays.asList(Ingredient.of(input1), Ingredient.of(input2), Ingredient.of(input3)),
                output.copy(), mana, color);
        advancedPlateRecipes.add(recipe);
        return recipe;
    }

    /**
     * Compatibility shim for the 1.7.10 API. The Ancient Alphirine block reads JSON
     * recipes from the recipe manager; this preserves the old registration surface.
     */
    public static AncientAlphirineRecipe registerAlphirineRecipe(ItemStack output, ItemStack input, int chance) {
        ResourceLocation id = new ResourceLocation(AdvancedBotany.MOD_ID, "api/ancient_alphirine_" + alphirineRecipes.size());
        AncientAlphirineRecipe recipe = new AncientAlphirineRecipe(id, Ingredient.of(input), output.copy(), chance,
                AncientAlphirineRecipe.DEFAULT_MANA_USAGE);
        alphirineRecipes.add(recipe);
        return recipe;
    }

    public static void registerDefaultBoardEntries() {
        if (diceList.isEmpty()) {
            addBotaniaItem(diceList, "dice");
        }
        if (relicList.isEmpty()) {
            addBotaniaItem(relicList, "infinite_fruit");
            addBotaniaItem(relicList, "king_key");
            addBotaniaItem(relicList, "flugel_eye");
            addBotaniaItem(relicList, "thor_ring");
            addBotaniaItem(relicList, "odin_ring");
            addBotaniaItem(relicList, "loki_ring");
        }
    }

    public static void registerFateBoardRelic(Item item) {
        relicList.add(new ItemStack(item));
    }

    private static void addBotaniaItem(List<ItemStack> list, String name) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("botania", name));
        if (item != null) {
            list.add(new ItemStack(item));
        }
    }
}
