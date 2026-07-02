package com.pulxes.advancedbotany.api;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.BuiltInRegistries;

public final class AdvancedBotanyAPI {
    public static final Rarity RARITY_NEBULA = Rarity.EPIC;
    public static final Rarity RARITY_WILD_HUNT = Rarity.RARE;
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
        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("botania", name));
        if (item != null) {
            list.add(new ItemStack(item));
        }
    }
}
