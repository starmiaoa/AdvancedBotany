package com.pulxes.advancedbotany.registry;

import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.common.entity.EntityAdvancedSpark;
import com.pulxes.advancedbotany.common.entity.EntityNebulaBlaze;
import com.pulxes.advancedbotany.common.entity.EntitySeed;
import com.pulxes.advancedbotany.common.entity.EntitySword;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, AdvancedBotany.MOD_ID);

    public static final RegistryObject<EntityType<EntityAdvancedSpark>> ADVANCED_SPARK = ENTITIES.register("advanced_spark",
            () -> EntityType.Builder.<EntityAdvancedSpark>of(EntityAdvancedSpark::new, MobCategory.MISC)
                    .sized(0.1F, 0.5F)
                    .clientTrackingRange(64)
                    .updateInterval(10)
                    .build(new ResourceLocation(AdvancedBotany.MOD_ID, "advanced_spark").toString()));

    public static final RegistryObject<EntityType<EntityNebulaBlaze>> NEBULA_BLAZE = ENTITIES.register("nebula_blaze",
            () -> EntityType.Builder.<EntityNebulaBlaze>of(EntityNebulaBlaze::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(64)
                    .updateInterval(10)
                    .setTrackingRange(64)
                    .setUpdateInterval(10)
                    .build(new ResourceLocation(AdvancedBotany.MOD_ID, "nebula_blaze").toString()));

    public static final RegistryObject<EntityType<EntitySword>> SWORD = ENTITIES.register("sword",
            () -> EntityType.Builder.<EntitySword>of(EntitySword::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(64)
                    .updateInterval(10)
                    .setTrackingRange(64)
                    .setUpdateInterval(10)
                    .build(new ResourceLocation(AdvancedBotany.MOD_ID, "sword").toString()));

    public static final RegistryObject<EntityType<EntitySeed>> SEED = ENTITIES.register("seed",
            () -> EntityType.Builder.<EntitySeed>of(EntitySeed::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(64)
                    .updateInterval(10)
                    .setTrackingRange(64)
                    .setUpdateInterval(10)
                    .build(new ResourceLocation(AdvancedBotany.MOD_ID, "seed").toString()));

    private ModEntities() {
    }

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}
