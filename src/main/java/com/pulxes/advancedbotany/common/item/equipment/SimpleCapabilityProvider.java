package com.pulxes.advancedbotany.common.item.equipment;

/**
 * Legacy compatibility holder. NeoForge capabilities are registered from ModForgeEvents.
 */
@Deprecated(forRemoval = true)
public final class SimpleCapabilityProvider<T> {
    private final Object capability;
    private final T instance;

    public SimpleCapabilityProvider(Object capability, T instance) {
        this.capability = capability;
        this.instance = instance;
    }

    public Object capability() {
        return capability;
    }

    public T instance() {
        return instance;
    }
}
