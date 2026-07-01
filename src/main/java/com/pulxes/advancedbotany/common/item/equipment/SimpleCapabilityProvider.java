package com.pulxes.advancedbotany.common.item.equipment;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SimpleCapabilityProvider<T> implements ICapabilityProvider {
    private final Capability<T> capability;
    private final LazyOptional<T> instance;

    public SimpleCapabilityProvider(Capability<T> capability, T instance) {
        this.capability = capability;
        this.instance = LazyOptional.of(() -> instance);
    }

    @Override
    public @NotNull <U> LazyOptional<U> getCapability(@NotNull Capability<U> requested, @Nullable net.minecraft.core.Direction side) {
        return requested == capability ? instance.cast() : LazyOptional.empty();
    }
}
