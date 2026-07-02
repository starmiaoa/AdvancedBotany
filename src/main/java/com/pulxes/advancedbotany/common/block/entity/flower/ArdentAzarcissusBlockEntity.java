package com.pulxes.advancedbotany.common.block.entity.flower;

import com.pulxes.advancedbotany.common.block.entity.GameBoardBlockEntity;
import com.pulxes.advancedbotany.registry.ModFlowers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import vazkii.botania.api.block_entity.GeneratingFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;

public class ArdentAzarcissusBlockEntity extends GeneratingFlowerBlockEntity {
    public static final String PLAYER_NAME = "ArdentAzarcissus#21sda2gaj91*21df#111sfq3jrns@#";
    public static final int WORK_MANA = 320;
    public static final int COOLDOWN_TIME = 120;
    public static final int MAX_MANA = 16000;
    public static final int COLOR = 14628246;
    private static final String TAG_COOLDOWN = "cooldown";
    private static final int RANGE = 1;

    private int cooldown;

    public ArdentAzarcissusBlockEntity(BlockPos pos, BlockState state) {
        this(ModFlowers.ARDENT_AZARCISSUS_BLOCK_ENTITY.get(), pos, state);
    }

    public ArdentAzarcissusBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tickFlower() {
        super.tickFlower();
        Level level = getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }
        if (cooldown > 0) {
            cooldown--;
            return;
        }
        if (getMana() >= getMaxMana()) {
            return;
        }

        boolean needsSync = false;
        BlockPos pos = getEffectivePos();
        for (int x = -RANGE; x <= RANGE; x++) {
            for (int z = -RANGE; z <= RANGE; z++) {
                BlockEntity blockEntity = level.getBlockEntity(pos.offset(x, 0, z));
                if (tryUseGameBoard(blockEntity)) {
                    needsSync = true;
                }
            }
        }
        if (needsSync) {
            syncFlower();
        }
    }

    private boolean tryUseGameBoard(BlockEntity blockEntity) {
        if (!(blockEntity instanceof GameBoardBlockEntity board) || !board.isSingleGame) {
            return false;
        }

        boolean gainedMana = false;
        if (!board.hasGame()) {
            board.setPlayer(PLAYER_NAME, true);
            cooldown = COOLDOWN_TIME;
        } else {
            if (!PLAYER_NAME.equals(board.playersName[0])) {
                board.playersName[0] = PLAYER_NAME;
            }
            if (!board.isCustomGame) {
                board.isCustomGame = true;
            }

            if (board.endGameTick == 0) {
                int winCount = board.slotChance[0] + board.slotChance[1] - (board.slotChance[2] + board.slotChance[3]);
                if (winCount > 0) {
                    int manaGain = Math.min(WORK_MANA * winCount, getMaxMana() - getMana());
                    if (manaGain > 0) {
                        addMana(manaGain);
                        gainedMana = true;
                    }
                }
                board.finishGame(false);
            } else {
                board.dropDice(PLAYER_NAME);
            }
            cooldown = COOLDOWN_TIME;
        }
        board.changeCustomStack(getFlowerStack());
        return gainedMana;
    }

    private ItemStack getFlowerStack() {
        return new ItemStack(ModFlowers.ARDENT_AZARCISSUS_ITEM.get());
    }

    @Override
    public RadiusDescriptor getRadius() {
        return RadiusDescriptor.Rectangle.square(getEffectivePos(), RANGE);
    }

    @Override
    public int getMaxMana() {
        return MAX_MANA;
    }

    @Override
    public int getColor() {
        return COLOR;
    }

    public void writeToPacketNBT(CompoundTag tag) {
        tag.putInt(TAG_COOLDOWN, cooldown);
    }

    public void readFromPacketNBT(CompoundTag tag) {
        cooldown = tag.getInt(TAG_COOLDOWN);
    }

    private void syncFlower() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), net.minecraft.world.level.block.Block.UPDATE_ALL);
        }
    }

}
