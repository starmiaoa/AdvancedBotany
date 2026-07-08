package com.pulxes.advancedbotany.common.item.equipment;

import com.pulxes.advancedbotany.api.AdvancedBotanyAPI;
import com.pulxes.advancedbotany.common.entity.EntityNebulaBlaze;
import com.pulxes.advancedbotany.registry.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import vazkii.botania.api.mana.ManaItemHandler;

public class NebulaBlazeItem extends Item {
    public NebulaBlazeItem(Properties properties) {
        super(properties.stacksTo(1).rarity(AdvancedBotanyAPI.RARITY_NEBULA));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide() || !(livingEntity instanceof Player player)) {
            return;
        }
        if (remainingUseDuration % 5 == 2 && ManaItemHandler.instance().requestManaExactForTool(stack, player, AdvancedBotanyEquipment.NEBULA_BLAZE_MANA, true)) {
            EntityNebulaBlaze blaze = new EntityNebulaBlaze(level, player);
            blaze.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F); // vanilla throwable spread, as the original inherited
            level.addFreshEntity(blaze);
            level.playSound(null, player.blockPosition(), ModSounds.NEBULA_BLAZE.get(), SoundSource.PLAYERS, 0.4F, 1.4F);
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72_000;
    }
}
