package com.pulxes.advancedbotany.common.entity;

import com.pulxes.advancedbotany.registry.ModEntities;
import java.awt.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.FloatingFlowerVariant;
import vazkii.botania.common.item.GrassSeedsItem;

public class EntitySeed extends ThrowableProjectile implements ItemSupplier {
    private static final EntityDataAccessor<ItemStack> SEED = SynchedEntityData.defineId(EntitySeed.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> RADIUS = SynchedEntityData.defineId(EntitySeed.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> ATTACKER = SynchedEntityData.defineId(EntitySeed.class, EntityDataSerializers.STRING);

    public EntitySeed(EntityType<? extends EntitySeed> entityType, Level level) {
        super(entityType, level);
    }

    public EntitySeed(Level level, Player player) {
        super(ModEntities.SEED.get(), player, level);
        setAttacker(player.getName().getString());
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(SEED, ItemStack.EMPTY);
        entityData.define(RADIUS, 1);
        entityData.define(ATTACKER, "");
    }

    @Override
    public void tick() {
        super.tick();
        if (tickCount >= 240) {
            discard();
            return;
        }
        if (level().isClientSide()) {
            spawnTrailParticles();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnTrailParticles() {
        int radius = Math.max(1, getRadius());
        float spreadDivisor = 4.0F / ((float) radius / 20.0F);
        float size = 1.0F + (float) radius / 12.0F;
        Color color = getSeedColor(getSeed());
        for (int i = 0; i < 5; i++) {
            double x = getX() + (Math.random() - 0.5D) / spreadDivisor;
            double y = getY() + (Math.random() - 0.5D) / spreadDivisor;
            double z = getZ() + (Math.random() - 0.5D) / spreadDivisor;
            BotaniaAPI.instance().sparkleFX(level(), x, y, z,
                    color.getRed() / 255.0F,
                    color.getGreen() / 255.0F,
                    color.getBlue() / 255.0F,
                    0.0625F * size + (float) Math.random() * 0.12F,
                    5);
        }
    }

    @Override
    protected float getGravity() {
        return 0.025F;
    }

    @Override
    protected void onHit(HitResult result) {
        if (result.getType() == HitResult.Type.BLOCK) {
            onHitBlock((BlockHitResult) result);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        BlockPos hitPos = result.getBlockPos();
        Block block = level().getBlockState(hitPos).getBlock();
        Player player = getAttackingPlayer();
        if (block instanceof BushBlock || block instanceof LeavesBlock || player == null) {
            return;
        }

        ItemStack seed = getSeed();
        if (!seed.isEmpty() && seed.getItem() instanceof GrassSeedsItem grassSeeds) {
            for (int xOffset = 0; xOffset < getRadius(); xOffset++) {
                for (int zOffset = 0; zOffset < getRadius(); zOffset++) {
                    int x = hitPos.getX() + xOffset - getRadius() / 2;
                    int y = hitPos.getY();
                    int z = hitPos.getZ() + zOffset - getRadius() / 2;
                    int scanY = y;
                    if (isTopBlock(new BlockPos(x, y - 1, z))) {
                        scanY = Math.max(level().getMinBuildHeight(), y - 20);
                    }
                    while (!isTopBlock(new BlockPos(x, scanY, z)) && Math.abs(scanY - y) <= 40) {
                        scanY++;
                    }
                    BlockPos target = new BlockPos(x, scanY, z);
                    if (!level().isClientSide() && isDirt(target)) {
                        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level(), target, level().getBlockState(target), player);
                        if (!MinecraftForge.EVENT_BUS.post(event)) {
                            ItemStack seedCopy = seed.copy();
                            seedCopy.setCount(1);
                            grassSeeds.useOn(new UseOnContext(level(), player, InteractionHand.MAIN_HAND, seedCopy,
                                    new BlockHitResult(Vec3.atCenterOf(target), result.getDirection(), target, false)));
                        }
                    } else if ((Math.random() < 0.15D || getRadius() < 3) && isDirt(target)) {
                        spawnGrowParticles(target);
                    }
                }
            }
        }
        discard();
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnGrowParticles(BlockPos pos) {
        Color color = getSeedColor(getSeed());
        for (int i = 0; i < 50; i++) {
            double x = (Math.random() - 0.5D) * 3.0D;
            double y = Math.random() + 0.5D;
            double z = (Math.random() - 0.5D) * 3.0D;
            BotaniaAPI.instance().sparkleFX(level(),
                    pos.getX() + 0.5D + x,
                    pos.getY() + 0.5D + y,
                    pos.getZ() + 0.5D + z,
                    color.getRed() / 255.0F,
                    color.getGreen() / 255.0F,
                    color.getBlue() / 255.0F,
                    (float) Math.random() * 0.15F + 0.15F,
                    5);
        }
    }

    private Player getAttackingPlayer() {
        if (getOwner() instanceof Player player) {
            return player;
        }
        String attacker = getAttacker();
        if (attacker.isEmpty()) {
            return null;
        }
        for (Player player : level().players()) {
            if (player.getName().getString().equals(attacker)) {
                return player;
            }
        }
        return null;
    }

    private boolean isDirt(BlockPos pos) {
        Block block = level().getBlockState(pos).getBlock();
        return block == Blocks.DIRT || block == Blocks.GRASS_BLOCK;
    }

    private boolean isTopBlock(BlockPos pos) {
        BlockState stateAbove = level().getBlockState(pos.above());
        return stateAbove.isAir() || stateAbove.getBlock() instanceof BushBlock;
    }

    public int getRadius() {
        return entityData.get(RADIUS);
    }

    public void setRadius(int radius) {
        entityData.set(RADIUS, Math.max(0, radius));
    }

    public ItemStack getSeed() {
        return entityData.get(SEED);
    }

    public void setSeed(ItemStack stack) {
        ItemStack seed = stack == null ? ItemStack.EMPTY : stack.copy();
        if (!seed.isEmpty()) {
            seed.setCount(1);
        }
        entityData.set(SEED, seed);
    }

    public String getAttacker() {
        return entityData.get(ATTACKER);
    }

    public void setAttacker(String attacker) {
        entityData.set(ATTACKER, attacker == null ? "" : attacker);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("ticks", tickCount);
        tag.putString("attacker", getAttacker());
        tag.put("seedStack", getSeed().save(new CompoundTag()));
        tag.putInt("radius", getRadius());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        tickCount = tag.getInt("ticks");
        setAttacker(tag.getString("attacker"));
        setSeed(ItemStack.of(tag.getCompound("seedStack")));
        setRadius(tag.getInt("radius"));
    }

    @Override
    public ItemStack getItem() {
        ItemStack seed = getSeed();
        return seed.isEmpty() ? new ItemStack(BotaniaItems.grassSeeds) : seed;
    }

    public static Color getSeedColor(ItemStack seed) {
        if (seed.getItem() instanceof FloatingFlowerVariant variant) {
            return switch (variant.getIslandType(seed).toString()) {
                case "podzol" -> new Color(0.5F, 0.37F, 0.0F);
                case "mycel" -> new Color(0.27F, 0.0F, 0.33F);
                case "dry" -> new Color(0.4F, 0.5F, 0.05F);
                case "golden" -> new Color(0.75F, 0.7F, 0.0F);
                case "vivid" -> new Color(0.0F, 0.5F, 0.1F);
                case "scorched" -> new Color(0.75F, 0.0F, 0.0F);
                case "infused" -> new Color(0.0F, 0.55F, 0.55F);
                case "mutated" -> new Color(0.4F, 0.1F, 0.4F);
                default -> new Color(0.0F, 0.4F, 0.0F);
            };
        }
        if (seed.is(BotaniaItems.podzolSeeds)) {
            return new Color(0.5F, 0.37F, 0.0F);
        } else if (seed.is(BotaniaItems.mycelSeeds)) {
            return new Color(0.27F, 0.0F, 0.33F);
        } else if (seed.is(BotaniaItems.drySeeds)) {
            return new Color(0.4F, 0.5F, 0.05F);
        } else if (seed.is(BotaniaItems.goldenSeeds)) {
            return new Color(0.75F, 0.7F, 0.0F);
        } else if (seed.is(BotaniaItems.vividSeeds)) {
            return new Color(0.0F, 0.5F, 0.1F);
        } else if (seed.is(BotaniaItems.scorchedSeeds)) {
            return new Color(0.75F, 0.0F, 0.0F);
        } else if (seed.is(BotaniaItems.infusedSeeds)) {
            return new Color(0.0F, 0.55F, 0.55F);
        } else if (seed.is(BotaniaItems.mutatedSeeds)) {
            return new Color(0.4F, 0.1F, 0.4F);
        }
        return new Color(0.0F, 0.4F, 0.0F);
    }
}
