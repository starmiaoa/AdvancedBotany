package com.pulxes.advancedbotany.api;

import net.minecraft.core.BlockPos;
import vazkii.botania.api.block.WandBindable;

/**
 * Compatibility API for bindable blocks that expose extra client-side render points.
 */
public interface IBoundRender extends WandBindable {
    BlockPos[] getBlocksCoord();
}
