package com.pulxes.advancedbotany.common.item.relic;

import com.pulxes.advancedbotany.common.entity.EntityManaVine;
import com.pulxes.advancedbotany.registry.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import vazkii.botania.api.mana.ManaItemHandler;

public class FreyrSlingshotItem extends ModRelicItem {
    private static final int MANA_COST = 5_000;
    private static final int USE_DURATION = 42_000;

    public FreyrSlingshotItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!canUseRelic(stack, player)) {
            return InteractionResultHolder.fail(stack);
        }
        if (ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_COST, false)) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeLeft) {
        if (!(livingEntity instanceof Player player) || !canUseRelic(stack, player)) {
            return;
        }
        int useTicks = getUseDuration(stack) - timeLeft;
        if (getPowerForTime(useTicks) < 1.0F) {
            return;
        }
        if (!level.isClientSide() && ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_COST, true)) {
            EntityManaVine vine = new EntityManaVine(level, player);
            level.addFreshEntity(vine);
            level.playSound(null, player.blockPosition(), ModSounds.FREYR_SLINGSHOT.get(), SoundSource.PLAYERS, 0.4F, 2.8F);
        }
    }

    private static float getPowerForTime(int useTicks) {
        float power = (float) useTicks / 20.0F;
        power = (power * power + power * 2.0F) / 3.0F;
        return Math.min(power, 1.0F);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_DURATION;
    }
}
