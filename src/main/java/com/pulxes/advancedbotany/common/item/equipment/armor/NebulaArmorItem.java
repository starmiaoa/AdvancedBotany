package com.pulxes.advancedbotany.common.item.equipment.armor;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.common.item.equipment.SimpleCapabilityProvider;
import com.pulxes.advancedbotany.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.ManaBarTooltip;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.item.equipment.armor.manasteel.ManasteelArmorItem;
import vazkii.botania.xplat.XplatAbstractions;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class NebulaArmorItem extends ManasteelArmorItem {
    public static final int MAX_MANA = 250_000;
    public static final int MAX_DISPLAY_DAMAGE = 1_000;
    public static final String TAG_MANA = "mana";
    protected static final String TEXTURE = AdvancedBotany.MOD_ID + ":textures/model/nebulaarmor.png";

    private static final int MANA_PER_ARMOR_DAMAGE = 15;
    private static final int MANA_PER_TICK_CHARGE = 1_000;
    private static final int MANA_GIFT_PER_TICK = 2;
    private static final float MIN_PROTECTION_FACTOR = 0.03F;
    private static final float PROTECTION_MANA_FACTOR = 0.0725F;
    private static final float MAX_BOOT_SPEED = 0.275F;
    private static final Set<UUID> PLAYERS_WITH_FLIGHT = new HashSet<>();
    private static final Set<UUID> PLAYERS_WITH_STEP_UP = new HashSet<>();
    private static final UUID HELM_ARMOR_UUID = uuid("nebula_helmet_armor");
    private static final UUID CHEST_ARMOR_UUID = uuid("nebula_chestplate_armor");
    private static final UUID LEGS_ARMOR_UUID = uuid("nebula_leggings_armor");
    private static final UUID BOOTS_ARMOR_UUID = uuid("nebula_boots_armor");
    private static final UUID HELM_HEALTH_UUID = uuid("nebula_helmet_health");
    private static final UUID CHEST_KNOCKBACK_UUID = uuid("nebula_chest_knockback");

    private final boolean revealingHelmet;

    public NebulaArmorItem(ArmorItem.Type type, boolean revealingHelmet, Properties properties) {
        super(type, AdvancedBotanyArmorMaterials.NEBULA,
                properties.stacksTo(1).durability(MAX_DISPLAY_DAMAGE).setNoRepair());
        this.revealingHelmet = revealingHelmet;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        syncDisplayDamage(stack);
    }

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        syncDisplayDamage(stack);
        if (!level.isClientSide() && getMana(stack) < getMaxMana(stack)
                && ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_PER_TICK_CHARGE, true)) {
            addMana(stack, MANA_PER_TICK_CHARGE);
        }

        if (type == ArmorItem.Type.BOOTS && level.isClientSide() && player.isSprinting()) {
            sparkleBoots(level, player);
        } else if (type == ArmorItem.Type.HELMET && !level.isClientSide() && hasArmorSet(player)) {
            healFromFood(player);
            giftMana(stack, player, MANA_GIFT_PER_TICK);
        }
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
        if (amount <= 0) {
            return 0;
        }

        int manaCost = Math.min(amount * MANA_PER_ARMOR_DAMAGE, getMana(stack));
        if (manaCost > 0 && entity instanceof Player player && !entity.level().isClientSide()
                && !ManaItemHandler.instance().requestManaExactForTool(stack, player, manaCost, true)) {
            addMana(stack, -manaCost);
        }
        syncDisplayDamage(stack);
        return 0;
    }

    @Override
    public String getArmorTextureAfterInk(ItemStack stack, EquipmentSlot slot) {
        return TEXTURE;
    }

    @Override
    public ItemStack[] getArmorSetStacks() {
        return new ItemStack[]{
                new ItemStack(ModItems.NEBULA_HELMET.get()),
                new ItemStack(ModItems.NEBULA_CHESTPLATE.get()),
                new ItemStack(ModItems.NEBULA_LEGGINGS.get()),
                new ItemStack(ModItems.NEBULA_BOOTS.get())
        };
    }

    @Override
    public boolean hasArmorSetItem(Player player, EquipmentSlot slot) {
        return hasNebulaArmorSetItem(player, slot);
    }

    @Override
    public MutableComponent getArmorSetName() {
        return Component.translatable("ab.armorset.nebula.name");
    }

    @Override
    public void addArmorSetDescription(ItemStack stack, List<Component> tooltip) {
        tooltip.add(Component.translatable("ab.armorset.nebula.desc0"));
        tooltip.add(Component.translatable("botania.armorset.terrasteel.desc1"));
        tooltip.add(Component.translatable("botania.armorset.terrasteel.desc2"));
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        float fraction = getManaFraction(stack);
        if (slot == type.getSlot()) {
            builder.put(Attributes.ARMOR, new AttributeModifier(armorUuid(type), "Nebula armor modifier",
                    getMaterial().getDefenseForType(type) * getProtectionScale(stack), AttributeModifier.Operation.ADDITION));
        }
        if (slot == EquipmentSlot.HEAD && type == ArmorItem.Type.HELMET) {
            builder.put(Attributes.MAX_HEALTH, new AttributeModifier(HELM_HEALTH_UUID,
                    "Nebula helmet modifier", 20.0D * fraction, AttributeModifier.Operation.ADDITION));
        } else if (slot == EquipmentSlot.CHEST && type == ArmorItem.Type.CHESTPLATE) {
            builder.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(CHEST_KNOCKBACK_UUID,
                    "Nebula chestplate modifier", 1.0D * fraction, AttributeModifier.Operation.ADDITION));
        }
        return builder.build();
    }

    @Override
    public int getDamage(ItemStack stack) {
        return MAX_DISPLAY_DAMAGE - Math.round(getManaFraction(stack) * MAX_DISPLAY_DAMAGE);
    }

    @Override
    public void setDamage(ItemStack stack, int damage) {
        int clamped = Mth.clamp(damage, 0, MAX_DISPLAY_DAMAGE);
        setMana(stack, Math.round((MAX_DISPLAY_DAMAGE - clamped) * ((float) MAX_MANA / MAX_DISPLAY_DAMAGE)));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getMana(stack) < getMaxMana(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * getManaFraction(stack));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return Mth.hsvToRgb(0.58F + getManaFraction(stack) * 0.18F, 1.0F, 1.0F);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return Optional.of(ManaBarTooltip.fromManaItem(stack));
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new SimpleCapabilityProvider<>(BotaniaForgeCapabilities.MANA_ITEM, new NebulaManaItem(stack));
    }

    public boolean isRevealingHelmet() {
        return revealingHelmet;
    }

    public static boolean hasNebulaArmorSetItem(Player player, EquipmentSlot slot) {
        ItemStack stack = player.getItemBySlot(slot);
        if (stack.isEmpty()) {
            return false;
        }
        return switch (slot) {
            case HEAD -> stack.is(ModItems.NEBULA_HELMET.get()) || stack.is(ModItems.NEBULA_HELMET_OF_REVEALING.get());
            case CHEST -> stack.is(ModItems.NEBULA_CHESTPLATE.get());
            case LEGS -> stack.is(ModItems.NEBULA_LEGGINGS.get());
            case FEET -> stack.is(ModItems.NEBULA_BOOTS.get());
            default -> false;
        };
    }

    public static boolean hasFullSet(Player player) {
        return hasNebulaArmorSetItem(player, EquipmentSlot.HEAD)
                && hasNebulaArmorSetItem(player, EquipmentSlot.CHEST)
                && hasNebulaArmorSetItem(player, EquipmentSlot.LEGS)
                && hasNebulaArmorSetItem(player, EquipmentSlot.FEET);
    }

    public static int getMana(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : Mth.clamp(tag.getInt(TAG_MANA), 0, MAX_MANA);
    }

    public static void setMana(ItemStack stack, int mana) {
        int clamped = Mth.clamp(mana, 0, MAX_MANA);
        if (clamped > 0) {
            stack.getOrCreateTag().putInt(TAG_MANA, clamped);
        } else if (stack.hasTag()) {
            stack.getTag().remove(TAG_MANA);
        }
        syncDisplayDamage(stack);
    }

    public static int getMaxMana(ItemStack stack) {
        return MAX_MANA;
    }

    public void addMana(ItemStack stack, int mana) {
        setMana(stack, getMana(stack) + mana);
    }

    public float getManaFraction(ItemStack stack) {
        return (float) getMana(stack) / (float) getMaxMana(stack);
    }

    public static void handlePlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) {
            return;
        }
        Player player = event.player;
        UUID id = player.getUUID();
        boolean hasChest = hasNebulaArmorSetItem(player, EquipmentSlot.CHEST);
        Abilities abilities = player.getAbilities();
        if (hasChest) {
            if (PLAYERS_WITH_FLIGHT.add(id) || !abilities.mayfly) {
                abilities.mayfly = true;
                player.onUpdateAbilities();
            }
        } else if (PLAYERS_WITH_FLIGHT.remove(id) && !abilities.instabuild) {
            abilities.mayfly = false;
            abilities.flying = false;
            player.onUpdateAbilities();
        }
    }

    public static void handleLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        UUID id = player.getUUID();
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        boolean hasBoots = boots.getItem() instanceof NebulaArmorItem armor && armor.type == ArmorItem.Type.BOOTS;
        if (!hasBoots) {
            if (PLAYERS_WITH_STEP_UP.remove(id)) {
                player.setMaxUpStep(0.6F);
            }
            return;
        }

        PLAYERS_WITH_STEP_UP.add(id);
        player.setMaxUpStep(player.isShiftKeyDown() ? 0.50001F : 1.0F);
        if ((player.onGround() || player.getAbilities().flying) && player.zza > 0.0F) {
            float speed = getBootSpeed(boots) * (player.isSprinting() ? 1.0F : 0.2F);
            player.moveRelative(player.getAbilities().flying ? speed * 0.6F : speed, new Vec3(0.0D, 0.0D, 1.0D));
        }
    }

    public static void handleLivingJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
            if (legs.getItem() instanceof NebulaArmorItem armor && armor.type == ArmorItem.Type.LEGGINGS) {
                player.setDeltaMovement(player.getDeltaMovement().add(0.0D, getJumpBoost(legs), 0.0D));
                player.fallDistance = -getFallBuffer(legs);
            }
        }
    }

    private static float getJumpBoost(ItemStack stack) {
        return 0.3F * (1.0F - (float) displayDamage(stack) / MAX_DISPLAY_DAMAGE);
    }

    private static float getFallBuffer(ItemStack stack) {
        return 12.0F * (1.0F - (float) displayDamage(stack) / MAX_DISPLAY_DAMAGE);
    }

    private static float getBootSpeed(ItemStack stack) {
        return MAX_BOOT_SPEED * (1.0F - (float) displayDamage(stack) / MAX_DISPLAY_DAMAGE);
    }

    private static float getProtectionScale(ItemStack stack) {
        return (MIN_PROTECTION_FACTOR + PROTECTION_MANA_FACTOR * ((float) getMana(stack) / MAX_MANA))
                / (MIN_PROTECTION_FACTOR + PROTECTION_MANA_FACTOR);
    }

    private static void healFromFood(Player player) {
        FoodData foodData = player.getFoodData();
        int food = foodData.getFoodLevel();
        if (food > 0 && food < 18 && player.getHealth() < player.getMaxHealth() && player.tickCount % 80 == 0) {
            player.heal(1.0F);
        }
    }

    private static boolean giftMana(ItemStack source, Player player, int manaToSend) {
        for (ItemStack target : ManaItemHandler.instance().getManaItems(player)) {
            if (tryGiftMana(source, target, manaToSend)) {
                return true;
            }
        }
        for (ItemStack target : ManaItemHandler.instance().getManaAccesories(player)) {
            if (tryGiftMana(source, target, manaToSend)) {
                return true;
            }
        }
        return false;
    }

    private static boolean tryGiftMana(ItemStack source, ItemStack target, int manaToSend) {
        if (target == source || target.isEmpty()) {
            return false;
        }
        ManaItem manaItem = XplatAbstractions.INSTANCE.findManaItem(target);
        if (manaItem == null || !manaItem.canReceiveManaFromItem(source)) {
            return false;
        }
        if (manaItem.getMana() + manaToSend > manaItem.getMaxMana()) {
            return false;
        }
        manaItem.addMana(manaToSend);
        return true;
    }

    private static void sparkleBoots(Level level, Player player) {
        float base = 0.6F;
        float r = base + level.random.nextFloat() * (1.0F - base);
        float g = base + level.random.nextFloat() * (1.0F - base);
        float b = base + level.random.nextFloat() * (1.0F - base);
        for (int i = 0; i < 2; i++) {
            BotaniaAPI.instance().sparkleFX(level,
                    player.getX() + level.random.nextDouble() - 0.5D,
                    player.getY() - 1.25D + level.random.nextDouble() / 4.0D - 0.125D,
                    player.getZ() + level.random.nextDouble() - 0.5D,
                    r, g, b, 0.7F + level.random.nextFloat() / 2.0F, 25);
        }
    }

    private static int displayDamage(ItemStack stack) {
        return MAX_DISPLAY_DAMAGE - Math.round(((float) getMana(stack) / MAX_MANA) * MAX_DISPLAY_DAMAGE);
    }

    private static void syncDisplayDamage(ItemStack stack) {
        if (stack.isDamageableItem()) {
            int damage = displayDamage(stack);
            if (damage > 0) {
                stack.getOrCreateTag().putInt("Damage", damage);
            } else if (stack.hasTag()) {
                stack.getTag().remove("Damage");
            }
        }
    }

    private static UUID uuid(String name) {
        return UUID.nameUUIDFromBytes((AdvancedBotany.MOD_ID + ":" + name).getBytes(StandardCharsets.UTF_8));
    }

    private static UUID armorUuid(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> HELM_ARMOR_UUID;
            case CHESTPLATE -> CHEST_ARMOR_UUID;
            case LEGGINGS -> LEGS_ARMOR_UUID;
            case BOOTS -> BOOTS_ARMOR_UUID;
        };
    }

    private class NebulaManaItem implements ManaItem {
        private final ItemStack stack;

        NebulaManaItem(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public int getMana() {
            return NebulaArmorItem.getMana(stack);
        }

        @Override
        public int getMaxMana() {
            return MAX_MANA;
        }

        @Override
        public void addMana(int mana) {
            NebulaArmorItem.setMana(stack, getMana() + mana);
        }

        @Override
        public boolean canReceiveManaFromPool(BlockEntity pool) {
            return true;
        }

        @Override
        public boolean canReceiveManaFromItem(ItemStack otherStack) {
            return true;
        }

        @Override
        public boolean canExportManaToPool(BlockEntity pool) {
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
