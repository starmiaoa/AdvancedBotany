package com.pulxes.advancedbotany.api;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import vazkii.botania.common.item.BotaniaItems;

public final class AdvancedBotanyAPI {
    public static final Rarity RARITY_NEBULA = Rarity.create("NEBULA", ChatFormatting.LIGHT_PURPLE);
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
            diceList.add(new ItemStack(BotaniaItems.dice));
        }
        if (relicList.isEmpty()) {
            relicList.add(new ItemStack(BotaniaItems.infiniteFruit));
            relicList.add(new ItemStack(BotaniaItems.kingKey));
            relicList.add(new ItemStack(BotaniaItems.flugelEye));
            relicList.add(new ItemStack(BotaniaItems.thorRing));
            relicList.add(new ItemStack(BotaniaItems.odinRing));
            relicList.add(new ItemStack(BotaniaItems.lokiRing));
        }
    }
}
