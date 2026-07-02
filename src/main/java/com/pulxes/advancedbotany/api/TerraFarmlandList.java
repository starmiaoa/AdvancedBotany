package com.pulxes.advancedbotany.api;

import java.util.Objects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class TerraFarmlandList {
    private final Block block;
    private final BlockState blockState;

    public TerraFarmlandList(Block block, BlockState blockState) {
        this.block = Objects.requireNonNull(block, "block");
        this.blockState = Objects.requireNonNull(blockState, "blockState");
    }

    public TerraFarmlandList(Block block) {
        this(block, block.defaultBlockState());
    }

    public Block getBlock() {
        return block;
    }

    public BlockState getBlockState() {
        return blockState;
    }
}
