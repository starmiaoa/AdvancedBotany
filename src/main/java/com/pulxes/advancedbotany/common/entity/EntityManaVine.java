package com.pulxes.advancedbotany.common.entity;

import com.pulxes.advancedbotany.registry.ModBlocks;
import com.pulxes.advancedbotany.registry.ModEntities;
import java.awt.Color;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.player.BonemealEvent;
import vazkii.botania.api.BotaniaAPI;

public class EntityManaVine extends ThrowableProjectile {
    private static final EntityDataAccessor<Optional<UUID>> ATTACKER =
            SynchedEntityData.defineId(EntityManaVine.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final int MAX_LIFETIME = 240;

    public EntityManaVine(EntityType<? extends EntityManaVine> entityType, Level level) {
        super(entityType, level);
    }

    public EntityManaVine(Level level, LivingEntity owner) {
        super(ModEntities.MANA_VINE.get(), owner, level);
        shootFromRotation(owner, owner.getXRot(), owner.getYRot(), 0.0F, 1.5F, 1.0F);
        setDeltaMovement(getDeltaMovement().scale(0.9D));
        if (owner instanceof Player player) {
            setAttacker(player.getUUID());
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(ATTACKER, Optional.empty());
    }

    @Override
    public void tick() {
        super.tick();
        if (tickCount >= MAX_LIFETIME) {
            discard();
            return;
        }

        if (level().isClientSide()) {
            spawnTrailParticles();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        if (result.getType() == HitResult.Type.BLOCK) {
            onHitBlock((BlockHitResult) result);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (level().isClientSide()) {
            return;
        }

        Player player = getAttackerPlayer();
        if (player == null) {
            discard();
            return;
        }

        BlockPos hitPos = result.getBlockPos();
        makeAnimalsLove(hitPos, player);
        growPlantsAndLianas(hitPos, player);
        discard();
    }

    private Player getAttackerPlayer() {
        UUID attacker = getAttacker();
        return attacker == null ? null : level().getPlayerByUUID(attacker);
    }

    private void makeAnimalsLove(BlockPos center, Player player) {
        AABB bounds = new AABB(center).inflate(10.0D);
        List<Animal> animals = level().getEntitiesOfClass(Animal.class, bounds, Animal::isAlive);
        for (Animal animal : animals) {
            if (!animal.hurt(level().damageSources().playerAttack(player), 0.0F)) {
                continue;
            }
            animal.setInLove(player);
            animal.setInLoveTime(1200);
            if (level() instanceof ServerLevel serverLevel) {
                serverLevel.broadcastEntityEvent(animal, (byte) 18);
            }
        }
    }

    private void growPlantsAndLianas(BlockPos center, Player player) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        for (int x = -3; x <= 3; x++) {
            for (int y = -1; y <= 5; y++) {
                for (int z = -3; z <= 3; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    BlockState state = level().getBlockState(pos);
                    Block block = state.getBlock();
                    if (block instanceof BonemealableBlock bonemealable && !(block instanceof GrassBlock)) {
                        fertilize(serverLevel, pos, state, bonemealable, player);
                    } else {
                        growLianaBelow(serverLevel, pos, center);
                    }
                }
            }
        }
    }

    private void fertilize(ServerLevel level, BlockPos pos, BlockState state, BonemealableBlock bonemealable, Player player) {
        if (state.getBlock() instanceof SaplingBlock || level.getBlockEntity(pos) != null) {
            return;
        }

        BonemealEvent event = EventHooks.fireBonemealEvent(player, level, pos, state, ItemStack.EMPTY);
        if (event.isCanceled()) {
            return;
        }
        if (event.isSuccessful()) {
            level.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, pos, 6 + level.random.nextInt(4));
            return;
        }

        if (!bonemealable.isValidBonemealTarget(level, pos, state)
                || !bonemealable.isBonemealSuccess(level, level.random, pos, state)) {
            return;
        }

        for (int i = 0; i < 12; i++) {
            if (!level.getBlockState(pos).equals(state)) {
                return;
            }
            bonemealable.performBonemeal(level, level.random, pos, state);
        }
        level.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, pos, 6 + level.random.nextInt(4));
    }

    private void growLianaBelow(ServerLevel level, BlockPos supportPos, BlockPos center) {
        BlockPos currentPos = supportPos.below();
        if (!level.getBlockState(currentPos).isAir()) {
            return;
        }

        BlockState firstState = ModBlocks.FREYR_LIANA.get().defaultBlockState();
        if (!firstState.canSurvive(level, currentPos)) {
            return;
        }

        int distance = (int) Vec3.atLowerCornerOf(supportPos).distanceToSqr(Vec3.atLowerCornerOf(center));
        if (level.random.nextInt(distance + 1) != 0) {
            return;
        }

        while (currentPos.getY() > level.getMinBuildHeight() && level.getBlockState(currentPos).isAir()) {
            BlockState liana = level.random.nextInt(4) < 3
                    ? ModBlocks.FREYR_LIANA.get().defaultBlockState()
                    : ModBlocks.LUMINOUS_FREYR_LIANA.get().defaultBlockState();
            if (!liana.canSurvive(level, currentPos)) {
                return;
            }
            level.setBlock(currentPos, liana, Block.UPDATE_ALL);
            level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, currentPos, Block.getId(ModBlocks.FREYR_LIANA.get().defaultBlockState()));
            currentPos = currentPos.below();
        }
    }

    private void spawnTrailParticles() {
        for (int i = 0; i < 4; i++) {
            double spread = 6.0D;
            double x = getX() + (random.nextDouble() / spread - 0.5D / spread);
            double y = getY() + (random.nextDouble() / spread - 0.5D / spread);
            double z = getZ() + (random.nextDouble() / spread - 0.5D / spread);
            Color color = getCorporeaRuneColor((int) x, (int) y, (int) z);
            BotaniaAPI.instance().sparkleFX(
                    level(),
                    x,
                    y,
                    z,
                    color.getRed() / 255.0F,
                    color.getGreen() / 255.0F,
                    color.getBlue() / 255.0F,
                    0.15F + random.nextFloat() * 0.12F,
                    3);
        }
    }

    private void spawnBurstParticles() {
        for (int i = 0; i < 32; i++) {
            Color color = getCorporeaRuneColor(Mth.floor(getX()), Mth.floor(getY()), Mth.floor(getZ()));
            BotaniaAPI.instance().sparkleFX(
                    level(),
                    getX(),
                    getY(),
                    getZ(),
                    color.getRed() / 255.0F,
                    color.getGreen() / 255.0F,
                    color.getBlue() / 255.0F,
                    0.2F + random.nextFloat() * 0.12F,
                    4);
        }
    }

    private Color getCorporeaRuneColor(int x, int y, int z) {
        double time = tickCount + (level().getGameTime() % 20) / 20.0D;
        time += (x ^ y ^ z) % 360;
        float sin = (float) (Math.sin(time / 20.0D) * 0.15D) - 0.15F;
        return new Color(Color.HSBtoRGB(0.319F, 0.92F, 0.95F + sin - 0.15F));
    }

    @Override
    public void remove(RemovalReason reason) {
        if (level().isClientSide() && !isRemoved()) {
            spawnBurstParticles();
        }
        super.remove(reason);
    }

    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps, boolean teleport) {
        // The original projectile ignores client interpolation corrections.
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0F;
    }

    public UUID getAttacker() {
        return entityData.get(ATTACKER).orElse(null);
    }

    public void setAttacker(UUID attacker) {
        entityData.set(ATTACKER, Optional.ofNullable(attacker));
    }

    public void setAttacker(String attacker) {
        try {
            setAttacker(attacker == null || attacker.isEmpty() ? null : UUID.fromString(attacker));
        } catch (IllegalArgumentException ignored) {
            setAttacker((UUID) null);
        }
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
        } else if (tag.contains("attacker", Tag.TAG_STRING)) {
            setAttacker(tag.getString("attacker"));
        }
    }
}
