package com.pulxes.advancedbotany.common.network;

import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.client.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
public final class SpaceBladeDashPacket implements CustomPacketPayload {
    public static final SpaceBladeDashPacket INSTANCE = new SpaceBladeDashPacket();
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(AdvancedBotany.MOD_ID, "space_blade_dash");
    public static final Type<SpaceBladeDashPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, SpaceBladeDashPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private SpaceBladeDashPacket() {
    }

    public void handle() {
        ClientPacketHandlers.handleSpaceBladeDash();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
