package com.pulxes.advancedbotany.common.network;

import com.pulxes.advancedbotany.AdvancedBotany;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public final class ModNetwork {
    private static final String PROTOCOL = "1";
    private static int nextId;

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(AdvancedBotany.MOD_ID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals);

    private ModNetwork() {
    }

    public static void register() {
        CHANNEL.registerMessage(
                nextId++,
                SpaceBladeDashPacket.class,
                SpaceBladeDashPacket::encode,
                SpaceBladeDashPacket::decode,
                SpaceBladeDashPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(
                nextId++,
                HornChargeHudPacket.class,
                HornChargeHudPacket::encode,
                HornChargeHudPacket::decode,
                HornChargeHudPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        CHANNEL.registerMessage(
                nextId++,
                FindNearBlocksPacket.class,
                FindNearBlocksPacket::encode,
                FindNearBlocksPacket::decode,
                FindNearBlocksPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    public static void sendSpaceBladeDash(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SpaceBladeDashPacket());
    }

    public static void sendHornChargeHud(ServerPlayer player, short chargeLoot) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new HornChargeHudPacket(chargeLoot));
    }

    public static void sendFindNearBlocks(ServerPlayer player, ResourceLocation blockId, int stateId) {
        if (blockId != null) {
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new FindNearBlocksPacket(blockId, stateId));
        }
    }
}
