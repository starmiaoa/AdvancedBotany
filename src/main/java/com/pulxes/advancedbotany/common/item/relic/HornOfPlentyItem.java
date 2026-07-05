package com.pulxes.advancedbotany.common.item.relic;

import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.common.network.ModNetwork;
import com.pulxes.advancedbotany.registry.ModSounds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import vazkii.botania.api.mana.ManaItemHandler;

public class HornOfPlentyItem extends ModRelicItem {
    private static final String TAG_CHARGE_LOOT = "chargeLoot";
    private static final String TAG_LAST_CHARGE_LOOT = "lastChargeLoot";
    private static final short MAX_CHARGE_LOOT = 16;
    private static final int MANA_COST = 64_000;
    private static final int USE_DURATION = 42_000;
    private static final int USE_THRESHOLD = 48;
    private static final TagKey<EntityType<?>> HORN_BLACKLIST = TagKey.create(Registries.ENTITY_TYPE,
            new ResourceLocation(AdvancedBotany.MOD_ID, "horn_of_plenty_blacklist"));
    private static final ThreadLocal<ExtraLootContext> EXTRA_LOOT_CONTEXT = new ThreadLocal<>();

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
        if (dropExtraLoot(player, victim, event)) {
            setChargeLoot(horn, (short) (getChargeLoot(horn) - 1));
            player.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.9F, 0.8F);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLootingLevel(LootingLevelEvent event) {
        ExtraLootContext context = EXTRA_LOOT_CONTEXT.get();
        if (context != null && event.getEntity() == context.victim && event.getDamageSource() == context.damageSource) {
            event.setLootingLevel(context.lootingLevel);
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

    private static boolean dropExtraLoot(Player player, LivingEntity victim, LivingDropsEvent event) {
        if (!(victim.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        int lootingLevel = (int) (event.getLootingLevel() * 1.5F);
        ResourceLocation lootTableId = victim.getLootTable();
        LootTable lootTable = serverLevel.getServer().getLootData().getLootTable(lootTableId);
        Entity killer = event.getSource().getEntity();
        Entity directKiller = event.getSource().getDirectEntity();
        LootParams.Builder builder = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.THIS_ENTITY, victim)
                .withParameter(LootContextParams.ORIGIN, victim.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, event.getSource())
                .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player)
                .withLuck(player.getLuck())
                .withOptionalParameter(LootContextParams.KILLER_ENTITY, killer)
                .withOptionalParameter(LootContextParams.DIRECT_KILLER_ENTITY, directKiller);
        List<ItemStack> drops = getRawEntityLootWithLooting(victim, event, lootingLevel, lootTable, builder);
        for (ItemStack drop : drops) {
            victim.spawnAtLocation(drop);
        }
        return !drops.isEmpty();
    }

    private static List<ItemStack> getRawEntityLootWithLooting(LivingEntity victim, LivingDropsEvent event,
                                                               int lootingLevel, LootTable lootTable,
                                                               LootParams.Builder builder) {
        List<ItemStack> drops = new ArrayList<>();
        EXTRA_LOOT_CONTEXT.set(new ExtraLootContext(victim, event.getSource(), lootingLevel));
        try {
            lootTable.getRandomItemsRaw(builder.create(LootContextParamSets.ENTITY), drop -> {
                if (!drop.isEmpty()) {
                    drops.add(drop.copy());
                }
            });
        } finally {
            EXTRA_LOOT_CONTEXT.remove();
        }
        return drops;
    }

    public static boolean isValidEntity(LivingEntity entity) {
        return !entity.getType().is(HORN_BLACKLIST);
    }

    private record ExtraLootContext(LivingEntity victim, net.minecraft.world.damagesource.DamageSource damageSource,
                                    int lootingLevel) {
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
