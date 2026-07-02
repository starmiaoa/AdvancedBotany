package com.pulxes.advancedbotany.common.entity;

import com.pulxes.advancedbotany.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EntityAlphirinePortal extends Entity {
    private static final EntityDataAccessor<ItemStack> DROP_STACK =
            SynchedEntityData.defineId(EntityAlphirinePortal.class, EntityDataSerializers.ITEM_STACK);
    private static final int DROP_DELAY = 40;

    public EntityAlphirinePortal(EntityType<? extends EntityAlphirinePortal> entityType, Level level) {
        super(entityType, level);
        noPhysics = true;
    }

    public EntityAlphirinePortal(Level level) {
        this(ModEntities.ALPHIRINE_PORTAL.get(), level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DROP_STACK, ItemStack.EMPTY);
    }

    @Override
    public void tick() {
        super.tick();
        setDeltaMovement(0.0D, 0.0D, 0.0D);

        if (tickCount < DROP_DELAY) {
            return;
        }

        ItemStack stack = getStack();
        if (stack.isEmpty()) {
            discard();
            return;
        }

        if (!level().isClientSide()) {
            ItemEntity result = new ItemEntity(level(), getX(), getY(), getZ(), stack.copy());
            level().addFreshEntity(result);
            discard();
        }
    }

    public ItemStack getStack() {
        return entityData.get(DROP_STACK);
    }

    public void setStack(ItemStack stack) {
        entityData.set(DROP_STACK, stack == null ? ItemStack.EMPTY : stack.copy());
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("portalTick", tickCount);
        CompoundTag stackTag = new CompoundTag();
        if (!getStack().isEmpty()) {
            getStack().save(registryAccess(), stackTag);
        }
        tag.put("dropStack", stackTag);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        tickCount = tag.getInt("portalTick");
        setStack(ItemStack.parseOptional(registryAccess(), tag.getCompound("dropStack")));
    }
}
