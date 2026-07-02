package com.pulxes.advancedbotany.common.network;

import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.client.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
public record HornChargeHudPacket(short chargeLoot) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(AdvancedBotany.MOD_ID, "horn_charge_hud");
    public static final Type<HornChargeHudPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, HornChargeHudPacket> STREAM_CODEC =
            StreamCodec.ofMember(HornChargeHudPacket::encode, HornChargeHudPacket::decode);

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeShort(chargeLoot);
    }

    public static HornChargeHudPacket decode(FriendlyByteBuf buffer) {
        return new HornChargeHudPacket(buffer.readShort());
    }

    public void handle() {
        ClientPacketHandlers.handleHornChargeHud(chargeLoot);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
