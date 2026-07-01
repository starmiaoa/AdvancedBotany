package com.pulxes.advancedbotany.common.item.equipment;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import vazkii.botania.common.handler.BotaniaSounds;

public class MaterialDestroyerItem extends DiggerItem {
    public static final String TAG_ENABLED = "enabled";

    public MaterialDestroyerItem(Properties properties) {
        super(0.0F, -2.8F, AdvancedBotanyEquipment.MITHRIL, BlockTags.MINEABLE_WITH_PICKAXE, properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        setEnabled(stack, !isEnabled(stack));
        if (!level.isClientSide()) {
            level.playSound(null, player.blockPosition(), BotaniaSounds.terraPickMode, SoundSource.PLAYERS, 0.5F, 0.4F);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return isEnabled(stack) ? 135.0F : 0.0F;
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return state.getBlock() != Blocks.BEDROCK;
    }

    @Override
    public boolean isCorrectToolForDrops(BlockState state) {
        return state.getBlock() != Blocks.BEDROCK;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return toolAction == ToolActions.PICKAXE_DIG || toolAction == ToolActions.AXE_DIG || toolAction == ToolActions.SHOVEL_DIG;
    }

    public static boolean isEnabled(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(TAG_ENABLED);
    }

    public static void setEnabled(ItemStack stack, boolean enabled) {
        stack.getOrCreateTag().putBoolean(TAG_ENABLED, enabled);
    }
}
