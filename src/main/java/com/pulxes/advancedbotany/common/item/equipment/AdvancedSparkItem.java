package com.pulxes.advancedbotany.common.item.equipment;

import com.pulxes.advancedbotany.common.entity.EntityAdvancedSpark;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.spark.SparkAttachable;
import vazkii.botania.forge.CapabilityUtil;

public class AdvancedSparkItem extends Item {
    public AdvancedSparkItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        SparkAttachable attachable = CapabilityUtil.findCapability(BotaniaForgeCapabilities.SPARK_ATTACHABLE, level, pos, state, blockEntity);
        ItemStack stack = context.getItemInHand();
        if (attachable == null || !attachable.canAttachSpark(stack) || attachable.getAttachedSpark() != null) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            EntityAdvancedSpark spark = new EntityAdvancedSpark(level);
            spark.setPos(pos.getX() + 0.5D, pos.getY() + 1.5D, pos.getZ() + 0.5D);
            level.addFreshEntity(spark);
            attachable.attachSpark(spark);
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
            if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
