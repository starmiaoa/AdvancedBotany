package com.pulxes.advancedbotany.common.item.equipment;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.pulxes.advancedbotany.api.AdvancedBotanyAPI;
import com.pulxes.advancedbotany.common.entity.EntitySword;
import com.pulxes.advancedbotany.common.network.ModNetwork;
import com.pulxes.advancedbotany.registry.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.api.mana.ManaItemHandler;

import java.util.List;
import java.util.UUID;

public class SpaceBladeItem extends SwordItem {
    public static final String TAG_MANA = "mana";
    public static final String TAG_POST_ATTACK_TICK = "postAttackTick";
    public static final String TAG_TICK = "tick";
    public static final String TAG_ENABLED_MODE = "isEnabledMode";
    private static final UUID SPEED_UUID = UUID.nameUUIDFromBytes("advancedbotany:space_blade_speed".getBytes(java.nio.charset.StandardCharsets.UTF_8));

    public SpaceBladeItem(Properties properties) {
        super(AdvancedBotanyEquipment.MITHRIL, 3, -2.4F, properties.stacksTo(1).rarity(AdvancedBotanyAPI.RARITY_NEBULA));
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity target) {
        if (!player.level().isClientSide()) {
            stack.getOrCreateTag().putInt(TAG_POST_ATTACK_TICK, 3);
            if (getLevel(stack) >= 3 && isEnabledMode(stack)) {
                float size = getLevel(stack) >= 5 ? 3.5F : getLevel(stack) >= 4 ? 2.5F : 1.5F;
                AABB bounds = target.getBoundingBox().inflate(size, 1.7D, size);
                List<LivingEntity> entities = player.level().getEntitiesOfClass(LivingEntity.class, bounds, living -> canDamage(player, living));
                for (LivingEntity living : entities) {
                    living.hurt(player.level().damageSources().playerAttack(player), getSwordDamage(stack));
                }
            }
        }
        return false;
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if (!(entity instanceof Player player) || player.level().isClientSide()) {
            return false;
        }
        if (player.getMainHandItem() != stack || player.isUsingItem()) {
            return false;
        }
        if (getLevel(stack) >= 1 && getPostAttackTick(stack) == 0
                && ManaItemHandler.instance().requestManaExactForTool(stack, player, AdvancedBotanyEquipment.SPACE_BLADE_PROJECTILE_MANA, true)) {
            EntitySword sword = new EntitySword(player.level(), player);
            sword.setDamage(getSwordDamage(stack));
            sword.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 0.0F);
            sword.setDeltaMovement(sword.getDeltaMovement().scale(0.2D));
            player.level().addFreshEntity(sword);
            player.level().playSound(null, player.blockPosition(), ModSounds.BLADE_SPACE.get(), SoundSource.PLAYERS, 0.5F, 3.6F);
        }
        return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!(entity instanceof Player player) || level.isClientSide()) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        int postAttackTick = tag.getInt(TAG_POST_ATTACK_TICK);
        if (postAttackTick > 0 && !player.isUsingItem()) {
            tag.putInt(TAG_POST_ATTACK_TICK, postAttackTick - 1);
        }
        int tick = tag.getInt(TAG_TICK);
        if (tick > 0) {
            tag.putInt(TAG_TICK, tick - 1);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (getCooldownTicks(stack) != 0) {
            return InteractionResultHolder.pass(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeLeft) {
        if (!(livingEntity instanceof Player player)) {
            return;
        }
        int useTime = getUseDuration(stack) - timeLeft;
        if (useTime >= 4) {
            return;
        }
        if (player.isShiftKeyDown() && getLevel(stack) >= 3) {
            if (!level.isClientSide()) {
                stack.getOrCreateTag().putBoolean(TAG_ENABLED_MODE, !isEnabledMode(stack));
            }
            return;
        }
        if (!level.isClientSide() && !player.isShiftKeyDown() && getCooldownTicks(stack) == 0 && getLevel(stack) >= 2) {
            dash(player);
            if (player instanceof ServerPlayer serverPlayer) {
                ModNetwork.sendSpaceBladeDash(serverPlayer);
            }
            stack.getOrCreateTag().putInt(TAG_TICK, AdvancedBotanyEquipment.SPACE_BLADE_DASH_COOLDOWN);
            level.playSound(null, player.blockPosition(), ModSounds.BLADE_SPACE.get(), SoundSource.PLAYERS, 2.3F, 1.2F);
        }
    }

    public static void dash(Player player) {
        Vec3 look = player.getLookAngle().normalize();
        player.setDeltaMovement(player.getDeltaMovement().add(look.x * 3.25D, look.y / 1.6D, look.z * 3.25D));
        player.hurtMarked = true;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72_000;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getCooldownTicks(stack) != 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F - (float) getCooldownTicks(stack) * 13.0F / AdvancedBotanyEquipment.SPACE_BLADE_DASH_COOLDOWN);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return Mth.hsvToRgb(0.836F, 1.0F - (float) getCooldownTicks(stack) / AdvancedBotanyEquipment.SPACE_BLADE_DASH_COOLDOWN, 1.0F);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot != EquipmentSlot.MAINHAND) {
            return super.getAttributeModifiers(slot, stack);
        }
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", Math.max(0.0F, getSwordDamage(stack) - 1.0F), AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(SPEED_UUID, "Weapon speed", 0.25D, AttributeModifier.Operation.MULTIPLY_BASE));
        return builder.build();
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new SimpleCapabilityProvider<>(BotaniaForgeCapabilities.MANA_ITEM, new SpaceBladeManaItem(stack));
    }

    public static int getMana(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(TAG_MANA);
    }

    public static void setMana(ItemStack stack, int mana) {
        stack.getOrCreateTag().putInt(TAG_MANA, Math.max(0, mana));
    }

    public static int getLevel(ItemStack stack) {
        int mana = getMana(stack);
        for (int i = AdvancedBotanyEquipment.SPACE_BLADE_LEVELS.length - 1; i > 0; i--) {
            if (mana >= AdvancedBotanyEquipment.SPACE_BLADE_LEVELS[i]) {
                return i;
            }
        }
        return 0;
    }

    public static boolean isEnabledMode(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(TAG_ENABLED_MODE);
    }

    public static int getCooldownTicks(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(TAG_TICK);
    }

    private static int getPostAttackTick(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(TAG_POST_ATTACK_TICK);
    }

    private static float getSwordDamage(ItemStack stack) {
        int level = getLevel(stack);
        return 4.0F + Math.round(AdvancedBotanyEquipment.MITHRIL.getAttackDamageBonus() + (float) (level * level) / 1.5F);
    }

    private static boolean canDamage(Player attacker, LivingEntity living) {
        if (living == attacker || !living.isAlive() || living.invulnerableTime != 0) {
            return false;
        }
        return !(living instanceof Player) || !(attacker.level() instanceof ServerLevel serverLevel) || serverLevel.getServer().isPvpAllowed();
    }

    private static class SpaceBladeManaItem implements ManaItem {
        private final ItemStack stack;

        private SpaceBladeManaItem(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public int getMana() {
            return SpaceBladeItem.getMana(stack);
        }

        @Override
        public int getMaxMana() {
            return Integer.MAX_VALUE;
        }

        @Override
        public void addMana(int mana) {
            long updated = (long) getMana() + mana;
            SpaceBladeItem.setMana(stack, (int) Mth.clamp(updated, 0L, (long) Integer.MAX_VALUE));
        }

        @Override
        public boolean canReceiveManaFromPool(net.minecraft.world.level.block.entity.BlockEntity blockEntity) {
            return true;
        }

        @Override
        public boolean canReceiveManaFromItem(ItemStack otherStack) {
            return true;
        }

        @Override
        public boolean canExportManaToPool(net.minecraft.world.level.block.entity.BlockEntity blockEntity) {
            return false;
        }

        @Override
        public boolean canExportManaToItem(ItemStack otherStack) {
            return false;
        }

        @Override
        public boolean isNoExport() {
            return true;
        }
    }
}
