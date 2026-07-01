package com.pulxes.advancedbotany.common.network;

import com.pulxes.advancedbotany.client.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FindNearBlocksPacket {
    private final ResourceLocation blockId;
    private final int stateId;

    public FindNearBlocksPacket(ResourceLocation blockId, int stateId) {
        this.blockId = blockId;
        this.stateId = stateId;
    }

    public static void encode(FindNearBlocksPacket packet, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(packet.blockId);
        buffer.writeInt(packet.stateId);
    }

    public static FindNearBlocksPacket decode(FriendlyByteBuf buffer) {
        return new FindNearBlocksPacket(buffer.readResourceLocation(), buffer.readInt());
    }

    public static void handle(FindNearBlocksPacket packet, Supplier<NetworkEvent.Context> context) {
        NetworkEvent.Context ctx = context.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientPacketHandlers.handleFindNearBlocks(packet.blockId, packet.stateId)));
        ctx.setPacketHandled(true);
    }
}
