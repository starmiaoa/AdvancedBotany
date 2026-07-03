package com.pulxes.advancedbotany.common.item.equipment;

import com.pulxes.advancedbotany.registry.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import vazkii.botania.client.fx.WispParticleData;
import vazkii.botania.api.mana.ManaItemHandler;

import java.util.List;

public class AquaSwordItem extends SwordItem {
    public AquaSwordItem(Properties properties) {
        super(AdvancedBotanyEquipment.MITHRIL, 3, -2.4F, properties.stacksTo(1));
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity target) {
        Level level = player.level();
        if (level.isClientSide()) {
            if (ManaItemHandler.instance().requestManaExactForTool(stack, player, AdvancedBotanyEquipment.AQUA_SWORD_SPLASH_MANA, false)) {
                spawnSplashParticles(level, target);
            }
            return false;
        }

        AABB bounds = target.getBoundingBox().inflate(1.7D);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, bounds, entity -> canDamage(player, entity));
        boolean splashed = false;
        for (LivingEntity living : entities) {
            if (!ManaItemHandler.instance().requestManaExactForTool(stack, player, AdvancedBotanyEquipment.AQUA_SWORD_SPLASH_MANA, false)) {
                continue;
            }
            if (living.hurt(level.damageSources().playerAttack(player), AdvancedBotanyEquipment.MITHRIL.getAttackDamageBonus() / 2.0F)) {
                ManaItemHandler.instance().requestManaExactForTool(stack, player, AdvancedBotanyEquipment.AQUA_SWORD_SPLASH_MANA, true);
                Vec3 look = player.getLookAngle().normalize();
                living.push(look.x * 1.35D, look.y / 1.8D, look.z * 1.35D);
                splashed = true;
            }
        }
        if (splashed) {
            level.playSound(null, player.blockPosition(), ModSounds.AQUA_SWORD.get(), SoundSource.PLAYERS, 1.2F, 1.2F);
        }
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) {
            return;
        }
        if (level.isClientSide()) {
            spawnHoldEffects(level, player, stack, remainingUseDuration);
            return;
        }
        AABB bounds = player.getBoundingBox().inflate(2.75D);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, bounds, entity -> canDamage(player, entity));
        for (LivingEntity living : entities) {
            double distance = living.distanceTo(player) / 2.5D;
            if (!ManaItemHandler.instance().requestManaExactForTool(stack, player, AdvancedBotanyEquipment.AQUA_SWORD_HOLD_MANA, false)) {
                continue;
            }
            if (living.hurt(level.damageSources().playerAttack(player), 1.0F)) {
                ManaItemHandler.instance().requestManaExactForTool(stack, player, AdvancedBotanyEquipment.AQUA_SWORD_HOLD_MANA, true);
                if (distance <= 1.0D) {
                    Vec3 delta = living.position().subtract(player.position()).multiply(1.0D, 0.0D, 1.0D);
                    if (delta.lengthSqr() > 0.0D) {
                        Vec3 push = delta.normalize().scale(1.2D);
                        living.push(push.x, 0.0D, push.z);
                    }
                }
            }
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

    private static boolean canDamage(Player attacker, LivingEntity living) {
        if (living == attacker || !living.isAlive()) {
            return false;
        }
        if (living instanceof Player && attacker.level() instanceof ServerLevel serverLevel && !serverLevel.getServer().isPvpAllowed()) {
            return false;
        }
        return true;
    }

    private static void spawnSplashParticles(Level level, Entity target) {
        double x = target.getX();
        double y = target.getY() + target.getEyeHeight();
        double z = target.getZ();
        for (int i = 0; i < 24; i++) {
            float mx = (float) ((level.random.nextDouble() - 0.5D) * 0.12D);
            float my = (float) ((level.random.nextDouble() - 0.5D) * 0.12D);
            float mz = (float) ((level.random.nextDouble() - 0.5D) * 0.12D);
            level.addParticle(WispParticleData.wisp(
                    0.17F + level.random.nextFloat() * 0.3F,
                    0.0F,
                    level.random.nextFloat() * 0.35F,
                    1.0F - level.random.nextFloat() * 0.4F,
                    0.512F
            ), x, y, z, mx, my, mz);
        }
    }

    private static void spawnHoldEffects(Level level, Player player, ItemStack stack, int remainingUseDuration) {
        if (!ManaItemHandler.instance().requestManaExactForTool(stack, player, AdvancedBotanyEquipment.AQUA_SWORD_HOLD_MANA, false)) {
            return;
        }

        int usedTicks = getUseTicks(stack, remainingUseDuration);
        if (usedTicks % 13 == 0) {
            level.playLocalSound(player.getX(), player.getY(), player.getZ(), SoundEvents.WATER_AMBIENT, SoundSource.PLAYERS, 0.8F, 1.7F, false);
        }

        int time = usedTicks % 120 + 1;
        if (time > 50) {
            time = 120 - time;
        }
        int wispCount = 8;
        double tickIncrement = 360.0D / wispCount;
        double ticks = time * 10.0D - tickIncrement;
        double radius = Math.sin(1.4F);
        for (int i = 0; i < wispCount; i++) {
            double x = player.getX() + Math.sin(ticks * Math.PI / 180.0D) * radius;
            double y = player.getY() + ticks * 0.001D + 0.1D;
            double z = player.getZ() + Math.cos(ticks * Math.PI / 180.0D) * radius;
            level.addParticle(WispParticleData.wisp(
                    0.3F,
                    0.0F,
                    level.random.nextFloat() * 0.35F,
                    1.0F - level.random.nextFloat() * 0.4F,
                    0.7F
            ), x, y, z, 0.0D, -0.1D + level.random.nextFloat() * 0.05D, 0.0D);
            ticks += tickIncrement;
        }
    }

    private static int getUseTicks(ItemStack stack, int remainingUseDuration) {
        return stack.getUseDuration() - remainingUseDuration;
    }
}
