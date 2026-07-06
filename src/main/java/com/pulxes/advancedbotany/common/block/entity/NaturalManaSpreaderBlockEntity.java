package com.pulxes.advancedbotany.common.block.entity;

import com.pulxes.advancedbotany.registry.ModBlockEntities;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.block.WandBindable;
import vazkii.botania.api.block.Wandable;
import vazkii.botania.api.internal.ManaBurst;
import vazkii.botania.api.mana.BurstProperties;
import vazkii.botania.api.mana.LensEffectItem;
import vazkii.botania.api.mana.ManaBlockType;
import vazkii.botania.api.mana.ManaCollector;
import vazkii.botania.api.mana.ManaNetworkAction;
import vazkii.botania.api.mana.ManaPool;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.ManaSpreader;
import vazkii.botania.common.entity.ManaBurstEntity;
import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.botania.common.helper.MathHelper;
import vazkii.botania.xplat.XplatAbstractions;

public class NaturalManaSpreaderBlockEntity extends BlockEntity implements ManaSpreader, WandBindable, Wandable, Container {
    private static final String TAG_MANA = "mana";
    private static final String TAG_LENS = "lens";
    private static final String TAG_ROTATION_X = "rotationX";
    private static final String TAG_ROTATION_Y = "rotationY";
    private static final String TAG_CAN_SHOOT = "canShoot";
    private static final String TAG_PINGBACK_TICKS = "pingbackTicks";
    private static final String TAG_LAST_PINGBACK_X = "lastPingbackX";
    private static final String TAG_LAST_PINGBACK_Y = "lastPingbackY";
    private static final String TAG_LAST_PINGBACK_Z = "lastPingbackZ";
    private static final String TAG_UUID = "uuid";
    private static final int SYNC_INTERVAL = 10;

    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);

    private int mana;
    private int ticks;
    private boolean syncPending;
    private boolean addedToNetwork;
    private ManaReceiver receiver;
    private boolean canShootBurst = true;
    private int lastBurstDeathTick = -1;
    private int burstParticleTick;
    private int pingbackTicks;
    private double lastPingbackX;
    private double lastPingbackY = -2.147483648E9D;
    private double lastPingbackZ;
    private UUID identity = UUID.randomUUID();
    private float rotationX = 180.0F;
    private float rotationY = 0.0F;

    public NaturalManaSpreaderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NATURAL_MANA_SPREADER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, NaturalManaSpreaderBlockEntity spreader) {
        if (level.isClientSide()) {
            spreader.onClientDisplayTick();
            return;
        }

        spreader.ensureInManaNetwork();
        spreader.pullManaFromAdjacentPools();
        spreader.checkForReceiver();
        spreader.tickPingback();
        spreader.tryShootBurst();

        if (spreader.syncPending && spreader.ticks % SYNC_INTERVAL == 0) {
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
            spreader.syncPending = false;
        }
        spreader.ticks++;
    }

    private void ensureInManaNetwork() {
        if (!addedToNetwork && level != null && !isRemoved()) {
            BotaniaAPI.instance().getManaNetworkInstance().fireManaNetworkEvent(this, ManaBlockType.COLLECTOR, ManaNetworkAction.ADD);
            addedToNetwork = true;
        }
    }

    private void pullManaFromAdjacentPools() {
        if (level == null || isFull()) {
            return;
        }
        for (Direction direction : Direction.values()) {
            BlockPos sourcePos = worldPosition.relative(direction);
            if (!level.hasChunkAt(sourcePos)) {
                continue;
            }
            ManaReceiver endpoint = findManaReceiver(sourcePos, direction.getOpposite());
            if (!(endpoint instanceof ManaPool pool) || endpoint == receiver || endpoint.getCurrentMana() <= 0 || isFull()) {
                continue;
            }
            int transfer = Math.min(endpoint.getCurrentMana(), getMaxMana() - getCurrentMana());
            if (transfer > 0) {
                pool.receiveMana(-transfer);
                receiveMana(transfer);
            }
        }
    }

    @Nullable
    private ManaReceiver findManaReceiver(BlockPos pos, @Nullable Direction side) {
        if (level == null) {
            return null;
        }
        BlockState state = level.getBlockState(pos);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return XplatAbstractions.INSTANCE.findBlockApi(ManaReceiver.LOOKUP, level, pos, state, blockEntity, side);
    }

    public void checkForReceiver() {
        ManaBurst burst = runBurstSimulation();
        receiver = burst instanceof ManaBurstEntity entity ? entity.getCollidedTile(true) : null;
    }

    private void tryShootBurst() {
        if (level == null || receiver == null || !canShootBurst || !receiver.canReceiveManaFromBursts() || receiver.isFull()) {
            return;
        }
        if (level.hasNeighborSignal(worldPosition)) {
            return;
        }
        ManaBurstEntity burst = getBurst(false);
        if (burst == null || level.isClientSide()) {
            return;
        }

        receiveMana(-burst.getStartingMana());
        burst.setShooterUUID(getIdentifier());
        level.addFreshEntity(burst);
        burst.ping();
        level.playSound(null, worldPosition, BotaniaSounds.spreaderFire, SoundSource.BLOCKS, 0.05F, 0.7F + 0.3F * level.random.nextFloat());
    }

    private void tickPingback() {
        if (canShootBurst || level == null) {
            return;
        }
        if (pingbackTicks > 0) {
            pingbackTicks--;
            return;
        }

        AABB bounds = new AABB(lastPingbackX, lastPingbackY, lastPingbackZ, lastPingbackX, lastPingbackY, lastPingbackZ)
                .inflate(0.5D, 0.5D, 0.5D);
        List<ThrowableProjectile> bursts = level.getEntitiesOfClass(ThrowableProjectile.class, bounds, entity -> entity instanceof ManaBurst);
        for (ThrowableProjectile projectile : bursts) {
            ManaBurst burst = (ManaBurst) projectile;
            if (getIdentifier().equals(burst.getShooterUUID())) {
                burst.ping();
                return;
            }
        }
        setCanShoot(true);
    }

    @Nullable
    private ManaBurstEntity getBurst(boolean fake) {
        if (level == null) {
            return null;
        }

        BurstProperties properties = new BurstProperties(
                AdvancedBotanyManaValues.NATURAL_SPREADER_BURST_MANA,
                AdvancedBotanyManaValues.NATURAL_SPREADER_PRE_LOSS_TICKS,
                AdvancedBotanyManaValues.NATURAL_SPREADER_LOSS_PER_TICK,
                AdvancedBotanyManaValues.NATURAL_SPREADER_GRAVITY,
                AdvancedBotanyManaValues.NATURAL_SPREADER_MOTION_MODIFIER,
                AdvancedBotanyManaValues.NATURAL_SPREADER_COLOR);

        ItemStack lens = getLens();
        if (!lens.isEmpty() && lens.getItem() instanceof LensEffectItem lensEffect) {
            lensEffect.apply(lens, properties, level);
        }

        if (getCurrentMana() < properties.maxMana && !fake) {
            return null;
        }

        ManaBurstEntity burst = new ManaBurstEntity(level, worldPosition, getRotationX(), getRotationY(), fake);
        burst.setSourceLens(lens.copy());
        burst.setColor(properties.color);
        burst.setMana(properties.maxMana);
        burst.setStartingMana(properties.maxMana);
        burst.setMinManaLoss(properties.ticksBeforeManaLoss);
        burst.setManaLossPerTick(properties.manaLossPerTick);
        burst.setGravity(properties.gravity);
        burst.setDeltaMovement(burst.getDeltaMovement().scale(properties.motionModifier));
        return burst;
    }

    public ItemStack getLens() {
        return items.get(0);
    }

    public void setLens(ItemStack lens) {
        items.set(0, lens);
        setChanged();
        syncPending = true;
    }

    public ItemStack removeLens() {
        ItemStack removed = items.get(0);
        items.set(0, ItemStack.EMPTY);
        setChanged();
        syncPending = true;
        return removed;
    }

    @Override
    public boolean isFull() {
        return getCurrentMana() >= getMaxMana();
    }

    @Override
    public void receiveMana(int amount) {
        int oldMana = mana;
        mana = Mth.clamp(mana + amount, 0, getMaxMana());
        if (oldMana != mana) {
            setChanged();
            syncPending = true;
        }
    }

    @Override
    public Level getManaReceiverLevel() {
        return getLevel();
    }

    @Override
    public BlockPos getManaReceiverPos() {
        return getBlockPos();
    }

    @Override
    public int getCurrentMana() {
        return mana;
    }

    @Override
    public boolean canReceiveManaFromBursts() {
        return true;
    }

    @Override
    public void onClientDisplayTick() {
        if (level != null) {
            ManaBurstEntity burst = getBurst(true);
            if (burst != null) {
                burst.getCollidedTile(false);
            }
        }
    }

    @Override
    public float getManaYieldMultiplier(ManaBurst burst) {
        return 1.0F;
    }

    @Override
    public int getMaxMana() {
        return AdvancedBotanyManaValues.NATURAL_SPREADER_MAX_MANA;
    }

    @Override
    public void setCanShoot(boolean canShoot) {
        canShootBurst = canShoot;
    }

    @Override
    public int getBurstParticleTick() {
        return burstParticleTick;
    }

    @Override
    public void setBurstParticleTick(int burstParticleTick) {
        this.burstParticleTick = burstParticleTick;
    }

    @Override
    public int getLastBurstDeathTick() {
        return lastBurstDeathTick;
    }

    @Override
    public void setLastBurstDeathTick(int lastBurstDeathTick) {
        this.lastBurstDeathTick = lastBurstDeathTick;
    }

    @Override
    public ManaBurst runBurstSimulation() {
        ManaBurstEntity burst = getBurst(true);
        if (burst != null) {
            burst.setScanBeam();
            burst.getCollidedTile(true);
        }
        return burst;
    }

    @Override
    public float getRotationX() {
        return rotationX;
    }

    @Override
    public float getRotationY() {
        return rotationY;
    }

    @Override
    public void setRotationX(float rotationX) {
        this.rotationX = rotationX;
    }

    @Override
    public void setRotationY(float rotationY) {
        this.rotationY = rotationY;
    }

    @Override
    public void commitRedirection() {
        setChanged();
        syncPending = true;
    }

    @Override
    public void pingback(ManaBurst burst, UUID expectedIdentity) {
        if (!getIdentifier().equals(expectedIdentity)) {
            return;
        }
        Entity entity = burst.entity();
        pingbackTicks = 20;
        lastPingbackX = entity.getX();
        lastPingbackY = entity.getY();
        lastPingbackZ = entity.getZ();
        setCanShoot(false);
    }

    @Override
    public UUID getIdentifier() {
        return identity;
    }

    public boolean canSelect(Player player, ItemStack wand, BlockPos pos, Direction side) {
        return true;
    }

    public boolean bindTo(Player player, ItemStack wand, BlockPos pos, Direction side) {
        if (level == null) {
            return false;
        }

        VoxelShape shape = level.getBlockState(pos).getShape(level, pos);
        AABB bounds = shape.isEmpty() ? new AABB(pos) : shape.bounds().move(pos);
        Vec3 source = Vec3.atCenterOf(worldPosition);
        Vec3 target = new Vec3(
                bounds.minX + (bounds.maxX - bounds.minX) / 2.0D,
                bounds.minY + (bounds.maxY - bounds.minY) / 2.0D,
                bounds.minZ + (bounds.maxZ - bounds.minZ) / 2.0D);
        Vec3 diff = target.subtract(source);

        Vec3 xzProjected = new Vec3(diff.x, diff.z, 0.0D);
        Vec3 up = new Vec3(0.0D, 1.0D, 0.0D);
        double xAngle = MathHelper.angleBetween(up, xzProjected) / Math.PI * 180.0D;
        if (target.x < source.x) {
            xAngle = -xAngle;
        }
        rotationX = (float) xAngle + 90.0F;

        Vec3 horizontal = new Vec3(diff.x, 0.0D, diff.z);
        double yAngle = MathHelper.angleBetween(diff, horizontal) * 180.0D / Math.PI;
        if (target.y < source.y) {
            yAngle = -yAngle;
        }
        rotationY = (float) yAngle;

        checkForReceiver();
        commitRedirection();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
        return true;
    }

    public BlockPos getBinding() {
        return receiver == null ? null : receiver.getManaReceiverPos();
    }

    @Override
    public boolean onUsedByWand(Player player, ItemStack stack, Direction side) {
        checkForReceiver();
        if (level != null && player != null) {
            level.playSound(player, worldPosition, BotaniaSounds.ding, SoundSource.BLOCKS, 0.11F, 1.0F);
        }
        return true;
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return getLens().isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(items, slot, amount);
        if (!removed.isEmpty()) {
            setChanged();
            syncPending = true;
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (!stack.isEmpty() && stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
        syncPending = true;
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null
                && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof LensEffectItem;
    }

    @Override
    public void clearContent() {
        items.set(0, ItemStack.EMPTY);
    }

    @Override
    public void setRemoved() {
        if (addedToNetwork && level != null) {
            BotaniaAPI.instance().getManaNetworkInstance().fireManaNetworkEvent(this, ManaBlockType.COLLECTOR, ManaNetworkAction.REMOVE);
            addedToNetwork = false;
        }
        super.setRemoved();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt(TAG_MANA, mana);
        tag.putFloat(TAG_ROTATION_X, rotationX);
        tag.putFloat(TAG_ROTATION_Y, rotationY);
        tag.putBoolean(TAG_CAN_SHOOT, canShootBurst);
        tag.putInt(TAG_PINGBACK_TICKS, pingbackTicks);
        tag.putDouble(TAG_LAST_PINGBACK_X, lastPingbackX);
        tag.putDouble(TAG_LAST_PINGBACK_Y, lastPingbackY);
        tag.putDouble(TAG_LAST_PINGBACK_Z, lastPingbackZ);
        tag.putUUID(TAG_UUID, identity);
        if (!getLens().isEmpty()) {
            tag.put(TAG_LENS, getLens().save(registries, new CompoundTag()));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        mana = Mth.clamp(tag.getInt(TAG_MANA), 0, getMaxMana());
        rotationX = tag.contains(TAG_ROTATION_X) ? tag.getFloat(TAG_ROTATION_X) : 180.0F;
        rotationY = tag.contains(TAG_ROTATION_Y) ? tag.getFloat(TAG_ROTATION_Y) : 0.0F;
        canShootBurst = !tag.contains(TAG_CAN_SHOOT) || tag.getBoolean(TAG_CAN_SHOOT);
        pingbackTicks = tag.getInt(TAG_PINGBACK_TICKS);
        lastPingbackX = tag.getDouble(TAG_LAST_PINGBACK_X);
        lastPingbackY = tag.contains(TAG_LAST_PINGBACK_Y) ? tag.getDouble(TAG_LAST_PINGBACK_Y) : -2.147483648E9D;
        lastPingbackZ = tag.getDouble(TAG_LAST_PINGBACK_Z);
        identity = tag.hasUUID(TAG_UUID) ? tag.getUUID(TAG_UUID) : UUID.randomUUID();
        items.set(0, tag.contains(TAG_LENS) ? ItemStack.parseOptional(registries, tag.getCompound(TAG_LENS)) : ItemStack.EMPTY);
        addedToNetwork = false;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
