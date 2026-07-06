package com.pulxes.advancedbotany.client;

import com.pulxes.advancedbotany.common.item.relic.SphereNavigationItem;
import com.pulxes.advancedbotany.common.item.equipment.SpaceBladeItem;
import com.pulxes.advancedbotany.registry.ModItems;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.botania.client.fx.WispParticleData;

public final class ClientPacketHandlers {
    private ClientPacketHandlers() {
    }

    public static void handleSpaceBladeDash() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            SpaceBladeItem.dash(player);
        }
    }

    public static void handleHornChargeHud(short chargeLoot) {
        ItemsRemainingHud.set(new ItemStack(ModItems.HORN_OF_PLENTY.get()), String.valueOf(chargeLoot));
    }

    public static void handleFindNearBlocks(ResourceLocation blockId, int stateId) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        Level level = minecraft.level;
        Block block = ForgeRegistries.BLOCKS.getValue(blockId);
        if (player == null || level == null || block == null) {
            return;
        }

        ItemStack renderStack = new ItemStack(block);
        int foundBlocks = 0;
        int maxFoundBlocks = 32;
        BlockPos playerPos = player.blockPosition();
        scan:
        for (int y = -32; y < 16; y++) {
            for (int x = -SphereNavigationItem.RANGE_SEARCH; x < SphereNavigationItem.RANGE_SEARCH; x++) {
                if (level.random.nextInt(maxFoundBlocks) >= maxFoundBlocks - foundBlocks || level.random.nextBoolean()) {
                    continue;
                }
                for (int z = -SphereNavigationItem.RANGE_SEARCH; z < SphereNavigationItem.RANGE_SEARCH; z++) {
                    if (level.random.nextInt(maxFoundBlocks) >= maxFoundBlocks - foundBlocks || level.random.nextBoolean()) {
                        continue;
                    }
                    if (foundBlocks >= maxFoundBlocks) {
                        break scan;
                    }
                    BlockPos pos = playerPos.offset(x, y, z);
                    if (pos.getY() < level.getMinBuildHeight()) {
                        continue;
                    }
                    BlockState state = level.getBlockState(pos);
                    if (state.getBlock() != block || (stateId >= 0 && Block.getId(state) != stateId)) {
                        continue;
                    }
                    foundBlocks++;
                    spawnNavigationParticles(level, pos, x, y, z);
                }
            }
        }
        if (foundBlocks > 0) {
            ItemsRemainingHud.set(renderStack, Component.translatable("ab.sphereNavigation.founded").getString() + " " + foundBlocks);
        }
    }

    public static void handleNavigationTargetSet(BlockState state) {
        ItemStack renderStack = new ItemStack(state.getBlock());
        Component targetName = renderStack.isEmpty() ? state.getBlock().getName() : renderStack.getHoverName();
        ItemsRemainingHud.set(renderStack, targetName.getString());
    }

    private static void spawnNavigationParticles(Level level, BlockPos pos, int relX, int relY, int relZ) {
        float distance = Math.abs(relX) + Math.min(16, Math.abs(relY)) + Math.abs(relZ);
        float hue = 120.0F - distance / 64.0F * 120.0F;
        if (hue <= 70.0F) {
            hue *= 0.1F;
        }
        Color color = new Color(Color.HSBtoRGB(hue / 360.0F, 0.9F + (float) (Math.random() * 0.1D), 1.0F));
        for (int i = 0; i < 11; i++) {
            // Original: wispFX with depth test + distance limit disabled so highlights show
            // through walls (the whole point of a block finder); /100 colours are original.
            level.addParticle(WispParticleData.wisp(
                            0.3F + (float) (Math.random() * 0.25D),
                            color.getRed() / 100.0F,
                            color.getGreen() / 100.0F,
                            color.getBlue() / 100.0F,
                            2.7F + 0.5F * (float) Math.random(),
                            false),
                    pos.getX() + 0.5D + (Math.random() - 0.5D),
                    pos.getY() + 0.5D + (Math.random() - 0.5D),
                    pos.getZ() + 0.5D + (Math.random() - 0.5D),
                    0.0D, 0.0D, 0.0D);
        }
    }
}
