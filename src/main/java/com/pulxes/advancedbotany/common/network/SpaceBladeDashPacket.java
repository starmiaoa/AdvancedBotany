package com.pulxes.advancedbotany.common.network;

import com.pulxes.advancedbotany.client.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SpaceBladeDashPacket {
    public static void encode(SpaceBladeDashPacket packet, FriendlyByteBuf buffer) {
    }

    public static SpaceBladeDashPacket decode(FriendlyByteBuf buffer) {
        return new SpaceBladeDashPacket();
    }

    public static void handle(SpaceBladeDashPacket packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientPacketHandlers::handleSpaceBladeDash));
        ctx.setPacketHandled(true);
    }
}
