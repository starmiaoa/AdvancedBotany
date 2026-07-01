package com.pulxes.advancedbotany.common.network;

import com.pulxes.advancedbotany.client.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class HornChargeHudPacket {
    private final short chargeLoot;

    public HornChargeHudPacket(short chargeLoot) {
        this.chargeLoot = chargeLoot;
    }

    public static void encode(HornChargeHudPacket packet, FriendlyByteBuf buffer) {
        buffer.writeShort(packet.chargeLoot);
    }

    public static HornChargeHudPacket decode(FriendlyByteBuf buffer) {
        return new HornChargeHudPacket(buffer.readShort());
    }

    public static void handle(HornChargeHudPacket packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientPacketHandlers.handleHornChargeHud(packet.chargeLoot)));
        ctx.setPacketHandled(true);
    }
}
