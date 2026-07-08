package com.pulxes.advancedbotany.common.item.equipment;

import com.pulxes.advancedbotany.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.item.BotaniaItems;

public class TerraHoeItem extends HoeItem {
    public TerraHoeItem(Properties properties) {
        super(AdvancedBotanyEquipment.terrasteelTier(), -3, 0.0F, properties.stacksTo(1));
    }

    private static void spawnTillParticles(Level level, BlockPos pos) {
        // Original: a burst of 48 green wisps when the hoe tills.
        float velMul = 0.025F;
        for (int i = 0; i < 48; i++) {
            double px = (Math.random() - 0.5D) * 3.0D;
            double py = Math.random() - 0.5D + 1.0D;
            double pz = (Math.random() - 0.5D) * 3.0D;
            float size = (float) Math.random() * 0.15F + 0.15F;
            level.addParticle(vazkii.botania.client.fx.WispParticleData.wisp(size, 0.0F, 0.4F, 0.0F),
                    pos.getX() + 0.5D + px, pos.getY() + 0.5D + py, pos.getZ() + 0.5D + pz,
                    -px * velMul, -py * velMul, -pz * velMul);
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (player == null || context.getClickedFace() == Direction.DOWN || !level.getBlockState(pos.above()).isAir()) {
            return InteractionResult.PASS;
        }

        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT)) {
            // Original posts UseHoeEvent and aborts when canceled; mirror via the modern tool-use event.
            if (net.minecraftforge.event.ForgeEventFactory.onToolUse(state, context,
                    net.minecraftforge.common.ToolActions.HOE_TILL, false) == null) {
                return InteractionResult.PASS;
            }
            BlockState farmland = ModBlocks.TERRA_FARMLAND.get().defaultBlockState();
            SoundType sound = farmland.getSoundType(level, pos, player);
            level.playSound(player, pos, sound.getPlaceSound(), SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
            if (level.isClientSide()) {
                spawnTillParticles(level, pos);
            } else {
                level.setBlock(pos, farmland, 11);
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(context.getHand()));
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        if (player.isShiftKeyDown() && state.is(BotaniaBlocks.enchantedSoil)) {
            if (!level.isClientSide()) {
                level.setBlock(pos, Blocks.DIRT.defaultBlockState(), 11);
                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(context.getHand()));
                net.minecraft.world.level.block.Block.popResource(level, pos.above(), new ItemStack(BotaniaItems.overgrowthSeed));
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide() && entity instanceof Player player && stack.getDamageValue() > 0
                && ManaItemHandler.instance().requestManaExactForTool(stack, player, AdvancedBotanyEquipment.TERRA_HOE_MANA_PER_REPAIR, true)) {
            stack.setDamageValue(stack.getDamageValue() - 1);
        }
    }
}
