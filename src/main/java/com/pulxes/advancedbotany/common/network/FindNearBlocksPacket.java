package com.pulxes.advancedbotany.common.network;

import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.client.ClientPacketHandlers;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
public record FindNearBlocksPacket(ResourceLocation blockId, int stateId) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(AdvancedBotany.MOD_ID, "find_near_blocks");
    public static final Type<FindNearBlocksPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, FindNearBlocksPacket> STREAM_CODEC =
            StreamCodec.ofMember(FindNearBlocksPacket::encode, FindNearBlocksPacket::decode);

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(blockId);
        buffer.writeInt(stateId);
    }

    public static FindNearBlocksPacket decode(FriendlyByteBuf buffer) {
        return new FindNearBlocksPacket(buffer.readResourceLocation(), buffer.readInt());
    }

    public void handle() {
        ClientPacketHandlers.handleFindNearBlocks(blockId, stateId);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
