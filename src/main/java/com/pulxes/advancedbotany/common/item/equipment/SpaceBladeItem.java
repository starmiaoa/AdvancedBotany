package com.pulxes.advancedbotany.common.item.equipment;

import com.pulxes.advancedbotany.api.AdvancedBotanyAPI;
import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.common.entity.EntitySword;
import com.pulxes.advancedbotany.common.item.ItemComponentData;
import com.pulxes.advancedbotany.common.network.ModNetwork;
import com.pulxes.advancedbotany.registry.ModSounds;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.api.mana.ManaItemHandler;

import java.util.List;
public class SpaceBladeItem extends SwordItem {
    public static final String TAG_MANA = "mana";
    public static final String TAG_POST_ATTACK_TICK = "postAttackTick";
    public static final String TAG_TICK = "tick";
    public static final String TAG_ENABLED_MODE = "isEnabledMode";
    private static final ResourceLocation ATTACK_DAMAGE_ID = ResourceLocation.fromNamespaceAndPath(AdvancedBotany.MOD_ID, "space_blade_damage");
    private static final ResourceLocation SPEED_ID = ResourceLocation.fromNamespaceAndPath(AdvancedBotany.MOD_ID, "space_blade_speed");

    public SpaceBladeItem(Properties properties) {
        super(AdvancedBotanyEquipment.MITHRIL,
                properties.stacksTo(1).rarity(AdvancedBotanyAPI.RARITY_NEBULA)
                        .attributes(SwordItem.createAttributes(AdvancedBotanyEquipment.MITHRIL, 3, -2.4F)));
    }

    @Override
    public int getEntityLifespan(ItemStack itemStack, Level level) {
        // The original blade never despawns as a dropped item (getEntityLifespan = MAX_VALUE).
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity target) {
        if (!player.level().isClientSide()) {
            ItemComponentData.putInt(stack, TAG_POST_ATTACK_TICK, 3);
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
            sword.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F); // vanilla throwable spread, as the original inherited
            sword.setDeltaMovement(sword.getDeltaMovement().scale(0.2D));
            player.level().addFreshEntity(sword);
            player.level().playSound(null, player.blockPosition(), ModSounds.BLADE_SPACE.get(), SoundSource.PLAYERS, 0.5F, 3.6F);
        }
        return false;
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
        int useTime = getUseDuration(stack, player) - timeLeft;
        if (useTime >= 4) {
            return;
        }
        if (player.isShiftKeyDown() && getLevel(stack) >= 3) {
            if (!level.isClientSide()) {
                ItemComponentData.putBoolean(stack, TAG_ENABLED_MODE, !isEnabledMode(stack));
            }
            return;
        }
        if (!level.isClientSide() && !player.isShiftKeyDown() && getCooldownTicks(stack) == 0 && getLevel(stack) >= 2) {
            dash(player);
            if (player instanceof ServerPlayer serverPlayer) {
                ModNetwork.sendSpaceBladeDash(serverPlayer);
            }
            ItemComponentData.putInt(stack, TAG_TICK, AdvancedBotanyEquipment.SPACE_BLADE_DASH_COOLDOWN);
            level.playSound(null, player.blockPosition(), ModSounds.BLADE_SPACE.get(), SoundSource.PLAYERS, 2.3F, 1.2F);
        }
    }

    public static void dash(Player player) {
        Vec3 look = player.getLookAngle().normalize();
        player.setDeltaMovement(player.getDeltaMovement().add(look.x * 3.25D, look.y / 1.6D, look.z * 3.25D));
        player.hurtMarked = true;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72_000;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getCooldownTicks(stack) != 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        int cooldown = Mth.clamp(getCooldownTicks(stack), 0, AdvancedBotanyEquipment.SPACE_BLADE_DASH_COOLDOWN);
        int charge = AdvancedBotanyEquipment.SPACE_BLADE_DASH_COOLDOWN - cooldown;
        return Math.round(13.0F * charge / AdvancedBotanyEquipment.SPACE_BLADE_DASH_COOLDOWN);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        int cooldown = Mth.clamp(getCooldownTicks(stack), 0, AdvancedBotanyEquipment.SPACE_BLADE_DASH_COOLDOWN);
        return Mth.hsvToRgb(0.836F, 1.0F - (float) cooldown / AdvancedBotanyEquipment.SPACE_BLADE_DASH_COOLDOWN, 1.0F);
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context, java.util.List<net.minecraft.network.chat.Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        // Original: rank line via Botania's own toolRank/rank lang keys.
        tooltip.add(net.minecraft.network.chat.Component.translatable("botaniamisc.toolRank",
                net.minecraft.network.chat.Component.translatable("botania.rank" + getLevel(stack))));
    }

    private static void spawnCooldownSparkles(ItemStack stack, Level level, Entity entity, boolean selected) {
        // Original: colored sparkles around the holder while the dash cooldown is above 26 ticks.
        int tick = ItemComponentData.getInt(stack, TAG_TICK);
        if (tick <= 26 || !selected) {
            return;
        }
        for (int i = 0; i < 14; i++) {
            float r = level.random.nextBoolean() ? 0.88235295F : 0.39607844F;
            float g = level.random.nextBoolean() ? 0.2627451F : 0.81960785F;
            float b = level.random.nextBoolean() ? 0.9411765F : 0.88235295F;
            level.addParticle(vazkii.botania.client.fx.SparkleParticleData.sparkle(
                            1.8F * (float) (Math.random() - 0.5D),
                            r + (float) (Math.random() / 4.0D - 0.125D),
                            g + (float) (Math.random() / 4.0D - 0.125D),
                            b + (float) (Math.random() / 4.0D - 0.125D), 3),
                    entity.getX() + (Math.random() - 0.5D),
                    entity.getY() + (Math.random() - 0.5D) * 2.0D - 0.5D,
                    entity.getZ() + (Math.random() - 0.5D),
                    0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!(entity instanceof Player player)) {
            return;
        }
        if (level.isClientSide()) {
            spawnCooldownSparkles(stack, level, entity, selected);
            return;
        }
        ItemComponentData.update(stack, tag -> {
            int postAttackTick = tag.getInt(TAG_POST_ATTACK_TICK);
            if (postAttackTick > 0 && !player.isUsingItem()) {
                tag.putInt(TAG_POST_ATTACK_TICK, postAttackTick - 1);
            }
            int tick = tag.getInt(TAG_TICK);
            if (tick > 0) {
                tag.putInt(TAG_TICK, tick - 1);
            }
        });
        updateAttributeModifiers(stack);
    }

    private static void updateAttributeModifiers(ItemStack stack) {
        ItemAttributeModifiers modifiers = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE,
                        new AttributeModifier(ATTACK_DAMAGE_ID, Math.max(0.0F, getSwordDamage(stack) - 1.0F), AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.MAINHAND)
                .add(Attributes.MOVEMENT_SPEED,
                        new AttributeModifier(SPEED_ID, 0.25D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                        EquipmentSlotGroup.MAINHAND)
                .build();
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, modifiers);
    }

    public static ManaItem createManaItem(ItemStack stack) {
        return new SpaceBladeManaItem(stack);
    }

    public static int getMana(ItemStack stack) {
        return ItemComponentData.getInt(stack, TAG_MANA);
    }

    public static void setMana(ItemStack stack, int mana) {
        ItemComponentData.putInt(stack, TAG_MANA, Math.max(0, mana));
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
        return ItemComponentData.getBoolean(stack, TAG_ENABLED_MODE);
    }

    public static int getCooldownTicks(ItemStack stack) {
        return ItemComponentData.getInt(stack, TAG_TICK);
    }

    private static int getPostAttackTick(ItemStack stack) {
        return ItemComponentData.getInt(stack, TAG_POST_ATTACK_TICK);
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
        public boolean acceptDispatchedManaFromItem(ItemStack otherStack) {
            // The original refuses mana from IManaGivingItem sources (e.g. Mana Flower, Band of Aura).
            return !(otherStack.getItem() instanceof ManaFlowerItem)
                    && !(otherStack.getItem() instanceof vazkii.botania.common.item.equipment.bauble.BandOfAuraItem);
        }

        @Override
        public boolean refuseRequestedManaFromItem(ItemStack otherStack) {
            return false;
        }

        @Override
        public boolean canDrainManaToPool(net.minecraft.world.level.block.entity.BlockEntity blockEntity) {
            return false;
        }

        @Override
        public boolean canSendRequestedManaToItem(ItemStack otherStack) {
            return false;
        }

        @Override
        public boolean isNoExport() {
            return true;
        }
    }
}
