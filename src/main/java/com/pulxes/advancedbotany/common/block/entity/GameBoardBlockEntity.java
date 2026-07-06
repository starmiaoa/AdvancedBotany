package com.pulxes.advancedbotany.common.block.entity;

import com.pulxes.advancedbotany.registry.ModBlockEntities;
import com.pulxes.advancedbotany.registry.ModSounds;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class GameBoardBlockEntity extends BlockEntity {
    private static final String TAG_SLOT_CHANCE = "slotChance";
    private static final String TAG_BOT_TICK = "botTick";
    private static final String TAG_END_GAME_TICK = "endGameTick";
    private static final String TAG_REQUEST_UPDATE = "requestUpdate";
    private static final String TAG_SINGLE_GAME = "isSingleGame";
    private static final String TAG_CUSTOM_GAME = "isAnonimGame";
    private static final String TAG_CUSTOM_STACK = "customStack";

    public final String[] playersName = new String[] {"", ""};
    public byte[] slotChance = new byte[] {0, 0, 0, 0};
    public final int[] clientTick = new int[] {0, 0, 0, 0};
    public int botTick = -1;
    public int endGameTick = -1;
    public boolean isSingleGame = true;
    public boolean isCustomGame;
    private boolean requestUpdate;
    private ItemStack customStack = ItemStack.EMPTY;

    public GameBoardBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PLAYING_BOARD.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, GameBoardBlockEntity board) {
        if (board.botTick > 0) {
            board.botTick--;
        }
        if (board.endGameTick > 0) {
            board.endGameTick--;
        }
        board.updateServer();
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, GameBoardBlockEntity board) {
        board.updateAnimationTicks();
    }

    public void updateAnimationTicks() {
        for (int i = 0; i < slotChance.length; i++) {
            clientTick[i] = slotChance[i] > 0 ? clientTick[i] + 1 : 0;
        }
    }

    protected void updateServer() {
        if (level == null) {
            return;
        }
        if (hasGame() && endGameTick == 0 && !isCustomGame) {
            finishGame();
        }
        if (level.getGameTime() % 20L == 0L && hasFullDice() && endGameTick == -1) {
            endGameTick = 28;
            requestUpdate = true;
        }

        boolean hasUpdate = false;
        if (isSingleGame) {
            if (botTick == 0 && hasGame()) {
                for (int i = 2; i < 4; i++) {
                    if (slotChance[i] == 0) {
                        slotChance[i] = (byte) (level.random.nextInt(6) + 1);
                        botTick = -1;
                        hasUpdate = true;
                        level.playSound(null, worldPosition, ModSounds.BOARD_CUBE.get(), SoundSource.BLOCKS, 0.6F, 1.0F);
                        break;
                    }
                }
            }
        } else if (botTick == 0 && hasGame()) {
            endGameTick = 0;
            hasUpdate = true;
        }

        if (requestUpdate || hasUpdate) {
            requestUpdate = false;
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
            setChanged();
        }
    }

    public void setPlayer(String name, boolean customGame) {
        isCustomGame = customGame;
        if (isSingleGame) {
            playersName[0] = name;
            playersName[1] = "";
            botTick = 8;
        } else {
            if (playersName[0].isEmpty()) {
                playersName[0] = name;
            } else if (!playersName[0].equals(name)) {
                playersName[1] = name;
            }
        }
        requestUpdate = true;
        setChanged();
    }

    public void setPlayer(Player player) {
        setPlayer(player.getName().getString(), false);
    }

    public boolean dropDice(Player player) {
        return dropDice(player.getName().getString());
    }

    public boolean dropDice(String name) {
        if (level == null) {
            return false;
        }
        if (isSingleGame) {
            if (name.equals(playersName[0]) && botTick == -1) {
                for (int i = 0; i < 2; i++) {
                    if (slotChance[i] == 0) {
                        if (!level.isClientSide()) {
                            slotChance[i] = (byte) (level.random.nextInt(6) + 1);
                            botTick = 18;
                            requestUpdate = true;
                            level.playSound(null, worldPosition, ModSounds.BOARD_CUBE.get(), SoundSource.BLOCKS, 0.6F, 1.0F);
                        }
                        return true;
                    }
                }
            }
            return false;
        }

        for (int playerIndex = 0; playerIndex < playersName.length; playerIndex++) {
            if (!name.equals(playersName[playerIndex])) {
                continue;
            }
            for (int slot = playerIndex * 2; slot < (playerIndex + 1) * 2; slot++) {
                if (slotChance[slot] == 0) {
                    if (!level.isClientSide()) {
                        slotChance[slot] = (byte) (level.random.nextInt(6) + 1);
                        botTick = playersName[1].isEmpty() ? 240 : 1200;
                        requestUpdate = true;
                        level.playSound(null, worldPosition, ModSounds.BOARD_CUBE.get(), SoundSource.BLOCKS, 0.6F, 1.0F);
                    }
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    public boolean hasFullDice() {
        for (byte chance : slotChance) {
            if (chance <= 0) {
                return false;
            }
        }
        return true;
    }

    public boolean hasGame() {
        if (isSingleGame) {
            return !playersName[0].isEmpty();
        }
        return !playersName[0].isEmpty() || !playersName[1].isEmpty();
    }

    public void finishGame() {
        finishGame(true);
    }

    public void finishGame(boolean hasChatMessage) {
        if (level == null || level.isClientSide()) {
            return;
        }
        if (!hasChatMessage) {
            resetGame();
            return;
        }
        if (!hasFullDice()) {
            sendNearMessage("ab.gameBoard.misc.notPlayer");
            resetGame();
            return;
        }

        String suffix = isSingleGame ? "" : ".mult";
        int playerScore = slotChance[0] + slotChance[1];
        int opponentScore = slotChance[2] + slotChance[3];
        if (playerScore > opponentScore) {
            sendNearMessage("ab.gameBoard.misc.0" + suffix, playersName[0]);
        } else if (playerScore == opponentScore) {
            sendNearMessage("ab.gameBoard.misc.1" + suffix);
        } else {
            sendNearMessage("ab.gameBoard.misc.2" + suffix, playersName[isSingleGame ? 0 : 1]);
        }
        resetGame();
    }

    private void sendNearMessage(String key, Object... args) {
        if (level == null) {
            return;
        }
        List<Player> players = level.getEntitiesOfClass(Player.class, new AABB(worldPosition).inflate(3.5D));
        Component message = Component.translatable(key, args).withStyle(ChatFormatting.DARK_GREEN);
        for (Player player : players) {
            player.displayClientMessage(message, false);
        }
    }

    public boolean changeCustomStack(ItemStack stack) {
        if (!isCustomGame) {
            return false;
        }
        if (customStack.isEmpty() || !ItemStack.isSameItemSameComponents(customStack, stack)) {
            customStack = stack.copy();
            requestUpdate = true;
            setChanged();
            return true;
        }
        return false;
    }

    public ItemStack getHudStack() {
        if (isCustomGame) {
            return customStack.isEmpty() ? new ItemStack(Items.PLAYER_HEAD) : customStack;
        }
        return new ItemStack(Items.PLAYER_HEAD);
    }

    public void resetGame() {
        playersName[0] = "";
        playersName[1] = "";
        for (int i = 0; i < slotChance.length; i++) {
            slotChance[i] = 0;
        }
        botTick = -1;
        endGameTick = -1;
        isCustomGame = false;
        customStack = ItemStack.EMPTY;
        requestUpdate = true;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        for (int i = 0; i < playersName.length; i++) {
            tag.putString("playerName" + i, playersName[i]);
        }
        tag.putByteArray(TAG_SLOT_CHANCE, slotChance);
        tag.putInt(TAG_BOT_TICK, botTick);
        tag.putInt(TAG_END_GAME_TICK, endGameTick);
        tag.putBoolean(TAG_REQUEST_UPDATE, requestUpdate);
        tag.putBoolean(TAG_SINGLE_GAME, isSingleGame);
        tag.putBoolean(TAG_CUSTOM_GAME, isCustomGame);
        if (!customStack.isEmpty()) {
            CompoundTag stackTag = new CompoundTag();
            customStack.save(registries, stackTag);
            tag.put(TAG_CUSTOM_STACK, stackTag);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        for (int i = 0; i < playersName.length; i++) {
            playersName[i] = tag.getString("playerName" + i);
        }
        byte[] chance = tag.getByteArray(TAG_SLOT_CHANCE);
        if (chance.length == 4) {
            slotChance = chance;
        }
        botTick = tag.getInt(TAG_BOT_TICK);
        endGameTick = tag.getInt(TAG_END_GAME_TICK);
        requestUpdate = tag.getBoolean(TAG_REQUEST_UPDATE);
        isSingleGame = !tag.contains(TAG_SINGLE_GAME) || tag.getBoolean(TAG_SINGLE_GAME);
        isCustomGame = tag.getBoolean(TAG_CUSTOM_GAME);
        customStack = tag.contains(TAG_CUSTOM_STACK) ? ItemStack.parseOptional(registries, tag.getCompound(TAG_CUSTOM_STACK)) : ItemStack.EMPTY;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket getUpdatePacket() {
        return net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        loadAdditional(tag, registries);
    }
}
