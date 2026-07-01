package com.pulxes.advancedbotany.api;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class AdvancedBotanyAPI {
    public static final Rarity RARITY_NEBULA = Rarity.create("NEBULA", ChatFormatting.LIGHT_PURPLE);
    public static final List<TerraFarmlandList> farmlandList = new ArrayList<>();

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
}
