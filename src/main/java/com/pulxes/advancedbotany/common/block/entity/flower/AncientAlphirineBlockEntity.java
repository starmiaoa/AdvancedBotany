package com.pulxes.advancedbotany.common.block.entity.flower;

import com.pulxes.advancedbotany.common.entity.EntityAlphirinePortal;
import com.pulxes.advancedbotany.common.recipe.AncientAlphirineRecipe;
import com.pulxes.advancedbotany.common.recipe.ContainerRecipeInput;
import com.pulxes.advancedbotany.registry.ModFlowers;
import com.pulxes.advancedbotany.registry.ModRecipes;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import vazkii.botania.api.block_entity.FunctionalFlowerBlockEntity;
import vazkii.botania.api.block_entity.RadiusDescriptor;

public class AncientAlphirineBlockEntity extends FunctionalFlowerBlockEntity {
    public static final int MANA_REQUIRED = 4500;
    public static final int MAX_MANA = 180000;
    public static final int COLOR = 13680472;
    private static final int RANGE = 1;

    public AncientAlphirineBlockEntity(BlockPos pos, BlockState state) {
        this(ModFlowers.ANCIENT_ALPHIRINE_BLOCK_ENTITY.get(), pos, state);
    }

    public AncientAlphirineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tickFlower() {
        super.tickFlower();
        Level level = getLevel();
        if (level == null || level.getGameTime() % 10 != 0 || getMana() < MANA_REQUIRED) {
            return;
        }

        List<ItemEntity> nearbyItems = level.getEntitiesOfClass(
                ItemEntity.class,
                new AABB(getEffectivePos()).inflate(1.0D, 0.0D, 1.0D),
                item -> !item.isRemoved() && !item.getItem().isEmpty());

        if (!nearbyItems.isEmpty()) {
            for (ItemEntity itemEntity : nearbyItems) {
                ItemStack stack = itemEntity.getItem();
                Optional<AncientAlphirineRecipe> recipe = findMatchingRecipe(level, stack);
                if (recipe.isEmpty()) {
                    continue;
                }

                AncientAlphirineRecipe alphirineRecipe = recipe.get();
                if (getMana() < alphirineRecipe.getManaUsage()) {
                    continue;
                }

                if (level.isClientSide) {
                    return;
                }

                ItemStack remaining = stack.copy();
                remaining.shrink(1);
                if (remaining.isEmpty()) {
                    itemEntity.discard();
                } else {
                    itemEntity.setItem(remaining);
                }

                if (level.random.nextInt(111) <= alphirineRecipe.getChance()) {
                    addMana(-alphirineRecipe.getManaUsage());
                    spawnPortal(alphirineRecipe.getResultItem(level.registryAccess()).copy());
                } else {
                    addMana(-(alphirineRecipe.getManaUsage() / 10));
                }
                syncFlower();
                return;
            }
        }
    }

    private Optional<AncientAlphirineRecipe> findMatchingRecipe(Level level, ItemStack input) {
        SimpleContainer container = new SimpleContainer(1);
        container.setItem(0, input);
        return level.getRecipeManager().getAllRecipesFor(ModRecipes.ANCIENT_ALPHIRINE_TYPE.get())
                .stream()
                .map(holder -> holder.value())
                .filter(recipe -> recipe.matches(new ContainerRecipeInput(container), level))
                .findFirst();
    }

    private void spawnPortal(ItemStack stack) {
        Level level = getLevel();
        if (level == null || level.isClientSide || stack.isEmpty()) {
            return;
        }

        BlockPos pos = getEffectivePos();
        double x = pos.getX() + 0.5D + (level.random.nextDouble() * 2.0D - 1.0D);
        double y = pos.getY() + 1.2D + (level.random.nextDouble() - 0.5D);
        double z = pos.getZ() + 0.5D + (level.random.nextDouble() * 2.0D - 1.0D);

        EntityAlphirinePortal portal = new EntityAlphirinePortal(level);
        portal.setPos(x, y, z);
        portal.setStack(stack.copy());
        level.addFreshEntity(portal);
    }

    @Override
    public RadiusDescriptor getRadius() {
        return RadiusDescriptor.Rectangle.square(getEffectivePos(), RANGE);
    }

    @Override
    public int getMaxMana() {
        return MAX_MANA;
    }

    @Override
    public int getColor() {
        return COLOR;
    }
    private void syncFlower() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), net.minecraft.world.level.block.Block.UPDATE_ALL);
        }
    }

}
