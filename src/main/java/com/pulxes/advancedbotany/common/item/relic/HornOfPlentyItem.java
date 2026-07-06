package com.pulxes.advancedbotany.common.item.relic;

import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.common.network.ModNetwork;
import com.pulxes.advancedbotany.common.item.ItemComponentData;
import com.pulxes.advancedbotany.registry.ModSounds;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import vazkii.botania.api.mana.ManaItemHandler;

public class HornOfPlentyItem extends ModRelicItem {
    private static final String TAG_CHARGE_LOOT = "chargeLoot";
    private static final String TAG_LAST_CHARGE_LOOT = "lastChargeLoot";
    private static final short MAX_CHARGE_LOOT = 16;
    private static final int MANA_COST = 64_000;
    private static final int USE_DURATION = 42_000;
    private static final int USE_THRESHOLD = 48;
    private static final TagKey<EntityType<?>> HORN_BLACKLIST = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(AdvancedBotany.MOD_ID, "horn_of_plenty_blacklist"));

    public HornOfPlentyItem(Properties properties) {
        super(properties);
        NeoForge.EVENT_BUS.register(this);
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
        int used = getUseDuration(stack, player) - remainingUseDuration;
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
        if (victim instanceof WitherBoss || victim instanceof EnderDragon
                || victim.getType().is(Tags.EntityTypes.BOSSES) || !isValidEntity(victim)) {
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
        int lootingLevel = (int) (getLootingLevel(serverLevel, player) * 1.5F);
        LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(victim.getLootTable());
        Entity killer = event.getSource().getEntity();
        Entity directKiller = event.getSource().getDirectEntity();
        LootParams.Builder builder = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.THIS_ENTITY, victim)
                .withParameter(LootContextParams.ORIGIN, victim.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, event.getSource())
                .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, killer)
                .withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, directKiller)
                .withLuck(killer instanceof Player killerPlayer ? killerPlayer.getLuck() : 0.0F);
        List<ItemStack> drops = getRandomItemsWithLooting(player, serverLevel, lootingLevel, lootTable, builder);
        for (ItemStack drop : drops) {
            victim.spawnAtLocation(drop);
        }
        return !drops.isEmpty();
    }

    private static List<ItemStack> getRandomItemsWithLooting(Player player, ServerLevel level, int lootingLevel,
                                                            LootTable lootTable, LootParams.Builder builder) {
        ItemStack originalMainHand = player.getMainHandItem();
        ItemStack lootingStack = originalMainHand.isEmpty() ? new ItemStack(Items.DIAMOND_SWORD) : originalMainHand.copy();
        Optional<Holder.Reference<Enchantment>> looting = level.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolder(Enchantments.LOOTING);
        looting.ifPresent(holder -> EnchantmentHelper.updateEnchantments(lootingStack, enchantments -> {
            if (lootingLevel > 0) {
                enchantments.set(holder, lootingLevel);
            } else {
                enchantments.removeIf(enchantment -> enchantment.is(Enchantments.LOOTING));
            }
        }));
        player.setItemInHand(InteractionHand.MAIN_HAND, lootingStack);
        try {
            return lootTable.getRandomItems(builder.create(LootContextParamSets.ENTITY));
        } finally {
            player.setItemInHand(InteractionHand.MAIN_HAND, originalMainHand);
        }
    }

    private static int getLootingLevel(ServerLevel level, Player player) {
        return level.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolder(Enchantments.LOOTING)
                .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, player.getMainHandItem()))
                .orElse(0);
    }

    public static boolean isValidEntity(LivingEntity entity) {
        return !entity.getType().is(HORN_BLACKLIST);
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
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION;
    }

    public void setLastChargeLoot(ItemStack stack, short count) {
        ItemComponentData.putShort(stack, TAG_LAST_CHARGE_LOOT, count);
    }

    public short getLastChargeLoot(ItemStack stack) {
        return ItemComponentData.getShort(stack, TAG_LAST_CHARGE_LOOT);
    }

    public void setChargeLoot(ItemStack stack, short count) {
        ItemComponentData.putShort(stack, TAG_CHARGE_LOOT, (short) Math.max(0, Math.min(MAX_CHARGE_LOOT, count)));
    }

    public short getChargeLoot(ItemStack stack) {
        return ItemComponentData.getShort(stack, TAG_CHARGE_LOOT);
    }

    public boolean hasChargeLoot(ItemStack stack) {
        return getChargeLoot(stack) > 0;
    }
}
