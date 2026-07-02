package com.pulxes.advancedbotany.common.item.relic;

import com.pulxes.advancedbotany.common.item.equipment.SimpleCapabilityProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.item.Relic;
import vazkii.botania.common.item.relic.RelicImpl;
import vazkii.botania.common.item.relic.RelicItem;
import vazkii.botania.xplat.XplatAbstractions;

public class ModRelicItem extends RelicItem {
    @Nullable
    private final ResourceLocation advancement;

    public ModRelicItem(Properties properties) {
        this(properties, null, true);
    }

    public ModRelicItem(Properties properties, boolean fateBoardRelic) {
        this(properties, null, fateBoardRelic);
    }

    public ModRelicItem(Properties properties, @Nullable ResourceLocation advancement, boolean fateBoardRelic) {
        super(properties.stacksTo(1).fireResistant());
        this.advancement = advancement;
    }

    @Override
    public SimpleCapabilityProvider<Relic> initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new SimpleCapabilityProvider<>(BotaniaForgeCapabilities.RELIC, new RelicImpl(stack, advancement));
    }

    protected boolean canUseRelic(ItemStack stack, Player player) {
        Relic relic = XplatAbstractions.INSTANCE.findRelic(stack);
        if (relic == null) {
            return true;
        }
        if (relic.getSoulbindUUID() == null) {
            if (!player.level().isClientSide()) {
                relic.tickBinding(player);
            }
            return true;
        }
        return relic.isRightPlayer(player);
    }
}
