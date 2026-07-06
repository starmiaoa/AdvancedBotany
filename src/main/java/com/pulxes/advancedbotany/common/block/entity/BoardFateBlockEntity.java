package com.pulxes.advancedbotany.common.block.entity;

import com.pulxes.advancedbotany.api.AdvancedBotanyAPI;
import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.registry.ModBlockEntities;
import com.pulxes.advancedbotany.registry.ModSounds;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.botania.api.item.Relic;
import vazkii.botania.xplat.XplatAbstractions;

public class BoardFateBlockEntity extends BaseInventoryBlockEntity {
    private static final String TAG_SLOT_CHANCE = "slotChance";
    private static final String TAG_REQUEST_UPDATE = "requestUpdate";
    private static final String TAG_PLAYER_ROOT = AdvancedBotany.MOD_ID + ":fate_board";
    private static final String TAG_GRANTED_RELICS = "granted_relics";
    private static final boolean[] FATE_BOARD_RELIC_ENABLED = new boolean[] {
            true, true, true, true, true, true, true, true, true, true, true, true
    };

    public byte[] slotChance = new byte[] {0, 0};
    public final int[] clientTick = new int[] {0, 0};
    public boolean requestUpdate;

    public BoardFateBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FATE_PLAYING_BOARD.get(), pos, state, 2);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BoardFateBlockEntity board) {
        board.updateServer();
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BoardFateBlockEntity board) {
        board.updateAnimationTicks();
    }

    public void updateAnimationTicks() {
        for (int i = 0; i < getContainerSize(); i++) {
            clientTick[i] = getItem(i).isEmpty() ? 0 : clientTick[i] + 1;
        }
    }

    protected void updateServer() {
        if (level == null) {
            return;
        }
        boolean hasUpdate = false;
        if (hasFreeSlot()) {
            hasUpdate = setDiceFate();
        }
        if (requestUpdate || hasUpdate) {
            requestUpdate = false;
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
            setChanged();
        }
    }

    public boolean insertDice(Player player, ItemStack heldItem) {
        if (level == null || !isDice(heldItem)) {
            return false;
        }
        for (int slot = 0; slot < getContainerSize(); slot++) {
            if (!getItem(slot).isEmpty()) {
                continue;
            }
            if (!level.isClientSide()) {
                ItemStack copy = heldItem.copy();
                copy.setCount(1);
                items.set(slot, copy);
                slotChance[slot] = (byte) (level.random.nextInt(6) + 1);
                heldItem.shrink(1);
                requestUpdate = true;
                level.playSound(null, worldPosition, ModSounds.BOARD_CUBE.get(), SoundSource.BLOCKS, 0.6F, 1.0F);
                sync();
            }
            return true;
        }
        return false;
    }

    public boolean spawnRelic(Player player) {
        if (level == null) {
            return false;
        }

        int relicCount = 0;
        for (int i = 0; i < getContainerSize(); i++) {
            ItemStack stack = getItem(i);
            if (stack.isEmpty()) {
                slotChance[i] = 0;
            } else {
                if (!isRightPlayer(player, stack)) {
                    if (!level.isClientSide()) {
                        dropRelic(player, i);
                    }
                    return true;
                }
                items.set(i, ItemStack.EMPTY);
            }
            relicCount += slotChance[i];
        }

        if (relicCount < 1) {
            return false;
        }

        if (!level.isClientSide()) {
            level.playSound(null, worldPosition, SoundEvents.ARROW_SHOOT, SoundSource.BLOCKS,
                    0.5F, 0.4F / (level.random.nextFloat() * 0.4F + 0.8F));

            ItemStack relic = getRelicForRoll(relicCount);
            if (!isRelicRollEnabled(relicCount) || relic.isEmpty() || hasRelic(player, relic)) {
                player.displayClientMessage(Component.translatable("botaniamisc.dudDiceRoll", relicCount).withStyle(ChatFormatting.DARK_GREEN), false);
            } else {
                bindRelicToPlayer(relic, player);
                markRelicGranted(player, relic);
                ItemEntity entityItem = new ItemEntity(level, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D, relic);
                level.addFreshEntity(entityItem);
                player.displayClientMessage(Component.translatable("botaniamisc.diceRoll", relicCount).withStyle(ChatFormatting.DARK_GREEN), false);
            }

            for (int i = 0; i < slotChance.length; i++) {
                slotChance[i] = 0;
            }
            requestUpdate = true;
            sync();
        }

        return true;
    }

    private ItemStack getRelicForRoll(int relicCount) {
        if (AdvancedBotanyAPI.relicList.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int index = Math.min(relicCount - 1, AdvancedBotanyAPI.relicList.size() - 1);
        return AdvancedBotanyAPI.relicList.get(index).copy();
    }

    private static boolean isRelicRollEnabled(int relicCount) {
        int index = relicCount - 1;
        return index >= 0 && (index >= FATE_BOARD_RELIC_ENABLED.length || FATE_BOARD_RELIC_ENABLED[index]);
    }

    private void dropRelic(Player player, int slot) {
        if (level == null) {
            return;
        }
        ItemStack stack = getItem(slot);
        if (stack.isEmpty()) {
            return;
        }
        Vec3 look = player.getLookAngle();
        ItemEntity entityItem = new ItemEntity(level, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.8D, worldPosition.getZ() + 0.5D, stack.copy());
        entityItem.setDeltaMovement(look.x * 0.15D, 0.25D, look.z * 0.15D);
        items.set(slot, ItemStack.EMPTY);
        level.addFreshEntity(entityItem);
        requestUpdate = true;
        sync();
    }

    protected boolean hasFreeSlot() {
        for (int i = 0; i < getContainerSize(); i++) {
            if (getItem(i).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    protected boolean setDiceFate() {
        if (level == null) {
            return false;
        }
        AABB bounds = new AABB(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(),
                worldPosition.getX() + 1.0D, worldPosition.getY() + 0.7D, worldPosition.getZ() + 1.0D);
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, bounds);
        for (ItemEntity item : items) {
            if (item.isRemoved() || item.getItem().isEmpty() || !isDice(item.getItem())) {
                continue;
            }
            ItemStack stack = item.getItem();
            for (int slot = 0; slot < getContainerSize(); slot++) {
                if (!getItem(slot).isEmpty()) {
                    continue;
                }
                ItemStack copy = stack.copy();
                copy.setCount(1);
                this.items.set(slot, copy);
                slotChance[slot] = (byte) (level.random.nextInt(6) + 1);
                stack.shrink(1);
                if (stack.isEmpty()) {
                    item.discard();
                }
                level.playSound(null, worldPosition, ModSounds.BOARD_CUBE.get(), SoundSource.BLOCKS, 0.6F, 1.0F);
                setChanged();
                return true;
            }
        }
        return false;
    }

    public static boolean isDice(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        for (ItemStack dice : AdvancedBotanyAPI.diceList) {
            // The original board matches dice by item only (TileBoardFate.isDice compares
            // getItem() + wildcardable meta, never NBT). A real Dice of Fate is a soulbound
            // relic carrying owner NBT, so tag-strict matching would reject every bound die.
            if (stack.is(dice.getItem())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isRightPlayer(Player player, ItemStack stack) {
        Relic relic = XplatAbstractions.INSTANCE.findRelic(stack);
        return relic == null || relic.isRightPlayer(player);
    }

    private static void bindRelicToPlayer(ItemStack stack, Player player) {
        Relic relic = XplatAbstractions.INSTANCE.findRelic(stack);
        if (relic != null) {
            relic.bindToUUID(player.getUUID());
        }
    }

    private static boolean hasRelicAdvancement(Player player, ItemStack stack) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        Relic relic = XplatAbstractions.INSTANCE.findRelic(stack);
        ResourceLocation advancementId = relic == null ? null : relic.getAdvancement();
        if (advancementId == null || serverPlayer.server == null) {
            return false;
        }
        Advancement advancement = serverPlayer.server.getAdvancements().getAdvancement(advancementId);
        return advancement != null && serverPlayer.getAdvancements().getOrStartProgress(advancement).isDone();
    }

    private static boolean hasRelic(Player player, ItemStack stack) {
        return hasRelicAdvancement(player, stack) || hasPersistentRelicGrant(player, stack);
    }

    private static boolean hasPersistentRelicGrant(Player player, ItemStack stack) {
        String id = relicId(stack);
        if (id.isEmpty()) {
            return false;
        }
        CompoundTag root = player.getPersistentData().getCompound(TAG_PLAYER_ROOT);
        ListTag granted = root.getList(TAG_GRANTED_RELICS, Tag.TAG_STRING);
        for (int i = 0; i < granted.size(); i++) {
            if (id.equals(granted.getString(i))) {
                return true;
            }
        }
        return false;
    }

    private static void markRelicGranted(Player player, ItemStack stack) {
        String id = relicId(stack);
        if (id.isEmpty() || hasPersistentRelicGrant(player, stack)) {
            return;
        }
        CompoundTag persistent = player.getPersistentData();
        CompoundTag root = persistent.getCompound(TAG_PLAYER_ROOT);
        ListTag granted = root.getList(TAG_GRANTED_RELICS, Tag.TAG_STRING);
        granted.add(StringTag.valueOf(id));
        root.put(TAG_GRANTED_RELICS, granted);
        persistent.put(TAG_PLAYER_ROOT, root);
    }

    private static String relicId(ItemStack stack) {
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return id == null ? "" : id.toString();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return isDice(stack);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putByteArray(TAG_SLOT_CHANCE, slotChance);
        tag.putBoolean(TAG_REQUEST_UPDATE, requestUpdate);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        byte[] chance = tag.getByteArray(TAG_SLOT_CHANCE);
        if (chance.length == 2) {
            slotChance = chance;
        }
        requestUpdate = tag.getBoolean(TAG_REQUEST_UPDATE);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putByteArray(TAG_SLOT_CHANCE, slotChance);
        tag.putBoolean(TAG_REQUEST_UPDATE, requestUpdate);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }
}
