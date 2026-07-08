package com.pulxes.advancedbotany.common.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class MithrilStorageBlock extends Block {
    public MithrilStorageBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.0F, 10.0F)
                .requiresCorrectToolForDrops()
                .sound(SoundType.METAL));
    }
}
