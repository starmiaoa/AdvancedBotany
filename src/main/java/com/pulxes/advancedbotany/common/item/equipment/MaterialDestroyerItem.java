package com.pulxes.advancedbotany.common.item.equipment;

import com.pulxes.advancedbotany.common.item.ItemComponentData;
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
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.ItemAbilities;
import vazkii.botania.common.handler.BotaniaSounds;

public class MaterialDestroyerItem extends DiggerItem {
    public static final String TAG_ENABLED = "enabled";

    public MaterialDestroyerItem(Properties properties) {
        super(AdvancedBotanyEquipment.MITHRIL, BlockTags.MINEABLE_WITH_PICKAXE,
                properties.stacksTo(1).attributes(DiggerItem.createAttributes(AdvancedBotanyEquipment.MITHRIL, 0.0F, -2.8F)));
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
        return isEnabled(stack) ? super.getDestroySpeed(stack, state) + 135.0F : 0.0F;
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return state.getBlock() != Blocks.BEDROCK;
    }

    public boolean isCorrectToolForDrops(BlockState state) {
        return state.getBlock() != Blocks.BEDROCK;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility toolAction) {
        return toolAction == ItemAbilities.PICKAXE_DIG || toolAction == ItemAbilities.AXE_DIG || toolAction == ItemAbilities.SHOVEL_DIG;
    }

    public static boolean isEnabled(ItemStack stack) {
        return ItemComponentData.getBoolean(stack, TAG_ENABLED);
    }

    public static void setEnabled(ItemStack stack, boolean enabled) {
        ItemComponentData.putBoolean(stack, TAG_ENABLED, enabled);
    }
}
