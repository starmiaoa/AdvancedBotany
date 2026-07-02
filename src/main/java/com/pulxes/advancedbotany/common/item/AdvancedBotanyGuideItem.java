package com.pulxes.advancedbotany.common.item;

import com.pulxes.advancedbotany.AdvancedBotany;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import vazkii.patchouli.api.PatchouliAPI;

public class AdvancedBotanyGuideItem extends Item {
    public static final ResourceLocation BOOK_ID = new ResourceLocation(AdvancedBotany.MOD_ID, "lexicon");

    public AdvancedBotanyGuideItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            PatchouliAPI.get().openBookGUI(serverPlayer, BOOK_ID);
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }
}
