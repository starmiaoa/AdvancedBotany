package com.pulxes.advancedbotany.common.item.equipment;

import com.pulxes.advancedbotany.api.AdvancedBotanyAPI;
import com.pulxes.advancedbotany.registry.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import vazkii.botania.api.mana.ManaItemHandler;

public class NebulaRodItem extends Item {
    public NebulaRodItem(Properties properties) {
        super(properties.stacksTo(1).durability(AdvancedBotanyEquipment.NEBULA_ROD_MAX_DAMAGE).rarity(AdvancedBotanyAPI.RARITY_NEBULA));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getDamageValue() == 0) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof ServerPlayer player)) {
            return;
        }
        int useTime = getUseDuration(stack) - remainingUseDuration;
        if (useTime <= 110 || player.isShiftKeyDown()) {
            return;
        }

        BlockPos target = findTopBlock(level, player);
        if (target == null) {
            player.stopUsingItem();
            player.displayClientMessage(Component.translatable("ab.nebulaRod.notTeleporting").withStyle(ChatFormatting.DARK_PURPLE), true);
            return;
        }

        EntityTeleportEvent.EnderEntity event = new EntityTeleportEvent.EnderEntity(player, target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            player.stopUsingItem();
            player.displayClientMessage(Component.translatable("ab.nebulaRod.notTeleportingEvent").withStyle(ChatFormatting.DARK_PURPLE), true);
            return;
        }

        player.connection.teleport(event.getTargetX(), event.getTargetY(), event.getTargetZ(), player.getYRot(), player.getXRot());
        level.playSound(null, player.blockPosition(), ModSounds.NEBULA_ROD.get(), SoundSource.PLAYERS, 1.2F, 1.2F);
        if (!player.getAbilities().instabuild) {
            stack.setDamageValue(AdvancedBotanyEquipment.NEBULA_ROD_MAX_DAMAGE);
        }
        player.stopUsingItem();
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide() && entity instanceof Player player && entity.tickCount % AdvancedBotanyEquipment.NEBULA_ROD_REPAIR_INTERVAL == 0
                && stack.getDamageValue() > 0
                && ManaItemHandler.instance().requestManaExactForTool(stack, player, AdvancedBotanyEquipment.NEBULA_ROD_MANA_PER_REPAIR, true)) {
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

    private static BlockPos findTopBlock(Level level, Player player) {
        Vec3 look = player.getLookAngle().normalize();
        int limit = AdvancedBotanyEquipment.NEBULA_ROD_XZ_LIMIT;
        for (int distance = 256; distance > 8; distance--) {
            int x = Mth.clamp(Mth.floor(player.getX() + look.x * distance), -(limit - 1), limit - 1);
            int z = Mth.clamp(Mth.floor(player.getZ() + look.z * distance), -(limit - 1), limit - 1);
            int top = level.getMaxBuildHeight() - 2;
            for (int y = top; y > level.getMinBuildHeight(); y--) {
                BlockPos solidPos = new BlockPos(x, y, z);
                BlockState state = level.getBlockState(solidPos);
                if (state.isAir() || state.is(Blocks.BEDROCK)) {
                    continue;
                }
                if (level.getBlockState(solidPos.above()).isAir() && level.getBlockState(solidPos.above(2)).isAir()) {
                    return solidPos.above();
                }
            }
        }
        return null;
    }
}
