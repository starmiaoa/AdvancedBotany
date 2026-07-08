package com.pulxes.advancedbotany.common.block.entity;

public final class AdvancedBotanyManaValues {
    public static final int MANA_CONTAINER_CAPACITY = 64_000_000;
    public static final int DILUTED_MANA_CONTAINER_CAPACITY = 8_000_000;
    public static final int FABULOUS_MANA_CONTAINER_CAPACITY = 64_000_000;

    public static final int NATURAL_SPREADER_MAX_MANA = 128_000;
    public static final int NATURAL_SPREADER_BURST_MANA = 32_000;
    public static final int NATURAL_SPREADER_PRE_LOSS_TICKS = 35;
    public static final float NATURAL_SPREADER_LOSS_PER_TICK = NATURAL_SPREADER_BURST_MANA / 4.5F;
    public static final float NATURAL_SPREADER_MOTION_MODIFIER = 2.5F;
    public static final float NATURAL_SPREADER_GRAVITY = 0.0F;
    public static final int NATURAL_SPREADER_COLOR = 0xCDD419; // original decimal 13489177

    public static final int MANA_CHARGER_SPEED = 11_240;

    private AdvancedBotanyManaValues() {
    }
}
