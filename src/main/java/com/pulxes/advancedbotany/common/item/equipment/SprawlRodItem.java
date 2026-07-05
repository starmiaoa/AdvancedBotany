package com.pulxes.advancedbotany.common.item.equipment;

import com.pulxes.advancedbotany.common.entity.EntitySeed;
import java.awt.Color;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.common.item.GrassSeedsItem;

public class SprawlRodItem extends Item {
    public SprawlRodItem(Properties properties) {
        super(properties.stacksTo(1).durability(AdvancedBotanyEquipment.SPRAWL_ROD_MAX_DAMAGE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack rod = player.getItemInHand(hand);
        if (rod.getDamageValue() == 0 && findGrassSeed(player) >= 0) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(rod);
        }
        return InteractionResultHolder.pass(rod);
    }

    @Override
    public void releaseUsing(ItemStack rod, Level level, LivingEntity livingEntity, int timeLeft) {
        if (!(livingEntity instanceof Player player)) {
            return;
        }
        int seedSlot = findGrassSeed(player);
        if (seedSlot < 0) {
            return;
        }
        int useTime = getUseDuration(rod) - timeLeft;
        if (!level.isClientSide()) {
            ItemStack seedStack = player.getInventory().getItem(seedSlot);
            EntitySeed seed = new EntitySeed(level, player);
            seed.setSeed(seedStack);
            seed.setRadius((int) (Math.min((float) useTime, 128.0F) / 128.0F * AdvancedBotanyEquipment.SPRAWL_ROD_MAX_AREA));
            seed.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(seed);
        }
        ItemStack seed = player.getInventory().getItem(seedSlot);
        seed.shrink(1);
        if (!player.getAbilities().instabuild) {
            rod.setDamageValue(Math.min(AdvancedBotanyEquipment.SPRAWL_ROD_MAX_DAMAGE,
                    (int) ((float) useTime / 128.0F * AdvancedBotanyEquipment.SPRAWL_ROD_MAX_DAMAGE)));
        }
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!level.isClientSide() || !(livingEntity instanceof Player player)) {
            return;
        }

        int useTime = getUseDuration(stack) - remainingUseDuration;
        if (useTime % 2 == 0 && useTime != 0) {
            return;
        }

        int seedSlot = findGrassSeed(player);
        if (seedSlot < 0) {
            return;
        }

        int ticks = Math.min(128, useTime);
        float charge = (float) ticks / 128.0F;
        Vec3 look = player.getLookAngle();
        double x = player.getX() + look.x * 1.4D + (level.random.nextDouble() - 0.5D) * charge * 0.3D;
        double y = player.getY() + look.y * 1.4D + (level.random.nextDouble() - 0.5D) * charge * 0.3D;
        double z = player.getZ() + look.z * 1.4D + (level.random.nextDouble() - 0.5D) * charge * 0.3D;
        Color color = EntitySeed.getSeedColor(player.getInventory().getItem(seedSlot));
        float size = Math.max(0.05F, 0.5F * charge - level.random.nextFloat() * 0.1F);

        level.addParticle(WispParticleData.wisp(
                        size,
                        color.getRed() / 255.0F,
                        color.getGreen() / 255.0F,
                        color.getBlue() / 255.0F,
                        0.5F),
                x, y, z, 0.0D, 0.0D, 0.5D);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide() && entity instanceof Player player && stack.getDamageValue() > 0
                && ManaItemHandler.instance().requestManaExactForTool(stack, player, AdvancedBotanyEquipment.SPRAWL_ROD_MANA_PER_REPAIR, true)) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72_000;
    }

    private static int findGrassSeed(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).getItem() instanceof GrassSeedsItem) {
                return i;
            }
        }
        return -1;
    }
}
