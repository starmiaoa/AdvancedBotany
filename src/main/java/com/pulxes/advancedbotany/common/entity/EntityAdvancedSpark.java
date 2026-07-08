package com.pulxes.advancedbotany.common.entity;

import com.pulxes.advancedbotany.common.item.equipment.AdvancedBotanyEquipment;
import com.pulxes.advancedbotany.registry.ModEntities;
import com.pulxes.advancedbotany.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkHooks;
import top.theillusivec4.curios.api.CuriosApi;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.mana.ManaItem;
import vazkii.botania.api.mana.ManaPool;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.spark.ManaSpark;
import vazkii.botania.api.mana.spark.SparkAttachable;
import vazkii.botania.api.mana.spark.SparkHelper;
import vazkii.botania.api.mana.spark.SparkUpgradeType;
import vazkii.botania.common.entity.ManaSparkEntity;
import vazkii.botania.common.helper.ColorHelper;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.forge.CapabilityUtil;
import vazkii.botania.network.EffectType;
import vazkii.botania.network.clientbound.BotaniaEffectPacket;
import vazkii.botania.xplat.XplatAbstractions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

public class EntityAdvancedSpark extends Entity implements ManaSpark {
    private static final EntityDataAccessor<Integer> UPGRADE = SynchedEntityData.defineId(EntityAdvancedSpark.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> INVISIBLE = SynchedEntityData.defineId(EntityAdvancedSpark.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> NETWORK = SynchedEntityData.defineId(EntityAdvancedSpark.class, EntityDataSerializers.INT);
    private static final SparkUpgradeType[] UPGRADE_TYPES = SparkUpgradeType.values();
    private static final EnumMap<SparkUpgradeType, Item> UPGRADE_ITEMS = new EnumMap<>(SparkUpgradeType.class);

    private final Set<ManaSpark> transfers = Collections.newSetFromMap(new WeakHashMap<>());
    private int removeTransferants = 2;

    public EntityAdvancedSpark(EntityType<? extends EntityAdvancedSpark> entityType, Level level) {
        super(entityType, level);
        noCulling = true;
    }

    public EntityAdvancedSpark(Level level) {
        this(ModEntities.ADVANCED_SPARK.get(), level);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(UPGRADE, SparkUpgradeType.NONE.ordinal());
        entityData.define(INVISIBLE, false);
        entityData.define(NETWORK, DyeColor.WHITE.getId());
    }

    @Override
    public void tick() {
        super.tick();
        setDeltaMovement(0.0D, 0.0D, 0.0D);
        SparkAttachable attached = getAttachedTile();
        if (attached == null) {
            if (!level().isClientSide()) {
                // remove(DISCARDED) already drops the spark + upgrade (shouldDestroy() is
                // true for DISCARDED); dropping here as well would duplicate the items.
                discard();
            }
            return;
        }

        if (!level().isClientSide()) {
            tickUpgrade(attached);
            distributeTransfers(attached);
        }
        if (removeTransferants > 0) {
            removeTransferants--;
        }
        updateTransfers();
    }

    private void tickUpgrade(SparkAttachable attached) {
        SparkUpgradeType upgrade = getUpgrade();
        if (upgrade == SparkUpgradeType.DISPERSIVE) {
            chargeNearbyPlayerItems(attached);
            return;
        }

        List<ManaSpark> nearby = SparkHelper.getSparksAround(level(), getX(), getY(), getZ(), getNetwork());
        if (upgrade == SparkUpgradeType.DOMINANT) {
            List<ManaSpark> valid = new ArrayList<>();
            for (ManaSpark spark : nearby) {
                if (spark != this && spark.getUpgrade() == SparkUpgradeType.NONE && spark.getAttachedManaReceiver() instanceof ManaPool) {
                    valid.add(spark);
                }
            }
            if (!valid.isEmpty()) {
                valid.get(level().random.nextInt(valid.size())).registerTransfer(this);
            }
        } else if (upgrade == SparkUpgradeType.RECESSIVE) {
            for (ManaSpark spark : nearby) {
                SparkUpgradeType other = spark.getUpgrade();
                if (spark != this && other != SparkUpgradeType.DOMINANT && other != SparkUpgradeType.RECESSIVE && other != SparkUpgradeType.ISOLATED) {
                    transfers.add(spark);
                }
            }
        }
    }

    private void chargeNearbyPlayerItems(SparkAttachable attached) {
        ManaReceiver receiver = getAttachedManaReceiver();
        if (receiver == null || receiver.getCurrentMana() <= 0) {
            return;
        }
        List<Player> players = level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(SparkHelper.SPARK_SCAN_RANGE));
        Collections.shuffle(players);
        for (Player player : players) {
            for (ItemStack stack : getPlayerManaStacks(player)) {
                ManaItem manaItem = stack.getCapability(BotaniaForgeCapabilities.MANA_ITEM).orElse(null);
                if (manaItem == null || !manaItem.canReceiveManaFromItem(new ItemStack(BotaniaItems.spark))) {
                    continue;
                }
                int toSend = Math.min(receiver.getCurrentMana(), Math.min(AdvancedBotanyEquipment.ADVANCED_SPARK_TRANSFER_SPEED, manaItem.getMaxMana() - manaItem.getMana()));
                if (toSend > 0) {
                    manaItem.addMana(toSend);
                    receiver.receiveMana(-toSend);
                    sendManaFlowParticles(player.getId());
                    return;
                }
            }
        }
    }

    private void distributeTransfers(SparkAttachable attached) {
        ManaReceiver receiver = getAttachedManaReceiver();
        Collection<ManaSpark> outgoing = getOutgoingTransfers();
        if (receiver == null || outgoing.isEmpty() || receiver.getCurrentMana() <= 0) {
            return;
        }

        int manaTotal = Math.min(AdvancedBotanyEquipment.ADVANCED_SPARK_TRANSFER_SPEED * outgoing.size(), receiver.getCurrentMana());
        int manaEach = manaTotal / outgoing.size();
        int spent = 0;
        if (manaEach <= outgoing.size()) {
            return;
        }
        for (ManaSpark spark : outgoing) {
            SparkAttachable other = spark.getAttachedTile();
            ManaReceiver targetReceiver = spark.getAttachedManaReceiver();
            if (other == null || targetReceiver == null || other.getAvailableSpaceForMana() <= 0 || spark.areIncomingTransfersDone()) {
                continue;
            }
            int spend = Math.min(other.getAvailableSpaceForMana(), manaEach);
            targetReceiver.receiveMana(spend);
            sendManaFlowParticles(((Entity) spark).getId());
            spent += spend;
        }
        receiver.receiveMana(-spent);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(BotaniaItems.twigWand)) {
            if (player.isShiftKeyDown()) {
                SparkUpgradeType upgrade = getUpgrade();
                if (!level().isClientSide()) {
                    if (upgrade != SparkUpgradeType.NONE) {
                        spawnAtLocation(upgradeStack(upgrade));
                        setUpgrade(SparkUpgradeType.NONE);
                        transfers.clear();
                        removeTransferants = 2;
                    } else {
                        discard();
                    }
                }
                return InteractionResult.sidedSuccess(level().isClientSide());
            }
            if (!level().isClientSide()) {
                for (ManaSpark spark : SparkHelper.getSparksAround(level(), getX(), getY(), getZ(), getNetwork())) {
                    ManaSparkEntity.particleBeam(player, this, (Entity) spark);
                }
            }
            return InteractionResult.sidedSuccess(level().isClientSide());
        }

        SparkUpgradeType upgrade = upgradeForItem(stack);
        if (upgrade != SparkUpgradeType.NONE && getUpgrade() == SparkUpgradeType.NONE) {
            if (!level().isClientSide()) {
                setUpgrade(upgrade);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
            return InteractionResult.sidedSuccess(level().isClientSide());
        }

        if (stack.is(BotaniaItems.phantomInk)) {
            if (!level().isClientSide()) {
                entityData.set(INVISIBLE, !entityData.get(INVISIBLE));
            }
            return InteractionResult.sidedSuccess(level().isClientSide());
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean isInvisible() {
        return entityData.get(INVISIBLE) || super.isInvisible();
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public void move(MoverType type, net.minecraft.world.phys.Vec3 movement) {
        super.move(type, movement);
    }

    @Override
    public BlockPos getAttachPos() {
        return blockPosition().below();
    }

    @Override
    public DyeColor getNetwork() {
        return DyeColor.byId(entityData.get(NETWORK));
    }

    @Override
    public void setNetwork(DyeColor color) {
        entityData.set(NETWORK, color == null ? DyeColor.WHITE.getId() : color.getId());
    }

    @Override
    public SparkAttachable getAttachedTile() {
        BlockPos pos = getAttachPos();
        BlockState state = level().getBlockState(pos);
        BlockEntity blockEntity = level().getBlockEntity(pos);
        return CapabilityUtil.findCapability(BotaniaForgeCapabilities.SPARK_ATTACHABLE, level(), pos, state, blockEntity);
    }

    @Override
    public ManaReceiver getAttachedManaReceiver() {
        BlockPos pos = getAttachPos();
        BlockState state = level().getBlockState(pos);
        BlockEntity blockEntity = level().getBlockEntity(pos);
        return CapabilityUtil.findCapability(BotaniaForgeCapabilities.MANA_RECEIVER, level(), pos, state, blockEntity);
    }

    @Override
    public Collection<ManaSpark> getOutgoingTransfers() {
        return transfers;
    }

    @Override
    public void registerTransfer(ManaSpark spark) {
        if (spark != this) {
            transfers.add(spark);
        }
    }

    @Override
    public void updateTransfers() {
        transfers.removeIf(spark -> {
            SparkAttachable attached = spark.getAttachedTile();
            SparkUpgradeType upgrade = getUpgrade();
            SparkUpgradeType otherUpgrade = spark.getUpgrade();
            return spark == this
                    || spark.areIncomingTransfersDone()
                    || attached == null
                    || attached.getAvailableSpaceForMana() <= 0
                    || !isValidTransferTarget(upgrade, otherUpgrade, spark.getAttachedManaReceiver());
        });
    }

    @Override
    public SparkUpgradeType getUpgrade() {
        int index = Mth.clamp(entityData.get(UPGRADE), 0, UPGRADE_TYPES.length - 1);
        return UPGRADE_TYPES[index];
    }

    @Override
    public void setUpgrade(SparkUpgradeType upgrade) {
        entityData.set(UPGRADE, upgrade == null ? SparkUpgradeType.NONE.ordinal() : upgrade.ordinal());
    }

    @Override
    public boolean areIncomingTransfersDone() {
        SparkAttachable attached = getAttachedTile();
        ManaReceiver receiver = getAttachedManaReceiver();
        if (receiver instanceof ManaPool) {
            return removeTransferants > 0;
        }
        return attached != null && attached.areIncomingTranfersDone();
    }

    @Override
    public void remove(RemovalReason reason) {
        boolean drop = !level().isClientSide() && reason.shouldDestroy();
        SparkUpgradeType upgrade = getUpgrade();
        super.remove(reason);
        if (drop) {
            spawnAtLocation(new ItemStack(ModItems.SUPERCONDUCTIVE_SPARK.get()));
            if (upgrade != SparkUpgradeType.NONE) {
                spawnAtLocation(upgradeStack(upgrade));
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("upgrade", getUpgrade().ordinal());
        tag.putBoolean("invis", entityData.get(INVISIBLE));
        tag.putInt("network", entityData.get(NETWORK));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setUpgrade(UPGRADE_TYPES[Mth.clamp(tag.getInt("upgrade"), 0, UPGRADE_TYPES.length - 1)]);
        entityData.set(INVISIBLE, tag.getBoolean("invis"));
        entityData.set(NETWORK, tag.contains("network") ? tag.getInt("network") : DyeColor.WHITE.getId());
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    private static SparkUpgradeType upgradeForItem(ItemStack stack) {
        if (stack.is(BotaniaItems.sparkUpgradeDispersive)) {
            return SparkUpgradeType.DISPERSIVE;
        }
        if (stack.is(BotaniaItems.sparkUpgradeDominant)) {
            return SparkUpgradeType.DOMINANT;
        }
        if (stack.is(BotaniaItems.sparkUpgradeRecessive)) {
            return SparkUpgradeType.RECESSIVE;
        }
        if (stack.is(BotaniaItems.sparkUpgradeIsolated)) {
            return SparkUpgradeType.ISOLATED;
        }
        return SparkUpgradeType.NONE;
    }

    private static ItemStack upgradeStack(SparkUpgradeType upgrade) {
        Item item = UPGRADE_ITEMS.get(upgrade);
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    private void sendManaFlowParticles(int targetEntityId) {
        XplatAbstractions.INSTANCE.sendToTracking(
                this,
                new BotaniaEffectPacket(
                        EffectType.SPARK_MANA_FLOW,
                        getX(),
                        getY(),
                        getZ(),
                        getId(),
                        targetEntityId,
                        ColorHelper.getColorValue(getNetwork())));
    }

    private static List<ItemStack> getPlayerManaStacks(Player player) {
        List<ItemStack> stacks = new ArrayList<>();
        Inventory inventory = player.getInventory();
        stacks.addAll(inventory.items);
        stacks.addAll(inventory.armor);
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            for (var slotResult : handler.findCurios(stack -> true)) {
                stacks.add(slotResult.stack());
            }
        });
        return stacks;
    }

    private static boolean isValidTransferTarget(SparkUpgradeType upgrade, SparkUpgradeType targetUpgrade, ManaReceiver targetReceiver) {
        if (upgrade == SparkUpgradeType.NONE && targetUpgrade == SparkUpgradeType.DOMINANT) {
            return true;
        }
        if (upgrade == SparkUpgradeType.RECESSIVE && (targetUpgrade == SparkUpgradeType.NONE || targetUpgrade == SparkUpgradeType.DISPERSIVE)) {
            return true;
        }
        return !(targetReceiver instanceof ManaPool);
    }

    static {
        UPGRADE_ITEMS.put(SparkUpgradeType.DISPERSIVE, BotaniaItems.sparkUpgradeDispersive);
        UPGRADE_ITEMS.put(SparkUpgradeType.DOMINANT, BotaniaItems.sparkUpgradeDominant);
        UPGRADE_ITEMS.put(SparkUpgradeType.RECESSIVE, BotaniaItems.sparkUpgradeRecessive);
        UPGRADE_ITEMS.put(SparkUpgradeType.ISOLATED, BotaniaItems.sparkUpgradeIsolated);
    }
}
