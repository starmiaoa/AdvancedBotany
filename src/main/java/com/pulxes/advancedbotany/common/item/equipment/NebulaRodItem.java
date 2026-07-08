package com.pulxes.advancedbotany.common.item.equipment;

import com.pulxes.advancedbotany.api.AdvancedBotanyAPI;
import com.pulxes.advancedbotany.registry.ModSounds;
import java.awt.Color;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.client.fx.WispParticleData;

public class NebulaRodItem extends Item {
    private static final String DIMENSION_BLACKLIST_PROPERTY = "advancedbotany.nebula_rod.dimension_blacklist";
    private static final Set<ResourceLocation> DIMENSION_BLACKLIST =
            parseDimensionBlacklist(System.getProperty(DIMENSION_BLACKLIST_PROPERTY, ""));

    public NebulaRodItem(Properties properties) {
        // Damage is a gameplay cooldown, not wear - the original forbids anvil repair.
        super(properties.stacksTo(1).setNoRepair().durability(AdvancedBotanyEquipment.NEBULA_ROD_MAX_DAMAGE).rarity(AdvancedBotanyAPI.RARITY_NEBULA));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getDamageValue() == 0 && canUseInDimension(level)) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!(livingEntity instanceof Player player)) {
            return;
        }
        int useTime = getUseDuration(stack, player) - remainingUseDuration;
        if (useTime > 110 && !player.isShiftKeyDown()) {
            if (!canUseInDimension(level)) {
                player.stopUsingItem();
                return;
            }
            if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                BlockPos target = findTopBlock(level, player);
                if (target == null) {
                    player.stopUsingItem();
                    player.displayClientMessage(Component.translatable("ab.nebulaRod.notTeleporting").withStyle(ChatFormatting.DARK_PURPLE), true);
                    return;
                }

                EntityTeleportEvent.EnderEntity event = new EntityTeleportEvent.EnderEntity(player, target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D);
                if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
                    player.stopUsingItem();
                    player.displayClientMessage(Component.translatable("ab.nebulaRod.notTeleportingEvent").withStyle(ChatFormatting.DARK_PURPLE), true);
                    return;
                }

                serverPlayer.connection.teleport(event.getTargetX(), event.getTargetY(), event.getTargetZ(), player.getYRot(), player.getXRot());
                level.playSound(null, player.blockPosition(), ModSounds.NEBULA_ROD.get(), SoundSource.PLAYERS, 1.2F, 1.2F);
            }
            if (!player.getAbilities().instabuild) {
                stack.setDamageValue(AdvancedBotanyEquipment.NEBULA_ROD_MAX_DAMAGE);
            }
            player.stopUsingItem();
        }

        spawnPortalParticle(level, player, useTime, level.random.nextBoolean() ? 9641964 : 4920962, 1.0F);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide() && entity instanceof Player player && entity.tickCount % AdvancedBotanyEquipment.NEBULA_ROD_REPAIR_INTERVAL == 0
                && stack.getDamageValue() > 0
                && ManaItemHandler.instance().requestManaExactForTool(stack, player, AdvancedBotanyEquipment.NEBULA_ROD_MANA_PER_REPAIR, true)) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72_000;
    }

    private static BlockPos findTopBlock(Level level, Player player) {
        Vec3 look = player.getLookAngle().normalize();
        int limit = AdvancedBotanyEquipment.NEBULA_ROD_XZ_LIMIT;
        for (int distance = 256; distance > 8; distance--) {
            int x = Mth.clamp(Mth.floor(player.getX() + look.x * distance), -(limit - 1), limit - 1);
            int z = Mth.clamp(Mth.floor(player.getZ() + look.z * distance), -(limit - 1), limit - 1);
            int top = level.getMaxBuildHeight() - 2;
            for (int y = top; y > level.getMinBuildHeight(); y--) {
                BlockPos solidPos = new BlockPos(x, y, z);
                BlockState state = level.getBlockState(solidPos);
                if (state.isAir() || state.is(Blocks.BEDROCK)) {
                    continue;
                }
                if (level.getBlockState(solidPos.above()).isAir() && level.getBlockState(solidPos.above(2)).isAir()) {
                    return solidPos.above();
                }
            }
        }
        return null;
    }

    private static boolean canUseInDimension(Level level) {
        return !DIMENSION_BLACKLIST.contains(level.dimension().location());
    }

    private static Set<ResourceLocation> parseDimensionBlacklist(String serialized) {
        if (serialized == null || serialized.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(serialized.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(ResourceLocation::tryParse)
                .filter(location -> location != null)
                .collect(Collectors.toUnmodifiableSet());
    }

    private static void spawnPortalParticle(Level level, Player player, int time, int color, float particleTime) {
        if (!level.isClientSide()) {
            return;
        }

        boolean isFinish = time > 80;
        int ticks = Math.min(100, time);
        if (time % 40 == 1) {
            // The original loops the nether-portal ambience (portal.portal) while charging.
            player.playSound(net.minecraft.sounds.SoundEvents.PORTAL_AMBIENT, 1.2F, 1.0F);
        }
        int totalSpiritCount = (int) Math.max(3.0F, (float) ticks / 100.0F * 18.0F);
        double tickIncrement = 360.0D / totalSpiritCount;
        int speed = 8;
        double wticks = (double) (ticks * speed) - tickIncrement;
        double radius = Math.sin((double) ticks / 100.0D) * Math.max(0.75D, 1.4D * (double) ticks / 100.0D);
        Vec3 look = player.getLookAngle();
        // Remote players get the original 1.62 eye-height offset so the ring sits at the upper body.
        float yawOffset = player.isLocalPlayer() ? 0.0F : 1.62F;
        MutableVec lookOffset = new MutableVec(look.x, look.y + yawOffset, look.z);
        MutableVec playerPos = new MutableVec(player.getX(), player.getY(), player.getZ());
        MutableVec particlePos = new MutableVec();

        for (int i = 0; i < totalSpiritCount; i++) {
            float size = Math.max(0.215F, (float) ticks / 100.0F);
            particlePos.set(Math.sin(wticks * Math.PI / 180.0D) / 1.825D * radius,
                    Math.cos(wticks * Math.PI / 180.0D) * radius,
                    0.8F);
            rotate(player.getXRot(), 1.0F, 0.0F, 0.0F, particlePos);
            rotate(-player.getYRot(), 0.0F, 1.0F, 0.0F, particlePos);
            particlePos.add(lookOffset).add(playerPos);
            wticks += tickIncrement;

            float[] hsb = Color.RGBtoHSB(color & 0xFF, color >> 8 & 0xFF, color >> 16 & 0xFF, null);
            int color1 = Color.HSBtoRGB(hsb[0], hsb[1], (float) ticks / 100.0F);
            float red = (float) (color1 & 0xFF) / 255.0F;
            float green = (float) (color1 >> 8 & 0xFF) / 255.0F;
            float blue = (float) (color1 >> 16 & 0xFF) / 255.0F;
            float motionSpeed = 0.25F * Math.min(1.0F, (float) (time - 80) / 30.0F);

            WispParticleData mainWisp = WispParticleData.wisp(0.3F * size, red, green, blue, 0.3F * particleTime);
            level.addParticle(mainWisp, particlePos.x, particlePos.y, particlePos.z,
                    isFinish ? (float) (look.x * -1.0D) * motionSpeed : 0.0F,
                    isFinish ? (float) (look.y * -1.0D) * motionSpeed : 0.0F,
                    isFinish ? (float) (look.z * -1.0D) * motionSpeed : 0.0F);

            WispParticleData randomWisp = WispParticleData.wisp((float) (Math.random() * 0.1F + 0.05F) * size,
                    red, green, blue, 0.4F * particleTime);
            level.addParticle(randomWisp, particlePos.x, particlePos.y, particlePos.z,
                    (float) (Math.random() - 0.5D) * 0.05F,
                    (float) (Math.random() - 0.5D) * 0.05F,
                    (float) (Math.random() - 0.5D) * 0.05F);
        }
    }

    private static void rotate(float angle, float axisX, float axisY, float axisZ, MutableVec vec) {
        MutableVec normalized = vec.copy().normalize();
        double halfAngle = Math.toRadians(angle) * 0.5D;
        double sin = Math.sin(halfAngle);
        double x = axisX * sin;
        double y = axisY * sin;
        double z = axisZ * sin;
        double w = Math.cos(halfAngle);
        double d = -x * normalized.x - y * normalized.y - z * normalized.z;
        double d1 = w * normalized.x + y * normalized.z - z * normalized.y;
        double d2 = w * normalized.y - x * normalized.z + z * normalized.x;
        double d3 = w * normalized.z + x * normalized.y - y * normalized.x;
        vec.x = d1 * w - d * x - d2 * z + d3 * y;
        vec.y = d2 * w - d * y + d1 * z - d3 * x;
        vec.z = d3 * w - d * z - d1 * y + d2 * x;
    }

    private static final class MutableVec {
        private double x;
        private double y;
        private double z;

        private MutableVec() {
        }

        private MutableVec(double x, double y, double z) {
            set(x, y, z);
        }

        private MutableVec set(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }

        private MutableVec add(MutableVec other) {
            x += other.x;
            y += other.y;
            z += other.z;
            return this;
        }

        private MutableVec copy() {
            return new MutableVec(x, y, z);
        }

        private MutableVec normalize() {
            double length = Math.sqrt(x * x + y * y + z * z);
            if (length != 0.0D) {
                x /= length;
                y /= length;
                z /= length;
            }
            return this;
        }
    }
}
