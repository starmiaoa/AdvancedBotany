package com.pulxes.advancedbotany.client;

import com.pulxes.advancedbotany.common.item.equipment.SpaceBladeItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class ClientPacketHandlers {
    private ClientPacketHandlers() {
    }

    public static void handleSpaceBladeDash() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            SpaceBladeItem.dash(player);
        }
    }
}
