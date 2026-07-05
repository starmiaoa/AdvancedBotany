package com.pulxes.advancedbotany.common.item.relic;

import com.pulxes.advancedbotany.common.network.ModNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.mana.ManaItemHandler;

import java.util.List;

public class SphereNavigationItem extends ModRelicItem {
    public static final int RANGE_SEARCH = 16;
    public static final int MAX_COOLDOWN = 158;
    private static final int MANA_COST = 2_500;
    private static final String TAG_FIND_BLOCK_ID = "findBlockID";
    private static final String TAG_FIND_BLOCK_META = "findBlockMeta";
    private static final String TAG_COOLDOWN = "cooldown";
    private static final String TAG_INACTIVE = "inactive";

    public SphereNavigationItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        Block block = getFindBlock(stack);
        if (block == null) {
            return super.getName(stack);
        }
        return super.getName(stack).copy()
                .append(Component.literal(" (").withStyle(ChatFormatting.RESET))
                .append(new ItemStack(block).getHoverName().copy().withStyle(ChatFormatting.GREEN))
                .append(Component.literal(")").withStyle(ChatFormatting.RESET));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable(isActive(stack) ? "botaniamisc.active" : "botaniamisc.inactive"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!canUseRelic(stack, player)) {
            return InteractionResultHolder.fail(stack);
        }
        if (player.isShiftKeyDown() && getFindBlock(stack) != null) {
            setActive(stack, !isActive(stack));
            level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.3F, 0.1F);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        ItemStack stack = context.getItemInHand();
        if (!canUseRelic(stack, player)) {
            return InteractionResult.FAIL;
        }
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        setFindBlock(stack, state);
        if (context.getLevel().isClientSide()) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> showNavigationTargetHud(state));
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }

    private static void showNavigationTargetHud(BlockState state) {
        try {
            Class<?> handlers = Class.forName("com.pulxes.advancedbotany.client.ClientPacketHandlers");
            handlers.getMethod("handleNavigationTargetSet", BlockState.class).invoke(null, state);
        } catch (ReflectiveOperationException ignored) {
            // Client-only HUD feedback should never break target binding.
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (level.isClientSide() || !(entity instanceof Player player) || !isActive(stack) || getFindBlock(stack) == null || !canUseRelic(stack, player)) {
            return;
        }
        if (canWork(stack) && ManaItemHandler.instance().requestManaExactForTool(stack, player, MANA_COST, true)) {
            setMaxTick(stack);
            if (player instanceof ServerPlayer serverPlayer) {
                ModNetwork.sendFindNearBlocks(serverPlayer, getFindBlockId(stack), getFindMeta(stack));
            }
        }
    }

    public boolean canWork(ItemStack stack) {
        int tick = getCooldown(stack);
        if (tick == 0) {
            return true;
        }
        if (tick > 0) {
            stack.getOrCreateTag().putInt(TAG_COOLDOWN, tick - 1);
        }
        return false;
    }

    public void setMaxTick(ItemStack stack) {
        stack.getOrCreateTag().putInt(TAG_COOLDOWN, MAX_COOLDOWN);
    }

    public static boolean isActive(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null || !tag.getBoolean(TAG_INACTIVE);
    }

    public static boolean hasTarget(ItemStack stack) {
        return getFindBlock(stack) != null;
    }

    public static void setActive(ItemStack stack, boolean active) {
        stack.getOrCreateTag().putBoolean(TAG_INACTIVE, !active);
    }

    public static void setFindBlock(ItemStack stack, BlockState state) {
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        if (id != null) {
            stack.getOrCreateTag().putString(TAG_FIND_BLOCK_ID, id.toString());
            stack.getOrCreateTag().putInt(TAG_FIND_BLOCK_META, Block.getId(state));
        }
    }

    @Nullable
    public static Block getFindBlock(ItemStack stack) {
        ResourceLocation id = getFindBlockId(stack);
        return id == null ? null : ForgeRegistries.BLOCKS.getValue(id);
    }

    @Nullable
    public static ResourceLocation getFindBlockId(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_FIND_BLOCK_ID)) {
            return null;
        }
        ResourceLocation id = ResourceLocation.tryParse(tag.getString(TAG_FIND_BLOCK_ID));
        return id == null || !ForgeRegistries.BLOCKS.containsKey(id) ? null : id;
    }

    public static int getFindMeta(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? -1 : tag.getInt(TAG_FIND_BLOCK_META);
    }

    private static int getCooldown(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : tag.getInt(TAG_COOLDOWN);
    }
}
