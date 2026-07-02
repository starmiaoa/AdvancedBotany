package com.pulxes.advancedbotany.registry;

import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.common.entity.EntityAdvancedSpark;
import com.pulxes.advancedbotany.common.entity.EntityAlphirinePortal;
import com.pulxes.advancedbotany.common.entity.EntityAnonymousSteve;
import com.pulxes.advancedbotany.common.entity.EntityManaVine;
import com.pulxes.advancedbotany.common.entity.EntityNebulaBlaze;
import com.pulxes.advancedbotany.common.entity.EntitySeed;
import com.pulxes.advancedbotany.common.entity.EntitySword;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, AdvancedBotany.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<EntityAdvancedSpark>> ADVANCED_SPARK = ENTITIES.register("advanced_spark",
            () -> EntityType.Builder.<EntityAdvancedSpark>of(EntityAdvancedSpark::new, MobCategory.MISC)
                    .sized(0.1F, 0.5F)
                    .clientTrackingRange(64)
                    .updateInterval(10)
                    .build(ResourceLocation.fromNamespaceAndPath(AdvancedBotany.MOD_ID, "advanced_spark").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityNebulaBlaze>> NEBULA_BLAZE = ENTITIES.register("nebula_blaze",
            () -> EntityType.Builder.<EntityNebulaBlaze>of(EntityNebulaBlaze::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(64)
                    .updateInterval(10)
                    .build(ResourceLocation.fromNamespaceAndPath(AdvancedBotany.MOD_ID, "nebula_blaze").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<EntitySword>> SWORD = ENTITIES.register("sword",
            () -> EntityType.Builder.<EntitySword>of(EntitySword::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(64)
                    .updateInterval(10)
                    .build(ResourceLocation.fromNamespaceAndPath(AdvancedBotany.MOD_ID, "sword").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityManaVine>> MANA_VINE = ENTITIES.register("mana_vine",
            () -> EntityType.Builder.<EntityManaVine>of(EntityManaVine::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(64)
                    .updateInterval(10)
                    .build(ResourceLocation.fromNamespaceAndPath(AdvancedBotany.MOD_ID, "mana_vine").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityAnonymousSteve>> ANONYMOUS_STEVE = ENTITIES.register("anonymous_steve",
            () -> EntityType.Builder.<EntityAnonymousSteve>of(EntityAnonymousSteve::new, MobCategory.MISC)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(32)
                    .updateInterval(20)
                    .noSave()
                    .build(ResourceLocation.fromNamespaceAndPath(AdvancedBotany.MOD_ID, "anonymous_steve").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityAlphirinePortal>> ALPHIRINE_PORTAL = ENTITIES.register("alphirine_portal",
            () -> EntityType.Builder.<EntityAlphirinePortal>of(EntityAlphirinePortal::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(64)
                    .updateInterval(10)
                    .build(ResourceLocation.fromNamespaceAndPath(AdvancedBotany.MOD_ID, "alphirine_portal").toString()));
    public static final DeferredHolder<EntityType<?>, EntityType<EntitySeed>> SEED = ENTITIES.register("seed",
            () -> EntityType.Builder.<EntitySeed>of(EntitySeed::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(64)
                    .updateInterval(10)
                    .build(ResourceLocation.fromNamespaceAndPath(AdvancedBotany.MOD_ID, "seed").toString()));

    private ModEntities() {
    }

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }

    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ANONYMOUS_STEVE.get(), EntityAnonymousSteve.createAttributes().build());
    }
}
