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

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import vazkii.botania.api.BotaniaAPI;

public class EntitySword extends ThrowableProjectile {
    private static final EntityDataAccessor<Float> DAMAGE = SynchedEntityData.defineId(EntitySword.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Optional<UUID>> ATTACKER = SynchedEntityData.defineId(EntitySword.class, EntityDataSerializers.OPTIONAL_UUID);

    public EntitySword(EntityType<? extends EntitySword> entityType, Level level) {
        super(entityType, level);
    }

    public EntitySword(Level level, Player owner) {
        super(ModEntities.SWORD.get(), owner, level);
        setAttacker(owner.getUUID());
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DAMAGE, 0.0F);
        entityData.define(ATTACKER, Optional.empty());
    }

    @Override
    public void tick() {
        super.tick();
        damageNearbyEntities();
        if (tickCount < 20) {
            setDeltaMovement(getDeltaMovement().scale(1.115D));
        } else if (tickCount > 160) {
            discard();
        }
        spawnTrailParticles();
    }

    private void spawnTrailParticles() {
        if (!level().isClientSide()) {
            return;
        }
        for (int i = 0; i < 12; i++) {
            float r = random.nextBoolean() ? 0.88235295F : 0.39607844F;
            float g = random.nextBoolean() ? 0.2627451F : 0.81960785F;
            float b = random.nextBoolean() ? 0.9411765F : 0.88235295F;
            BotaniaAPI.instance().sparkleFX(
                    level(),
                    getX() + (random.nextDouble() - 0.5D) * 0.25D,
                    getY() + (random.nextDouble() - 0.5D) * 0.25D,
                    getZ() + (random.nextDouble() - 0.5D) * 0.25D,
                    r + (float) (random.nextDouble() / 4.0D - 0.125D),
                    g + (float) (random.nextDouble() / 4.0D - 0.125D),
                    b + (float) (random.nextDouble() / 4.0D - 0.125D),
                    1.6F * (random.nextFloat() - 0.5F),
                    2);
        }
    }

    private void damageNearbyEntities() {
        AABB bounds = new AABB(xOld, yOld, zOld, getX(), getY(), getZ()).inflate(1.0D);
        List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class, bounds, this::canDamage);
        for (LivingEntity living : entities) {
            if (level().isClientSide()) {
                return;
            }
            living.hurt(damageSource(), getDamage());
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

    public float getDamage() {
        return entityData.get(DAMAGE);
    }

    public void setDamage(float damage) {
        entityData.set(DAMAGE, damage);
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
        tag.putFloat("disDamage", getDamage());
        UUID attacker = getAttacker();
        if (attacker != null) {
            tag.putUUID("attacker", attacker);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        tickCount = tag.getInt("ticks");
        setDamage(tag.getFloat("disDamage"));
        if (tag.hasUUID("attacker")) {
            setAttacker(tag.getUUID("attacker"));
        }
    }
}
