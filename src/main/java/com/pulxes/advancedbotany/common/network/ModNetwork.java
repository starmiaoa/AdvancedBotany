package com.pulxes.advancedbotany.common.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetwork {
    private static final String PROTOCOL = "1";

    private ModNetwork() {
    }

    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL);
        registrar.playToClient(SpaceBladeDashPacket.TYPE, SpaceBladeDashPacket.STREAM_CODEC,
                (packet, context) -> packet.handle());
        registrar.playToClient(HornChargeHudPacket.TYPE, HornChargeHudPacket.STREAM_CODEC,
                (packet, context) -> packet.handle());
        registrar.playToClient(FindNearBlocksPacket.TYPE, FindNearBlocksPacket.STREAM_CODEC,
                (packet, context) -> packet.handle());
    }

    public static void sendSpaceBladeDash(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, SpaceBladeDashPacket.INSTANCE);
    }

    public static void sendHornChargeHud(ServerPlayer player, short chargeLoot) {
        PacketDistributor.sendToPlayer(player, new HornChargeHudPacket(chargeLoot));
    }

    public static void sendFindNearBlocks(ServerPlayer player, net.minecraft.resources.ResourceLocation blockId, int stateId) {
        if (blockId != null) {
            PacketDistributor.sendToPlayer(player, new FindNearBlocksPacket(blockId, stateId));
        }
    }
}
