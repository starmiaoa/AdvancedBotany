package com.pulxes.advancedbotany.common.entity;

import com.pulxes.advancedbotany.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EntityNebulaBlaze extends ThrowableProjectile {
    private static final EntityDataAccessor<Optional<UUID>> ATTACKER = SynchedEntityData.defineId(EntityNebulaBlaze.class, EntityDataSerializers.OPTIONAL_UUID);

    public EntityNebulaBlaze(EntityType<? extends EntityNebulaBlaze> entityType, Level level) {
        super(entityType, level);
    }

    public EntityNebulaBlaze(Level level, Player owner) {
        super(ModEntities.NEBULA_BLAZE.get(), owner, level);
        setAttacker(owner.getUUID());
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(ATTACKER, Optional.empty());
    }

    @Override
    public void tick() {
        if (tickCount >= 240) {
            discard();
            return;
        }
        Vec3 previous = position();
        damageNearbyEntities();
        super.tick();
        homeTowardNearestTarget(previous);
    }

    private void homeTowardNearestTarget(Vec3 previous) {
        AABB bounds = getBoundingBox().inflate(3.75D);
        List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class, bounds,
                entity -> !(entity instanceof Player) && entity.isAlive());
        for (LivingEntity living : entities) {
            Vec3 delta = position().subtract(living.getX(), living.getY() + living.getEyeHeight(), living.getZ());
            double distance = delta.length();
            if (distance < 3.1D) {
                distance = 3.1D;
            }
            double pull = 1.0D - distance;
            setPos(previous.x, previous.y, previous.z);
            setDeltaMovement(delta.scale(pull * 0.325D / distance).add(living.getDeltaMovement().scale(0.85D)));
            move(net.minecraft.world.entity.MoverType.SELF, getDeltaMovement());
            return;
        }
    }

    private void damageNearbyEntities() {
        AABB bounds = new AABB(position(), position().add(getDeltaMovement())).inflate(1.0D);
        List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class, bounds, this::canDamage);
        for (LivingEntity living : entities) {
            if (level().isClientSide()) {
                return;
            }
            living.hurt(damageSource(), 18.0F);
            discard();
            return;
        }
    }

    private boolean canDamage(LivingEntity living) {
        UUID attacker = getAttacker();
        if (attacker != null && living.getUUID().equals(attacker)) {
            return false;
        }
        if (living instanceof Player && level() instanceof ServerLevel serverLevel && !serverLevel.getServer().isPvpAllowed()) {
            return false;
        }
        return living.invulnerableTime == 0;
    }

    private DamageSource damageSource() {
        UUID attacker = getAttacker();
        if (attacker != null && level() instanceof ServerLevel serverLevel) {
            ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(attacker);
            if (player != null) {
                return level().damageSources().playerAttack(player);
            }
        }
        return level().damageSources().magic();
    }

    @Override
    protected void onHit(HitResult result) {
        if (result.getType() != HitResult.Type.MISS) {
            super.onHit(result);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        // Damage is handled with the original broad AABB sweep in tick().
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        BlockState state = level().getBlockState(result.getBlockPos());
        if (!(state.getBlock() instanceof BushBlock) && !(state.getBlock() instanceof LeavesBlock)) {
            discard();
        }
    }

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps, boolean teleport) {
        // Original client entity ignores server interpolation corrections.
    }

    @Override
    protected float getGravity() {
        return 0.0F;
    }

    public UUID getAttacker() {
        return entityData.get(ATTACKER).orElse(null);
    }

    public void setAttacker(UUID attacker) {
        entityData.set(ATTACKER, Optional.ofNullable(attacker));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("ticks", tickCount);
        UUID attacker = getAttacker();
        if (attacker != null) {
            tag.putUUID("attacker", attacker);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        tickCount = tag.getInt("ticks");
        if (tag.hasUUID("attacker")) {
            setAttacker(tag.getUUID("attacker"));
        }
    }
}
