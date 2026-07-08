package com.pulxes.advancedbotany.common.item.equipment.armor;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.client.model.armor.AdvancedBotanyArmorModels;
import com.pulxes.advancedbotany.common.item.equipment.SimpleCapabilityProvider;
import com.pulxes.advancedbotany.registry.ModItems;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
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
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
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

/**
 * Faithful reimplementation of the 1.7.10 {@code ItemNebulaArmor} (an {@code ISpecialArmor}).
 *
 * <p>Core design, mirroring the original:
 * <ul>
 *   <li><b>Mana-backed, never breaks.</b> The {@link AdvancedBotanyArmorMaterials#NEBULA} material has
 *       zero durability, so the piece is unbreakable — it never takes vanilla item damage. Charge is
 *       stored as mana (NBT) and shown through a custom bar, exactly like the original stored mana and
 *       displayed it on the (never-consumed) durability bar.</li>
 *   <li><b>Percentage damage absorption.</b> {@link #handleLivingHurt} ports {@code getProperties} +
 *       {@code damageArmor}: each worn piece absorbs {@code defense * (0.03 + 0.0725 * manaFraction)} of
 *       the blow and pays {@code min(damage * 15, mana)} mana for it. At full mana the summed ratio
 *       reaches 100% — the original's "near-invincible" set.</li>
 * </ul>
 *
 * <p><b>Fire/lava immunity (intentional QoL, beyond the 1.7.10 letter).</b> The original let the
 * burning-over-time source ({@code onFire}) bypass the armor. Here the whole {@code is_fire} family is
 * routed through the absorption path instead of being skipped, and a full set extinguishes the wearer
 * each tick, so a mana-charged Nebula set shrugs off fire and lava.
 */
public class NebulaArmorItem extends ManasteelArmorItem {
    public static final int MAX_MANA = 250_000;
    public static final String TAG_MANA = "mana";
    protected static final String TEXTURE = AdvancedBotany.MOD_ID + ":textures/model/nebulaarmor.png";

    private static final int MANA_PER_ARMOR_DAMAGE = 15;
    private static final int MANA_PER_TICK_CHARGE = 1_000;
    private static final int MANA_GIFT_PER_TICK = 2;
    private static final float MIN_PROTECTION_FACTOR = 0.03F;
    private static final float PROTECTION_MANA_FACTOR = 0.0725F;
    private static final float MAX_JUMP_BOOST = 0.3F;
    private static final float MAX_FALL_BUFFER = 12.0F;
    private static final float MAX_BOOT_SPEED = 0.275F;

    private static final Set<UUID> PLAYERS_WITH_FLIGHT = new HashSet<>();
    private static final Set<UUID> PLAYERS_WITH_STEP_UP = new HashSet<>();
    private static final UUID HELM_HEALTH_UUID = uuid("nebula_helmet_health");
    private static final UUID CHEST_KNOCKBACK_UUID = uuid("nebula_chest_knockback");

    private final boolean revealingHelmet;

    public NebulaArmorItem(ArmorItem.Type type, boolean revealingHelmet, Properties properties) {
        // The NEBULA material carries zero durability, so the piece is unbreakable and never takes
        // vanilla damage. No durability/repair wiring is needed: charge lives entirely in mana.
        super(type, AdvancedBotanyArmorMaterials.NEBULA, properties.stacksTo(1));
        this.revealingHelmet = revealingHelmet;
    }

    // ------------------------------------------------------------------ appearance / capability

    @Override
    public String getArmorTextureAfterInk(ItemStack stack, EquipmentSlot slot) {
        return TEXTURE;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public HumanoidModel<?> getHumanoidArmorModel(LivingEntity living, ItemStack stack,
                                                          EquipmentSlot slot, HumanoidModel<?> defaultModel) {
                HumanoidModel<LivingEntity> model = AdvancedBotanyArmorModels.nebula(getEquipmentSlot());
                return model == null ? defaultModel : model;
            }
        });
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new SimpleCapabilityProvider<>(BotaniaForgeCapabilities.MANA_ITEM, new NebulaManaItem(stack));
    }

    public boolean isRevealingHelmet() {
        // The original revealing helmet only exposed Thaumcraft's IGoggles/IRevealer, which has no
        // 1.20.1 target. The flag is kept purely to distinguish the decorative variant.
        return revealingHelmet;
    }

    // ------------------------------------------------------------------ mana bar / attributes

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
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        float fraction = getManaFraction(stack);
        if (slot == EquipmentSlot.HEAD && type == ArmorItem.Type.HELMET) {
            builder.put(Attributes.MAX_HEALTH, new AttributeModifier(HELM_HEALTH_UUID,
                    "Nebula helmet modifier", 20.0D * fraction, AttributeModifier.Operation.ADDITION));
        } else if (slot == EquipmentSlot.CHEST && type == ArmorItem.Type.CHESTPLATE) {
            builder.put(Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(CHEST_KNOCKBACK_UUID,
                    "Nebula chestplate modifier", 1.0D * fraction, AttributeModifier.Operation.ADDITION));
        }
        return builder.build();
    }

    // ------------------------------------------------------------------ Botania armor-set tooltip

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

    // ------------------------------------------------------------------ per-tick upkeep

    @Override
    public void onArmorTick(ItemStack stack, Level level, Player player) {
        if (level.isClientSide()) {
            if (type == ArmorItem.Type.BOOTS && player.isSprinting()) {
                sparkleBoots(level, player);
            }
            return;
        }

        // Every worn piece pulls mana from the player's other mana sources to recharge itself.
        if (getMana(stack) < getMaxMana(stack)
                && ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_PER_TICK_CHARGE, true)) {
            addMana(stack, MANA_PER_TICK_CHARGE);
        }

        // Helmet drives the full-set bonuses: sustain, mana sharing, and fire immunity.
        if (type == ArmorItem.Type.HELMET && hasFullSet(player)) {
            healFromFood(player);
            giftMana(stack, player, MANA_GIFT_PER_TICK);
            if (player.isOnFire()) {
                player.clearFire();
            }
        }
    }

    // ------------------------------------------------------------------ static event handlers (see ModForgeEvents)

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

    public static void handleLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        DamageSource source = event.getSource();
        boolean fire = source.is(DamageTypeTags.IS_FIRE);
        // Faithful: armor-bypassing sources (fall, magic, wither, the void, ...) are not absorbed.
        // Deliberate exception: fire/lava IS absorbed here, giving the charged set fire immunity.
        if (source.is(DamageTypeTags.BYPASSES_ARMOR) && !fire) {
            return;
        }

        float ratio = 0.0F;
        for (ItemStack stack : player.getArmorSlots()) {
            if (stack.getItem() instanceof NebulaArmorItem armor) {
                ratio += armor.getMaterial().getDefenseForType(armor.type)
                        * (MIN_PROTECTION_FACTOR + PROTECTION_MANA_FACTOR * armor.getManaFraction(stack));
            }
        }
        if (ratio <= 0.0F) {
            return;
        }

        int damage = Math.max(0, Mth.ceil(event.getAmount()));
        for (ItemStack stack : player.getArmorSlots()) {
            if (stack.getItem() instanceof NebulaArmorItem armor) {
                armor.consumeProtectionMana(stack, player, damage);
            }
        }
        event.setAmount(event.getAmount() * Math.max(0.0F, 1.0F - Math.min(1.0F, ratio)));
    }

    // ------------------------------------------------------------------ set-membership helpers

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

    // ------------------------------------------------------------------ mana storage

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

    // ------------------------------------------------------------------ internals

    private void consumeProtectionMana(ItemStack stack, Player player, int damage) {
        if (damage <= 0 || player.level().isClientSide()) {
            return;
        }
        int manaCost = Math.min(damage * MANA_PER_ARMOR_DAMAGE, getMana(stack));
        // requestManaExactForTool spends from the player's mana network first; only if that fails do we
        // draw the cost from the armor piece itself. Mirrors the original damageArmor() fallback.
        if (manaCost > 0 && !ManaItemHandler.instance().requestManaExactForTool(stack, player, manaCost, true)) {
            addMana(stack, -manaCost);
        }
    }

    private static float getJumpBoost(ItemStack stack) {
        return MAX_JUMP_BOOST * fraction(stack);
    }

    private static float getFallBuffer(ItemStack stack) {
        return MAX_FALL_BUFFER * fraction(stack);
    }

    private static float getBootSpeed(ItemStack stack) {
        return MAX_BOOT_SPEED * fraction(stack);
    }

    private static float fraction(ItemStack stack) {
        return (float) getMana(stack) / (float) MAX_MANA;
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
        for (ItemStack target : player.getArmorSlots()) {
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

    private static UUID uuid(String name) {
        return UUID.nameUUIDFromBytes((AdvancedBotany.MOD_ID + ":" + name).getBytes(StandardCharsets.UTF_8));
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
