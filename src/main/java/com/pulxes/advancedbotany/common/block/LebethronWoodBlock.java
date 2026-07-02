package com.pulxes.advancedbotany.common.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class LebethronWoodBlock extends Block {
    public LebethronWoodBlock(int lightLevel) {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOD)
                .strength(6.0F)
                .sound(SoundType.WOOD)
                .ignitedByLava()
                .lightLevel(state -> lightLevel));
    }

    public LebethronWoodBlock() {
        this(0);
    }
}
