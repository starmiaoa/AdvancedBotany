package com.pulxes.advancedbotany.common.block.entity.flower;

import com.pulxes.advancedbotany.registry.ModFlowers;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.minecraft.core.BlockPos;
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
                if (blockEntity != null && tryUseGameBoard(blockEntity)) {
                    needsSync = true;
                }
            }
        }
        if (needsSync) {
            sync();
        }
    }

    private boolean tryUseGameBoard(BlockEntity blockEntity) {
        if (findField(blockEntity.getClass(), "isSingleGame") == null) {
            return false;
        }

        try {
            if (!getBooleanField(blockEntity, "isSingleGame")) {
                return false;
            }

            boolean gainedMana = false;
            if (!invokeBoolean(blockEntity, "hasGame")) {
                invoke(blockEntity, "setPlayer", new Class<?>[] {String.class, boolean.class}, PLAYER_NAME, true);
                cooldown = COOLDOWN_TIME;
            } else {
                String[] playersName = (String[]) getField(blockEntity, "playersName");
                if (playersName != null && playersName.length > 0 && !PLAYER_NAME.equals(playersName[0])) {
                    playersName[0] = PLAYER_NAME;
                }

                if (!getBooleanField(blockEntity, "isCustomGame")) {
                    setBooleanField(blockEntity, "isCustomGame", true);
                }

                if (getIntField(blockEntity, "endGameTick") == 0) {
                    int[] slotChance = (int[]) getField(blockEntity, "slotChance");
                    if (slotChance != null && slotChance.length >= 4) {
                        int winCount = slotChance[0] + slotChance[1] - (slotChance[2] + slotChance[3]);
                        if (winCount > 0) {
                            int manaGain = Math.min(WORK_MANA * winCount, getMaxMana() - getMana());
                            if (manaGain > 0) {
                                addMana(manaGain);
                                gainedMana = true;
                            }
                        }
                    }
                    invoke(blockEntity, "finishGame", new Class<?>[] {boolean.class}, false);
                } else {
                    invoke(blockEntity, "dropDice", new Class<?>[] {String.class}, PLAYER_NAME);
                }
                cooldown = COOLDOWN_TIME;
            }
            tryInvoke(blockEntity, "changeCustomStack", new Class<?>[] {ItemStack.class}, getFlowerStack());
            return gainedMana;
        } catch (ClassCastException | ReflectiveOperationException ignored) {
            return false;
        }
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

    @Override
    public void writeToPacketNBT(CompoundTag tag) {
        super.writeToPacketNBT(tag);
        tag.putInt(TAG_COOLDOWN, cooldown);
    }

    @Override
    public void readFromPacketNBT(CompoundTag tag) {
        super.readFromPacketNBT(tag);
        cooldown = tag.getInt(TAG_COOLDOWN);
    }

    private static Object getField(Object target, String name) throws ReflectiveOperationException {
        Field field = findField(target.getClass(), name);
        if (field == null) {
            throw new NoSuchFieldException(name);
        }
        return field.get(target);
    }

    private static boolean getBooleanField(Object target, String name) throws ReflectiveOperationException {
        return (boolean) getField(target, name);
    }

    private static void setBooleanField(Object target, String name, boolean value) throws ReflectiveOperationException {
        Field field = findField(target.getClass(), name);
        if (field == null) {
            throw new NoSuchFieldException(name);
        }
        field.setBoolean(target, value);
    }

    private static int getIntField(Object target, String name) throws ReflectiveOperationException {
        return (int) getField(target, name);
    }

    private static boolean invokeBoolean(Object target, String name) throws ReflectiveOperationException {
        return (boolean) invoke(target, name, new Class<?>[0]);
    }

    private static Object invoke(Object target, String name, Class<?>[] parameterTypes, Object... args) throws ReflectiveOperationException {
        Method method = findMethod(target.getClass(), name, parameterTypes);
        if (method == null) {
            throw new NoSuchMethodException(name);
        }
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof ReflectiveOperationException reflectiveOperationException) {
                throw reflectiveOperationException;
            }
            throw exception;
        }
    }

    private static void tryInvoke(Object target, String name, Class<?>[] parameterTypes, Object... args) {
        try {
            invoke(target, name, parameterTypes, args);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private static Field findField(Class<?> type, String name) {
        Class<?> current = type;
        while (current != null) {
            try {
                Field field = current.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    private static Method findMethod(Class<?> type, String name, Class<?>... parameterTypes) {
        Class<?> current = type;
        while (current != null) {
            try {
                Method method = current.getDeclaredMethod(name, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
                current = current.getSuperclass();
            }
        }
        return null;
    }
}
