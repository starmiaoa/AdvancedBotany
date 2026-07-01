package com.pulxes.advancedbotany.registry;

import com.pulxes.advancedbotany.AdvancedBotany;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;
import vazkii.botania.api.BotaniaForgeCapabilities;
import vazkii.botania.api.block.Wandable;
import vazkii.botania.api.mana.ManaReceiver;
import vazkii.botania.api.mana.spark.SparkAttachable;
import vazkii.botania.forge.CapabilityUtil;

public final class ModForgeEvents {
    private static final ResourceLocation MANA_RECEIVER = new ResourceLocation(AdvancedBotany.MOD_ID, "mana_receiver");
    private static final ResourceLocation SPARK_ATTACHABLE = new ResourceLocation(AdvancedBotany.MOD_ID, "spark_attachable");
    private static final ResourceLocation WANDABLE = new ResourceLocation(AdvancedBotany.MOD_ID, "wandable");

    private ModForgeEvents() {
    }

    @SubscribeEvent
    public static void attachBlockEntityCapabilities(AttachCapabilitiesEvent<BlockEntity> event) {
        BlockEntity blockEntity = event.getObject();
        ResourceLocation blockEntityId = ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(blockEntity.getType());
        if (blockEntityId == null || !AdvancedBotany.MOD_ID.equals(blockEntityId.getNamespace())) {
            return;
        }

        if (blockEntity instanceof ManaReceiver manaReceiver) {
            event.addCapability(MANA_RECEIVER, CapabilityUtil.makeProvider(BotaniaForgeCapabilities.MANA_RECEIVER, manaReceiver));
        }
        if (blockEntity instanceof SparkAttachable sparkAttachable) {
            event.addCapability(SPARK_ATTACHABLE, CapabilityUtil.makeProvider(BotaniaForgeCapabilities.SPARK_ATTACHABLE, sparkAttachable));
        }
        if (blockEntity instanceof Wandable wandable) {
            event.addCapability(WANDABLE, CapabilityUtil.makeProvider(BotaniaForgeCapabilities.WANDABLE, wandable));
        }
    }
}
