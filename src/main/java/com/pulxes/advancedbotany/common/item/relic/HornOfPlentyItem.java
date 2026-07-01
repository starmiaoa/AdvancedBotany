package com.pulxes.advancedbotany.common.item.relic;

import com.pulxes.advancedbotany.common.network.ModNetwork;
import com.pulxes.advancedbotany.registry.ModSounds;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.botania.api.mana.ManaItemHandler;

public class HornOfPlentyItem extends ModRelicItem {
    private static final String TAG_CHARGE_LOOT = "chargeLoot";
    private static final String TAG_LAST_CHARGE_LOOT = "lastChargeLoot";
    private static final short MAX_CHARGE_LOOT = 16;
    private static final int MANA_COST = 64_000;
    private static final int USE_DURATION = 42_000;
    private static final int USE_THRESHOLD = 48;
    private static final Method DROP_FROM_LOOT_TABLE = findDropFromLootTable();

    public HornOfPlentyItem(Properties properties) {
        super(properties);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!canUseRelic(stack, player)) {
            return InteractionResultHolder.fail(stack);
        }
        if (!hasChargeLoot(stack) && ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_COST, false)) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player) || !canUseRelic(stack, player)) {
            return;
        }
        int used = getUseDuration(stack) - remainingUseDuration;
        if (used > USE_THRESHOLD) {
            if (!level.isClientSide() && ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_COST, true)) {
                setChargeLoot(stack, MAX_CHARGE_LOOT);
                level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.2F, 4.0F);
            }
            player.stopUsingItem();
        } else if (level.isClientSide() && used % 19 == 0) {
            player.playSound(ModSounds.HORN_PLENTY_USING.get(), 2.4F, 2.47F);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!level.isClientSide() && entity instanceof ServerPlayer player) {
            short chargeLoot = getChargeLoot(stack);
            if (getLastChargeLoot(stack) != chargeLoot) {
                setLastChargeLoot(stack, chargeLoot);
                ModNetwork.sendHornChargeHud(player, chargeLoot);
            }
        }
    }

    @SubscribeEvent
    public void onLivingDrops(LivingDropsEvent event) {
        Entity sourceEntity = event.getSource().getEntity();
        if (!(sourceEntity instanceof Player player)) {
            return;
        }
        ItemStack horn = findChargedHorn(player);
        if (horn.isEmpty() || player.level().isClientSide()) {
            return;
        }
        LivingEntity victim = event.getEntity();
        if (victim instanceof WitherBoss || victim instanceof EnderDragon || !isValidEntity(victim)) {
            return;
        }
        if (player.level().random.nextInt(100) >= 20) {
            return;
        }
        if (dropExtraLoot(victim, event)) {
            setChargeLoot(horn, (short) (getChargeLoot(horn) - 1));
            player.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.9F, 0.8F);
        }
    }

    private ItemStack findChargedHorn(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof HornOfPlentyItem horn && horn.hasChargeLoot(stack) && horn.canUseRelic(stack, player)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static boolean dropExtraLoot(LivingEntity victim, LivingDropsEvent event) {
        if (DROP_FROM_LOOT_TABLE != null) {
            try {
                DROP_FROM_LOOT_TABLE.invoke(victim, event.getSource(), true);
                return true;
            } catch (ReflectiveOperationException ignored) {
            }
        }
        List<ItemEntity> extraDrops = new ArrayList<>();
        for (ItemEntity drop : event.getDrops()) {
            extraDrops.add(new ItemEntity(victim.level(), drop.getX(), drop.getY(), drop.getZ(), drop.getItem().copy()));
        }
        event.getDrops().addAll(extraDrops);
        return !extraDrops.isEmpty();
    }

    private static Method findDropFromLootTable() {
        for (String name : List.of("dropFromLootTable", "m_6668_")) {
            try {
                Method method = LivingEntity.class.getDeclaredMethod(name, DamageSource.class, boolean.class);
                method.setAccessible(true);
                return method;
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return null;
    }

    public static boolean isValidEntity(LivingEntity entity) {
        return true;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return hasChargeLoot(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * getChargeLoot(stack) / MAX_CHARGE_LOOT);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x55FF55;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_DURATION;
    }

    public void setLastChargeLoot(ItemStack stack, short count) {
        stack.getOrCreateTag().putShort(TAG_LAST_CHARGE_LOOT, count);
    }

    public short getLastChargeLoot(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getShort(TAG_LAST_CHARGE_LOOT);
    }

    public void setChargeLoot(ItemStack stack, short count) {
        stack.getOrCreateTag().putShort(TAG_CHARGE_LOOT, (short) Math.max(0, Math.min(MAX_CHARGE_LOOT, count)));
    }

    public short getChargeLoot(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getShort(TAG_CHARGE_LOOT);
    }

    public boolean hasChargeLoot(ItemStack stack) {
        return getChargeLoot(stack) > 0;
    }
}
