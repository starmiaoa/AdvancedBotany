package com.pulxes.advancedbotany.registry;

import com.pulxes.advancedbotany.AdvancedBotany;
import com.pulxes.advancedbotany.common.block.entity.flower.AncientAlphirineBlockEntity;
import com.pulxes.advancedbotany.common.block.entity.flower.ArdentAzarcissusBlockEntity;
import com.pulxes.advancedbotany.common.block.entity.flower.AspecolusBlockEntity;
import com.pulxes.advancedbotany.common.block.entity.flower.DictariusBlockEntity;
import com.pulxes.advancedbotany.common.block.entity.flower.PureGladiolusBlockEntity;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import vazkii.botania.api.block_entity.SpecialFlowerBlockEntity;
import vazkii.botania.common.block.FloatingSpecialFlowerBlock;
import vazkii.botania.common.item.block.SpecialFlowerBlockItem;
import vazkii.botania.forge.block.ForgeSpecialFlowerBlock;

public final class ModFlowers {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, AdvancedBotany.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AdvancedBotany.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AdvancedBotany.MOD_ID);

    private static final BlockBehaviour.Properties FLOWER_PROPERTIES = BlockBehaviour.Properties.copy(Blocks.POPPY);
    private static final BlockBehaviour.Properties FLOATING_PROPERTIES = BlockBehaviour.Properties.of()
            .mapColor(MapColor.PLANT)
            .strength(0.5F)
            .sound(SoundType.GRAVEL)
            .lightLevel(state -> 15);

    public static final RegistryObject<Block> ANCIENT_ALPHIRINE = specialFlower(
            "ancient_alphirine", MobEffects.REGENERATION, 0, ModFlowers::ancientAlphirineBlockEntity);
    public static final RegistryObject<Block> FLOATING_ANCIENT_ALPHIRINE = floatingFlower(
            "floating_ancient_alphirine", ModFlowers::ancientAlphirineBlockEntity);
    public static final RegistryObject<Block> ARDENT_AZARCISSUS = specialFlower(
            "ardent_azarcissus", MobEffects.LUCK, 0, ModFlowers::ardentAzarcissusBlockEntity);
    public static final RegistryObject<Block> FLOATING_ARDENT_AZARCISSUS = floatingFlower(
            "floating_ardent_azarcissus", ModFlowers::ardentAzarcissusBlockEntity);
    public static final RegistryObject<Block> DICTARIUS = specialFlower(
            "dictarius", MobEffects.HEALTH_BOOST, 0, ModFlowers::dictariusBlockEntity);
    public static final RegistryObject<Block> FLOATING_DICTARIUS = floatingFlower(
            "floating_dictarius", ModFlowers::dictariusBlockEntity);
    public static final RegistryObject<Block> ASPECOLUS = specialFlower(
            "aspecolus", MobEffects.NIGHT_VISION, 0, ModFlowers::aspecolusBlockEntity);
    public static final RegistryObject<Block> FLOATING_ASPECOLUS = floatingFlower(
            "floating_aspecolus", ModFlowers::aspecolusBlockEntity);
    public static final RegistryObject<Block> PURE_GLADIOLUS = specialFlower(
            "pure_gladiolus", MobEffects.GLOWING, 0, ModFlowers::pureGladiolusBlockEntity);
    public static final RegistryObject<Block> FLOATING_PURE_GLADIOLUS = floatingFlower(
            "floating_pure_gladiolus", ModFlowers::pureGladiolusBlockEntity);

    public static final RegistryObject<Item> ANCIENT_ALPHIRINE_ITEM = flowerItem("ancient_alphirine", ANCIENT_ALPHIRINE);
    public static final RegistryObject<Item> FLOATING_ANCIENT_ALPHIRINE_ITEM = flowerItem("floating_ancient_alphirine", FLOATING_ANCIENT_ALPHIRINE);
    public static final RegistryObject<Item> ARDENT_AZARCISSUS_ITEM = flowerItem("ardent_azarcissus", ARDENT_AZARCISSUS);
    public static final RegistryObject<Item> FLOATING_ARDENT_AZARCISSUS_ITEM = flowerItem("floating_ardent_azarcissus", FLOATING_ARDENT_AZARCISSUS);
    public static final RegistryObject<Item> DICTARIUS_ITEM = flowerItem("dictarius", DICTARIUS);
    public static final RegistryObject<Item> FLOATING_DICTARIUS_ITEM = flowerItem("floating_dictarius", FLOATING_DICTARIUS);
    public static final RegistryObject<Item> ASPECOLUS_ITEM = flowerItem("aspecolus", ASPECOLUS);
    public static final RegistryObject<Item> FLOATING_ASPECOLUS_ITEM = flowerItem("floating_aspecolus", FLOATING_ASPECOLUS);
    public static final RegistryObject<Item> PURE_GLADIOLUS_ITEM = flowerItem("pure_gladiolus", PURE_GLADIOLUS);
    public static final RegistryObject<Item> FLOATING_PURE_GLADIOLUS_ITEM = flowerItem("floating_pure_gladiolus", FLOATING_PURE_GLADIOLUS);

    public static final RegistryObject<BlockEntityType<AncientAlphirineBlockEntity>> ANCIENT_ALPHIRINE_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("ancient_alphirine", () -> BlockEntityType.Builder.of(
                    AncientAlphirineBlockEntity::new,
                    ANCIENT_ALPHIRINE.get(),
                    FLOATING_ANCIENT_ALPHIRINE.get()).build(null));
    public static final RegistryObject<BlockEntityType<ArdentAzarcissusBlockEntity>> ARDENT_AZARCISSUS_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("ardent_azarcissus", () -> BlockEntityType.Builder.of(
                    ArdentAzarcissusBlockEntity::new,
                    ARDENT_AZARCISSUS.get(),
                    FLOATING_ARDENT_AZARCISSUS.get()).build(null));
    public static final RegistryObject<BlockEntityType<DictariusBlockEntity>> DICTARIUS_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("dictarius", () -> BlockEntityType.Builder.of(
                    DictariusBlockEntity::new,
                    DICTARIUS.get(),
                    FLOATING_DICTARIUS.get()).build(null));
    public static final RegistryObject<BlockEntityType<AspecolusBlockEntity>> ASPECOLUS_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("aspecolus", () -> BlockEntityType.Builder.of(
                    AspecolusBlockEntity::new,
                    ASPECOLUS.get(),
                    FLOATING_ASPECOLUS.get()).build(null));
    public static final RegistryObject<BlockEntityType<PureGladiolusBlockEntity>> PURE_GLADIOLUS_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("pure_gladiolus", () -> BlockEntityType.Builder.of(
                    PureGladiolusBlockEntity::new,
                    PURE_GLADIOLUS.get(),
                    FLOATING_PURE_GLADIOLUS.get()).build(null));

    private ModFlowers() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
    }

    public static void addToCreativeTab(CreativeModeTab.Output output) {
        flowerItems().forEach(item -> output.accept(item.get()));
    }

    public static List<RegistryObject<Block>> flowerBlocks() {
        return List.of(
                ANCIENT_ALPHIRINE,
                FLOATING_ANCIENT_ALPHIRINE,
                ARDENT_AZARCISSUS,
                FLOATING_ARDENT_AZARCISSUS,
                DICTARIUS,
                FLOATING_DICTARIUS,
                ASPECOLUS,
                FLOATING_ASPECOLUS,
                PURE_GLADIOLUS,
                FLOATING_PURE_GLADIOLUS);
    }

    private static List<RegistryObject<Item>> flowerItems() {
        return List.of(
                ANCIENT_ALPHIRINE_ITEM,
                FLOATING_ANCIENT_ALPHIRINE_ITEM,
                ARDENT_AZARCISSUS_ITEM,
                FLOATING_ARDENT_AZARCISSUS_ITEM,
                DICTARIUS_ITEM,
                FLOATING_DICTARIUS_ITEM,
                ASPECOLUS_ITEM,
                FLOATING_ASPECOLUS_ITEM,
                PURE_GLADIOLUS_ITEM,
                FLOATING_PURE_GLADIOLUS_ITEM);
    }

    private static RegistryObject<Block> specialFlower(
            String name,
            MobEffect effect,
            int effectDuration,
            Supplier<BlockEntityType<? extends SpecialFlowerBlockEntity>> blockEntityType) {
        return BLOCKS.register(name, () -> new ForgeSpecialFlowerBlock(effect, effectDuration, FLOWER_PROPERTIES, blockEntityType));
    }

    private static RegistryObject<Block> floatingFlower(
            String name,
            Supplier<BlockEntityType<? extends SpecialFlowerBlockEntity>> blockEntityType) {
        return BLOCKS.register(name, () -> new FloatingSpecialFlowerBlock(FLOATING_PROPERTIES, blockEntityType));
    }

    private static RegistryObject<Item> flowerItem(String name, RegistryObject<Block> block) {
        return ITEMS.register(name, () -> new SpecialFlowerBlockItem(block.get(), new Item.Properties()));
    }

    private static BlockEntityType<? extends SpecialFlowerBlockEntity> ancientAlphirineBlockEntity() {
        return ANCIENT_ALPHIRINE_BLOCK_ENTITY.get();
    }

    private static BlockEntityType<? extends SpecialFlowerBlockEntity> ardentAzarcissusBlockEntity() {
        return ARDENT_AZARCISSUS_BLOCK_ENTITY.get();
    }

    private static BlockEntityType<? extends SpecialFlowerBlockEntity> dictariusBlockEntity() {
        return DICTARIUS_BLOCK_ENTITY.get();
    }

    private static BlockEntityType<? extends SpecialFlowerBlockEntity> aspecolusBlockEntity() {
        return ASPECOLUS_BLOCK_ENTITY.get();
    }

    private static BlockEntityType<? extends SpecialFlowerBlockEntity> pureGladiolusBlockEntity() {
        return PURE_GLADIOLUS_BLOCK_ENTITY.get();
    }
}
